package com.sapient.payment.events;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;

import com.sapient.ecomm_commons.domain.Payment;

@EnableBinding(PaymentEvent.class)
public class PaymentEventPublisher {

	@Bean
	public MessageSource<Payment> orderMessage() {
		return () -> MessageBuilder.withPayload(new Payment()).build();
	}

}
