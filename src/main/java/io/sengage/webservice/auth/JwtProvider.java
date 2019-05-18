package io.sengage.webservice.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.sengage.webservice.dagger.ExtensionModule;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.inject.Named;

public class JwtProvider {
	
	private static final int EXPIRY_LEEWAY_MINUTES = 20;
	private final String secret;
	
	@Inject
	public JwtProvider(@Named(ExtensionModule.EXTENSION_SECRET) String secret) {
		this.secret = secret;
	}

	public String signJwt(Map<TwitchJWTField, Object> claimsMap) {
		Claims claims = Jwts.claims();
		claims.put(TwitchJWTField.EXP.value, getExpiry());
		
		for (Map.Entry<TwitchJWTField, Object> entry : claimsMap.entrySet()) {
			claims.put(entry.getKey().value, entry.getValue());
		}
		
		return Jwts.builder()
				.setClaims(claims)
				.signWith(getSecretKey())
				.compact();
	}
	
	
	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
	}
	
	private Date getExpiry() {
		return Date.from(Instant.now().plus(EXPIRY_LEEWAY_MINUTES, ChronoUnit.MINUTES));
	}
}