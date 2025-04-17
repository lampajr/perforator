package io.github.lampajr.storage;

import io.github.lampajr.model.DownloadArtifactEvent;
import io.github.lampajr.model.StartTestEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.kohsuke.github.GHEventPayload;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class Storage {

    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 10000)
    @Channel("start-event-out")
    Emitter<String> startEventEmitter;

    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 10000)
    @Channel("download-artifact-out")
    Emitter<String> downloadArtifactEmitter;

    ConcurrentHashMap<String, StartTestEvent> startEvents = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, DownloadArtifactEvent> downloadArtifactEvents = new ConcurrentHashMap<>();

    public void addStartEvent(GHEventPayload.IssueComment issueComment) {
        String uuid = UUID.randomUUID().toString();
        startEvents.put(uuid, new StartTestEvent(issueComment));
        startEventEmitter.send(uuid);
    }

    public StartTestEvent getStartEvent(String uuid) {
        return startEvents.remove(uuid);
    }

    public void addDownloadArtifactEvent(String benchmarkId, String artifactId, GHEventPayload payload) {
        String uuid = UUID.randomUUID().toString();
        downloadArtifactEvents.put(uuid, new DownloadArtifactEvent(benchmarkId, artifactId, payload));
        downloadArtifactEmitter.send(uuid);
    }

    public DownloadArtifactEvent getDownloadArtifactEvent(String uuid) {
        return downloadArtifactEvents.remove(uuid);
    }
}
