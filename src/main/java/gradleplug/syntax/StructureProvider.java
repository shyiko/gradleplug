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
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;

import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 22.02.2011
 */
public abstract class StructureProvider {

    private String rootDelegate;
    private Map<String, String> closureDelegates;

    protected StructureProvider(String rootDelegate, Map<String, String> closureDelegates) {
        this.rootDelegate = rootDelegate;
        this.closureDelegates = closureDelegates;
    }

    public PsiMethod[] getMethods(PsiElement place) {
        for (Map.Entry<String, String> entry: closureDelegates.entrySet()){
            String method = entry.getKey();
            if (enclosingCall(method, place) != null) {
                String delegate = entry.getValue();
                return getClassMethods(place, delegate);
            }
        }
        return getClassMethods(place, rootDelegate);
    }

    private PsiMethod[] getClassMethods(PsiElement place, String className) {
        Project project = place.getProject();
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        PsiClass psiClass = javaPsiFacade.findClass(className, scope);
        if (psiClass == null) {
            return new PsiMethod[0];
        }
        return psiClass.getMethods();
    }

    @Nullable
    private GrCall enclosingCall(String methodName, PsiElement place) {
        if (place == null) {
            return null;
        }
        GrCall call = PsiTreeUtil.getParentOfType(place, GrCall.class, true);
        if (call == null) {
            return null;
        }
        while (call != null && !methodName.equals(getInvokedMethodName(call))) {
            call = PsiTreeUtil.getParentOfType(call, GrCall.class, true);
        }
        if (call == null) {
            return null;
        }
        final GrArgumentList argumentList = call.getArgumentList();
        if (argumentList != null) {
            for (GrExpression arg : argumentList.getExpressionArguments()) {
                if (arg instanceof GrClosableBlock && PsiTreeUtil.findCommonParent(place, arg) == arg) {
                    return call;
                }
            }
        }
        if (call instanceof GrMethodCallExpression) {
            for (GrExpression arg : call.getClosureArguments()) {
                if (arg instanceof GrClosableBlock && PsiTreeUtil.findCommonParent(place, arg) == arg) {
                    return call;
                }
            }
        }
        return null;
    }

    @Nullable
    private static String getInvokedMethodName(GrCall call) {
        if (call instanceof GrMethodCall) {
            final GrExpression expr = ((GrMethodCall) call).getInvokedExpression();
            if (expr instanceof GrReferenceExpression) {
                return ((GrReferenceExpression) expr).getName();
            }
        }
        return null;
    }
}
