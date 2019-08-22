package io.sengage.webservice.sengames.model.singlestroke;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendLineRequest implements SingleStrokeGameSpecificState {
	public static final String typeName = "SendLineRequest";
	public final String type = typeName; // Required for GSON to deserialze
	private Stroke stroke;
}
