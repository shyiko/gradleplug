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
package gradleplug.resolving;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 10.02.2011
 */
public class ResolveContext {

    private Map<String, Module> modules;
    private boolean resolveDependencies;
    private File buildFile;

    public ResolveContext(File buildFile, Map<String, Module> modules) {
        this(buildFile, true, modules);
    }

    public ResolveContext(File buildFile) {
        this(buildFile, false, Collections.<String, Module>emptyMap());
    }

    private ResolveContext(File buildFile, boolean resolveDependencies, Map<String, Module> modules) {
        if (buildFile == null) {
            throw new IllegalArgumentException("buildFile should not be null");
        }
        this.buildFile = buildFile;
        this.resolveDependencies = resolveDependencies;
        if (modules == null) {
            throw new IllegalArgumentException("modules should not be null");
        }
        this.modules = modules;
    }

    @NotNull
    public Map<String, Module> getModules() {
        return modules;
    }

    public boolean isResolveDependencies() {
        return resolveDependencies;
    }

    @NotNull
    public File getBuildFile() {
        return buildFile;
    }
}
