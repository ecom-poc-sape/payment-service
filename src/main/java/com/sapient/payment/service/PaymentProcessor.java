package com.sapient.payment.service;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapient.ecomm_commons.domain.Payment;
import com.sapient.ecomm_commons.domain.PaymentStatus;
import com.sapient.payment.domain.Order;


@EnableBinding(KafkaStreamsProcessor.class)
public class PaymentProcessor {
	
	Logger log = LoggerFactory.getLogger(this.getClass());

	public static final String INPUT_TOPIC = "input";
	public static final String OUTPUT_TOPIC = "output";
	static ObjectMapper mapper = new ObjectMapper();

	private static final String STORE_NAME = "payment-store";

	@Autowired
	private InteractiveQueryService queryService;

	ReadOnlyKeyValueStore<Object, Object> keyValueStore;

	@StreamListener(INPUT_TOPIC)
	@SendTo(OUTPUT_TOPIC)
	public KStream<String, String> process(KStream<String, String> input) {

		return input.map((key, value) -> {
			Order order = getObjectFromString(value, Order.class);
			return new KeyValue<>(order.getId(),
					getJsonStringFromObject(new Payment(order.getId(), order.getCustomerId(), PaymentStatus.INITIATED)));
		}).groupByKey(Serialized.with(Serdes.String(), Serdes.String()))
				.reduce((previousValue, newValue) -> newValue,
						Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(STORE_NAME)
								.withKeySerde(Serdes.String()).withValueSerde(Serdes.String()))
				.toStream();

	}

	public <T> T getObjectFromString(String input, Class<T> resultClass) {
		try {
			return mapper.readValue(input, resultClass);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public <T> String getJsonStringFromObject(T obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Payment getPaymentByOrderId(String orderId) {
		if (keyValueStore == null) {
			keyValueStore = queryService.getQueryableStore(STORE_NAME, QueryableStoreTypes.keyValueStore());
		}

		return getObjectFromString((String) keyValueStore.get(orderId), Payment.class);
	}

}
