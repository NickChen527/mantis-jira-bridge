package com.nick.mantis_jira_bridge.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableRetry
@EnableScheduling
@EnableConfigurationProperties({JiraProperties.class, MantisProperties.class})
public class BridgeConfig {
}
