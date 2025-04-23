package io.github.lampajr.model;

import org.kohsuke.github.GHEventPayload;

public class StartTestEvent {
    public String benchmarkId;
    public boolean withBaseline;
    public GHEventPayload payload;

    // required for serialization, if any
    public StartTestEvent() {
    }

    public StartTestEvent(String benchmarkId, boolean withBaseline, GHEventPayload payload) {
        this.benchmarkId = benchmarkId;
        this.withBaseline = withBaseline;
        this.payload = payload;
    }

}
