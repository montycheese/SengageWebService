package io.sengage.webservice.cache;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.sengames.model.pubsub.JoinGameMessage;
import io.sengage.webservice.utils.Constants;
import io.sengage.webservice.utils.GameItemToJoinGamePubSubMessageMapper;

@Log4j2
@AllArgsConstructor
public class S3BackedGameCache implements GameCache {
	private static final int NO_SUCH_KEY_STATUS_CODE = 404;
	private static final String NO_SUCH_KEY_ERROR_CODE = "NoSuchKey";
	
	private final AmazonS3 s3Client;
	private final Gson gson;
	private final String cacheBucketName;
	

	@Override
	public void putGameDetails(String channelId, GameItem game) {
		JoinGameMessage message = GameItemToJoinGamePubSubMessageMapper.get(game);
		s3Client.putObject(
				cacheBucketName, 
				String.format(Constants.GAME_SESSION_DETAILS_CACHE_S3_PATH_FORMAT, channelId),
				gson.toJson(message, JoinGameMessage.class)
			);
		
	}

	@Override
	public void clearGameDetails(String channelId) {
		try {
			s3Client.deleteObject(
					cacheBucketName, 
					String.format(Constants.GAME_SESSION_DETAILS_CACHE_S3_PATH_FORMAT, channelId)
			);	
		} catch (Exception e) {
			// ok to swallow because the next PUT will overwrite this anyways
			log.warn("Failed to delete cache at path {} due to {}",
					String.format(Constants.GAME_SESSION_DETAILS_CACHE_S3_PATH_FORMAT, channelId), e);
		}
		
	}

	@Override
	public Object getGameDetails(String channelId) {
		String output = "";
		try {
			output = s3Client.getObjectAsString(cacheBucketName, String.format(Constants.GAME_SESSION_DETAILS_CACHE_S3_PATH_FORMAT, channelId));
			if (output == null) {
				output = "";
			}
		} catch (AmazonS3Exception e) {
			if (!(NO_SUCH_KEY_ERROR_CODE.equalsIgnoreCase(e.getErrorCode()) && NO_SUCH_KEY_STATUS_CODE == e.getStatusCode())) {
				throw e;
			}
		}
		
		return output;
		
	}

}
