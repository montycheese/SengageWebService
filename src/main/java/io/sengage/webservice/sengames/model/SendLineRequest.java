package io.sengage.webservice.sengames.model;

import java.util.List;

import lombok.Data;

@Data
public class SendLineRequest implements SingleStrokeGameSpecificState {
	
	private List<Integer> pointA;
	private List<Integer> pointB;
	private String username;
	private String colorHex;
	private double width;
	private double radius;
	private StrokeType strokeType;
	private final String type = "SendLineRequest"; // Required for GSON to deserialze
}
