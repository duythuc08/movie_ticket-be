package com.example.movie_ticket_be.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class MomoConfig {

    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;

    @Value("${momo.access-key:F8BBA842ECF85}")
    private String accessKey;

    @Value("${momo.secret-key:K951B6PE1waDMi640xX08PD3vg6EkVlz}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;

    @Value("${momo.redirect-url:http://localhost:3000/payment/result}")
    private String redirectUrl;

    @Value("${momo.ipn-url:http://localhost:8080/api/payment/momo/ipn}")
    private String ipnUrl;

    public static final String REQUEST_TYPE = "payWithMethod";
}