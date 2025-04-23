package io.github.lampajr.processor;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ResultConverterTest {

    @Inject
    ResultConverter converter;

    @Test
    void toMarkdown() {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        ObjectNode table1 = json.putObject("table 1");
        table1.putObject("column1")
                .put("value", "value1")
                .put("unit", "unit1");

        ObjectNode table2 = json.putObject("table 2");
        table2.putObject("column1")
                .put("value", "value1")
                .put("unit", "unit1");
        table2.putObject("column2")
                .put("value", "value2")
                .put("unit", "unit3");
        String markdown = converter.toMarkdown(json);
        System.out.println(markdown);
    }

    @Test
    void toMarkdownWithBaseline() {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        ObjectNode table1 = json.putObject("table 1");
        table1.putObject("column1")
                .put("value", 333)
                .put("unit", "unit1");

        ObjectNode table2 = json.putObject("table 2");
        table2.putObject("column1")
                .put("value", 45)
                .put("unit", "unit1");
        table2.putObject("column2")
                .put("value", 0)
                .put("unit", "unit3");

        ObjectNode baseline = JsonNodeFactory.instance.objectNode();
        ObjectNode baseTable1 = baseline.putObject("table 1");
        baseTable1.putObject("column1")
                .put("value", 100)
                .put("unit", "unit1");

        ObjectNode baseTable2 = baseline.putObject("table 2");
        baseTable2.putObject("column1")
                .put("value", 1)
                .put("unit", "unit1");
        baseTable2.putObject("column2")
                .put("value", 2)
                .put("unit", "unit3");

        String markdown = converter.toMarkdown(json, baseline);
        System.out.println(markdown);
    }
}