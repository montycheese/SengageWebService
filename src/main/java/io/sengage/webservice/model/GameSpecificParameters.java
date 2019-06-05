package io.sengage.webservice.model;

import io.sengage.webservice.sengames.model.flappybird.CreateFlappyBirdGameParameters;
import io.sengage.webservice.sengames.model.singlestroke.CreateSingleStrokeGameParameters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type")
	@JsonSubTypes({
	    @Type(value = CreateSingleStrokeGameParameters.class, name = "SINGLE_STROKE"),
	    @Type(value = CreateFlappyBirdGameParameters.class, name = "FLAPPY_BIRD_BR") })

public interface GameSpecificParameters {

}
