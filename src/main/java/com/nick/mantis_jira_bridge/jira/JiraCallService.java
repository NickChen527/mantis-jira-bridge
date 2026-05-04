package com.nick.mantis_jira_bridge.jira;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/** Thin wrapper around JiraCloudClient that adds retry behavior. */
@Service
@RequiredArgsConstructor
public class JiraCallService {

    private final JiraCloudClient jiraClient;

    @Retryable(
            retryFor = {feign.FeignException.FeignServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public JiraCreateIssueResponse createIssue(JiraCreateIssueRequest request) {
        return jiraClient.createIssue(request);
    }
}
