package io.github.lampajr.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lampajr.Util;
import io.github.lampajr.benchmark.Benchmark;
import io.github.lampajr.model.StartTestEvent;
import io.github.lampajr.storage.Storage;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Startup
@ApplicationScoped
public class PerfProcessor {

    @Inject
    Storage storage;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ResultConverter resultConverter;

    private final Map<String, Benchmark> benchmarks;

    PerfProcessor() {
        benchmarks = new HashMap<>();

        try {
            List<String> resourceFiles = Util.getResourceFiles("benchmarks");
            for (String file : resourceFiles) {
                Yaml yaml = new Yaml();
                InputStream inputStream = Util.getResourceAsStream("benchmarks/".concat(file));

                Benchmark benchmark = yaml.loadAs(inputStream, Benchmark.class);

                benchmarks.put(benchmark.id, benchmark);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Incoming("start-event-in")
    public void processStartEvents(String uuid) throws IOException, InterruptedException {
        StartTestEvent event = storage.getStartEvent(uuid);
        if (event == null) {
            Log.error("Cannot find event with " + uuid);
        } else {
            GHEventPayload payload = event.payload;
            if (payload instanceof GHEventPayload.IssueComment issueComment) {
                Log.info("Received start event from issue " + issueComment.getIssue().getNumber());

                // /perforator run <test-id>
                String[] comment = issueComment.getComment().getBody().split(" ");
                String benchmarkId = comment[2];

                Benchmark benchmark = benchmarks.get(benchmarkId);
                if (benchmark == null) {
                    Log.error("Cannot find benchmark with id " + benchmarkId);
                    issueComment.getIssue().comment(":bangbang: Cannot find benchmark with id '" + benchmarkId + "'");
                    return;
                }

                // run job and look for the file result
                runBenchmark(benchmark, issueComment.getIssue());
            }
        }
    }

    // TODO: this should NOT be blocking
    void runBenchmark(Benchmark benchmark, GHIssue issue) throws InterruptedException, IOException {
        // comment to post into the issue
        StringBuilder comment = new StringBuilder()
                .append("You benchmark ")
                .append(benchmark.id)
                .append(" completed successfully.\n\n");

        File log = new File("/tmp/test.log");
        File errorLog = new File("/tmp/test.error.log");
        ProcessBuilder builder = new ProcessBuilder()
                .command("bash", "-c", benchmark.script)
                .redirectOutput(ProcessBuilder.Redirect.to(log))
                .redirectError(ProcessBuilder.Redirect.to(errorLog));
        Process process = builder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            Log.error("Benchmark exited with code " + exitCode);
            issue.comment(":bangbang: Benchmark execution failed, pls contact your administrators");
            return;
        }
        Log.info("Benchmark exited with code " + exitCode);

        JsonNode result = null;
        try {
            result = objectMapper.readValue(new File(benchmark.result), JsonNode.class);
        } catch (IOException e) {
            Log.error("Error reading result from file " + benchmark.result, e);
            issue.comment(":bangbang: Benchmark completed but cannot find results at " + benchmark.result);
            return;
        }
        Log.info("Benchmark result: " + result);

        comment.append(resultConverter.toMarkdown(result));
        issue.comment(comment.toString());
    }
}
