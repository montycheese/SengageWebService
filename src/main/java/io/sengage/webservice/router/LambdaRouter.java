package io.sengage.webservice.router;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.sengage.webservice.function.BaseLambda;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;

public class LambdaRouter {
	Map<String, Resource> routeMap = new LinkedHashMap<>();
	
	/**
	 * Register an activity with a method to the router.
	 * @param resource Class holding the activity, http method, and the path regex
	 */
	public LambdaRouter registerActivity(Resource resource) {
		routeMap.put(resource.getClassName(), resource);
		return this;
	}
	
	public Optional<BaseLambda<ServerlessInput, ServerlessOutput>> getMatchingActivity(
			String requestPath, String requestMethod) {
		// find first activity matching pattern.
		for (Map.Entry<String, Resource> entry: routeMap.entrySet()) {
			if(entry.getValue().matches(requestPath,  requestMethod)) {
				Class<?> clazz;
				try {
					clazz = Class.forName(entry.getKey());
					System.out.println("Before");
					@SuppressWarnings("unchecked")
					BaseLambda<ServerlessInput, ServerlessOutput> activity = 
							(BaseLambda<ServerlessInput, ServerlessOutput>) clazz.newInstance();
					System.out.println("After");
					return Optional.of(activity);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
					return Optional.empty();
				}

			}
		}
		return Optional.empty();
	}
}