package io.sengage.webservice.persistence.ddb;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import io.sengage.webservice.exception.ItemAlreadyExistsException;
import io.sengage.webservice.exception.ItemNotFoundException;
import io.sengage.webservice.model.Player;
import io.sengage.webservice.model.PlayerStatus;
import io.sengage.webservice.model.SingleStrokePlayer;
import io.sengage.webservice.persistence.PlayerDataProvider;

@AllArgsConstructor
@Builder
public class DynamoDBPlayerDataProvider implements PlayerDataProvider {

	private final DynamoDBMapper mapper;
	
	@Override
	public Player getPlayer(String gameId, String opaqueUserId) throws ItemNotFoundException {
		Player player =  mapper.load(Player.class, gameId, opaqueUserId);
		if (player == null) {
			throw new ItemNotFoundException("Could not find player: " + opaqueUserId + " in game: " + gameId);
		}
		return player;
	}

	@Override
	public void createPlayer(Player player) throws ItemAlreadyExistsException {
		Instant now = Instant.now();
		player.setJoinedAt(now);
		player.setModifiedAt(now);
		try {
			mapper.save(player, saveIfNotExists());
		} catch (ConditionalCheckFailedException e) {
			throw new ItemAlreadyExistsException(e);
		}
	}
	
	private DynamoDBSaveExpression saveIfNotExists() {
	    DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
	    Map<String, ExpectedAttributeValue> expected = new HashMap<>();
	    expected.put(Player.GAME_ID_ATTR_NAME, new ExpectedAttributeValue().withExists(false));
	    expected.put(Player.OPAQUE_ID_ATTR_NAME, new ExpectedAttributeValue().withExists(false));

	    saveExpression.setExpected(expected);
	    
	    return saveExpression;
	}
	
	private DynamoDBSaveExpression saveIfExists(String gameId, String opaqueId) {
	    DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
	    Map<String, ExpectedAttributeValue> expected = new HashMap<>();
	    expected.put(Player.GAME_ID_ATTR_NAME, new ExpectedAttributeValue(
	    		new AttributeValue(gameId)).withExists(true));
	    expected.put(Player.OPAQUE_ID_ATTR_NAME, new ExpectedAttributeValue(
	    		new AttributeValue(opaqueId))
	    .withExists(true));

	    saveExpression.setExpected(expected);
	    
	    return saveExpression;
	}

	@Override
	public void updateGamePlayer(Player player) throws ItemNotFoundException {
		player.setModifiedAt(Instant.now());
		try {
			mapper.save(player, saveIfExists(player.getGameId(), player.getOpaqueId()));
		} catch (ConditionalCheckFailedException e) {
			throw new ItemNotFoundException(e);
		}
		
		
	}

	@Override
	public List<? extends Player> listPlayers(String gameId, PlayerStatus status, Class<? extends Player> clazz) {
		// TODO Auto-generated method stub
		
		String val = ":val1";
		String val2 = ":val2";
		String keyConditionalExpression = String.format("%s = %s and %s = %s", 
				Player.GAME_ID_ATTR_NAME, val, Player.PLAYER_STATUS_ATTR_NAME, val2);
		Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
		eav.put(val, new AttributeValue().withS(gameId));
		eav.put(val2, new AttributeValue().withS(status.name()));
		
		return listPlayers(keyConditionalExpression, eav, clazz);
	}
	
	private List<? extends Player> listPlayers(String keyConditionalExpression,
			Map<String, AttributeValue> eav,
			Class<? extends Player> clazz) {
		if (clazz.equals(SingleStrokePlayer.class)) {
			DynamoDBQueryExpression<SingleStrokePlayer> q = new DynamoDBQueryExpression<SingleStrokePlayer>()				
					.withIndexName(Player.GAME_ID_PLAYER_STATUS_INDEX)
					.withKeyConditionExpression(keyConditionalExpression)
					.withExpressionAttributeValues(eav);
			return mapper.query(SingleStrokePlayer.class, q);
		}
		
		return null;
		
	}

	@Override
	public int getNumberOfPlayersInGame(String gameId, PlayerStatus status) {
		// todo support provindg null status
		String val = ":val1";
		String val2 = ":val2";
		String keyConditionalExpression = String.format("%s = %s and %s = %s", 
				Player.GAME_ID_ATTR_NAME, val, Player.PLAYER_STATUS_ATTR_NAME, val2);
		Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
		eav.put(val, new AttributeValue().withS(gameId));
		eav.put(val2, new AttributeValue().withS(status.name()));
		DynamoDBQueryExpression<Player> q = new DynamoDBQueryExpression<Player>()				
				.withIndexName(Player.GAME_ID_PLAYER_STATUS_INDEX)
				.withKeyConditionExpression(keyConditionalExpression)
				.withExpressionAttributeValues(eav);
		return mapper.count(Player.class, q);
	}
}
