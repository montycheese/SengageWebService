package io.sengage.webservice.model.flappybird;

import java.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@DynamoDBTable(tableName = Player.TABLE_NAME)
public class FlappyBirdPlayer extends Player {
	private String character;
	private int attempt;
	
	public FlappyBirdPlayer(
			String gameId,
			String opaqueId,
			String userId,
			String userName,
			Instant joinedAt,
			Instant modifiedAt,
			PlayerStatus playerStatus,
			String character,
			long distance,
			int attempt
			) {
		super(gameId, opaqueId, userId, userName, joinedAt, modifiedAt, playerStatus, distance);
		this.character = character;
		this.attempt = attempt;
	}
	
	
	@DynamoDBIgnore
	public long getDistance() {
		return getScore();
	}
}
