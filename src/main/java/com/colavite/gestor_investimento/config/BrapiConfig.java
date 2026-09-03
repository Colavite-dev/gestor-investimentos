package com.colavite.gestor_investimento.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(BrapiProperties.class)
public class BrapiConfig {

    @Bean
    @Qualifier("brapiRestClient")
    public RestClient brapiRestClient(BrapiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory).defaultHeader("User-Agent", "gestor-investimento/1.0");
        if (StringUtils.hasText(properties.token())) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token().trim());
        }
        return builder.build();
    }
}
