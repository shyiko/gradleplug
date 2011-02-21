/*
 * Copyright 2011 Stanley Shyiko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gradleplug.resolving.impl;

import com.intellij.openapi.module.Module;
import gradleplug.resolving.ResolveContext;
import gradleplug.resolving.ResolveResult;
import gradleplug.resolving.ResolveException;
import gradleplug.resolving.Resolver;
import gradleplug.resolving.dependencies.ResolvedDependency;
import gradleplug.resolving.dependencies.ResolvedLibraryDependency;
import gradleplug.resolving.dependencies.ResolvedModuleDependency;
import gradleplug.resolving.impl.gradle.GradlePlugGradleLauncherFactory;
import gradleplug.resolving.tasks.ResolvedTask;
import org.gradle.BuildResult;
import org.gradle.GradleLauncher;
import org.gradle.StartParameter;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.*;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.TaskContainer;

import java.util.*;

import static gradleplug.resolving.dependencies.ResolvedLibraryDependency.ArtifactType;
import static gradleplug.resolving.dependencies.ResolvedLibraryDependency.Scope;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 09.02.2011
 */
public class ResolverImpl implements Resolver {

    private static final String COMPILE_CONFIG = "compile";
    private static final String RUNTIME_CONFIG = "runtime";
    private static final String TEST_COMPILE_CONFIG = "testCompile";
    private static final String TEST_RUNTIME_CONFIG = "testRuntime";

    private static Map<String, Scope> scopeMapping = new HashMap<String, Scope>() {{
        put(COMPILE_CONFIG, Scope.COMPILE);
        put(RUNTIME_CONFIG, Scope.RUNTIME);
        put(TEST_COMPILE_CONFIG, Scope.TEST);
        put(TEST_RUNTIME_CONFIG, Scope.TEST);
    }};
    private static Map<String, ArtifactType> artifactsTypeMapping = new HashMap<String, ArtifactType>() {{
        put("jar", ArtifactType.CLASSES);
        put("source", ArtifactType.SOURCES);
        put("javadoc", ArtifactType.JAVADOCS);
    }};

    public ResolveResult resolve(ResolveContext context) throws ResolveException {
        BuildResult buildResult;
        try {
            buildResult = getGradleBuildResult(context);
            buildResult.rethrowFailure();
        } catch (Exception ex) {
            throw new ResolveException("Failed to resolve " + context.getBuildFile(), ex.getCause());
        }
        return resolveBuildResult(context, buildResult);
    }

    private BuildResult getGradleBuildResult(ResolveContext context) {
        BuildResult result;
        StartParameter startParameter = new StartParameter();
        startParameter.setBuildFile(context.getBuildFile());
        if (context.isResolveDependencies()) {
            startParameter.setTaskNames(Arrays.asList("dependencies"));
        } else {
            startParameter.setTaskNames(Arrays.asList("tasks"));
        }

        GradleLauncher.injectCustomFactory(new GradlePlugGradleLauncherFactory());

        GradleLauncher instance = GradleLauncher.newInstance(startParameter);
        if (context.isResolveDependencies()) {
            result = instance.run();
        } else {
            result = instance.getBuildAnalysis();
        }
        return result;
    }

    private ResolveResult resolveBuildResult(ResolveContext context, BuildResult buildResult) throws ResolveException {
        List<ResolvedTask> resolvedTasks = resolveTasks(buildResult);
        resolvedTasks.addAll(resolveImplicitTasks(buildResult));
        List<ResolvedDependency> resolvedDependencies;
        if (context.isResolveDependencies()) {
            resolvedDependencies = resolvedDependencies(context, buildResult);
        } else {
            resolvedDependencies = Collections.emptyList();
        }
        return new ResolveResult(resolvedTasks, resolvedDependencies);
    }

    public List<ResolvedTask> resolveTasks(BuildResult buildResult) {
        List<ResolvedTask> result = new ArrayList<ResolvedTask>();
        Gradle gradle = buildResult.getGradle();
        Project project = gradle.getRootProject();
        TaskContainer taskContainer = project.getTasks();
        for (Task task : taskContainer.getAll()) {
            result.add(new ResolvedTask(task.getName(), task.getDescription()));
        }
        return result;
    }

