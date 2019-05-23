package io.sengage.webservice.model;

import io.sengage.webservice.sengames.model.StrokeType;

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
	private List<Integer> pointA;
	private List<Integer> pointB;
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
			List<Integer> pointA,
			List<Integer> pointB,
			double width,
			String colorHex
			) {
		super(gameId, opaqueId, userId, userName, joinedAt, modifiedAt, playerStatus);
		this.strokeType = strokeType;
		this.pointA = pointA;
		this.pointB = pointB;
		this.width = width;
		this.colorHex = colorHex;
	}
}
