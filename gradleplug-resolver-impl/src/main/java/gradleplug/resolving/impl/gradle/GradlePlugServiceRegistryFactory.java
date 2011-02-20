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
package gradleplug.resolving.impl.gradle;

import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.gradle.StartParameter;
import org.gradle.api.internal.ClassGenerator;
import org.gradle.api.internal.artifacts.ConfigurationContainerFactory;
import org.gradle.api.internal.artifacts.DefaultConfigurationContainerFactory;
import org.gradle.api.internal.artifacts.ivyservice.*;
import org.gradle.api.internal.artifacts.ivyservice.moduleconverter.*;
import org.gradle.api.internal.artifacts.ivyservice.moduleconverter.dependencies.*;
import org.gradle.api.internal.project.ServiceRegistry;
import org.gradle.api.internal.project.TopLevelBuildServiceRegistry;
import org.gradle.logging.ProgressLoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 16.02.2011
 */
public class GradlePlugServiceRegistryFactory extends TopLevelBuildServiceRegistry {

    private final Map<String, ModuleDescriptor> clientModuleRegistry = new HashMap<String, ModuleDescriptor>();

    public GradlePlugServiceRegistryFactory(ServiceRegistry parent, StartParameter startParameter) {
        super(parent, startParameter);
    }

    @Override
    protected ConfigurationContainerFactory createConfigurationContainerFactory() {
        DependencyDescriptorFactory dependencyDescriptorFactoryDelegate = createDependencyDescriptorFactory(ProjectDependencyDescriptorFactory.RESOLVE_DESCRIPTOR_STRATEGY);
        PublishModuleDescriptorConverter fileModuleDescriptorConverter = new PublishModuleDescriptorConverter(
                createResolveModuleDescriptorConverter(ProjectDependencyDescriptorFactory.IVY_FILE_DESCRIPTOR_STRATEGY),
                new DefaultArtifactsToModuleDescriptorConverter(DefaultArtifactsToModuleDescriptorConverter.IVY_FILE_STRATEGY));

        return new DefaultConfigurationContainerFactory(clientModuleRegistry,
                new DefaultSettingsConverter(
                        get(ProgressLoggerFactory.class)
                ),
                get(PublishModuleDescriptorConverter.class),
                get(PublishModuleDescriptorConverter.class),
                fileModuleDescriptorConverter,
                new GradlePlugIvyFactory(),
                new SelfResolvingDependencyResolver(
                        new DefaultIvyDependencyResolver(
                                new DefaultIvyReportConverter(dependencyDescriptorFactoryDelegate))),
                new DefaultIvyDependencyPublisher(new DefaultPublishOptionsFactory()),
                get(ClassGenerator.class));
    }

    private DependencyDescriptorFactory createDependencyDescriptorFactory(ProjectDependencyDescriptorStrategy projectDependencyStrategy) {
        DefaultModuleDescriptorFactoryForClientModule clientModuleDescriptorFactory = new DefaultModuleDescriptorFactoryForClientModule();
        DependencyDescriptorFactory dependencyDescriptorFactoryDelegate = new DependencyDescriptorFactoryDelegate(
                new ClientModuleDependencyDescriptorFactory(
                        get(ExcludeRuleConverter.class), clientModuleDescriptorFactory, clientModuleRegistry),
                new ProjectDependencyDescriptorFactory(
                        get(ExcludeRuleConverter.class),
                        projectDependencyStrategy),
                get(ExternalModuleDependencyDescriptorFactory.class));
        clientModuleDescriptorFactory.setDependencyDescriptorFactory(dependencyDescriptorFactoryDelegate);
        return dependencyDescriptorFactoryDelegate;
    }

    private ResolveModuleDescriptorConverter createResolveModuleDescriptorConverter(ProjectDependencyDescriptorStrategy projectDependencyStrategy) {
        DependencyDescriptorFactory dependencyDescriptorFactoryDelegate = createDependencyDescriptorFactory(projectDependencyStrategy);
        return new ResolveModuleDescriptorConverter(
                get(ModuleDescriptorFactory.class),
                get(ConfigurationsToModuleDescriptorConverter.class),
                new DefaultDependenciesToModuleDescriptorConverter(
                        dependencyDescriptorFactoryDelegate,
                        get(ExcludeRuleConverter.class)));
    }

}

