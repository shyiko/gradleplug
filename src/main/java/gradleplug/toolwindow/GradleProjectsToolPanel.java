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

import com.intellij.execution.Location;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import gradleplug.DataKeys;
import gradleplug.GradleTaskLocation;
import gradleplug.toolwindow.structure.GradleNode;
import gradleplug.toolwindow.structure.ProjectNode;
import gradleplug.toolwindow.structure.TaskNode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 08.02.2011
 */
public class GradleProjectsToolPanel extends SimpleToolWindowPanel implements DataProvider {

    private final Project project;
    private final SimpleTree tree;

    public GradleProjectsToolPanel(Project project) {
        super(true, true);
        this.project = project;
        this.tree = new SimpleTree() {{
            setModel(new GradleProjectsToolTreeModel());
            setCellRenderer(new GradleProjectsToolTreeNodeRenderer());
            setRootVisible(false);
            setShowsRootHandles(true);
            setSelectionModel(new GradleProjectsToolTreeSelectionModel());
        }};

        final ActionManager actionManager = ActionManager.getInstance();
        final ActionToolbar actionToolbar = actionManager.createActionToolbar("GradlePlug Toolbar",
                (DefaultActionGroup) actionManager.getAction("Gradle.ToolWindow.Toolbar"), true);
        actionToolbar.setTargetComponent(tree);
        setToolbar(actionToolbar.getComponent());
        setContent(ScrollPaneFactory.createScrollPane(this.tree));

        tree.addMouseListener(new PopupHandler() {
            public void invokePopup(final Component comp, final int x, final int y) {
                TreePath pathForLocation = tree.getPathForLocation(x, y);
                if (pathForLocation == null) {
                    return;
                }
                GradleNode gradleNode = (GradleNode) pathForLocation.getLastPathComponent();
                String menuId = gradleNode.getMenuId();
                if (menuId == null) {
                    return;
                }
                final ActionGroup actionGroup = (ActionGroup) actionManager.getAction(menuId);
                if (actionGroup != null) {
                    actionToolbar.updateActionsImmediately();
                    actionManager.createActionPopupMenu("", actionGroup).getComponent().show(comp, x, y);
                }
            }
        });

    }

    public SimpleTree getTree() {
        return tree;
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
        if (DataKeys.SELECTED_PROJECT_NODES.is(dataId)) {
            Set<ProjectNode> result = new HashSet<ProjectNode>();
            TreePath[] selectionPaths = tree.getSelectionPaths();
            if (selectionPaths != null) {
                for (TreePath selectionPath : selectionPaths) {
                    ProjectNode projectNode = (ProjectNode) selectionPath.getPathComponent(1);
                    result.add(projectNode);
                }
            }
            return result;
        } else if (DataKeys.PROJECT_CONTAINER_NODE.is(dataId)) {
            return tree.getModel().getRoot();
        } else if (Location.DATA_KEY.is(dataId)) {
            TaskNode[] taskNodes = tree.getSelectedNodes(TaskNode.class, null);
            if (taskNodes.length == 0) {
                return null;
            }
            File buildFile = taskNodes[0].getParent().getParent().getBuildFile();
            List<String> tasks = new ArrayList<String>();
            for (TaskNode taskNode : taskNodes) {
                tasks.add(taskNode.getName());
            }
            LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
            VirtualFile buildFileVirtual = localFileSystem.findFileByIoFile(buildFile);
            return new GradleTaskLocation(project, buildFileVirtual, tasks);
        }
        return null;
    }
}

