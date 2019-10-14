package io.sengage.webservice.balance;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.sengage.webservice.model.Platform;
import io.sengage.webservice.persistence.PaymentDataProvider;

@Singleton
public class TransactionProcessorFactory {
	
	private final PaymentDataProvider paymentDataProvider;
	
	@Inject
	public TransactionProcessorFactory(PaymentDataProvider paymentDataProvider) {
		this.paymentDataProvider = paymentDataProvider;
	}
	
	public TransactionProcessor get(Platform platform) {
		switch(platform) {
		case TWITCH:
			return new TwitchTransactionProcessor(paymentDataProvider);
		default:
			throw new IllegalArgumentException("Unsupported platform: " + platform);
		}
	}
}
