package com.nick.mantis_jira_bridge.jira;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JiraCreateIssueRequest {

    private Fields fields;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Fields {
        private Map<String, String> project;
        private String summary;
        private Map<String, Object> description;
        private Map<String, String> issuetype;
        private Map<String, String> priority;
    }
}
