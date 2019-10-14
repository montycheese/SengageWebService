package io.sengage.webservice.model;

import lombok.Data;

@Data
public class UpdateUserBalanceRequest {
	long amount;
	StreamingServicePaymentMetadata streamingServicePaymentMetadata;
	Platform platform;
	long version;
	String userName;
}
