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

    // in this PoC we can see each start event as a run
    ConcurrentHashMap<String, StartTestEvent> startEvents = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, DownloadArtifactEvent> downloadArtifactEvents = new ConcurrentHashMap<>();

    public String addStartEvent(String benchmarkId, GHEventPayload.IssueComment issueComment) {
        String uuid = UUID.randomUUID().toString();
        startEvents.put(uuid, new StartTestEvent(benchmarkId, issueComment));
        startEventEmitter.send(uuid);
        return uuid;
    }

    public StartTestEvent getStartEvent(String uuid) {
        return startEvents.get(uuid);
    }

    public StartTestEvent removeStartEvent(String uuid) {
        return startEvents.remove(uuid);
    }

    public void addDownloadArtifactEvent(String benchmarkId, String runId, String artifactId, GHEventPayload payload) {
        String uuid = UUID.randomUUID().toString();
        downloadArtifactEvents.put(uuid, new DownloadArtifactEvent(benchmarkId, runId, artifactId, payload));
        downloadArtifactEmitter.send(uuid);
    }

    public DownloadArtifactEvent popDownloadArtifactEvent(String uuid) {
        return downloadArtifactEvents.remove(uuid);
    }
}
