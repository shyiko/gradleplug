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

import com.intellij.execution.PsiLocation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;

import java.util.List;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 13.02.2011
 */
public class GradleTaskLocation extends PsiLocation<PsiFile> {

    private GroovyFile buildFile;
    private List<String> tasks;

    @SuppressWarnings({"ConstantConditions"})
    public GradleTaskLocation(Project project, VirtualFile buildFile, List<String> tasks) {
        super(project, PsiManager.getInstance(project).findFile(project.getProjectFile()));
        this.buildFile = (GroovyFile) PsiManager.getInstance(project).findFile(buildFile);
        this.tasks = tasks;
    }

    public GroovyFile getBuildFile() {
        return buildFile;
    }

    public List<String> getTasks() {
        return tasks;
    }
}
