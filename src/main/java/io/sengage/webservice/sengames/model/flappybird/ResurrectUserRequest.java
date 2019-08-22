package io.sengage.webservice.sengames.model.flappybird;

import io.sengage.webservice.model.GameSpecificState;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResurrectUserRequest implements GameSpecificState {
	public static final String typeName = "ResurrectUserRequest";
	public final String type = typeName; // Required for GSON to deserialze
}
