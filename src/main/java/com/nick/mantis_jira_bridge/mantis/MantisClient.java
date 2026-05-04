package com.nick.mantis_jira_bridge.mantis;

import com.nick.mantis_jira_bridge.mantis.dto.MantisIssueListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "mantis",
        url = "${bridge.mantis.base-url}",
        configuration = MantisFeignConfig.class
)
public interface MantisClient {

    @GetMapping("/api/rest/issues")
    MantisIssueListResponse getIssues(
            @RequestParam("page_size") int pageSize,
            @RequestParam("page") int page,
            @RequestParam(value = "filter_id", required = false) String filterId
    );
}
