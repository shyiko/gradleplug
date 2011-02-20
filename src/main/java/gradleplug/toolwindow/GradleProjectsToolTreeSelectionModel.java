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

import gradleplug.toolwindow.structure.GradleNode;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 15.02.2011
 */
public class GradleProjectsToolTreeSelectionModel extends DefaultTreeSelectionModel {

    private static final TreePath[] EMPTY_SELECTION = new TreePath[0];

    @Override
    public void addSelectionPaths(TreePath[] paths) {
        TreePath selectionPath = getSelectionPath();
        if (selectionPath != null) {
            paths = filter(selectionPath, paths);
        }
        super.addSelectionPaths(paths);
    }

    @Override
    public void setSelectionPaths(TreePath[] paths) {
        TreePath selectionPath = paths[0];
        if (paths.length > 1) {
            paths = filter(selectionPath, paths);
        }
        super.setSelectionPaths(paths);
    }

    private TreePath[] filter(TreePath alreadySelectedPath, TreePath[] additionalPaths) {
        GradleNode alreadySelectedNode = (GradleNode) alreadySelectedPath.getLastPathComponent();
        switch (alreadySelectedNode.getSelectionStyle()) {
            case NOT_SELECTABLE:
                return EMPTY_SELECTION;
            case UNDER_SAME_PARENT:
                return filterUnderTheSameParent(alreadySelectedNode, additionalPaths);
            case OF_SAME_TYPE:
                return filterOfTheSameType(alreadySelectedNode, additionalPaths);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private TreePath[] filterUnderTheSameParent(GradleNode alreadySelectedNode, TreePath[] additionalPaths) {
        TreeNode alreadySelectedNodeParent = alreadySelectedNode.getParent();
        List<TreePath> result = new ArrayList<TreePath>();
        for (TreePath path : additionalPaths) {
            GradleNode lastPathComponent = (GradleNode) path.getLastPathComponent();
            if (alreadySelectedNode.getClass().isInstance(lastPathComponent) &&
                    alreadySelectedNodeParent.equals(lastPathComponent.getParent())) {
                result.add(path);
            }
        }
        return result.toArray(new TreePath[result.size()]);
    }

    private TreePath[] filterOfTheSameType(GradleNode alreadySelectedNode, TreePath[] additionalPaths) {
        List<TreePath> result = new ArrayList<TreePath>();
        for (TreePath path : additionalPaths) {
            GradleNode lastPathComponent = (GradleNode) path.getLastPathComponent();
            if (alreadySelectedNode.getClass().isInstance(lastPathComponent)) {
                result.add(path);
            }
        }
        return result.toArray(new TreePath[result.size()]);
    }
}
