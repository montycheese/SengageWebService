package io.sengage.webservice.sengames.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.sengage.webservice.model.GameSpecificParameters;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSingleStrokeGameParameters implements GameSpecificParameters {
	public String type = "SINGLE_STROKE";
}
