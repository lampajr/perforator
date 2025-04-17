package io.github.lampajr.model;

import org.kohsuke.github.GHEventPayload;

public class DownloadArtifactEvent {
    public String benchmarkId;
    public String artifactId;
    public GHEventPayload payload;

    public DownloadArtifactEvent() {
    }

    public DownloadArtifactEvent(String benchmarkId, String artifactId, GHEventPayload payload) {
        this.benchmarkId = benchmarkId;
        this.artifactId = artifactId;
        this.payload = payload;
    }
}
