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

import gradleplug.toolwindow.structure.ProjectContainerNode;

import javax.swing.tree.DefaultTreeModel;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 12.02.2011
 */
public class GradleProjectsToolTreeModel extends DefaultTreeModel {

    public GradleProjectsToolTreeModel() {
        super(new ProjectContainerNode());
        ((ProjectContainerNode) getRoot()).setTreeModel(this);
    }

    @Override
    public void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
        super.fireTreeNodesRemoved(source, path, childIndices, children);
    }

    @Override
    public void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
        super.fireTreeNodesInserted(source, path, childIndices, children);
    }

    @Override
    public void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        super.fireTreeNodesChanged(source, path, childIndices, children);
    }

    @Override
    public void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        super.fireTreeStructureChanged(source, path, childIndices, children);
    }
}
