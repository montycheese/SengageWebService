package io.sengage.webservice.model;

import io.sengage.webservice.persistence.converters.InstantConverter;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = Player.TABLE_NAME)
public class Player {
	public static final String TABLE_NAME = "Player";
	public static final String GAME_ID_ATTR_NAME = "GameId";
	public static final String OPAQUE_ID_ATTR_NAME = "OpaqueId";
	
	@DynamoDBHashKey(attributeName = GAME_ID_ATTR_NAME)
	private String gameId;
	@DynamoDBHashKey(attributeName = OPAQUE_ID_ATTR_NAME)
	private String opaqueId;
	private String userId;
	private String userName;
	@DynamoDBTypeConverted(converter = InstantConverter.class)
	private Instant joinedAt;
	@DynamoDBTypeConverted(converter = InstantConverter.class)
	private Instant modifiedAt;
	
	
}
