package io.sengage.webservice.model;

import java.time.Instant;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@DynamoDBTable(tableName = GameItem.TABLE_NAME)
public class GameItem {
	public static final String TABLE_NAME = "Game";
	public static final String GAME_ID_ATTR = "GameId";
	
	@DynamoDBHashKey(attributeName = GAME_ID_ATTR)
	private String gameId;
	private String channelId;
	private String streamerUserId;
	private String streamerUserName;
	private String streamerOpaqueId;
	private Game game;
	private GameStatus gameStatus;
	private Instant createdAt;
	private Instant modifiedAt;
	private int duration;
	
	public static GameItem from(Game game, int duration, StreamInfo streamInfo) {
		Instant now = Instant.now();
		
		return GameItem.builder()
		.gameId(UUID.randomUUID().toString())
		.game(game)
		.duration(duration)
		.gameStatus(GameStatus.INIT)
		.streamerOpaqueId(streamInfo.getStreamerOpaqueId())
		.channelId(streamInfo.getChannelId())
		.streamerUserId(streamInfo.getStreamerUserId())
		.createdAt(now)
		.modifiedAt(now)
		.build();
	}
}
