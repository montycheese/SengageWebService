package io.sengage.webservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class StreamInfo {
	private String channelId;
	private String streamerUserId;
	private String streamerUserName;
	private String streamerOpaqueId;
}
