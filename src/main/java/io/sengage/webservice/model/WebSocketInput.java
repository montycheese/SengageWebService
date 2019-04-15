package io.sengage.webservice.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketInput {
	private RequestContext requestContext;
	private String body;
	private boolean isBase64Encoded;
	Map<String, String> headers;
	Map<String, List<String>> multiValueHeaders;
	Map<String, String> queryStringParameters;
	Map<String, List<String>> multiValueQueryStringParameters;
	 
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RequestContext {
		private String routeKey;
		private Authorizer authorizer;
		private String messageId;
		private String integrationLatency;
	    private String eventType;
		private String error;
	    private String extendedRequestId;
		private String requestTime;
		private String messageDirection;
		private String stage;
		private float connectedAt;
		private float requestTimeEpoch;
		private Identity identity;
		private String requestId;
		private String domainName;
		private String connectionId;
		private String apiId;
		private String status;
		
		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		public static class Authorizer {
			 private String principalId;
			 private float integrationLatency;
		}
		 
		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		public static class Identity {
			private String cognitoIdentityPoolId = null;
			private String accountId = null;
			private String cognitoIdentityId = null;
			private String caller = null;
			private String sourceIp;
			private String accessKey = null;
			private String cognitoAuthenticationType = null;
			private String cognitoAuthenticationProvider = null;
			private String userArn = null;
			private String userAgent = null;
			private String user = null;
		}
	}
}
