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
package gradleplug.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import gradleplug.bundles.GradlePlugBundle;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
public class ProjectConfigurationEditor implements Configurable {

    private final JPanel rootPanel = new JPanel(new GridBagLayout());
    private final JCheckBox autoCleanupCheckbox = new JCheckBox(GradlePlugBundle.message("auto.cleanup.checkbox.text"));

    private final ProjectConfigurationProjectComponent configurationProjectComponent;

    public ProjectConfigurationEditor(Project project) {
        configurationProjectComponent = project.getComponent(ProjectConfigurationProjectComponent.class);
    }

    public JComponent createComponent() {
        final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
        rootPanel.add(autoCleanupCheckbox, gc);
        gc.weighty = 1.0;
        rootPanel.add(new JPanel(new GridBagLayout()), gc);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return rootPanel;
    }

    @Nls
    public String getDisplayName() {
        return GradlePlugBundle.message("project.settings.tab");
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    public boolean isModified() {
        ProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        return configuration.isAutoCleanup() != autoCleanupCheckbox.isSelected();
    }

    public void apply() throws ConfigurationException {
        ProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        configuration.setAutoCleanup(autoCleanupCheckbox.isSelected());
    }

    public void reset() {
        ProjectConfiguration configuration = configurationProjectComponent.getConfiguration();
        autoCleanupCheckbox.setSelected(configuration.isAutoCleanup());
    }

    public void disposeUIResources() {
    }
}
