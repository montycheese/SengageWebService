package io.sengage.webservice.twitch;

import io.sengage.webservice.auth.JwtProvider;
import io.sengage.webservice.auth.TwitchJWTField;
import io.sengage.webservice.dagger.ExtensionModule;
import io.sengage.webservice.model.GameCancellationReason;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.sengames.model.pubsub.CancelGameMessage;
import io.sengage.webservice.sengames.model.pubsub.EndGameMessage;
import io.sengage.webservice.sengames.model.pubsub.JoinGameMessage;
import io.sengage.webservice.sengames.model.pubsub.PubSubGameMessage;
import io.sengage.webservice.sengames.model.pubsub.StartGameMessage;
import io.sengage.webservice.utils.GameItemToEndgamePubSubMessageMapper;
import io.sengage.webservice.utils.GameItemToJoinGamePubSubMessageMapper;
import io.sengage.webservice.utils.GameItemToStartGamePubSubMessageMapper;
import io.sengage.webservice.utils.PlayerToPlayerCompletePubSubMessageMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.gson.Gson;

public final class TwitchClient {
	
	private static final String TWITCH_API_BASE_URL = "https://api.twitch.tv";
	
	private final String clientId;
	private final String extensionOwnerId;
	private final String extensionVersion;
	private final HttpRequestFactory requestFactory;
	private final JsonFactory jsonFactory;
	private final Gson gson;
	private final JwtProvider jwtProvider;
	

	@Inject
	public TwitchClient(HttpRequestFactory requestFactory, JsonFactory jsonFactory,
			@Named(ExtensionModule.CLIENT_ID) String clientId,
			@Named(ExtensionModule.EXTENSION_OWNER_TWITCH_ID) String ownerId,
			@Named(ExtensionModule.EXTENSION_VERSION) String extensionVersion,
			Gson gson, 
			JwtProvider jwtProvider) {
		this.clientId = clientId;
		this.requestFactory = requestFactory;
		this.jsonFactory = jsonFactory;
		this.extensionOwnerId = ownerId;
		this.extensionVersion = extensionVersion;
		this.gson = gson;
		this.jwtProvider = jwtProvider;
	}
	
	public boolean notifyChannelJoinGame(GameItem gameItem) {
		String urlString = getSendExtensionPubSubMessageUrl(gameItem.getChannelId());
		
		JoinGameMessage message = GameItemToJoinGamePubSubMessageMapper.get(gameItem);
		
		return sendPubSubMessage(message, gameItem.getChannelId(), urlString);
	}
	
	public boolean notifyChannelGameStarted(GameItem gameItem) {
		String urlString = getSendExtensionPubSubMessageUrl(gameItem.getChannelId());

		StartGameMessage message = GameItemToStartGamePubSubMessageMapper.get(gameItem);
		return sendPubSubMessage(message, gameItem.getChannelId(), urlString);
	}
	
	public boolean notifyChannelGameEnded(GameItem gameItem) {
		String urlString = getSendExtensionPubSubMessageUrl(gameItem.getChannelId());

		EndGameMessage message = GameItemToEndgamePubSubMessageMapper.get(gameItem);
		
		return sendPubSubMessage(message, gameItem.getChannelId(), urlString);
	}
	
	public boolean notifyChannelGameCancelled(GameItem gameItem, GameCancellationReason cancellationReason) {
		String urlString = getSendExtensionPubSubMessageUrl(gameItem.getChannelId());	
		
		CancelGameMessage message = CancelGameMessage.builder()
				.game(gameItem.getGame())
				.gameId(gameItem.getGameId())
				.gameStatus(gameItem.getGameStatus())
				.cancellationReason(cancellationReason)
				.build();

		return sendPubSubMessage(message, gameItem.getChannelId(), urlString);
	}
	
	public boolean notifyChannelPlayerComplete(PlayerCompleteRequest playerCompleteRequest) {
		String urlString = getSendExtensionPubSubMessageUrl(playerCompleteRequest.getChannelId());	
		
		PubSubGameMessage message = PlayerToPlayerCompletePubSubMessageMapper.get(playerCompleteRequest);
		
		return sendPubSubMessage(message, playerCompleteRequest.getChannelId(), urlString);
	}
	
