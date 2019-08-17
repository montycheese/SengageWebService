package io.sengage.webservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelGameRequest {
	private String gameId;
	private String cancellationReason;
}
