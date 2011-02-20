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

import org.gradle.GradleLauncher;
import org.gradle.StartParameter;
import org.gradle.api.internal.ExceptionAnalyser;
import org.gradle.api.internal.project.GlobalServicesRegistry;
import org.gradle.api.internal.project.IProjectFactory;
import org.gradle.api.internal.project.ServiceRegistry;
import org.gradle.cache.CacheRepository;
import org.gradle.configuration.BuildConfigurer;
import org.gradle.initialization.*;
import org.gradle.invocation.DefaultGradle;
import org.gradle.logging.LoggingManagerInternal;
import org.gradle.util.WrapUtil;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 16.02.2011
 */
public class GradlePlugGradleLauncherFactory implements GradleLauncherFactory {

    private final ServiceRegistry sharedServices;
    private final NestedBuildTracker tracker;

    public GradlePlugGradleLauncherFactory() {
        this(new GradlePlugGlobalServicesRegistry());
    }

    private GradlePlugGradleLauncherFactory(GlobalServicesRegistry globalServices) {
        sharedServices = globalServices;
        tracker = new NestedBuildTracker();
        GradleLauncher.injectCustomFactory(this);
    }

    public DefaultGradleLauncher newInstance(StartParameter startParameter) {
        BuildRequestMetaData requestMetaData;
        if (tracker.getCurrentBuild() != null) {
            requestMetaData = new DefaultBuildRequestMetaData(tracker.getCurrentBuild().getServices().get(BuildClientMetaData.class), System.currentTimeMillis());
        } else {
            requestMetaData = new DefaultBuildRequestMetaData(System.currentTimeMillis());
        }
        return doNewInstance(startParameter, requestMetaData);
    }

    public DefaultGradleLauncher newInstance(StartParameter startParameter, BuildRequestMetaData requestMetaData) {
        assert tracker.getCurrentBuild() == null;
        return doNewInstance(startParameter, requestMetaData);
    }

    public StartParameter createStartParameter(String... commandLineArgs) {
        throw new UnsupportedOperationException();
    }

    private DefaultGradleLauncher doNewInstance(StartParameter startParameter, BuildRequestMetaData requestMetaData) {
        GradlePlugServiceRegistryFactory serviceRegistry = new GradlePlugServiceRegistryFactory(sharedServices, startParameter);
        serviceRegistry.add(BuildRequestMetaData.class, requestMetaData);
        serviceRegistry.add(BuildClientMetaData.class, requestMetaData.getClient());
        LoggingManagerInternal loggingManager = serviceRegistry.newInstance(LoggingManagerInternal.class);
        DefaultGradle gradle = new DefaultGradle(
                tracker.getCurrentBuild(),
                startParameter, serviceRegistry);
        return new DefaultGradleLauncher(
                gradle,
                serviceRegistry.get(InitScriptHandler.class),
                new SettingsHandler(
                        new EmbeddedScriptSettingsFinder(
                                new DefaultSettingsFinder(WrapUtil.<ISettingsFileSearchStrategy>toList(
                                        new MasterDirSettingsFinderStrategy(),
                                        new ParentDirSettingsFinderStrategy()))),
                        serviceRegistry.get(SettingsProcessor.class),
                        new BuildSourceBuilder(
                                this,
                                serviceRegistry.get(ClassLoaderFactory.class),
                                serviceRegistry.get(CacheRepository.class))),
                new DefaultGradlePropertiesLoader(),
                new BuildLoader(
                        serviceRegistry.get(IProjectFactory.class)
                ),
                serviceRegistry.get(BuildConfigurer.class),
                gradle.getBuildListenerBroadcaster(),
                serviceRegistry.get(ExceptionAnalyser.class),
                loggingManager);
    }
}

