package com.github.markozajc.juno.game;

import static com.github.markozajc.juno.decks.impl.UnoStandardDeck.getDeck;
import static com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules.UnoHouseRule.PROGRESSIVE;
import static java.lang.String.format;
import static java.lang.System.out;

import java.util.stream.IntStream;

import javax.annotation.*;

import org.junit.jupiter.api.Test;

import com.github.markozajc.juno.decks.UnoDeck;
import com.github.markozajc.juno.decks.impl.UnoStandardDeck;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.players.impl.UnoStrategicPlayer;
import com.github.markozajc.juno.rules.pack.UnoRulePack;
import com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules;

import static org.junit.jupiter.api.Assertions.*;

class UnoControlledGameTest {

	private static final int ROUNDS = 10000;

	private static final class UnoStressTestGame extends UnoControlledGame {

		public UnoStressTestGame(@Nonnull UnoDeck unoDeck, @Nonnegative int cardAmount, @Nonnull UnoRulePack rules,
								 @Nonnull UnoPlayer... players) {
			super(unoDeck, cardAmount, rules, players);
		}

		@Override
		public void onEvent(String format, Object... arguments) {}
	}

	private static final String DEBUG_FORMAT = "\nDebug information: EXT:%s,EXM:%s,RDN:%s,DRC:%s,DIC:%s,TCR:%s";
	private static final String HAND_COUNT_FORMAT = ",H%sC:%s";

	private static String gatherDebugInfo(UnoGame game, int i, Exception e) {
		StringBuilder builder = new StringBuilder();
		builder.append(format(DEBUG_FORMAT, e.getClass().getSimpleName(), e.getMessage(), i, game.getDraw().getSize(),
							  game.getDiscard().getSize(), game.getTopCard()));
		for (int p = 0; p < game.getPlayers().size(); p++) {
			UnoPlayer player = game.getPlayers().get(p);
			builder.append(format(HAND_COUNT_FORMAT, p, player.getCards().size()));
		}
		return builder.toString();
	}

	@Test
	void testStressTwoPlayers() {
		stress(2);
	}

	@Test
	void testStressThreePlayers() {
		stress(3);
	}

	@SuppressWarnings("null")
	private static void stress(int playerCount) {
		var players = IntStream.rangeClosed(1, playerCount)
			.mapToObj(i -> format("P%d", i))
			.map(UnoStrategicPlayer::new)
			.toArray(UnoStrategicPlayer[]::new);

		out.printf("[==== INITIATING THE STRESS TEST FOR %d PLAYERS ====]%n", playerCount);
		UnoGame game = new UnoStressTestGame(getDeck(), 7, UnoOfficialRules.getPack(PROGRESSIVE), players);
		for (int i = 0; i < ROUNDS; i++) {
			try {
				game.play();

				assertEquals(game.getDiscard().getSize() + game.getDraw().getSize()
							 + game.getPlayers().stream().mapToInt(player -> player.getCards().size()).sum(),
							 UnoStandardDeck.getExpectedSize());
			} catch (Exception e) {
				e.printStackTrace();
				fail("The stress test has failed. " + gatherDebugInfo(game, i, e));
			}
		}
		out.printf("[==== STRESS TEST PLAYED %d ROUNDS ====]%n", ROUNDS);
	}

}
