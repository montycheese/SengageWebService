package io.sengage.webservice.sengames.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Stroke {
	private List<Integer> pointA;
	private List<Integer> pointB;
	private String colorHex;
	private double width;
	private StrokeType strokeType;
}
