package io.sengage.webservice.persistence.ddb;

import java.time.Instant;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.MinigamesUserBalance;
import io.sengage.webservice.persistence.PaymentDataProvider;

@AllArgsConstructor
@NoArgsConstructor
public class DynamoDBPaymentDataProvider implements PaymentDataProvider {
	
	public DynamoDBMapper mapper;

	@Override
	public MinigamesUserBalance updateBalance(MinigamesUserBalance newBalance) throws ItemVersionMismatchException {
		newBalance.setModifiedAt(Instant.now());
		try {
			mapper.save(newBalance);	
		} catch (ConditionalCheckFailedException e) {
			throw new ItemVersionMismatchException(e);
		}
		return newBalance;
		
	}

	@Override
	public MinigamesUserBalance getBalance(String channelId, String userId) {
		return mapper.load(MinigamesUserBalance.class, channelId, userId);
	}

}