	public boolean sendExtensionChatMessage(String channelId, String message) {
		boolean success = false;
		String urlString = String.format("%s/extensions/%s/%s/channels/%s/chat", 
				TWITCH_API_BASE_URL, clientId, extensionVersion, channelId);
		GenericUrl url = new GenericUrl(urlString);

		
		HttpContent content = new JsonHttpContent(jsonFactory,  new ChatMessage(message));
		
		String authToken = jwtProvider.signJwt(getClaimsForExtensionChatMessage(channelId));
		
		try {
			sendHttpPostRequest(content, url, authToken, false);
			success = true;
		} catch (IOException e) {
			throw new RuntimeException(String.format("Exception thrown while sending extension chat message to channel [%s] at url [%s]",
					channelId, urlString), e);
		}
		return success;
	}
	
	private Map<TwitchJWTField, Object> getClaimsForChannelMessage(String channelId) {
		Map<String, Object> pubsubPerms = new HashMap<>();
		pubsubPerms.put("send", Arrays.asList("broadcast"));
		
		Map<TwitchJWTField, Object> claimsAsMap = new HashMap<>();
		claimsAsMap.put(TwitchJWTField.PUBSUB_PERMS, pubsubPerms);
		claimsAsMap.put(TwitchJWTField.USER_ID, extensionOwnerId);
		claimsAsMap.put(TwitchJWTField.ROLE,  TwitchRole.EXTERNAL.name().toLowerCase());
		claimsAsMap.put(TwitchJWTField.CHANNEL_ID, channelId);
		return claimsAsMap;
	}
	
	private Map<TwitchJWTField, Object> getClaimsForExtensionChatMessage(String channelId) {
		
		Map<TwitchJWTField, Object> claimsAsMap = new HashMap<>();
		
		claimsAsMap.put(TwitchJWTField.USER_ID, extensionOwnerId);
		claimsAsMap.put(TwitchJWTField.ROLE, TwitchRole.EXTERNAL.name().toLowerCase());
		claimsAsMap.put(TwitchJWTField.CHANNEL_ID, channelId);
		
		return claimsAsMap;
	}
	
	
	private boolean sendPubSubMessage(PubSubGameMessage message, String channelId, String urlString) {
		boolean success = false;
		HttpContent content = new JsonHttpContent(jsonFactory, 
				PubSubMessage.builder()
				.contentType(PubSubMessage.JSON)
				.targets(Arrays.asList("broadcast"))
				.message(gson.toJson(message, message.getClass()))
				.build());
		
		String authToken = jwtProvider.signJwt(getClaimsForChannelMessage(channelId));
		GenericUrl url = new GenericUrl(urlString);
		try {
			sendHttpPostRequest(content, url, authToken, true);
			success = true;
		} catch (IOException e) {
			throw new RuntimeException(String.format("Exception thrown while sending pubsub message to channel [%s] at url [%s]",
					channelId, urlString), e);
		}
		return success;
	}
	
	private void sendHttpPostRequest(HttpContent content, GenericUrl url, String authToken, boolean retry)
			throws IOException {
		HttpRequest request = requestFactory.buildPostRequest(url, content);
		initHttpHeaders(request, authToken);
		
		if (retry) {
			request.setUnsuccessfulResponseHandler(getUnsuccessfulResponseHandler());	
		}
		HttpResponse response =  request.execute();
		response.disconnect();
	}
	
	private void initHttpHeaders(HttpRequest request, String authToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.fromHttpHeaders(request.getHeaders());
		headers.setAuthorization(String.format("Bearer %s", authToken));
		headers.setContentType("application/json");
		headers.set("Client-ID", clientId);
		request.setHeaders(headers);
	}
	
	private HttpUnsuccessfulResponseHandler getUnsuccessfulResponseHandler() {
		return new HttpBackOffUnsuccessfulResponseHandler(new ExponentialBackOff());
	}
	
	private String getSendExtensionPubSubMessageUrl(String channelId) {
		return  String.format("%s/extensions/message/%s", TWITCH_API_BASE_URL, channelId);
	}
	
}