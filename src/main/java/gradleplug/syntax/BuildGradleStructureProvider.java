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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 22.02.2011
 */
public class BuildGradleStructureProvider extends StructureProvider {

    private BuildGradleStructureProvider() {
        super("org.gradle.api.Project", loadFromClasspath("build.gradle.closures"));
    }

    private static class BuildGradleStructureProviderHolder {
        private static final BuildGradleStructureProvider instance = new BuildGradleStructureProvider();
    }

    public static BuildGradleStructureProvider getInstance() {
        return BuildGradleStructureProviderHolder.instance;
    }

    private static Map<String, String> loadFromClasspath(String filename) {
        Properties properties = new Properties();
        InputStream stream = BuildGradleStructureProvider.class.getResourceAsStream(filename);
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //noinspection unchecked,RedundantCast
        return (Map<String, String>) ((Map) properties);
    }
}
