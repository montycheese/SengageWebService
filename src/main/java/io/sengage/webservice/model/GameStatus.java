package io.sengage.webservice.model;

import lombok.Getter;

public enum GameStatus {
	INIT(0),
	WAITING_FOR_PLAYERS(1),
	IN_PROGRESS(2),
	COMPLETED(3),
	TIME_UP(3),
	TERMINAL(4),
	ERROR_STATE(5),
	CANCELLED(5)
	;
	
	@Getter
	private final int ordinal;
	
	private GameStatus(int ordinal) {
		this.ordinal = ordinal;
	}
	
	public boolean isOnOrBefore(GameStatus other) {
		return this.ordinal <= other.ordinal;
	}
	
	public boolean isBefore(GameStatus other) {
		return this.ordinal < other.ordinal;
	}
	
	public boolean isOnOrAfter(GameStatus other) {
		return this.ordinal >= other.ordinal;
	}
	
	public boolean isAfter(GameStatus other) {
		return this.ordinal > other.ordinal;
	}
	
}
