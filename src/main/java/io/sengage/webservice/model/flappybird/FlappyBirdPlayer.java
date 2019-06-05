package io.sengage.webservice.model.flappybird;

import java.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;

import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.sengames.model.flappybird.GameCharacter;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@DynamoDBTable(tableName = Player.TABLE_NAME)
public class FlappyBirdPlayer extends Player {
	@DynamoDBTypeConvertedEnum
	private GameCharacter character;
	private long distance;
	private int attempt;
	
	public FlappyBirdPlayer(
			String gameId,
			String opaqueId,
			String userId,
			String userName,
			Instant joinedAt,
			Instant modifiedAt,
			PlayerStatus playerStatus,
			GameCharacter character,
			long distance,
			int attempt
			) {
		super(gameId, opaqueId, userId, userName, joinedAt, modifiedAt, playerStatus);
		this.character = character;
		this.distance = distance;
		this.attempt = attempt;
	}
}
