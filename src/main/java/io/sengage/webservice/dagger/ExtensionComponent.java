package io.sengage.webservice.dagger;

import io.sengage.webservice.function.CreateGame;
import io.sengage.webservice.function.GetFinalGameResults;
import io.sengage.webservice.function.JoinGame;
import io.sengage.webservice.function.UpdateGameState;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {BaseModule.class, ExtensionModule.class, DataModule.class, NetworkingModule.class})
public interface ExtensionComponent {
	void injectCreateGame(CreateGame createGame);
	void injectUpdateGameState(UpdateGameState updateGameState);
	void injectJoinGame(JoinGame joinGame);
	void injectGetFinalGameResults(GetFinalGameResults getFinalGameResults);
}
