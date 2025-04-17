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

    public Map<String, String> artifacts;
}
