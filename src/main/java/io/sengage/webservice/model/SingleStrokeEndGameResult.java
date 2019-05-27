package io.sengage.webservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.sengage.webservice.sengames.model.Stroke;

@EqualsAndHashCode(callSuper=false)
@Data
public class SingleStrokeEndGameResult extends EndGameResult {

	private Stroke stroke;
	
	public SingleStrokeEndGameResult(Stroke stroke) {
		this.stroke = stroke;
	}

}
