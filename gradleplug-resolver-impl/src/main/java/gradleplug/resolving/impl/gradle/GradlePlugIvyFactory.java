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

import gradleplug.resolving.events.DownloadProgressEvent;
import gradleplug.resolving.events.ResolveEvent;
import gradleplug.resolving.events.StartingDownloadEvent;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.EventManager;
import org.apache.ivy.core.event.IvyEvent;
import org.apache.ivy.core.event.IvyListener;
import org.apache.ivy.core.event.download.StartArtifactDownloadEvent;
import org.apache.ivy.core.event.resolve.StartResolveDependencyEvent;
import org.apache.ivy.core.module.id.ArtifactRevisionId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.gradle.api.internal.artifacts.ivyservice.DefaultIvyFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:stanley.shyiko@gmail.com">shyiko</a>
 * @since 17.02.2011
 */
public class GradlePlugIvyFactory extends DefaultIvyFactory {

    public Ivy createIvy(IvySettings ivySettings) {
        Ivy ivy = super.createIvy(ivySettings);
        EventManager eventManager = ivy.getResolveEngine().getEventManager();
        final gradleplug.resolving.events.EventManager pluginEventManager = gradleplug.resolving.events.EventManager.getInstance();
        final Map<String, Long> downloadStats = new HashMap<String, Long>();
        eventManager.addIvyListener(new IvyListener() {

            public void progress(IvyEvent event) {
                if (event.getClass() == TransferEvent.class) {
                    handleTransferEvent((TransferEvent) event);
                } else if (event.getClass() == StartResolveDependencyEvent.class) {
                    handleStartResolveDependencyEvent((StartResolveDependencyEvent) event);
                } else if (event.getClass() == StartArtifactDownloadEvent.class) {
                    handleStartArtifactDownloadEvent((StartArtifactDownloadEvent) event);
                }
            }

            private void handleTransferEvent(TransferEvent event) {
                int eventType = event.getEventType();
                if (eventType == TransferEvent.TRANSFER_STARTED || eventType == TransferEvent.TRANSFER_PROGRESS) {
                    String artifactURI = event.getResource().getName();
                    Long downloaded = downloadStats.get(artifactURI);
                    downloaded = (downloaded == null ? 0 : downloaded) + event.getLength();
                    downloadStats.put(artifactURI, downloaded);
                    DownloadProgressEvent downloadProgressEvent = new DownloadProgressEvent(artifactURI, downloaded, event.getTotalLength());
                    pluginEventManager.fireEvent(downloadProgressEvent);
                } else if (eventType == TransferEvent.TRANSFER_ERROR || eventType == TransferEvent.TRANSFER_COMPLETED) {
                    String artifactURI = event.getResource().getName();
                    downloadStats.put(artifactURI, null);
                }
            }

            private void handleStartResolveDependencyEvent(StartResolveDependencyEvent event) {
                ModuleRevisionId dep = event.getDependencyDescriptor().getDependencyRevisionId();
                ResolveEvent resolveEvent = new ResolveEvent(dep.getOrganisation(), dep.getName(), dep.getRevision(), dep.getBranch());
                pluginEventManager.fireEvent(resolveEvent);
            }

            private void handleStartArtifactDownloadEvent(StartArtifactDownloadEvent event) {
                ArtifactRevisionId artifact = event.getArtifact().getId();
                ModuleRevisionId dep = artifact.getModuleRevisionId();
                StartingDownloadEvent startingDownloadEvent = new StartingDownloadEvent(dep.getOrganisation(),
                        dep.getName(), dep.getRevision(), dep.getBranch(), artifact.getName(), artifact.getExt(), artifact.getType());
                pluginEventManager.fireEvent(startingDownloadEvent);
            }
        });
        return ivy;
    }
}
