package io.sengage.webservice.model.singlestroke;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.sengage.webservice.model.EndGameResult;
import io.sengage.webservice.model.GetFinalGameResultsResponse;

@EqualsAndHashCode(callSuper = false)
@Data
public class SingleStrokeFinalGameResults extends GetFinalGameResultsResponse {

	private double distanceCovered;
	private int totalPlayers;
	
	public SingleStrokeFinalGameResults(List<? extends EndGameResult> results,
			double distanceCovered,
			int totalPlayers) {
		super(results);
		this.distanceCovered = distanceCovered;
		this.totalPlayers = totalPlayers;
	}

}
