package io.sengage.webservice.sengames.model.singlestroke;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.sengage.webservice.model.GameSpecificParameters;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSingleStrokeGameParameters implements GameSpecificParameters {
	public static final String type = "SINGLE_STROKE";
	
	private String image; // todo enum
	
}
