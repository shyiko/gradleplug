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

import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.Location;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.runner.GroovyScriptRunConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 13.02.2011
 */
public class GradleTaskRuntimeConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {

    protected PsiElement sourceElement;

    public GradleTaskRuntimeConfigurationProducer() {
        super(GradleTaskConfigurationType.getInstance());
    }

    public PsiElement getSourceElement() {
        return sourceElement;
    }

    public int compareTo(final Object o) {
        return RuntimeConfigurationProducer.PREFERED;
    }

    @Override
    protected RunnerAndConfigurationSettings findExistingByElement(Location location,
                                                                   @NotNull RunnerAndConfigurationSettings[] existingConfigurations,
                                                                   ConfigurationContext context) {
        if (!(location instanceof GradleTaskLocation)) {
            return null;
        }
        GradleTaskLocation taskLocation = (GradleTaskLocation) location;
        String programParameters = getProgramParameters(taskLocation);
        VirtualFile virtualFile = taskLocation.getBuildFile().getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        String scriptPath = virtualFile.getPath();
        for (RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
            GroovyScriptRunConfiguration configuration = (GroovyScriptRunConfiguration) existingConfiguration.getConfiguration();
            if (programParameters.equals(configuration.getProgramParameters()) &&
                    scriptPath.equals(configuration.getScriptPath())) {
                return existingConfiguration;
            }
        }
        return null;
    }

    protected RunnerAndConfigurationSettings createConfigurationByElement(final Location location, final ConfigurationContext context) {
        if (!(location instanceof GradleTaskLocation)) {
            return null;
        }
        GradleTaskLocation taskLocation = (GradleTaskLocation) location;
        RunnerAndConfigurationSettings settings = createConfiguration(taskLocation);
        sourceElement = taskLocation.getBuildFile();
        return settings;
    }

    private RunnerAndConfigurationSettings createConfiguration(GradleTaskLocation taskLocation) {
        GroovyFile buildFile = taskLocation.getBuildFile();
        PsiClass scriptClass = buildFile.getScriptClass();
        if (scriptClass == null) {
            return null;
        }
        Project project = scriptClass.getProject();
        RunManagerEx runManagerEx = RunManagerEx.getInstanceEx(project);
        RunnerAndConfigurationSettings settings =
                runManagerEx.createConfiguration(getConfigurationName(taskLocation), getConfigurationFactory());
        final GroovyScriptRunConfiguration configuration = (GroovyScriptRunConfiguration) settings.getConfiguration();
        PsiDirectory containingDirectory = buildFile.getContainingDirectory();
        if (containingDirectory == null) {
            return null;
        }
        configuration.setWorkDir(containingDirectory.getVirtualFile().getPath());
        VirtualFile virtualFile = buildFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        configuration.setModule(JavaExecutionUtil.findModule(scriptClass));
        /*
                    Forced to use the ugly ones below because of GroovyScriptRunConfiguration class signature changes (IDEA 10 builds)
                */
        setScriptPath(configuration, virtualFile.getPath());
        setScriptParams(configuration, (getProgramParameters(taskLocation)));
        return settings;
    }

    private void setScriptPath(GroovyScriptRunConfiguration configuration, String scriptPath) {
        setValueRethrowingExceptions(configuration, "setScriptPath", String.class, "scriptPath", scriptPath);
    }

    private void setScriptParams(GroovyScriptRunConfiguration configuration, String scriptParams) {
        setValueRethrowingExceptions(configuration, "setProgramParameters", String.class, "scriptParams", scriptParams);
    }

    private void setValueRethrowingExceptions(Object object, String method, Class methodParamType, String field, Object value) {
        try {
            setValue(object, method, methodParamType, field, value);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private void setValue(Object object, String method, Class methodParamType, String field, Object value)
            throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class cls = object.getClass();
        try {
            Method setProgramParameters = cls.getMethod(method, methodParamType);
            setProgramParameters.invoke(object, value);
        } catch (NoSuchMethodException ex) {
            Field scriptParamsField = cls.getField(field);
            scriptParamsField.set(object, value);
        }
    }

    private String getConfigurationName(GradleTaskLocation taskLocation) {
        GroovyFile buildFile = taskLocation.getBuildFile();
        PsiDirectory containingDirectory = buildFile.getContainingDirectory();
        assert containingDirectory != null;
        return containingDirectory.getName() + " " + getProgramParameters(taskLocation);
    }

    private String getProgramParameters(GradleTaskLocation taskLocation) {
        StringBuilder sb = new StringBuilder();
        for (String task : taskLocation.getTasks()) {
            sb.append(task).append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }
}

