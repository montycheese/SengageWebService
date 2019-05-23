package io.sengage.webservice.persistence.ddb;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import io.sengage.webservice.exception.ItemAlreadyExistsException;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.persistence.PlayerDataProvider;

@AllArgsConstructor
@Builder
public class DynamoDBPlayerDataProvider implements PlayerDataProvider {

	private final DynamoDBMapper mapper;
	
	@Override
	public Optional<Player> getPlayer(String gameId, String opaqueUserId) {
		return Optional.ofNullable(mapper.load(Player.class, gameId, opaqueUserId));
	}

	@Override
	public void createPlayer(Player player) throws ItemAlreadyExistsException {
		Instant now = Instant.now();
		player.setJoinedAt(now);
		player.setModifiedAt(now);
		try {
			mapper.save(player, saveIfNotExists(player.getGameId(), player.getOpaqueId()));
		} catch (ConditionalCheckFailedException e) {
			throw new ItemAlreadyExistsException(e);
		}
	}
	
	private DynamoDBSaveExpression saveIfNotExists(String gameId, String opaqueId) {
	    DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
	    Map<String, ExpectedAttributeValue> expected = new HashMap<>();
	    expected.put(Player.GAME_ID_ATTR_NAME, new ExpectedAttributeValue().withExists(false));
	    expected.put(Player.OPAQUE_ID_ATTR_NAME, new ExpectedAttributeValue().withExists(false));

	    saveExpression.setExpected(expected);
	    
	    return saveExpression;
	}

}
