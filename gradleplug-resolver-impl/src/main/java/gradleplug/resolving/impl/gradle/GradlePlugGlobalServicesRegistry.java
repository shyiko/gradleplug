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
package gradleplug.resolving.impl.gradle;

import org.gradle.api.internal.project.GlobalServicesRegistry;
import org.gradle.logging.LoggingServiceRegistry;
import org.gradle.logging.internal.OutputEvent;
import org.gradle.logging.internal.OutputEventListener;
import org.gradle.logging.internal.OutputEventRenderer;
import org.gradle.logging.internal.StdOutLoggingSystem;
import org.gradle.util.TrueTimeProvider;

import java.io.PrintStream;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 17.02.2011
 */
public class GradlePlugGlobalServicesRegistry extends GlobalServicesRegistry {

    public GradlePlugGlobalServicesRegistry() {
        super(new LoggingServiceRegistry() {

            @Override
            protected StdOutLoggingSystem createStdOutLoggingSystem() {
                return new StdOutLoggingSystem(new OutputEventListener() {
                    public void onOutput(OutputEvent event) {

                    }
                }, new TrueTimeProvider()) {
                    @Override
                    protected void set(PrintStream printStream) {
                    }
                };
            }

            @Override
            protected OutputEventRenderer createOutputEventRenderer() {
                return new OutputEventRenderer() {
                    @Override
                    public void onOutput(OutputEvent event) {

                    }
                };
            }
        });
    }
}
