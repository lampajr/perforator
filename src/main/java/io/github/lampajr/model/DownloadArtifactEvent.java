package io.github.lampajr.model;

import org.kohsuke.github.GHEventPayload;

public class DownloadArtifactEvent {
    public String benchmarkId;
    public String runId;
    public String artifactId;
    public GHEventPayload payload;

    public DownloadArtifactEvent() {
    }

    public DownloadArtifactEvent(String benchmarkId, String runId, String artifactId, GHEventPayload payload) {
        this.benchmarkId = benchmarkId;
        this.runId = runId;
        this.artifactId = artifactId;
        this.payload = payload;
    }
}
