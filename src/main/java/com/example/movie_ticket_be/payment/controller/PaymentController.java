package com.example.movie_ticket_be.payment.controller;

import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.core.config.VNPayConfig;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.payment.dto.request.PaymentConfirmRequest;
import com.example.movie_ticket_be.payment.enums.PaymentType;
import com.example.movie_ticket_be.payment.service.PaymentService;
import com.example.movie_ticket_be.payment.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
	PaymentService paymentService;
	VNPayService vnPayService;
	OrderRepository orderRepository;

	@GetMapping("/create-vnpay-url")
	public ApiResponse<String> createVnPayUrl(HttpServletRequest request, @RequestParam Long orderId) {
		Orders order = orderRepository.findByOrderId(orderId)
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
		String paymentUrl = vnPayService.createPaymentUrl(request, order.getOrderId(), order.getFinalPrice());
		return ApiResponse.<String>builder().message("Tạo URL thanh toán thành công").result(paymentUrl).build();
	}

	@GetMapping("/vnpay-callback")
	public RedirectView vnpayCallback(HttpServletRequest request) {
		String status = request.getParameter("vnp_ResponseCode");
		String txnRef = request.getParameter("vnp_TxnRef");

		Map<String, String> fields = new HashMap<>();
		for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
			String fieldName = params.nextElement();
			String fieldValue = request.getParameter(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				fields.put(fieldName, fieldValue);
			}
		}
		if (fields.containsKey("vnp_SecureHashType")) fields.remove("vnp_SecureHashType");
		String vnp_SecureHash = request.getParameter("vnp_SecureHash");
		if (fields.containsKey("vnp_SecureHash")) fields.remove("vnp_SecureHash");

		String signValue = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashAllFields(fields));

		if (signValue.equals(vnp_SecureHash)) {
			if ("00".equals(status)) {
				PaymentConfirmRequest confirmReq = new PaymentConfirmRequest();
				confirmReq.setOrderId(Long.parseLong(txnRef));
				confirmReq.setTransactionId(request.getParameter("vnp_TransactionNo"));
				confirmReq.setPaymentInfo(request.getParameter("vnp_OrderInfo"));
				confirmReq.setPaymentType(PaymentType.VNPAY);
				paymentService.processSuccess(confirmReq);
				return new RedirectView("http://localhost:3000/payment-success/" + txnRef);
			} else {
				Orders order = orderRepository.findByOrderId(Long.parseLong(txnRef)).orElse(null);
				if (order != null) paymentService.processFail(order);
				return new RedirectView(
						"http://localhost:3000/payment-fail/" + txnRef + "?vnp_ResponseCode=" + status);
			}
		} else {
			return new RedirectView("http://localhost:3000/payment-error");
		}
	}

	private String hashAllFields(Map<String, String> fields) {
		List<String> fieldNames = new ArrayList<>(fields.keySet());
		Collections.sort(fieldNames);
		StringBuilder sb = new StringBuilder();
		Iterator<String> itr = fieldNames.iterator();
		while (itr.hasNext()) {
			String fieldName = itr.next();
			String fieldValue = fields.get(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				sb.append(fieldName).append("=");
				try {
					sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
				} catch (Exception e) {
					log.error("Error encoding field {}", fieldName, e);
				}
				if (itr.hasNext()) sb.append("&");
			}
		}
		return sb.toString();
	}
}
