package io.github.lampajr.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Iterator;
import java.util.Map;

@ApplicationScoped
public class ResultConverter {

    /**
     * Convert the provided JSON result object into a table markdown string
     * @param result JsonNode
     * @return table markdown as string
     */
    // TODO: there is no validation atm
    public String toMarkdown(JsonNode result) {
        StringBuilder builder = new StringBuilder();
        if (result.isObject()) {
            ObjectNode asObject = (ObjectNode) result;
            Iterator<Map.Entry<String, JsonNode>> it = asObject.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> table = it.next();
                String title = table.getKey();
                ObjectNode data = (ObjectNode) table.getValue();

                // title
                builder.append("### ").append(title).append("\n").append("\n");

                // columns
                builder.append("| Metric | Value |").append("\n");
                builder.append("| ------ |:-----:|").append("\n");

                // data
                data.fields().forEachRemaining(elem -> {
                    builder.append("| ").append(elem.getKey()).append(" | ").append(elem.getValue().asText()).append(" |")
                            .append("\n");
                });

                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
