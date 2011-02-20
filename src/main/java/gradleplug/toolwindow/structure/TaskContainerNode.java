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
import com.intellij.util.ui.UIUtil;
import gradleplug.bundles.GradlePlugBundle;
import gradleplug.toolwindow.SelectionStyle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 19.02.2011
 */
public class TaskContainerNode extends GradleNode {

    private static final Icon TASK_CONTAINER_OPEN_ICON = IconLoader.getIcon("/images/taskContainerOpen.png");
    private static final Icon TASK_CONTAINER_CLOSED_ICON = IconLoader.getIcon("/images/taskContainerClosed.png");

    public TaskContainerNode() {
        super(GradlePlugBundle.message("task.container.node.title"));
        setOpenIcon(TASK_CONTAINER_OPEN_ICON);
        setClosedIcon(TASK_CONTAINER_CLOSED_ICON);
    }

    public List<TaskNode> getTasks() {
        List<TaskNode> result = new ArrayList<TaskNode>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            result.add((TaskNode) getChildAt(i));
        }
        return result;
    }

    public void setTasks(List<TaskNode> tasks) {
        removeAllChildren();
        for (TaskNode task : tasks) {
            add(task);
        }
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                ProjectNode projectNode = getParent();
                ProjectContainerNode projectContainerNode = projectNode.getParent();
                projectContainerNode.getTreeModel().reload(TaskContainerNode.this);
            }
        });
    }

    public ProjectNode getParent() {
        return (ProjectNode) super.getParent();
    }

    @NotNull
    @Override
    public SelectionStyle getSelectionStyle() {
        return SelectionStyle.NOT_SELECTABLE;
    }
}

