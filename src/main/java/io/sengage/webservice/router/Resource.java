package io.sengage.webservice.router;

import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class Resource {

	private String className;
	private String httpMethod;
	private Pattern pattern;
	
	public boolean matches(String httpPath, String method) {
		return pattern.matcher(httpPath).find() && method.equalsIgnoreCase(this.httpMethod);
	}

}