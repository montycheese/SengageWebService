package io.sengage.webservice.model.flappybird;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.sengage.webservice.model.EndGameResult;
import io.sengage.webservice.model.GetFinalGameResultsResponse;

@EqualsAndHashCode(callSuper = false)
@Data
public class FlappyBirdFinalGameResults extends GetFinalGameResultsResponse {
	
	private double distanceCovered;
	private int totalPlayers;
	
	public FlappyBirdFinalGameResults(List<? extends EndGameResult> results,
			double distanceCovered,
			int totalPlayers) {
		super(results);
		this.distanceCovered = distanceCovered;
		this.totalPlayers = totalPlayers;
	}
}
