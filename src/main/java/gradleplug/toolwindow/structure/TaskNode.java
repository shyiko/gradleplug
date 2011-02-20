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
package gradleplug.toolwindow.structure;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 19.02.2011
 */
public class TaskNode extends GradleNode {

    private static final Icon TASK_ICON = IconLoader.getIcon("/images/task.png");

    private String name;
    private String description;

    public TaskNode(String name, String description) {
        super(name, false);
        this.name = name;
        this.description = description;
        setIcon(TASK_ICON);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskContainerNode getParent() {
        return (TaskContainerNode) super.getParent();
    }

    @Override
    public String getMenuId() {
        return "Gradle.ToolTree.TaskNode.Menu";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TaskNode taskNode = (TaskNode) o;

        if (description != null ? !description.equals(taskNode.description) : taskNode.description != null) {
            return false;
        }
        if (!name.equals(taskNode.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}

