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

import gradleplug.toolwindow.SelectionStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 19.02.2011
 */
public abstract class GradleNode extends DefaultMutableTreeNode {

    private Icon openIcon;
    private Icon closedIcon;

    protected GradleNode() {
    }

    protected GradleNode(Object userObject) {
        super(userObject);
    }

    protected GradleNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    @Nullable
    public Icon getOpenIcon() {
        return openIcon;
    }

    protected void setOpenIcon(Icon openIcon) {
        this.openIcon = openIcon;
    }

    @Nullable
    public Icon getClosedIcon() {
        return closedIcon;
    }

    protected void setClosedIcon(Icon closedIcon) {
        this.closedIcon = closedIcon;
    }

    protected void setIcon(Icon icon) {
        setOpenIcon(icon);
        setClosedIcon(icon);
    }

    @NotNull
    public SelectionStyle getSelectionStyle() {
        return SelectionStyle.UNDER_SAME_PARENT;
    }

    @Nullable
    public String getMenuId() {
        return null;
    }
}
