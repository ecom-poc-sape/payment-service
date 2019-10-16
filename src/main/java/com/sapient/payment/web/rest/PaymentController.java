package com.sapient.payment.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sapient.ecomm_commons.domain.Payment;
import com.sapient.ecomm_commons.domain.PaymentDetails;
import com.sapient.ecomm_commons.domain.PaymentStatus;
import com.sapient.payment.events.PaymentEvent;
import com.sapient.payment.service.PaymentProcessor;

@RestController("/payment")
public class PaymentController {

	@Autowired
	private PaymentProcessor paymentProcessor;

	@Autowired
	private PaymentEvent paymentEvent;

	@PostMapping("/{orderId}")
	public ResponseEntity<String> payment(@PathVariable String orderId, @RequestBody PaymentDetails paymentDetails) {

		Payment payment = paymentProcessor.getPaymentByOrderId(orderId);
		payment.setStatus(PaymentStatus.COMPLETED);
		payment.setDetails(paymentDetails);
		paymentEvent.paymentMessageChannel().send(MessageBuilder.withPayload(payment).build());
		return ResponseEntity.accepted().body("Success");

	}

}
