package com.nick.mantis_jira_bridge.jira;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdfBuilderTest {

    @SuppressWarnings("unchecked")
    @Test
    void fromPlainText_producesValidAdfStructure() {
        Map<String, Object> doc = AdfBuilder.fromPlainText("Hello world");

        assertThat(doc.get("type")).isEqualTo("doc");
        assertThat(doc.get("version")).isEqualTo(1);
        List<Map<String, Object>> content = (List<Map<String, Object>>) doc.get("content");
        assertThat(content).hasSize(1);
        Map<String, Object> paragraph = content.get(0);
        assertThat(paragraph.get("type")).isEqualTo("paragraph");
        List<Map<String, Object>> texts = (List<Map<String, Object>>) paragraph.get("content");
        Map<String, Object> textNode = texts.get(0);
        assertThat(textNode.get("text")).isEqualTo("Hello world");
    }

    @SuppressWarnings("unchecked")
    @Test
    void fromPlainText_handlesNull() {
        Map<String, Object> doc = AdfBuilder.fromPlainText(null);
        List<Map<String, Object>> content = (List<Map<String, Object>>) doc.get("content");
        Map<String, Object> paragraph = content.get(0);
        List<Map<String, Object>> texts = (List<Map<String, Object>>) paragraph.get("content");
        Map<String, Object> textNode = texts.get(0);
        assertThat(textNode.get("text")).isEqualTo("");
    }

    @SuppressWarnings("unchecked")
    @Test
    void fromLines_producesOneParagraphPerLine() {
        Map<String, Object> doc = AdfBuilder.fromLines(List.of("Line 1", "Line 2", "Line 3"));

        List<?> content = (List<?>) doc.get("content");
        assertThat(content).hasSize(3);
    }
}
