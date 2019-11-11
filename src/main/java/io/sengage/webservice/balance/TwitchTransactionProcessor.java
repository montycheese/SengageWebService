package io.sengage.webservice.balance;

import java.time.Instant;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import io.sengage.webservice.exception.FundsLimitExceededException;
import io.sengage.webservice.exception.InsufficientFundsException;
import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.MinigamesUserBalance;
import io.sengage.webservice.model.Platform;
import io.sengage.webservice.model.StreamingServicePaymentMetadata;
import io.sengage.webservice.persistence.PaymentDataProvider;
import io.sengage.webservice.utils.Constants;

@Log4j2
@AllArgsConstructor
@Builder
public class TwitchTransactionProcessor implements TransactionProcessor {
	
	private final PaymentDataProvider paymentDataProvider;

	@Override
	public MinigamesUserBalance handleBalanceUpdate(String channelId, String userId, long delta,
			StreamingServicePaymentMetadata metadata, long prevVersionId)
			throws InsufficientFundsException, FundsLimitExceededException {

		MinigamesUserBalance balance = handleBalanceUpdate(channelId, userId, delta, prevVersionId);
		/* TODO: Perhaps just don't use this for Twitch since their API supports retrieving transaction receipts.
		TwitchPaymentMetadata twitchPaymentMetadata = (TwitchPaymentMetadata) metadata;
		TransactionReceipt txReceipt = TransactionReceipt.builder()
				.transactionId(twitchPaymentMetadata.getTransactionId())
				.userId(userId)
				.channelId(channelId)
				.currency(Currency.from(twitchPaymentMetadata.getProduct().getCost().getType()))
				.cost(Long.valueOf(twitchPaymentMetadata.getProduct().getCost().getAmount()))
				.platform(Platform.TWITCH)
				.sku(twitchPaymentMetadata.getProduct().getSku())
				.createdAt(Instant.now())
				.build();
		paymentDataProvider.storeTransactionReceipt(txReceipt);*/
		return balance;
	}

	@Override
	public MinigamesUserBalance handleBalanceUpdate(String channelId,
			String userId, long delta, long prevVersionId) throws InsufficientFundsException,
			FundsLimitExceededException {
		MinigamesUserBalance currentBalance = paymentDataProvider.getBalance(channelId, userId);
		
		log.debug("Balance for user {} in channel {} is {}.", userId, channelId, currentBalance != null ? currentBalance.getBalance() : 0);		
		if (currentBalance == null && prevVersionId != 0) {
			throw new IllegalArgumentException("Provided a balance with prev version id > 0 but was not found in DB.");		
		} else if (!hasSufficientFunds(currentBalance, delta)) {
			throw new InsufficientFundsException(
					String.format("Insufficient funds for purchase. Current balance: %s, purchase amount: %s", currentBalance, Math.abs(delta))
			);
		} else if (exceedsMaxBalance(currentBalance, delta)) {
			throw new FundsLimitExceededException(
				String.format("Exceeded maximum balance. Current balance: %s, purchased amount: %s", currentBalance, delta)
			);
		}
		
		long updatedBalance;
		if (currentBalance == null) {
			updatedBalance = delta;
		} else {
			updatedBalance = currentBalance.getBalance() + delta;
		}
		
		MinigamesUserBalance newBalance;
		try {
			newBalance = MinigamesUserBalance.builder()
					.channelId(channelId)
					.userId(userId)
					.balance(updatedBalance)
					.platform(Platform.TWITCH)
					.initializedAt(currentBalance != null ? currentBalance.getInitializedAt() : Instant.now())
					.build();
			if (prevVersionId != 0) {
				newBalance.setVersion(prevVersionId);
			}
			
			newBalance = paymentDataProvider.updateBalance(newBalance);	
		} catch (ItemVersionMismatchException e) {
			// item version mismatch
			throw new RuntimeException("Version mismatch while updating user balance. Got: "+ prevVersionId, e);
		}
		// sanity check
		if (newBalance.getBalance() != updatedBalance) {
			// there was a catastrophic failure
			throw new IllegalStateException("Balance after transaction does not equal expected amount");
		}
		return newBalance;
	}

	@Override
	public Optional<MinigamesUserBalance> getUserBalance(String channelId, String userId) {
		return Optional.ofNullable(paymentDataProvider.getBalance(channelId, userId));
	}
	
	private boolean hasSufficientFunds(MinigamesUserBalance balance, long delta) {
		if (balance == null) {
			return delta >= 0;
		}
		return balance.getBalance() + delta >= 0;
	}
	
	private boolean exceedsMaxBalance(MinigamesUserBalance balance, long delta) {
		if (balance == null) {
			return delta > Constants.MAX_USER_BALANCE;
		}
		return balance.getBalance() + delta > Constants.MAX_USER_BALANCE;
			
	}

}
