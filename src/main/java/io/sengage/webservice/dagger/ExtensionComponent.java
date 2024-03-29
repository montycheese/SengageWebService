package io.sengage.webservice.dagger;

import io.sengage.webservice.function.CancelGame;
import io.sengage.webservice.function.CreateGame;
import io.sengage.webservice.function.FetchChannelActivity;
import io.sengage.webservice.function.GetFinalGameResults;
import io.sengage.webservice.function.GetUserBalance;
import io.sengage.webservice.function.JoinGame;
import io.sengage.webservice.function.KeepWarm;
import io.sengage.webservice.function.Ping;
import io.sengage.webservice.function.QuitGame;
import io.sengage.webservice.function.UpdateGameState;
import io.sengage.webservice.function.UpdateUserBalance;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {BaseModule.class, ExtensionModule.class, DataModule.class, NetworkingModule.class, StateMachineModule.class })
public interface ExtensionComponent {
	void injectCreateGame(CreateGame createGame);
	void injectUpdateGameState(UpdateGameState updateGameState);
	void injectJoinGame(JoinGame joinGame);
	void injectGetFinalGameResults(GetFinalGameResults getFinalGameResults);
	void injectCancelGame(CancelGame cancelGame);
	void injectQuitGame(QuitGame quitGame);
	void injectGetUserBalance(GetUserBalance getUserBalance);
	void injectUpdateUserBalance(UpdateUserBalance updateUserBalance);
	void injectFetchChannelActivity(FetchChannelActivity fetchChannelActivity);
	void injectPing(Ping ping);
	void injectKeepWarm(KeepWarm keepWarm);
}
