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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import gradleplug.adapters.ProjectComponentAdapter;
import gradleplug.bundles.GradlePlugBundle;
import gradleplug.resolving.Resolver;
import org.jetbrains.plugins.groovy.gradle.GradleSettings;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 10.02.2011
 */
public class ResolverLoader extends ProjectComponentAdapter {

    private String gradleHome;
    private Resolver resolver;

    public ResolverLoader(Project project) {
        super(project);
    }

    public Resolver getResolver() {
        return resolver;
    }

    public boolean loadGradle() {
        final AtomicBoolean result = new AtomicBoolean();
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            public void run() {
                String gradleHome = getGradleHomeFromProjectSettings();
                if (gradleHome == null) {
                    boolean takeToGradleSettings = Messages.showYesNoDialog(GradlePlugBundle.message("no.configured.gradle.message"),
                            GradlePlugBundle.message("no.configured.gradle.title"), Messages.getQuestionIcon()) == 0;
                    if (takeToGradleSettings) {
                        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Gradle");
                        gradleHome = getGradleHomeFromProjectSettings();
                    }
                }
                if (gradleHome != null && !gradleHome.equals(ResolverLoader.this.gradleHome)) {
                    ResolverLoader.this.gradleHome = gradleHome;
                    reloadResolver(gradleHome);
                }
                result.set(gradleHome != null);
            }
        });
        return result.get();
    }

    private String getGradleHomeFromProjectSettings() {
        VirtualFile gradleHome = GradleSettings.getInstance(project).getSdkHome();
        return gradleHome != null ? gradleHome.getPath() : null;
    }

    private void reloadResolver(String gradleHome) {
        List<URL> gradleLibraries = getGradleLibraries(gradleHome);
        URL[] urls = new URL[gradleLibraries.size() + 1];
        int i = 0;
        for (URL gradleLibrary : gradleLibraries) {
            urls[i++] = gradleLibrary;
        }
        urls[i] = findResolverImplJar();
        ClassLoader parentClassLoader = ResolverLoader.class.getClassLoader();
        URLClassLoader gradleClassLoader = new GradleClassLoader(urls, parentClassLoader);
        try {
            Class<?> resolverImpl = gradleClassLoader.loadClass("gradleplug.resolving.impl.ResolverImpl");
            resolver = (Resolver) resolverImpl.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URL findResolverImplJar() {
        PluginClassLoader classLoader = (PluginClassLoader) getClass().getClassLoader();
        PluginId pluginId = classLoader.getPluginId();
        IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(pluginId);
        if (pluginDescriptor == null) {
            throw new RuntimeException("Failed to determine plugin descriptor");
        }
        File pluginHomeDir = pluginDescriptor.getPath();
        return toURL(new File(pluginHomeDir, "lib/ext/gradleplug-resolver-impl.jar"));
    }

    private List<URL> getGradleLibraries(String gradleHome) {
        return listJarsRecursive(new File(gradleHome, "lib"));
    }

    private List<URL> listJarsRecursive(File directory) {
        List<URL> result = new ArrayList<URL>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                result.addAll(listJarsRecursive(file));
            } else {
                if (file.getName().toLowerCase().endsWith(".jar")) {
                    if (isJDK6() && !isJDK6Compatible(file)) {
                        continue;
                    }
                    result.add(toURL(file));
                }
            }
        }
        return result;
    }

    private boolean isJDK6() {
        return System.getProperty("java.version").contains("1.6");
    }

    private boolean isJDK6Compatible(File file) {
        boolean result = true;
        try {
            ZipFile zipFile = new ZipFile(file);
            try {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    if (zipEntry.getName().contains("javax/xml")) {
                        result = false;
                        break;
                    }
                }
            } finally {
                zipFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
