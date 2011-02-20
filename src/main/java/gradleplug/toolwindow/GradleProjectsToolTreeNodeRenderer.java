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
package gradleplug.toolwindow;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.ui.SimpleTextAttributes;
import gradleplug.toolwindow.structure.GradleNode;
import gradleplug.toolwindow.structure.ProjectNode;
import gradleplug.toolwindow.structure.TaskNode;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 12.02.2011
 */
public class GradleProjectsToolTreeNodeRenderer extends NodeRenderer {

    @Override
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof GradleNode) {
            GradleNode node = (GradleNode) value;
            if (expanded) {
                setIcon(node.getOpenIcon());
            } else {
                setIcon(node.getClosedIcon());
            }
            if (node instanceof ProjectNode) {
                String buildFile = ((ProjectNode) node).getBuildFile().getAbsolutePath();
                append(" (" + buildFile + ")", new SimpleTextAttributes(Font.PLAIN, Color.GRAY));
            } else if (node instanceof TaskNode) {
                String description = ((TaskNode) node).getDescription();
                if (description != null) {
                    append(" (" + description + ")", new SimpleTextAttributes(Font.PLAIN, Color.GRAY));
                }
            }
        }
    }
}
