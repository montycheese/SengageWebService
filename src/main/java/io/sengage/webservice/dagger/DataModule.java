package io.sengage.webservice.dagger;

import io.sengage.webservice.persistence.GameDataProvider;
import io.sengage.webservice.persistence.ddb.DynamoDBGameDataProvider;

import javax.inject.Singleton;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {

	@Provides
	@Singleton
	static GameDataProvider provideGameDataProvider(DynamoDBMapper mapper) {
		return new DynamoDBGameDataProvider(mapper);
	}
	
	@Provides
	@Singleton
	static DynamoDBMapper provideDDBMapper(AmazonDynamoDB ddb) {
		return new DynamoDBMapper(ddb);
	}
	
	@Provides
	@Singleton
	static AmazonDynamoDB provideDDB() {
		return AmazonDynamoDBClientBuilder.defaultClient();
	}
	
}
