package io.sengage.webservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class StreamContext {
	private String channelId;
	private String userId;
	private String userName;
	private String opaqueId;
}
