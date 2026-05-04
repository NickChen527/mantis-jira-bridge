package com.nick.mantis_jira_bridge.jira;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "jira-cloud",
        url = "${bridge.jira.base-url}",
        configuration = JiraFeignConfig.class
)
public interface JiraCloudClient {

    @PostMapping(value = "/rest/api/3/issue", consumes = "application/json")
    JiraCreateIssueResponse createIssue(@RequestBody JiraCreateIssueRequest request);
}
