package io.github.lampajr.handler;

import io.github.lampajr.storage.Storage;
import io.quarkiverse.githubapp.event.IssueComment;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kohsuke.github.GHEventPayload;

import java.io.IOException;

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
        if (comment.length > 2 && comment[0].equalsIgnoreCase(prompt) && issueComment.getIssue().isPullRequest()) {
            if (comment[1].equalsIgnoreCase("run")) {
                // /perforator run <test-id>
                // send back an early feedback that the command is received
                issueComment.getIssue().comment(":wave: Thanks for using Perforator! \n\nStarting performance test..");
                storage.addStartEvent(issueComment);
            } else if (comment.length > 4 && comment[1].equalsIgnoreCase("get") && comment[3].equalsIgnoreCase("from")) {
                // /perforator get <artifact-name> from <test-id>
                // send back an early feedback that the command is received
                issueComment.getIssue()
                        .comment(":hourglass_flowing_sand: Retrieving artifact " + comment[2] + " from " + comment[4]);
                storage.addDownloadArtifactEvent(comment[4], comment[2], issueComment);
            }

        }
    }
}
