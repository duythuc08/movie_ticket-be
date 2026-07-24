package com.example.movie_ticket_be.payment.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.payment.config.VnpayConfig;
import com.example.movie_ticket_be.payment.dto.request.PaymentConfirmRequest;
import com.example.movie_ticket_be.payment.enums.PaymentType;
import com.example.movie_ticket_be.payment.service.MomoService;
import com.example.movie_ticket_be.payment.service.PaymentService;
import com.example.movie_ticket_be.payment.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

	PaymentService paymentService;
	VNPayService vnPayService;
	VnpayConfig vnpayConfig;
	MomoService momoService;
	OrderRepository orderRepository;

	// ===================== VNPAY =====================

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
		String rawOrderInfo = request.getParameter("vnp_OrderInfo");
		String txnRef = rawOrderInfo != null && rawOrderInfo.contains("#") ? rawOrderInfo.split("#")[1] : null;

		Map<String, String> fields = new HashMap<>();
		for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
			String fieldName = params.nextElement();
			String fieldValue = request.getParameter(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				fields.put(fieldName, fieldValue);
			}
		}
		fields.remove("vnp_SecureHashType");
		String vnp_SecureHash = request.getParameter("vnp_SecureHash");
		fields.remove("vnp_SecureHash");

		String signValue = VnpayConfig.hmacSHA512(vnpayConfig.getSecretKey(), hashAllFields(fields));

		if (signValue.equals(vnp_SecureHash)) {
			if ("00".equals(status)) {
				PaymentConfirmRequest confirmReq = new PaymentConfirmRequest();
				confirmReq.setOrderId(Long.valueOf(txnRef));
				confirmReq.setTransactionId(request.getParameter("vnp_TransactionNo"));
				confirmReq.setPaymentInfo(request.getParameter("vnp_OrderInfo"));
				confirmReq.setPaymentType(PaymentType.VNPAY);
				paymentService.processSuccess(confirmReq);
				return new RedirectView("http://localhost:3000/payment-success/" + txnRef);
			} else {
				Orders order = orderRepository.findByOrderId(Long.valueOf(txnRef)).orElse(null);
				if (order != null) paymentService.processFail(order);
				return new RedirectView(
						"http://localhost:3000/payment-fail/" + txnRef + "?vnp_ResponseCode=" + status);
			}
		} else {
			return new RedirectView("http://localhost:3000/payment-error");
		}
	}

	// ===================== MOMO =====================

	@GetMapping("/create-momo-url")
	public ApiResponse<String> createMomoUrl(@RequestParam Long orderId) {
		Orders order = orderRepository.findByOrderId(orderId)
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
		String paymentUrl = momoService.createPaymentUrl(order.getOrderId(), order.getFinalPrice());
		return ApiResponse.<String>builder().message("Tạo URL thanh toán MoMo thành công").result(paymentUrl).build();
	}

	// MoMo redirect user về sau khi thanh toán (giống vnpay-callback)
	@GetMapping("/momo/callback")
	public RedirectView momoCallback(HttpServletRequest request) {
		Map<String, String> params = new HashMap<>();
		for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements();) {
			String name = names.nextElement();
			params.put(name, request.getParameter(name));
		}

		if (!momoService.verifyCallback(params)) {
			return new RedirectView("http://localhost:3000/payment-error");
		}

		String rawOrderInfo = params.get("orderInfo");
		String orderId = rawOrderInfo != null && rawOrderInfo.contains("#") ? rawOrderInfo.split("#")[1] : null;
		String resultCode = params.get("resultCode");

		if ("0".equals(resultCode)) {
			PaymentConfirmRequest confirmReq = new PaymentConfirmRequest();
			confirmReq.setOrderId(Long.valueOf(orderId));
			confirmReq.setTransactionId(params.get("transId"));
			confirmReq.setPaymentInfo(params.get("orderInfo"));
			confirmReq.setPaymentType(PaymentType.MOMO);
			paymentService.processSuccess(confirmReq);
			return new RedirectView("http://localhost:3000/payment-success/" + orderId);
		} else {
			Orders order = orderRepository.findByOrderId(Long.valueOf(orderId)).orElse(null);
			if (order != null) paymentService.processFail(order);
			return new RedirectView(
					"http://localhost:3000/payment-fail/" + orderId + "?resultCode=" + resultCode);
		}
	}

	// MoMo IPN — server-to-server, xử lý dự phòng nếu user đóng tab trước khi redirect
	@PostMapping("/momo/ipn")
	public ResponseEntity<Map<String, Object>> momoIpn(@RequestBody Map<String, String> params) {
		if (!momoService.verifyCallback(params)) {
			return ResponseEntity.ok(Map.of("resultCode", 1, "message", "Invalid signature"));
		}

		String rawOrderInfo = params.get("orderInfo");
		String orderId = rawOrderInfo != null && rawOrderInfo.contains("#") ? rawOrderInfo.split("#")[1] : null;
		String resultCode = params.get("resultCode");

		if ("0".equals(resultCode)) {
			PaymentConfirmRequest confirmReq = new PaymentConfirmRequest();
			confirmReq.setOrderId(Long.valueOf(orderId));
			confirmReq.setTransactionId(params.get("transId"));
			confirmReq.setPaymentInfo(params.get("orderInfo"));
			confirmReq.setPaymentType(PaymentType.MOMO);
			paymentService.processSuccess(confirmReq);
		} else {
			Orders order = orderRepository.findByOrderId(Long.valueOf(orderId)).orElse(null);
			if (order != null) paymentService.processFail(order);
		}

		return ResponseEntity.ok(Map.of("resultCode", 0, "message", "OK"));
	}

	@PostMapping("/retry")
	public ApiResponse<String> retryPayment(HttpServletRequest request, @RequestParam Long orderId, @RequestParam PaymentType method) {
		String payUrl = "";
		if (method == PaymentType.VNPAY) {
			Orders order = paymentService.prepareOrderForRetry(orderId, PaymentType.VNPAY);
			payUrl = vnPayService.createPaymentUrl(request, order.getOrderId(), order.getFinalPrice());
		} else if (method == PaymentType.MOMO) {
			payUrl = paymentService.retryMomoPayment(orderId);
		} else {
			throw new AppException(ErrorCode.INVALID_KEY);
		}
		return ApiResponse.<String>builder().result(payUrl).build();
	}

	@PostMapping("/momo/retry")
	public ApiResponse<String> retryMomoPayment(@RequestParam Long orderId) {
		String payUrl = paymentService.retryMomoPayment(orderId);
		return ApiResponse.<String>builder().result(payUrl).build();
	}

	// ===================== HELPER =====================

	private String hashAllFields(Map<String, String> fields) {
		List<String> fieldNames = new ArrayList<>(fields.keySet());
		Collections.sort(fieldNames);
		StringBuilder sb = new StringBuilder();
		Iterator<String> itr = fieldNames.iterator();
		while (itr.hasNext()) {
			String fieldName = itr.next();
			String fieldValue = fields.get(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				sb.append(fieldName).append("=")
				  .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
				if (itr.hasNext()) sb.append("&");
			}
		}
		return sb.toString();
	}
}
