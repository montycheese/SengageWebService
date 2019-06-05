package io.sengage.webservice.model.singlestroke;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.sengage.webservice.model.EndGameResult;
import io.sengage.webservice.sengames.model.singlestroke.Stroke;

@EqualsAndHashCode(callSuper=false)
@Data
public class SingleStrokeEndGameResult extends EndGameResult {

	public static final String type = "SingleStrokeEndGameResult";
	private Stroke stroke;
	
	public SingleStrokeEndGameResult(Stroke stroke) {
		this.stroke = stroke;
	}

}
