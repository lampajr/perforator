package io.github.lampajr.model;

import org.kohsuke.github.GHEventPayload;

public class StartTestEvent {
    public String benchmarkId;
    public GHEventPayload payload;

    // required for serialization, if any
    public StartTestEvent() {
    }

    public StartTestEvent(String benchmarkId, GHEventPayload payload) {
        this.benchmarkId = benchmarkId;
        this.payload = payload;
    }

}
