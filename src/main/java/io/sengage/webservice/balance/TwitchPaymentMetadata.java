package io.sengage.webservice.balance;

import io.sengage.webservice.model.StreamingServicePaymentMetadata;

public class TwitchPaymentMetadata implements StreamingServicePaymentMetadata {
	public static final String typeName = "TwitchPaymentMetadata";
	public final String type = typeName; // Required for GSON to deserialze
	// TX Receiept.
}
