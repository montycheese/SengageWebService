package io.sengage.webservice.persistence.ddb;

import java.time.Instant;
import java.util.Optional;

import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.persistence.GameDataProvider;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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
	
	
}
