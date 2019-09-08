package io.sengage.webservice.router;


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import io.sengage.webservice.function.BaseLambda;
import io.sengage.webservice.model.ServerlessInput;
import io.sengage.webservice.model.ServerlessOutput;

@Log4j2
public class LambdaRouter {
	Set<Resource> routes = new HashSet<>();
	
	/**
	 * Register an activity with a method to the router.
	 * @param resource Class holding the activity, http method, and the path regex
	 */
	public LambdaRouter registerActivity(Resource resource) {
		routes.add(resource);
		return this;
	}
	
	public Optional<BaseLambda<ServerlessInput, ServerlessOutput>> getMatchingActivity(
			String requestPath, String requestMethod) {
		// find first activity matching pattern.
		for (Resource resource: routes) {
			if(resource.matches(requestPath,  requestMethod)) {
				Class<?> clazz;
				try {
					clazz = Class.forName(resource.getClassName());
					@SuppressWarnings("unchecked")
					BaseLambda<ServerlessInput, ServerlessOutput> activity = 
							(BaseLambda<ServerlessInput, ServerlessOutput>) clazz.newInstance();
					return Optional.of(activity);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					log.warn("Caught exception while trying to find matching resource with path {} and method {}", 
							requestPath, requestMethod, e);
					return Optional.empty();
				}

			}
		}
		return Optional.empty();
	}
}