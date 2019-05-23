package io.sengage.webservice.sengames.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleStrokeGameUpdateResponse implements HandleGameUpdateResponse {
	List<Stroke> strokes;
}
