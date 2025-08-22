package com.loopers.infrastructure.payment;

import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentClientConfig {

    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
            1000,  // connectTimeout (1초)
            3000   // readTimeout (3초)
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new PaymentClientErrorDecoder();
    }
}
