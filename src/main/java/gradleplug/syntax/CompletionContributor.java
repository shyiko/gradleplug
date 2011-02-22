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
package gradleplug.syntax;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.plugins.groovy.lang.completion.GroovyCompletionUtil;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 20.02.2011
 */
public abstract class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {

    private String fileName;
    private StructureProvider structureProvider;

    protected CompletionContributor(String fileName, StructureProvider structureProvider) {
        this.fileName = fileName;
        this.structureProvider = structureProvider;
    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        PsiFile containingFile = parameters.getOriginalFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null || !fileName.equalsIgnoreCase(virtualFile.getName())) {
            return;
        }
        PsiMethod[] methods = structureProvider.getMethods(parameters.getPosition());
        for (PsiMethod psiMethod : methods) {
            result.addElement(GroovyCompletionUtil.generateLookupElement(psiMethod));
        }
    }
}
