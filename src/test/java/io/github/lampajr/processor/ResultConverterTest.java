package io.github.lampajr.processor;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultConverterTest {

    final ResultConverter converter = new ResultConverter();

    @Test
    void toMarkdown() {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.putObject("table 1")
                .put("column1", "value1")
                .put("column2", "value2")
                .put("column3", "value3");


        json.putObject("table 2")
                .put("column1", "value1")
                .put("column2", "value2")
                .put("column3", "value3");
        String markdown = converter.toMarkdown(json);
        System.out.println(markdown);
    }
}