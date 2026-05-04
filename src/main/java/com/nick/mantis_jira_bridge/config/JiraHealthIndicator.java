package com.nick.mantis_jira_bridge.config;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class JiraHealthIndicator implements HealthIndicator {

    private final JiraProperties jiraProperties;

    @Override
    public Health health() {
        try {
            var client = RestClient.builder()
                    .baseUrl(jiraProperties.getBaseUrl())
                    .build();
            client.get()
                    .uri("/rest/api/3/serverInfo")
                    .retrieve()
                    .toBodilessEntity();
            return Health.up().withDetail("jira", "reachable").build();
        } catch (Exception e) {
            // A 4xx (e.g. 401) still means Jira is reachable
            String msg = e.getMessage();
            if (msg != null && (msg.contains("401") || msg.contains("403"))) {
                return Health.up().withDetail("jira", "reachable").build();
            }
            log.warn("Jira health check failed: {}", msg);
            return Health.down().withDetail("error", msg).build();
        }
    }
}
