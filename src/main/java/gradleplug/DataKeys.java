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

import com.intellij.openapi.actionSystem.DataKey;
import gradleplug.toolwindow.structure.ProjectContainerNode;
import gradleplug.toolwindow.structure.ProjectNode;

import java.util.Collection;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 12.02.2011
 */
public class DataKeys {

    public static final DataKey<ProjectContainerNode> PROJECT_CONTAINER_NODE = DataKey.create("projectContainerNode");
    public static final DataKey<Collection<ProjectNode>> SELECTED_PROJECT_NODES = DataKey.create("selectedProjectNodes");
}
