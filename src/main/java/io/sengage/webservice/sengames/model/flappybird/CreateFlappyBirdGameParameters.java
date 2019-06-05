package io.sengage.webservice.sengames.model.flappybird;

import io.sengage.webservice.model.GameSpecificParameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFlappyBirdGameParameters implements GameSpecificParameters {
	public final static String type = "FLAPPY_BIRD_BR";
	
	private int difficulty;
}
