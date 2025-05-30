package io.github.lampajr.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.lampajr.model.Metric;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Iterator;
import java.util.Map;

@ApplicationScoped
public class ResultConverter {

    @Inject
    ObjectMapper objectMapper;

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
                    Metric m = objectMapper.convertValue(elem.getValue(), Metric.class);
                    builder.append("| ").append(elem.getKey()).append(" | ").append(m.value).append(" ").append(m.unit)
                            .append(" |")
                            .append("\n");
                });

                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public String toMarkdown(JsonNode result, JsonNode baseline) {
        if (baseline == null) {
            return toMarkdown(result);
        }
        StringBuilder builder = new StringBuilder();
        if (result.isObject() && baseline.isObject()) {
            ObjectNode resAsObject = (ObjectNode) result;
            ObjectNode baseAsObject = (ObjectNode) baseline;
            Iterator<Map.Entry<String, JsonNode>> it = resAsObject.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> table = it.next();
                String title = table.getKey();
                ObjectNode resData = (ObjectNode) table.getValue();
                ObjectNode baseData = (ObjectNode) baseAsObject.get(table.getKey());

                // title
                builder.append("### ").append(title).append("\n").append("\n");

                // columns
                builder.append("| Metric | Value | Baseline | Delta |").append("\n");
                builder.append("| ------ |:-----:|:-----:|:-----:|").append("\n");

                // data
                resData.fields().forEachRemaining(elem -> {
                    Metric m = objectMapper.convertValue(elem.getValue(), Metric.class);
                    Metric baseM = objectMapper.convertValue(baseData.get(elem.getKey()), Metric.class);
                    builder.append("| ").append(elem.getKey()).append(" | ").append(m.value).append(" ").append(m.unit)
                            .append(" | ").append(baseM.value).append(" ").append(baseM.unit).append(" | ")
                            .append(Integer.parseInt(m.value) < Integer.parseInt(baseM.value) ? ":white_check_mark:" : ":x:")
                            .append(" |").append("\n");
                });

                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
