package io.sengage.webservice.balance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.sengage.webservice.model.StreamingServicePaymentMetadata;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TwitchPaymentMetadata implements StreamingServicePaymentMetadata {
	public static final String typeName = "TwitchPaymentMetadata";
	public final String type = typeName; // Required for GSON to deserialze
	private String transactionId;
	private Product product;
	private String userId;
	private String displayName;
	private String initiator;
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Product {
		private String sku;
		private String displayName;
		private Cost cost;
		
		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		public static class Cost {
			private String amount;
			private String type;
		}
	}
}
