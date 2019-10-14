package io.sengage.webservice.persistence;

import io.sengage.webservice.exception.ItemVersionMismatchException;
import io.sengage.webservice.model.MinigamesUserBalance;

public interface PaymentDataProvider {
	MinigamesUserBalance updateBalance(MinigamesUserBalance newBalance) throws ItemVersionMismatchException;
	MinigamesUserBalance getBalance(String channelId, String userId);
}
