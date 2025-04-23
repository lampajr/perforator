package io.github.lampajr.benchmark;

import java.util.Map;

/**
 * This class represents a benchmark configuration
 */
public class Benchmark {
    public String name;
    public String id;
    // full path of the script to run
    public String script;
    // full path where the result obtained from the job is stored
    public String result;
    // full path where the baseline is stored
    public String baseline;

    public Map<String, String> artifacts;
}
