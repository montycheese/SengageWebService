package io.sengage.webservice.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GetFinalGameResultsResponse {
	List<? extends EndGameResult> results;
}
