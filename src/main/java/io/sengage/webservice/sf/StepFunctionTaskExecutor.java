package io.sengage.webservice.sf;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.extern.log4j.Log4j2;

import com.amazonaws.services.stepfunctions.AWSStepFunctionsAsync;
import com.amazonaws.services.stepfunctions.builder.StateMachine;
import com.amazonaws.services.stepfunctions.builder.states.EndTransition;
import com.amazonaws.services.stepfunctions.builder.states.NextStateTransition;
import com.amazonaws.services.stepfunctions.builder.states.TaskState;
import com.amazonaws.services.stepfunctions.builder.states.WaitForSeconds;
import com.amazonaws.services.stepfunctions.builder.states.WaitState;
import com.amazonaws.services.stepfunctions.model.CreateStateMachineRequest;
import com.amazonaws.services.stepfunctions.model.CreateStateMachineResult;
import com.amazonaws.services.stepfunctions.model.DeleteStateMachineRequest;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.amazonaws.services.stepfunctions.model.StartExecutionResult;
import com.google.gson.Gson;

import io.sengage.webservice.dagger.StateMachineModule;
import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.utils.GameToWaitForPlayersToJoinDurationMapper;

@Log4j2
public class StepFunctionTaskExecutor {

	private final Gson gson;
	private final AWSStepFunctionsAsync sfClient;
	private final String targetLambdaArn;
	private final String stateMachineExecutionArn;
	private final String WAIT_STATE_NAME = "WaitForTimeStamp";
	
	@Inject
	public StepFunctionTaskExecutor(Gson gson, AWSStepFunctionsAsync sfClient,
			@Named(StateMachineModule.GAME_TASK_HANDLER_LAMBDA_ARN) String targetLambdaArn, 
			@Named(StateMachineModule.STATE_MACHINE_EXE_ROLE_ARN) String stateMachineExecutionArn) {
		this.gson = gson;
		this.sfClient = sfClient;
		this.targetLambdaArn = targetLambdaArn;
		this.stateMachineExecutionArn = stateMachineExecutionArn;
	}
	
	
	public String executeStartGameStateMachine(GameItem gameItem) {
		StartExecutionRequest startExecutionRequest = new StartExecutionRequest();
		
		String stateMachineArn = createStateMachine(gameItem);
		
		startExecutionRequest.withName("StartGame-" + gameItem.getGameId())
		.withStateMachineArn(stateMachineArn);
		
		StartExecutionResult response = sfClient.startExecution(startExecutionRequest);
		
		log.info("StepFunctionTaskExecutor#executeStartGameStateMachine(): Started Execution " + response.getExecutionArn());
		
		return stateMachineArn;
	}
	
	public String executeGameTimeUpStateMachine(GameItem gameItem) {
		StartExecutionRequest startExecutionRequest = new StartExecutionRequest();
		String stateMachineArn = createGameTimeUpStateMachine(gameItem);
		
		startExecutionRequest.withName("EndGame-" + gameItem.getGameId())
		.withStateMachineArn(stateMachineArn);
		sfClient.startExecutionAsync(startExecutionRequest);
		
		return stateMachineArn;
	}
	
	public void cleanUpGameStateMachineResources(GameItem gameItem) {
		
		for (String arn : gameItem.getStateMachineArns()) {
			DeleteStateMachineRequest req = new DeleteStateMachineRequest()
				.withStateMachineArn(arn);
			log.info("Deleting State Machine with arn: " + arn);
			try {
				// make async/
				sfClient.deleteStateMachine(req);
			} catch (Exception e) {
				log.error("Failed to delete State Machine with arn: " + arn, e);
			}
		}
	}
	
	private String createGameTimeUpStateMachine(GameItem gameItem) {
		int waitDuration = GameToWaitForPlayersToJoinDurationMapper.get(gameItem.getGame());
		// subtract the time we use to allow players to join the game from the overall game time.
		int secondsToWaitToExpireGame = gameItem.getDuration() - waitDuration;
		CreateStateMachineRequest req = new CreateStateMachineRequest();
		
		String invokeExpireGameLambdaStateName = "ExpireGameState";
		
		StateMachine expireGameStateMachine = StateMachine.builder()
		.startAt(WAIT_STATE_NAME)
		.timeoutSeconds(gameItem.getDuration() * 2)
		.comment("StateMachine to trigger expire game")
		.state(WAIT_STATE_NAME, WaitState.builder()
				.comment("Waiting state to allow players to join before starting.")
				.waitFor(WaitForSeconds.builder().seconds(secondsToWaitToExpireGame))
				.transition(NextStateTransition.builder().nextStateName(invokeExpireGameLambdaStateName)))
		.state(invokeExpireGameLambdaStateName, TaskState.builder()
				.comment("State that triggers a task to expire the game.")
				.resource(targetLambdaArn)
				.parameters(gson.toJson(GameContextInput.builder()
						.eventDetail(EventDetail.GAME_OUT_OF_TIME)
						.game(gameItem.getGame())
						.gameId(gameItem.getGameId())
						.build(), GameContextInput.class))
				.transition(EndTransition.builder()))
		.build();
		
		
		req.withDefinition(expireGameStateMachine)
		.withName("ExpireGame-" + gameItem.getGameId())
		.withRoleArn(stateMachineExecutionArn);
		
		CreateStateMachineResult result = sfClient.createStateMachine(req);
		
		log.info("Successfully created state machine with arn:" + result.getStateMachineArn());
		
		return result.getStateMachineArn();
	}
	
	private String createStateMachine(GameItem gameItem) {
		int secondsToWaitForPlayersToJoin = GameToWaitForPlayersToJoinDurationMapper.get(gameItem.getGame());
		CreateStateMachineRequest req = new CreateStateMachineRequest();
		
		String invokeStartGameLambdaStateName = "StartGameState";
		StateMachine startGameStateMachine = StateMachine.builder()
		.startAt(WAIT_STATE_NAME)
		.timeoutSeconds(gameItem.getDuration() * 2) // give it game time * 2 for buffer
		.comment("State Machine to Trigger starting game")
		.state(WAIT_STATE_NAME, WaitState.builder()
				.comment("Waiting state to allow players to join before starting.")
				.waitFor(WaitForSeconds.builder().seconds(secondsToWaitForPlayersToJoin))
				.transition(NextStateTransition.builder().nextStateName(invokeStartGameLambdaStateName)))
		.state(invokeStartGameLambdaStateName, TaskState.builder()
				.comment("State that triggers a task to start the game.")
				.resource(targetLambdaArn)
				.parameters(gson.toJson(GameContextInput.builder()
						.eventDetail(EventDetail.WAITING_FOR_PLAYERS_COMPLETE)
						.game(gameItem.getGame())
						.gameId(gameItem.getGameId())
						.build(), GameContextInput.class))
				.transition(EndTransition.builder()))
		.build();
		req.withDefinition(startGameStateMachine)
		.withName("StartGame-" + gameItem.getGameId())
		.withRoleArn(stateMachineExecutionArn);
		
		CreateStateMachineResult result = sfClient.createStateMachine(req);
		
		log.info("Successfully created state machine with arn:" + result.getStateMachineArn());
		
		return result.getStateMachineArn();
	}
}
