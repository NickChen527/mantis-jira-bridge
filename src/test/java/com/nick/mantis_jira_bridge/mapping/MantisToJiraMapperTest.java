package com.nick.mantis_jira_bridge.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nick.mantis_jira_bridge.config.JiraProperties;
import com.nick.mantis_jira_bridge.mantis.dto.MantisIssue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MantisToJiraMapperTest {

    private MantisToJiraMapper mapper;
    private JiraProperties jiraProperties;

    @BeforeEach
    void setUp() {
        mapper = new MantisToJiraMapper();
        jiraProperties = new JiraProperties();
        jiraProperties.setBaseUrl("https://mantis.example.com");
        jiraProperties.setEmail("test@example.com");
        jiraProperties.setApiToken("token");
        jiraProperties.setProjectKey("PROJ");
        jiraProperties.setDefaultIssueType("Task");
    }

    @Test
    void toJiraRequest_mapsBasicFields() throws Exception {
        var issue = buildIssue(1L, "Bug in login", "Steps to reproduce...", "high", null);

        var request = mapper.toJiraRequest(issue, jiraProperties);

        assertThat(request.getFields().getProject()).containsEntry("key", "PROJ");
        assertThat(request.getFields().getSummary()).isEqualTo("Bug in login");
        assertThat(request.getFields().getIssuetype()).containsEntry("name", "Task");
        assertThat(request.getFields().getPriority()).containsEntry("name", "High");
    }

    @ParameterizedTest
    @CsvSource({
            "urgent, Highest",
            "high, High",
            "normal, Medium",
            "low, Low",
            "none, Lowest"
    })
    void toJiraRequest_mapsPriorityCorrectly(String mantisPriority, String expectedJiraPriority) {
        var issue = buildIssue(1L, "Test", "Desc", mantisPriority, null);

        var request = mapper.toJiraRequest(issue, jiraProperties);

        assertThat(request.getFields().getPriority()).containsEntry("name", expectedJiraPriority);
    }

    @Test
    void toJiraRequest_unknownPriorityDefaultsToMedium() {
        var issue = buildIssue(1L, "Test", "Desc", "unknown_priority", null);

        var request = mapper.toJiraRequest(issue, jiraProperties);

        assertThat(request.getFields().getPriority()).containsEntry("name", "Medium");
    }

    @Test
    void toJiraRequest_nullPriorityDefaultsToMedium() {
        var issue = buildIssue(1L, "Test", "Desc", null, null);

        var request = mapper.toJiraRequest(issue, jiraProperties);

        assertThat(request.getFields().getPriority()).containsEntry("name", "Medium");
    }

    @Test
    void toJiraRequest_longSummaryIsTruncated() {
        String longSummary = "A".repeat(300);
        var issue = buildIssue(1L, longSummary, "Desc", "normal", null);

        var request = mapper.toJiraRequest(issue, jiraProperties);

        assertThat(request.getFields().getSummary()).hasSize(250);
    }

    @Test
    void toJiraRequest_nullSummaryUsesDefault() {
        var issue = buildIssue(1L, null, "Desc", "normal", null);

        var request = mapper.toJiraRequest(issue, jiraProperties);

        assertThat(request.getFields().getSummary()).isEqualTo("Mantis Issue #1");
    }

    @Test
    void toJiraRequest_descriptionContainsBacklink() throws Exception {
        var issue = buildIssue(42L, "Title", "Some description", "normal", null);

        var request = mapper.toJiraRequest(issue, jiraProperties);

        var objectMapper = new ObjectMapper();
        String descJson = objectMapper.writeValueAsString(request.getFields().getDescription());
        assertThat(descJson).contains("view.php?id=42");
    }

    private MantisIssue buildIssue(Long id, String summary, String description,
                                    String priorityName, String severityName) {
        try {
            var json = String.format("""
                {
                  "id": %d,
                  "summary": %s,
                  "description": %s,
                  "priority": %s,
                  "severity": %s
                }
                """,
                    id,
                    summary == null ? "null" : "\"" + summary + "\"",
                    description == null ? "null" : "\"" + description + "\"",
                    priorityName == null ? "null" : "{\"name\":\"" + priorityName + "\",\"label\":\"" + priorityName + "\"}",
                    severityName == null ? "null" : "{\"name\":\"" + severityName + "\",\"label\":\"" + severityName + "\"}"
            );
            return new ObjectMapper().readValue(json, MantisIssue.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
