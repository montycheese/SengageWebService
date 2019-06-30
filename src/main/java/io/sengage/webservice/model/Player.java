package io.sengage.webservice.model;

import io.sengage.webservice.persistence.converters.InstantConverter;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = Player.TABLE_NAME)
public class Player {
	public static final String TABLE_NAME = "Player";
	public static final String GAME_ID_ATTR_NAME = "GameId";
	public static final String OPAQUE_ID_ATTR_NAME = "OpaqueId";
	public static final String PLAYER_STATUS_ATTR_NAME = "PlayerStatus";
	public static final String GAME_ID_PLAYER_STATUS_INDEX = GAME_ID_ATTR_NAME + "-" + PLAYER_STATUS_ATTR_NAME + "-" + "Index";
	public static final String SCORE_ATTR_NAME = "Score"; // use this attribute for all games that are ranked
	public static final String GAME_ID_SCORE_INDEX = GAME_ID_ATTR_NAME + "-" + SCORE_ATTR_NAME + "-" + "Index";
	
	@DynamoDBHashKey(attributeName = GAME_ID_ATTR_NAME)
	private String gameId;
	@DynamoDBRangeKey(attributeName = OPAQUE_ID_ATTR_NAME)
	private String opaqueId;
	private String userId;
	private String userName;
	@DynamoDBTypeConverted(converter = InstantConverter.class)
	private Instant joinedAt;
	@DynamoDBTypeConverted(converter = InstantConverter.class)
	private Instant modifiedAt;
	@DynamoDBIndexRangeKey(attributeName = PLAYER_STATUS_ATTR_NAME, localSecondaryIndexName = GAME_ID_PLAYER_STATUS_INDEX)
	@DynamoDBTypeConvertedEnum
	private PlayerStatus playerStatus;
	@DynamoDBIndexRangeKey(attributeName = SCORE_ATTR_NAME, localSecondaryIndexName = GAME_ID_SCORE_INDEX)
	private long score;
}
