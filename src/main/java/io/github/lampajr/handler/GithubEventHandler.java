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
                String runId = storage.addStartEvent(comment[2], issueComment);
                issueComment.getIssue()
                        .comment(":wave: Thanks for using Perforator! \n\nStarting performance test run " + runId + "..");
            } else if (comment.length > 6 && comment[1].equalsIgnoreCase("get") && comment[3].equalsIgnoreCase(
                    "from") && comment[5].equalsIgnoreCase("of")) {
                // /perforator get <artifact-name> from <run-id> of <test-id>
                // send back an early feedback that the command is received
                issueComment.getIssue()
                        .comment(":hourglass_flowing_sand: Retrieving artifact " + comment[2] + " from " + comment[6]);

                storage.addDownloadArtifactEvent(comment[6], comment[4], comment[2], issueComment);
            }

        }
    }
}
