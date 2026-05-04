package com.nick.mantis_jira_bridge.jira;

import java.util.List;
import java.util.Map;

/**
 * Builds minimal Atlassian Document Format (ADF) structures for Jira Cloud REST v3.
 */
public final class AdfBuilder {

    private AdfBuilder() {}

    public static Map<String, Object> fromPlainText(String text) {
        return Map.of(
                "type", "doc",
                "version", 1,
                "content", List.of(
                        Map.of(
                                "type", "paragraph",
                                "content", List.of(
                                        Map.of("type", "text", "text", text == null ? "" : text)
                                )
                        )
                )
        );
    }

    public static Map<String, Object> fromLines(List<String> lines) {
        var paragraphs = lines.stream()
                .map(line -> Map.of(
                        "type", "paragraph",
                        "content", List.of(Map.of("type", "text", "text", line))
                ))
                .toList();
        return Map.of("type", "doc", "version", 1, "content", paragraphs);
    }
}
