package com.example.movie_ticket_be.payment.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.payment.config.MomoConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomoService {

    private final MomoConfig momoConfig;

    public String createPaymentUrl(Long orderId, BigDecimal amount) {
        String requestId = momoConfig.getPartnerCode() + System.currentTimeMillis();
        String orderIdStr = String.valueOf(System.currentTimeMillis()) + orderId;
        String orderInfo = "Thanh toán đơn hàng #" + orderId;
        String extraData = "";

        String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + amount.longValue()
                + "&extraData=" + extraData
                + "&ipnUrl=" + momoConfig.getIpnUrl()
                + "&orderId=" + orderIdStr
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + momoConfig.getPartnerCode()
                + "&redirectUrl=" + momoConfig.getRedirectUrl()
                + "&requestId=" + requestId
                + "&requestType=" + MomoConfig.REQUEST_TYPE;

        String signature = hmacSHA256(momoConfig.getSecretKey(), rawSignature);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", momoConfig.getPartnerCode());
        requestBody.put("requestType", MomoConfig.REQUEST_TYPE);
        requestBody.put("ipnUrl", momoConfig.getIpnUrl());
        requestBody.put("redirectUrl", momoConfig.getRedirectUrl());
        requestBody.put("orderId", orderIdStr);
        requestBody.put("amount", amount.longValue());
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("requestId", requestId);
        requestBody.put("extraData", extraData);
        requestBody.put("lang", "vi");
        requestBody.put("signature", signature);

        String endpoint = momoConfig.getEndpoint();
        if (endpoint == null) throw new IllegalStateException("MoMo endpoint is not configured");
        RestTemplate restTemplate = new RestTemplate();
        Map<?, ?> response = restTemplate.postForObject(endpoint, requestBody, Map.class);

        if (response != null && response.get("payUrl") != null) {
            return (String) response.get("payUrl");
        }
        log.error("MoMo API response: {}", response);
        throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    // Xác thực chữ ký từ callback/IPN của MoMo
    public boolean verifyCallback(Map<String, String> params) {
        String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + params.get("amount")
                + "&extraData=" + params.get("extraData")
                + "&message=" + params.get("message")
                + "&orderId=" + params.get("orderId")
                + "&orderInfo=" + params.get("orderInfo")
                + "&orderType=" + params.get("orderType")
                + "&partnerCode=" + params.get("partnerCode")
                + "&payType=" + params.get("payType")
                + "&requestId=" + params.get("requestId")
                + "&responseTime=" + params.get("responseTime")
                + "&resultCode=" + params.get("resultCode")
                + "&transId=" + params.get("transId");

        String expected = hmacSHA256(momoConfig.getSecretKey(), rawSignature);
        return expected.equals(params.get("signature"));
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result)
                sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("HMAC-SHA256 error", e);
            return "";
        }
    }
}
