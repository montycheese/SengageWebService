package io.sengage.webservice.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LambdaAuthorizerOutput {
	 private String principalId;
	 private PolicyDocument policyDocument;
	 private Context context;
	 private String usageIdentifierKey;
	 
	 @Builder
	 @Data
	 @AllArgsConstructor
	 @NoArgsConstructor
	 public static class Context {
		 private String stringKey;
		 private String numberKey;
		 private String booleanKey;
	 }
	 
	 @Builder
	 @Data
	 @AllArgsConstructor
	 @NoArgsConstructor
	 public static class PolicyDocument {
		 private String Version;
		 List<Map<String, Object>> Statement;
		
	 }
}
