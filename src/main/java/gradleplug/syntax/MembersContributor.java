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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.completion.GroovyCompletionUtil;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 20.02.2011
 */
public abstract class MembersContributor extends NonCodeMembersContributor {

    private String fileName;
    private StructureProvider structureProvider;

    protected MembersContributor(String fileName, StructureProvider structureProvider) {
        this.fileName = fileName;
        this.structureProvider = structureProvider;
    }

    @Override
    public void processDynamicElements(@NotNull PsiType psiType, PsiScopeProcessor processor, GroovyPsiElement place, ResolveState state) {
        PsiFile containingFile = place.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null || !fileName.equalsIgnoreCase(virtualFile.getName())) {
            return;
        }
        PsiMethod[] methods = structureProvider.getMethods(place);
        for (PsiMethod psiMethod : methods) {
            if (!ResolveUtil.processElement(processor, psiMethod, state)) {
                return;
            }
        }
    }
}