    private List<ResolvedTask> resolveImplicitTasks(BuildResult buildResult) {
        List<ResolvedTask> result = new ArrayList<ResolvedTask>();
        Gradle gradle = buildResult.getGradle();
        Project project = gradle.getRootProject();
        if (project instanceof ProjectInternal) {
            ProjectInternal projectInternal = (ProjectInternal) project;
            for (Task task : projectInternal.getImplicitTasks().getAll()) {
                result.add(new ResolvedTask(task.getName(), task.getDescription()));
            }
        }
        return result;
    }

    public List<ResolvedDependency> resolvedDependencies(ResolveContext context, BuildResult buildResult) throws ResolveException {
        List<ResolvedDependency> result = new ArrayList<ResolvedDependency>();
        Gradle gradle = buildResult.getGradle();
        Project rootProject = gradle.getRootProject();
        ConfigurationContainer configurationContainer = rootProject.getConfigurations();
        Map<String, MappedConfiguration> configurations = getMappedConfigurations(context, configurationContainer);
        subtractDependencies(configurations.get(TEST_COMPILE_CONFIG), configurations.get(COMPILE_CONFIG));
        subtractDependencies(configurations.get(TEST_RUNTIME_CONFIG), configurations.get(RUNTIME_CONFIG));
        subtractDependencies(configurations.get(TEST_RUNTIME_CONFIG), configurations.get(TEST_COMPILE_CONFIG));
        subtractDependencies(configurations.get(RUNTIME_CONFIG), configurations.get(COMPILE_CONFIG));
        for (MappedConfiguration mappedConfiguration : configurations.values()) {
            result.addAll(mappedConfiguration.libraryDependencies);
            result.addAll(mappedConfiguration.moduleDependencies);
        }
        return result;
    }

    private void subtractDependencies(MappedConfiguration from, MappedConfiguration that) {
        if (from == null || that == null) {
            return;
        }
        from.libraryDependencies.removeAll(that.libraryDependencies);
        from.moduleDependencies.removeAll(that.moduleDependencies);
    }

    private Map<String, MappedConfiguration> getMappedConfigurations(ResolveContext context, ConfigurationContainer configurationContainer) throws ResolveException {
        Map<String, MappedConfiguration> result = new HashMap<String, MappedConfiguration>();
        for (Map.Entry<String, Scope> entry : scopeMapping.entrySet()) {
            Configuration configuration = configurationContainer.findByName(entry.getKey());
            if (configuration != null) {
                Scope scope = entry.getValue();
                MappedConfiguration mappedConfiguration = new MappedConfiguration(context, configuration, scope);
                result.put(entry.getKey(), mappedConfiguration);
            }
        }
        return result;
    }

    private class MappedConfiguration {

        private ResolveContext context;
        private Scope scope;
        private Set<ResolvedLibraryDependency> libraryDependencies;
        private Set<ResolvedModuleDependency> moduleDependencies;

        private MappedConfiguration(ResolveContext context, Configuration configuration, Scope scope) throws ResolveException {
            this.context = context;
            this.scope = scope;
            resolveLibraryDependencies(configuration);
            resolveModuleDependencies(configuration);
        }

        private void resolveLibraryDependencies(Configuration configuration) {
            libraryDependencies = new HashSet<ResolvedLibraryDependency>();
            ResolvedConfiguration resolvedConfiguration = configuration.getResolvedConfiguration();
            for (ResolvedArtifact resolvedArtifact : resolvedConfiguration.getResolvedArtifacts()) {
                org.gradle.api.artifacts.ResolvedDependency resolvedDependency = resolvedArtifact.getResolvedDependency();
                ArtifactType artifactType = artifactsTypeMapping.get(resolvedArtifact.getType());
                if (artifactType != null) {
                    libraryDependencies.add(new ResolvedLibraryDependency(resolvedDependency.getModuleGroup(), resolvedDependency.getModuleName(),
                            resolvedDependency.getModuleVersion(), artifactType, resolvedArtifact.getFile(), scope));
                }
            }
        }

        private void resolveModuleDependencies(Configuration configuration) throws ResolveException {
            moduleDependencies = new HashSet<ResolvedModuleDependency>();
            Map<String, Module> modules = context.getModules();
            Set<ProjectDependency> dependencies = configuration.getDependencies(ProjectDependency.class);
            for (ProjectDependency dependency : dependencies) {
                Module module = modules.get(dependency.getName());
                if (module != null) {
                    moduleDependencies.add(new ResolvedModuleDependency(module));
                } else {
                    throw new ResolveException("Failed to locate module " + dependency.getName());
                }
            }
        }
    }
}
