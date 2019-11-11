package io.sengage.webservice.balance;

import java.time.Instant;

import io.sengage.webservice.model.Platform;
import io.sengage.webservice.persistence.converters.InstantConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = TransactionReceipt.TABLE_NAME)
public class TransactionReceipt {
	public static final String TX_ID_ATTR = "TransactionId";
	public static final String USER_ID_ATTR = "UserId"; 
	public static final String TABLE_NAME = "MinigamesTransactionReceipt";
	
	@DynamoDBHashKey(attributeName = USER_ID_ATTR)
	private String userId;
	@DynamoDBRangeKey(attributeName = TX_ID_ATTR)
	private String transactionId;
	private String channelId;
	private long cost;
	@DynamoDBTypeConvertedEnum
	private Currency currency;
	@DynamoDBTypeConvertedEnum
	private Platform platform;
	private String sku;
	@DynamoDBTypeConverted(converter = InstantConverter.class)
	private Instant createdAt;
	
}
