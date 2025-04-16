package io.github.lampajr.processor;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResultConverter {

    /**
     * Convert the provided JSON result object into a table markdown string
     * @param result JsonNode
     * @return table markdown as string
     */
    public String toMarkdown(JsonNode result) {
        return "";
    }
}
