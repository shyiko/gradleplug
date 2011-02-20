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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import gradleplug.adapters.ProjectComponentAdapter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 30.01.2011
 */
@State(name = "ProjectConfigurationProjectComponent",
        storages = {@Storage(id = "other", file = "$PROJECT_FILE$")}
)
public class ProjectConfigurationProjectComponent extends ProjectComponentAdapter implements PersistentStateComponent<Element> {

    private static final String AUTO_CLEANUP = "autoCleanup";

    private ProjectConfiguration configuration;

    public ProjectConfigurationProjectComponent(Project project) {
        super(project);
        configuration = new ProjectConfiguration();
    }

    public void loadState(Element state) {
        String autoCleanup = state.getAttributeValue(AUTO_CLEANUP);
        configuration.setAutoCleanup(autoCleanup == null || autoCleanup.equalsIgnoreCase("true"));
    }

    public Element getState() {
        Element result = new Element(getComponentName());
        result.setAttribute(AUTO_CLEANUP, ((Boolean) configuration.isAutoCleanup()).toString());
        return result;
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ProjectConfigurationProjectComponent";
    }

    public ProjectConfiguration getConfiguration() {
        return configuration;
    }
}
