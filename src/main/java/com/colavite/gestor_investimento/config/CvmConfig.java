package com.colavite.gestor_investimento.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(CvmProperties.class)
public class CvmConfig {

    @Bean
    @Qualifier("cvmRestClient")
    public RestClient cvmRestClient(CvmProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "gestor-investimento/1.0")
                .build();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
