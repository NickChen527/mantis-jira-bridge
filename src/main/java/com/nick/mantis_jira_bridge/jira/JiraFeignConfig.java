package com.nick.mantis_jira_bridge.jira;

import com.nick.mantis_jira_bridge.config.JiraProperties;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;

public class JiraFeignConfig {

    @Bean
    public RequestInterceptor jiraBasicAuthInterceptor(JiraProperties props) {
        return new BasicAuthRequestInterceptor(props.getEmail(), props.getApiToken());
    }
}
