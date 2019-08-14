package io.sengage.webservice.persistence.ddb;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.model.GameStatus;
import io.sengage.webservice.persistence.GameDataProvider;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;

public class DynamoDBGameDataProvider implements GameDataProvider {

	private final DynamoDBMapper mapper;
	public DynamoDBGameDataProvider(DynamoDBMapper mapper) {
		this.mapper = mapper;
	}
	@Override
	public String createGame(GameItem gameItem) {
		mapper.save(gameItem);
		return gameItem.getGameId();
	}
	@Override
	public void updateGame(GameItem gameItem) throws ItemVersionMismatchException {
		gameItem.setModifiedAt(Instant.now());
		try {
			mapper.save(gameItem);
		} catch (ConditionalCheckFailedException e) {
			throw new ItemVersionMismatchException(e);
		}
	}
	@Override
	public Optional<GameItem> getGame(String gameId) {
		return Optional.ofNullable(mapper.load(GameItem.class, gameId));
	}
	@Override
	public int getNumberOfGamesWithStatuses(String channelId, Set<GameStatus> statuses, Instant timeWindow) {
		AtomicInteger num = new AtomicInteger(0);

		String val = ":val1";
		String val2 = ":val2";
		String val3 = ":val3";
		String keyConditionalExpression = String.format("%s = %s and %s = %s", 
				GameItem.CHANNEL_ID_ATTR, val, GameItem.GAME_STATUS_ATTR, val2);
		String filterExpression = String.format("%s > %s", GameItem.CREATED_AT_ATTR, val3);
		
		statuses.parallelStream()
		.forEach(status -> {
			Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
			eav.put(val, new AttributeValue().withS(channelId));
			eav.put(val2, new AttributeValue().withS(status.name()));
			eav.put(val3, new AttributeValue().withS(timeWindow.toString()));
			
			DynamoDBQueryExpression<GameItem> query = new DynamoDBQueryExpression<GameItem>()
					.withIndexName(GameItem.CHANNEL_ID_GAME_STATUS_INDEX)
					.withKeyConditionExpression(keyConditionalExpression)
					.withExpressionAttributeValues(eav)
					// to avoid leaked games from preventing games from starting, ignore anything before TIME_WINDOW
					.withFilterExpression(filterExpression)
					.withConsistentRead(false);
			num.getAndAdd(mapper.count(GameItem.class, query));
		});
		
		return num.get();
	}
	
	
}
