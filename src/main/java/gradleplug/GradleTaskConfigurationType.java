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

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyIcons;
import org.jetbrains.plugins.groovy.runner.GroovyScriptRunConfiguration;

import javax.swing.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 14.02.2011
 */
public class GradleTaskConfigurationType implements ConfigurationType {

    private final GroovyFactory configurationFactory;

    public GradleTaskConfigurationType() {
        configurationFactory = new GroovyFactory(this);
    }

    public String getDisplayName() {
        return "Gradle Task";
    }

    public String getConfigurationTypeDescription() {
        return "Gradle Task";
    }

    public Icon getIcon() {
        return GroovyIcons.GROOVY_ICON_16x16;
    }

    @NonNls
    @NotNull
    public String getId() {
        return "GradleTaskConfigurationType";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{configurationFactory};
    }

    public static GradleTaskConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(GradleTaskConfigurationType.class);
    }

    public static class GroovyFactory extends ConfigurationFactory {
        public GroovyFactory(ConfigurationType type) {
            super(type);
        }

        public RunConfiguration createTemplateConfiguration(Project project) {
            return new GroovyScriptRunConfiguration("Gradle Task", project, this);
        }

    }
}

