package com.nick.mantis_jira_bridge.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nick.mantis_jira_bridge.mantis.dto.MantisIssue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MantisWebhookPayload {

    private String action;

    @JsonProperty("issue")
    @NotNull
    private MantisIssue issue;
}
