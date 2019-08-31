package io.sengage.webservice.dagger;

import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsAsync;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsAsyncClientBuilder;

import dagger.Module;
import dagger.Provides;

@Module
public class StateMachineModule {
	
	public static final String STATE_MACHINE_EXE_ROLE_ARN = "StateMachineExecutionRoleArn";
	public static final String GAME_TASK_HANDLER_LAMBDA_ARN = "GameTaskHandlerLambdaArn";

	@Singleton
	@Provides
	@Named(STATE_MACHINE_EXE_ROLE_ARN)
	static String provideStateMachineExecutionRoleArn() {
		return System.getenv(STATE_MACHINE_EXE_ROLE_ARN);
	}
	
	@Singleton
	@Provides
	@Named(GAME_TASK_HANDLER_LAMBDA_ARN)
	static String provideGameTaskHandlerLambdaArn() {
		return System.getenv(GAME_TASK_HANDLER_LAMBDA_ARN);
	}
	
	@Provides
	@Singleton
	static AWSStepFunctionsAsync provideAWSStepFunctionsAsync() {
		return AWSStepFunctionsAsyncClientBuilder
				.standard()
				.withCredentials(new EnvironmentVariableCredentialsProvider())
				.build();
	}
}
