package io.github.lampajr.handler;

import io.github.lampajr.storage.Storage;
import io.quarkiverse.githubapp.event.IssueComment;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.kohsuke.github.GHEventPayload;

import java.io.IOException;
import java.util.UUID;

@ApplicationScoped
class GithubEventHandler {

    @ConfigProperty(name = "perforator.prompt")
    String prompt;

    @Inject
    Storage storage;

    void onComment(@IssueComment GHEventPayload.IssueComment issueComment) throws IOException {
        Log.info("Received comment on issue number " + issueComment.getIssue().getNumber());

        // TODO: implement more robust parsing logic
        String[] comment = issueComment.getComment().getBody().split(" ");
        if (comment.length > 0 && comment[0].equalsIgnoreCase(prompt) && issueComment.getIssue().isPullRequest()) {
            // send back an early feedback that the command is received
            issueComment.getIssue().comment(":wave: Thanks for using Perforator! Starting performance test..");
            storage.addStartEvent(issueComment);
        }
    }
}
