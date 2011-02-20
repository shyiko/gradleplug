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

import com.intellij.util.ui.UIUtil;
import gradleplug.toolwindow.GradleProjectsToolTreeModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 19.02.2011
 */
public class ProjectContainerNode extends GradleNode {

    private GradleProjectsToolTreeModel treeModel;

    public void setTreeModel(GradleProjectsToolTreeModel treeModel) {
        this.treeModel = treeModel;
    }

    protected GradleProjectsToolTreeModel getTreeModel() {
        return treeModel;
    }

    public boolean hasProjects() {
        return getChildCount() > 0;
    }

    public List<ProjectNode> getProjects() {
        List<ProjectNode> result = new ArrayList<ProjectNode>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            result.add((ProjectNode) getChildAt(i));
        }
        return result;
    }

    public boolean containsProject(File buildFile) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ProjectNode child = (ProjectNode) getChildAt(i);
            if (child.getBuildFile().equals(buildFile)) {
                return true;
            }
        }
        return false;
    }

    public ProjectNode addProject(File buildFile) {
        final ProjectNode projectNode = new ProjectNode(buildFile);
        final int childIndex = getChildCount();
        insert(projectNode, childIndex);
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                if (childIndex == 0) {
                    treeModel.reload();
                } else {
                    treeModel.fireTreeNodesInserted(this, new Object[]{getRoot()}, new int[]{childIndex}, new Object[]{projectNode});
                }
            }
        });
        return projectNode;
    }

    public void removeProject(final ProjectNode projectNode) {
        final int projectIndex = getIndex(projectNode);
        remove(projectIndex);
        UIUtil.invokeLaterIfNeeded(new Runnable() {

            public void run() {
                treeModel.fireTreeNodesRemoved(this, new Object[]{getRoot()}, new int[]{projectIndex}, new Object[]{projectNode});
            }
        });
    }
}