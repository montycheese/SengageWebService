package io.sengage.webservice.model;

import io.sengage.webservice.persistence.converters.InstantConverter;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@DynamoDBTable(tableName = MinigamesUserBalance.TABLE_NAME)
public class MinigamesUserBalance {
	public static final String TABLE_NAME = "MinigamesUserBalance";
	public static final String CHANNEL_ID_ATTR = "ChannelId";
	public static final String USER_ID_ATTR = "UserId";
	
	@DynamoDBHashKey(attributeName = CHANNEL_ID_ATTR)
	private String channelId;
	@DynamoDBRangeKey(attributeName = USER_ID_ATTR)
	private String userId;
	private long balance;
	@DynamoDBTypeConverted(converter = InstantConverter.class)
	private Instant initializedAt;
	@DynamoDBTypeConverted(converter = InstantConverter.class)
	private Instant modifiedAt;
	@DynamoDBTypeConvertedEnum
	private Platform platform;
	@DynamoDBVersionAttribute
	private Long version;
}
