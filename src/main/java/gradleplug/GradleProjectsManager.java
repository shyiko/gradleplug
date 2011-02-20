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
package gradleplug;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.net.HttpConfigurable;
import gradleplug.adapters.ProjectComponentAdapter;
import gradleplug.bundles.GradlePlugBundle;
import gradleplug.configuration.ProjectConfigurationProjectComponent;
import gradleplug.dependencies.DependencySyncManager;
import gradleplug.messages.Message;
import gradleplug.messages.MessagesProjectComponent;
import gradleplug.resolving.ResolveContext;
import gradleplug.resolving.ResolveException;
import gradleplug.resolving.ResolveResult;
import gradleplug.resolving.Resolver;
import gradleplug.resolving.dependencies.ResolvedDependency;
import gradleplug.resolving.dependencies.ResolvedLibraryDependency;
import gradleplug.resolving.dependencies.ResolvedModuleDependency;
import gradleplug.resolving.events.*;
import gradleplug.resolving.tasks.ResolvedTask;
import gradleplug.toolwindow.GradleProjectsToolWindow;
import gradleplug.toolwindow.structure.ProjectContainerNode;
import gradleplug.toolwindow.structure.ProjectNode;
import gradleplug.toolwindow.structure.TaskContainerNode;
import gradleplug.toolwindow.structure.TaskNode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 19.02.2011
 */
public class GradleProjectsManager extends ProjectComponentAdapter {

    private ProjectContainerNode projectContainer;

    public GradleProjectsManager(Project project) {
        super(project);
        projectContainer = project.getComponent(GradleProjectsToolWindow.class).getProjectContainer();
    }

    public boolean addProject(@NotNull File buildFile) {
        boolean result = !projectContainer.containsProject(buildFile);
        if (result) {
            projectContainer.addProject(buildFile);
        }
        return result;
    }

    public void reimportProjects(Collection<ProjectNode> projectNodes) {
        scheduleReimport(projectNodes, true);
    }

    public void reloadTasks(Collection<ProjectNode> projectNodes) {
        scheduleReimport(projectNodes, false);
    }

