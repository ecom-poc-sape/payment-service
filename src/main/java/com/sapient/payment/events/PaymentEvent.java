package com.sapient.payment.events;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface PaymentEvent {
	
	@Output("payment")
	MessageChannel paymentMessageChannel();

}
