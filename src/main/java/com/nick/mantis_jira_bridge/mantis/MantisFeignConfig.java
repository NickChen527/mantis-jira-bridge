package com.nick.mantis_jira_bridge.mantis;

import com.nick.mantis_jira_bridge.config.MantisProperties;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class MantisFeignConfig {

    @Bean
    public RequestInterceptor mantisTokenInterceptor(MantisProperties props) {
        return template -> template.header("Authorization", props.getApiToken());
    }
}
