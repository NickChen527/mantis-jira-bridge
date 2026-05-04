package com.nick.mantis_jira_bridge.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "bridge.jira")
public class JiraProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String email;

    @NotBlank
    private String apiToken;

    @NotBlank
    private String projectKey;

    private String defaultIssueType = "Task";
}
