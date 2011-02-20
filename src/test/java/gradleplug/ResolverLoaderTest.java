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

import com.intellij.openapi.module.Module;
import gradleplug.resolving.ResolveContext;
import gradleplug.resolving.ResolveResult;
import gradleplug.resolving.Resolver;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 18.02.2011
 */
public class ResolverLoaderTest {

    @Test
    public void testResolver() throws Exception {
        ResolverLoader resolverLoader = new ResolverLoader(null);
        assertTrue(resolverLoader.loadGradle());
        resolverLoader.getResolver();
        Resolver resolver = resolverLoader.getResolver();
        File buildGradle = new File(this.getClass().getResource("build.gradle").toURI());
        ResolveContext context = new ResolveContext(buildGradle, Collections.<String, Module>emptyMap());
        ResolveResult resolveResult = resolver.resolve(context);
        assertNotNull(resolveResult);
    }
}
