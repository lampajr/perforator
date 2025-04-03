package io.github.lampajr.model;

import org.kohsuke.github.GHEventPayload;

public class StartTestEvent {
    public GHEventPayload payload;

    // required for serialization, if any
    public StartTestEvent() {
    }

    public StartTestEvent(GHEventPayload payload) {
        this.payload = payload;
    }

}
