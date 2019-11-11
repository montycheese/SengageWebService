package io.sengage.webservice.balance;

public enum Currency {
	BITS
	;
	
	public static Currency from(String currency) {
		for (Currency c : values()) {
			if (c.name().equalsIgnoreCase(currency)) {
				return c;
			}
		}
		throw new IllegalArgumentException("Could not find matching currency for: " + currency);
	}
}
