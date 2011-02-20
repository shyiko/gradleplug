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
package gradleplug.resolving.impl;

import com.intellij.openapi.module.Module;
import gradleplug.resolving.ResolveContext;
import gradleplug.resolving.ResolveException;
import gradleplug.resolving.ResolveResult;
import gradleplug.resolving.dependencies.ResolvedDependency;
import gradleplug.resolving.tasks.ResolvedTask;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 11.02.2011
 */
public class ResolverImplTest {

    private ResolverImpl resolver;

    @Before
    public void setUp() {
        resolver = new ResolverImpl();
    }

    @Test
    public void testResolve() throws Exception {
        assertFalse(LoggerFactory.getLogger("").isDebugEnabled());
        File buildGradle = new File(this.getClass().getResource("build.gradle").toURI());
        ResolveContext context = new ResolveContext(buildGradle, Collections.<String, Module>emptyMap());
        ResolveResult resolveResult = resolver.resolve(context);
        assertNotNull(resolveResult);
        List<ResolvedDependency> resolvedDependencies = resolveResult.getResolvedDependencies();
        assertEquals(8, resolvedDependencies.size());
        List<ResolvedTask> resolvedTasks = resolveResult.getResolvedTasks();
        assertEquals(21, resolvedTasks.size());
    }

    @Test
    public void testResolveEmptyBuildFile() throws Exception {
        File buildGradle = new File(this.getClass().getResource("empty-build.gradle").toURI());
        ResolveContext context = new ResolveContext(buildGradle, Collections.<String, Module>emptyMap());
        ResolveResult resolveResult = resolver.resolve(context);
        assertNotNull(resolveResult);
        List<ResolvedDependency> resolvedDependencies = resolveResult.getResolvedDependencies();
        assertEquals(0, resolvedDependencies.size());
        List<ResolvedTask> resolvedTasks = resolveResult.getResolvedTasks();
        assertEquals(5, resolvedTasks.size());
    }

    @Test
    public void testResolveTasksOnlyOnEmptyBuildFile() throws Exception {
        File buildGradle = new File(this.getClass().getResource("empty-build.gradle").toURI());
        ResolveContext context = new ResolveContext(buildGradle);
        ResolveResult resolveResult = resolver.resolve(context);
        assertNotNull(resolveResult);
        List<ResolvedDependency> resolvedDependencies = resolveResult.getResolvedDependencies();
        assertEquals(0, resolvedDependencies.size());
        List<ResolvedTask> resolvedTasks = resolveResult.getResolvedTasks();
        assertEquals(5, resolvedTasks.size());
    }

    @Test(expected = ResolveException.class)
    public void testResolveWithNonExistingDependency() throws Exception {
        File buildGradle = new File(this.getClass().getResource("build-with-non-existing-dependency.gradle").toURI());
        ResolveContext context = new ResolveContext(buildGradle, Collections.<String, Module>emptyMap());
        resolver.resolve(context);
    }

    @Test
    public void testResolveTasksOnly() throws Exception {
        File buildGradle = new File(this.getClass().getResource("build.gradle").toURI());
        ResolveContext context = new ResolveContext(buildGradle);
        ResolveResult resolveResult = resolver.resolve(context);
        assertNotNull(resolveResult);
        List<ResolvedDependency> resolvedDependencies = resolveResult.getResolvedDependencies();
        assertEquals(0, resolvedDependencies.size());
        List<ResolvedTask> resolvedTasks = resolveResult.getResolvedTasks();
        assertEquals(21, resolvedTasks.size());
    }

    @Test
    public void testResolveTasksOnlyWithNonExistingDependency() throws Exception {
        File buildGradle = new File(this.getClass().getResource("build-with-non-existing-dependency.gradle").toURI());
        ResolveContext context = new ResolveContext(buildGradle);
        resolver.resolve(context);
        ResolveResult resolveResult = resolver.resolve(context);
        assertNotNull(resolveResult);
        List<ResolvedDependency> resolvedDependencies = resolveResult.getResolvedDependencies();
        assertEquals(0, resolvedDependencies.size());
        List<ResolvedTask> resolvedTasks = resolveResult.getResolvedTasks();
        assertEquals(21, resolvedTasks.size());
    }
}
