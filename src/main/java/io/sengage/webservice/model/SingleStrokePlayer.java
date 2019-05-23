package io.sengage.webservice.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = Player.TABLE_NAME)
public class SingleStrokePlayer extends Player {
	private SingleStrokeStrokeType strokeType;
	private int[] pointA;
	private int[] pointB;
	private double width;
	private Float radius; // optional
	private String colorHex;
}
