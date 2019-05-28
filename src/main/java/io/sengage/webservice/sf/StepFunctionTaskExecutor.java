package io.sengage.webservice.sf;

import javax.inject.Inject;
import javax.inject.Named;

import com.amazonaws.services.stepfunctions.AWSStepFunctionsAsync;
import com.amazonaws.services.stepfunctions.builder.StateMachine;
import com.amazonaws.services.stepfunctions.builder.states.EndTransition;
import com.amazonaws.services.stepfunctions.builder.states.NextStateTransition;
import com.amazonaws.services.stepfunctions.builder.states.TaskState;
import com.amazonaws.services.stepfunctions.builder.states.WaitForSeconds;
import com.amazonaws.services.stepfunctions.builder.states.WaitState;
import com.amazonaws.services.stepfunctions.model.CreateStateMachineRequest;
import com.amazonaws.services.stepfunctions.model.CreateStateMachineResult;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.google.gson.Gson;

import io.sengage.webservice.dagger.TaskModule;
import io.sengage.webservice.events.EventDetail;
import io.sengage.webservice.model.GameItem;
import io.sengage.webservice.utils.GameToWaitForPlayersToJoinDurationMapper;

public class StepFunctionTaskExecutor {

	private final Gson gson;
	private final AWSStepFunctionsAsync sfClient;
	private final String targetLambdaArn;
	private final String stateMachineExecutionArn;
	
	@Inject
	public StepFunctionTaskExecutor(Gson gson, AWSStepFunctionsAsync sfClient,
			@Named(TaskModule.GAME_TASK_HANDLER_LAMBDA_ARN) String targetLambdaArn, 
			@Named(TaskModule.STATE_MACHINE_EXE_ROLE_ARN) String stateMachineExecutionArn) {
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
		
		sfClient.startExecutionAsync(startExecutionRequest);
		
		
		return stateMachineArn;
	}
	
	public String executeGameTimeUpStateMachine(GameItem gameItem) {
		
		return null;
	}
	
	private String createStateMachine(GameItem gameItem) {
		int secondsToWaitForPlayersToJoin = GameToWaitForPlayersToJoinDurationMapper.get(gameItem.getGame());
		CreateStateMachineRequest req = new CreateStateMachineRequest();
		
		String invokeStartGameLambdaStateName = "StartGameState";
		String waitStateName = "WaitForTimeStamp";
		StateMachine startGameStateMachine = StateMachine.builder()
		.startAt(waitStateName)
		.timeoutSeconds(gameItem.getDuration() * 2) // give it game time * 2 for buffer
		.comment("State Machine to Trigger starting game")
		.state(waitStateName, WaitState.builder()
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
		
		System.out.println("Successfully created state machine with arn:" + result.getStateMachineArn());
		
		return result.getStateMachineArn();
	}
}