    private void scheduleReimport(final Collection<ProjectNode> projectNodes, final boolean resolveDependencies) {
        HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
        httpConfigurable.setAuthenticator();
        ResolverLoader resolverLoader = project.getComponent(ResolverLoader.class);
        if (resolverLoader.loadGradle()) {
            new Task.Backgroundable(project, GradlePlugBundle.message("synchronization.task.title"), false) {
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setText(GradlePlugBundle.message("synchronization.preparing.message"));
                    indicator.setFraction(0.0);
                    bindWatcher(indicator);
                    reimport(indicator, projectNodes, resolveDependencies);
                    indicator.setFraction(1.0);
                }
            }.queue();
        }
    }

    private void bindWatcher(final ProgressIndicator indicator) {
        EventManager eventManager = EventManager.getInstance();
        eventManager.setEventListener(new gradleplug.resolving.events.EventListener() {
            public void onEvent(Event event) {
                //todo: refactor
                String statusMessage = null;
                if (event instanceof ResolveEvent) {
                    ResolveEvent e = (ResolveEvent) event;
                    String dependency = getAsAString(e.getOrg(), "") + ":" +
                            getAsAString(e.getModule(), "") + ":" +
                            getAsAString(e.getRev(), "unknown") +
                            getAsAString(":/", e.getBranch(), "");
                    statusMessage = GradlePlugBundle.message("resolve.event.message", dependency);
                } else if (event instanceof StartingDownloadEvent) {
                    StartingDownloadEvent e = (StartingDownloadEvent) event;
                    String dependency = getAsAString(e.getOrg(), "") + ":" +
                            getAsAString(e.getModule(), "") + ":" +
                            getAsAString(e.getRev(), "unknown") +
                            getAsAString(":/", e.getBranch(), "") +
                            getAsAString(" (", e.getType(), ")", "");
                    statusMessage = GradlePlugBundle.message("starting.download.event.message", dependency);
                } else if (event instanceof DownloadProgressEvent) {
                    DownloadProgressEvent e = (DownloadProgressEvent) event;
                    statusMessage = GradlePlugBundle.message("download.progress.event.message", e.getUri(),
                            e.getBytesCompleted() / 1024, e.getTotalSize() / 1024);
                }
                if (statusMessage != null) {
                    indicator.setText(statusMessage);
                }
            }
        });
    }

    private String getAsAString(String prefix, String value, String suffix, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return prefix + value + suffix;
    }

    private String getAsAString(String prefix, String value, String defaultValue) {
        return getAsAString("", value, "", defaultValue);
    }

    private String getAsAString(String value, String defaultValue) {
        return getAsAString("", value, defaultValue);
    }

    private void reimport(ProgressIndicator indicator, final Collection<ProjectNode> projectNodes, boolean resolveDependencies) {
        ProjectConfigurationProjectComponent configurationProjectComponent = project.getComponent(ProjectConfigurationProjectComponent.class);
        boolean autoCleanup = configurationProjectComponent.getConfiguration().isAutoCleanup();
        ResolverLoader resolverLoader = project.getComponent(ResolverLoader.class);
        Resolver resolver = resolverLoader.getResolver();
        Map<String, Module> modules = getProjectModules(project);
        for (ProjectNode projectNode : projectNodes) {
            ResolveContext resolveContext;
            if (resolveDependencies) {
                resolveContext = new ResolveContext(projectNode.getBuildFile(), modules);
            } else {
                resolveContext = new ResolveContext(projectNode.getBuildFile());
            }
            try {
                indicator.setText(GradlePlugBundle.message("synchronizing.project.message", projectNode.getName()));
                ResolveResult resolveResult = resolver.resolve(resolveContext);
                // sync dependencies
                List<Module> boundModules = getBoundModules(project, resolveContext.getBuildFile());
                for (Module boundModule : boundModules) {
                    DependencySyncManager dependencySyncManager = boundModule.getComponent(DependencySyncManager.class);
                    for (ResolvedDependency resolvedDependency : resolveResult.getResolvedDependencies()) {
                        switch (resolvedDependency.getDependencyType()) {
                            case LIBRARY:
                                dependencySyncManager.addResolvedLibraryDependency((ResolvedLibraryDependency) resolvedDependency);
                                break;
                            case MODULE:
                                dependencySyncManager.addResolvedModuleDependency((ResolvedModuleDependency) resolvedDependency);
                                break;
                            default:
                                throw new UnsupportedOperationException();
                        }
                    }
                    dependencySyncManager.commit(autoCleanup);
                }
                // sync tasks
                TaskContainerNode taskContainerNode = projectNode.getTaskContainerNode();
                List<TaskNode> taskNodes = new ArrayList<TaskNode>();
                for (ResolvedTask resolvedTask : resolveResult.getResolvedTasks()) {
                    taskNodes.add(new TaskNode(resolvedTask.getName(), resolvedTask.getDescription()));
                }
                taskContainerNode.setTasks(taskNodes);
            } catch (ResolveException e) {
                handleException(projectNode, e);
            }
        }
    }

    private void handleException(ProjectNode projectNode, ResolveException e) {
        MessagesProjectComponent messagesComponent = project.getComponent(MessagesProjectComponent.class);
        Message message;
        if (e.getCause() == null) {
            message = new Message(Message.Type.ERROR, e.getMessage());
        } else {
            message = new Message(Message.Type.ERROR, e.getMessage(),
                    GradlePlugBundle.message("message.panel.fail.reason.message", e.getCause().getMessage()));
        }
        messagesComponent.show(GradlePlugBundle.message("message.panel.project.tab.title", projectNode.getName()), message);
    }

    private Map<String, Module> getProjectModules(Project project) {
        Map<String, Module> result = new HashMap<String, Module>();
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            result.put(module.getName(), module);
        }
        return result;
    }

    private List<Module> getBoundModules(Project project, File buildFile) {
        List<Module> result = new ArrayList<Module>();
        File buildFileDir = buildFile.getParentFile();
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        MODULES_CYCLE:
        for (Module module : moduleManager.getModules()) {
            ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
            for (VirtualFile virtualFile : rootManager.getContentRoots()) {
                if (buildFileDir.equals(new File(virtualFile.getPath()))) {
                    result.add(module);
                    continue MODULES_CYCLE;
                }
            }
        }
        return result;
    }

}
