package io.sengage.webservice.model.singlestroke;

import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.sengames.model.singlestroke.StrokeType;

import java.time.Instant;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@DynamoDBTable(tableName = Player.TABLE_NAME)
public class SingleStrokePlayer extends Player {
	@DynamoDBTypeConvertedEnum
	private StrokeType strokeType;
	private List<Double> pointA;
	private List<Double> pointB;
	private double width;
	private String colorHex;
	
	public SingleStrokePlayer(
			String gameId,
			String opaqueId,
			String userId,
			String userName,
			Instant joinedAt,
			Instant modifiedAt,
			PlayerStatus playerStatus,
			StrokeType strokeType,
			List<Double> pointA,
			List<Double> pointB,
			double width,
			String colorHex
			) {
		super(gameId, opaqueId, userId, userName, joinedAt, modifiedAt, playerStatus, 0);
		this.strokeType = strokeType;
		this.pointA = pointA;
		this.pointB = pointB;
		this.width = width;
		this.colorHex = colorHex;
	}
}
