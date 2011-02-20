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
import java.io.File;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 19.02.2011
 */
public class ProjectNode extends GradleNode {

    private static final Icon PROJECT_ICON = IconLoader.getIcon("/icons/gradle/gradle.png");

    private File buildFile;

    public ProjectNode(File buildFile) {
        super(buildFile.getParentFile().getName());
        this.buildFile = buildFile;
        insert(new TaskContainerNode(), 0);
        setIcon(PROJECT_ICON);
    }

    public String getName() {
        return (String) getUserObject();
    }

    public File getBuildFile() {
        return buildFile;
    }

    public TaskContainerNode getTaskContainerNode() {
        return (TaskContainerNode) getFirstChild();
    }

    public ProjectContainerNode getParent() {
        return (ProjectContainerNode) super.getParent();
    }

    @Override
    public String getMenuId() {
        return "Gradle.ToolTree.ProjectNode.Menu";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectNode that = (ProjectNode) o;

        if (!buildFile.equals(that.buildFile)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return buildFile.hashCode();
    }
}