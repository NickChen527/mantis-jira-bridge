package com.nick.mantis_jira_bridge.mapping;

import com.nick.mantis_jira_bridge.config.JiraProperties;
import com.nick.mantis_jira_bridge.jira.AdfBuilder;
import com.nick.mantis_jira_bridge.jira.JiraCreateIssueRequest;
import com.nick.mantis_jira_bridge.mantis.dto.MantisIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MantisToJiraMapper {

    private static final Map<String, String> PRIORITY_MAP = Map.of(
            "urgent", "Highest",
            "high", "High",
            "normal", "Medium",
            "low", "Low",
            "none", "Lowest"
    );

    public JiraCreateIssueRequest toJiraRequest(MantisIssue issue, JiraProperties jiraProps) {
        var descriptionLines = buildDescriptionLines(issue, jiraProps);

        return JiraCreateIssueRequest.builder()
                .fields(JiraCreateIssueRequest.Fields.builder()
                        .project(Map.of("key", jiraProps.getProjectKey()))
                        .summary(buildSummary(issue))
                        .description(AdfBuilder.fromLines(descriptionLines))
                        .issuetype(Map.of("name", jiraProps.getDefaultIssueType()))
                        .priority(buildPriority(issue))
                        .build())
                .build();
    }

    private String buildSummary(MantisIssue issue) {
        String summary = issue.getSummary();
        if (summary == null || summary.isBlank()) {
            return "Mantis Issue #" + issue.getId();
        }
        return summary.length() > 250 ? summary.substring(0, 250) : summary;
    }

    private List<String> buildDescriptionLines(MantisIssue issue, JiraProperties jiraProps) {
        List<String> lines = new ArrayList<>();
        if (issue.getDescription() != null && !issue.getDescription().isBlank()) {
            lines.add(issue.getDescription());
            lines.add("");
        }
        lines.add("---");
        lines.add("Source: " + jiraProps.getBaseUrl().replaceAll("/api/.*", "")
                + "/view.php?id=" + issue.getId());
        if (issue.getReporter() != null && issue.getReporter().getName() != null) {
            lines.add("Reporter: " + issue.getReporter().getName());
        }
        if (issue.getSeverity() != null && issue.getSeverity().getLabel() != null) {
            lines.add("Severity: " + issue.getSeverity().getLabel());
        }
        return lines;
    }

    private Map<String, String> buildPriority(MantisIssue issue) {
        if (issue.getPriority() == null || issue.getPriority().getName() == null) {
            return Map.of("name", "Medium");
        }
        String jiraPriority = PRIORITY_MAP.getOrDefault(
                issue.getPriority().getName().toLowerCase(), "Medium");
        return Map.of("name", jiraPriority);
    }
}
