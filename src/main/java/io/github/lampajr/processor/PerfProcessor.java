package io.github.lampajr.processor;

import io.github.lampajr.model.StartTestEvent;
import io.github.lampajr.storage.Storage;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kohsuke.github.GHEventPayload;

import java.io.IOException;

@ApplicationScoped
public class PerfProcessor {

    @Inject
    Storage storage;

    @Incoming("start-event-in")
    public void processStartEvents(String uuid) throws IOException {
        StartTestEvent event = storage.getStartEvent(uuid);
        if (event == null) {
            Log.error("Cannot find event with " + uuid);
        } else {
            GHEventPayload payload = event.payload;
            if (payload instanceof GHEventPayload.IssueComment issueComment) {
                Log.info("Received start event from issue " + issueComment.getIssue().getNumber());
                // TODO: simulate job execution
                issueComment.getIssue().comment(":wave: Performance test completed.");
            }
        }
    }
}
