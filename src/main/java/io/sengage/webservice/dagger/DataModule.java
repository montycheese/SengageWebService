package io.sengage.webservice.dagger;

import io.sengage.webservice.cache.GameCache;
import io.sengage.webservice.cache.S3BackedGameCache;
import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.PlayerDataProvider;
import io.sengage.webservice.persistence.ddb.DynamoDBGameDataProvider;
import io.sengage.webservice.persistence.ddb.DynamoDBPlayerDataProvider;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3;
import com.google.gson.Gson;

import dagger.Module;
import dagger.Provides;


@Module(includes = BaseModule.class)
public class DataModule {
	
	@Provides
	@Singleton
	static GameCache provideGameCache(AmazonS3 amazonS3, Gson gson, @Named(BaseModule.S3_BUCKET_NAME) String bucketName) {
		return new S3BackedGameCache(amazonS3, gson, bucketName);
	}

	@Provides
	@Singleton
	static GameDataProvider provideGameDataProvider(DynamoDBMapper mapper) {
		return new DynamoDBGameDataProvider(mapper);
	}
	
	@Provides
	@Singleton
	static PlayerDataProvider providePlayerDataProvider(DynamoDBMapper mapper) {
		return new DynamoDBPlayerDataProvider(mapper);
	}
	
	@Provides
	@Singleton
	static DynamoDBMapper provideDDBMapper(AmazonDynamoDB ddb) {
		return new DynamoDBMapper(ddb);
	}
	
	@Provides
	@Singleton
	static AmazonDynamoDB provideDDB(EnvironmentVariableCredentialsProvider credProvider) {
		ClientConfiguration cfg = new ClientConfiguration()
		.withProtocol(Protocol.HTTP);
		
		return AmazonDynamoDBClientBuilder.standard()
				.withClientConfiguration(cfg)
				.withCredentials(credProvider)
				.build();
	}
	
}
