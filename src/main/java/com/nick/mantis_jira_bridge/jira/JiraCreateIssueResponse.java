package com.nick.mantis_jira_bridge.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraCreateIssueResponse {

    private String id;
    private String key;
    private String self;
}
