package io.github.lampajr.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lampajr.Util;
import io.github.lampajr.benchmark.Benchmark;
import io.github.lampajr.model.DownloadArtifactEvent;
import io.github.lampajr.model.StartTestEvent;
import io.github.lampajr.storage.Storage;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Startup
@ApplicationScoped
public class PerfProcessor {

    @ConfigProperty(name = "perforator.pattern")
    String runIdPattern;

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

    @Incoming("download-artifact-in")
    public void processDownloadArtifact(String uuid) throws IOException {
        DownloadArtifactEvent event = storage.popDownloadArtifactEvent(uuid);
        if (event == null) {
            Log.error("Cannot find download artifact event with " + uuid);
            return;
        }

        StartTestEvent run = storage.getStartEvent(event.runId);
        if (run == null) {
            Log.error("Cannot find run with " + event.runId);
        } else if (!Objects.equals(run.benchmarkId, event.benchmarkId)) {
            Log.error("Trying to get artifact from run with different benchmark id " + event.benchmarkId);
        } else {
            GHEventPayload payload = event.payload;
            if (payload instanceof GHEventPayload.IssueComment issueComment) {
                Benchmark benchmark = benchmarks.get(event.benchmarkId);
                if (benchmark == null) {
                    Log.error("Cannot find benchmark with id " + event.benchmarkId);
                    issueComment.getIssue().comment(":x: Cannot find benchmark with id '" + event.benchmarkId + "'");
                    return;
                }

                String artifactLocation = benchmark.artifacts.get(event.artifactId);
                if (artifactLocation == null) {
                    Log.error("Cannot find artifact with id " + event.artifactId);
                    issueComment.getIssue().comment(":x: Cannot find artifact with id '" + event.benchmarkId + "'");
                    return;
                }

                // get file and post it to the issue based on the extension (e.g., txt or html or png if possible)
                getBenchmarkArtifact(event.runId, issueComment.getIssue(), event.artifactId, artifactLocation);
            }

        }
    }

    @Incoming("start-event-in")
    public void processStartEvent(String uuid) throws IOException, InterruptedException {
        StartTestEvent event = storage.getStartEvent(uuid);
        if (event == null) {
            Log.error("Cannot find start test event with " + uuid);
        } else {
            GHEventPayload payload = event.payload;
            if (payload instanceof GHEventPayload.IssueComment issueComment) {
                Log.info("Received start event from issue " + issueComment.getIssue().getNumber());

                Benchmark benchmark = benchmarks.get(event.benchmarkId);
                if (benchmark == null) {
                    Log.error("Cannot find benchmark with id " + event.benchmarkId);
                    issueComment.getIssue().comment(":x: Cannot find benchmark with id '" + event.benchmarkId + "'");
                    return;
                }

                // run job and look for the file result
                runBenchmark(benchmark, uuid, event.withBaseline, issueComment.getIssue());
            }
        }
    }

    void getBenchmarkArtifact(String runId, GHIssue issue, String artifactId, String artifactLocation)
            throws IOException {
        StringBuilder comment = new StringBuilder();
        artifactLocation = artifactLocation.replace(runIdPattern, runId);
        String extension = FilenameUtils.getExtension(artifactLocation);
        switch (extension) {
            case "txt":
            case "json":
                // get the file content and copy it to the comment as it is
                comment.append("### Artifact ").append(artifactId).append("\n\n");
                comment.append("```").append(extension).append("\n");
                Files.readAllLines(Paths.get(artifactLocation), StandardCharsets.UTF_8)
                        .forEach(line -> comment.append(line).append("\n"));
                comment.append("```");
                break;
            case "png":
            case "jpg":
                // TODO: get the file content and copy it to the comment
                Log.error("Image extension not supported!");
                comment.append(":x: Image extension not supported!");
                //                break;
            default:
                comment.append("The artifact you asked for can be found at: [").append(artifactLocation).append("](")
                        .append(artifactLocation).append(")");
                break;
        }

        issue.comment(comment.toString());
    }

    // TODO: this should NOT be blocking
    void runBenchmark(Benchmark benchmark, String runId, boolean useBaseline, GHIssue issue)
            throws InterruptedException, IOException {
        // comment to post into the issue
        StringBuilder comment = new StringBuilder()
                .append("You benchmark ")
                .append(benchmark.id)
                .append(" completed successfully.\n\n");

        File log = new File("/tmp/test.log");
        File errorLog = new File("/tmp/test.error.log");
        ProcessBuilder builder = new ProcessBuilder()
                .command("bash", "-c", benchmark.script + " " + runId)
                .redirectOutput(ProcessBuilder.Redirect.to(log))
                .redirectError(ProcessBuilder.Redirect.to(errorLog));
        Process process = builder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            Log.error("Benchmark exited with code " + exitCode);
            issue.comment(":x: Benchmark execution failed, pls contact your administrators");
            return;
        }
        Log.info("Benchmark exited with code " + exitCode);

        JsonNode result;
        JsonNode baseline = null;
        try {
            result = objectMapper.readValue(new File(benchmark.result.replace(runIdPattern, runId)), JsonNode.class);
        } catch (IOException e) {
            Log.error("Error reading result from file " + benchmark.result, e);
            issue.comment(":x: Benchmark completed but cannot find results at " + benchmark.result);
            return;
        }
        Log.info("Benchmark result: " + result);

        if (useBaseline) {
            try {
                baseline = objectMapper.readValue(new File(benchmark.baseline.replace(runIdPattern, runId)), JsonNode.class);
            } catch (IOException e) {
                Log.error("Error reading baseline from file " + benchmark.baseline, e);
                issue.comment(":x: Benchmark completed but cannot find baseline at " + benchmark.baseline);
                return;
            }
        }
        Log.info("Benchmark baseline: " + baseline);

        comment.append(resultConverter.toMarkdown(result, baseline));
        issue.comment(comment.toString());
    }
}
