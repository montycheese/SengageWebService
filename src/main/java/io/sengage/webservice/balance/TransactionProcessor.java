package io.sengage.webservice.balance;

import java.util.Optional;

import io.sengage.webservice.exception.FundsLimitExceededException;
import io.sengage.webservice.exception.InsufficientFundsException;
import io.sengage.webservice.model.MinigamesUserBalance;
import io.sengage.webservice.model.StreamingServicePaymentMetadata;

public interface TransactionProcessor {
	
	/**
	 * Fetches the balance for a user in a given channel.
	 * @param channelId The channel Id 
	 * @param userId The user's unique immutable ID.
	 * @return The MinigamesUserBalance object if the user has funds, otherwise an empty optional
	 */
	Optional<MinigamesUserBalance> getUserBalance(String channelId, String userId);
	
	MinigamesUserBalance handleBalanceUpdate(String channelId, String userId, long delta, long prevVersionId)
			throws InsufficientFundsException, FundsLimitExceededException;
	
	MinigamesUserBalance handleBalanceUpdate(String channelId, String userId, long delta, StreamingServicePaymentMetadata metadata)
			throws InsufficientFundsException, FundsLimitExceededException;
}
