package com.github.markozajc.juno.game.impl;

import java.util.*;

import javax.annotation.Nonnull;

import com.github.markozajc.juno.decks.impl.UnoStandardDeck;
import com.github.markozajc.juno.game.*;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.players.impl.*;
import com.github.markozajc.juno.rules.pack.UnoRulePack;
import com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules;
import com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules.UnoHouseRule;

/**
 * A console-based {@link UnoControlledGame} implementation. This is not meant to be
 * used in production and is solely an example implementation.
 *
 * @author Marko Zajc
 */
public class UnoConsoleGame extends UnoControlledGame {

	@SuppressWarnings({ "resource", "null" })
	@Nonnull
	private static UnoRulePack getRulePack() {
		List<UnoHouseRule> rules = new ArrayList<>();
		Scanner s = new Scanner(System.in);
		for (UnoHouseRule rule : UnoHouseRule.values()) {
			System.out.print("Activate the " + rule.getName() + " house rule? [y/n] ");
			if (s.nextLine().equalsIgnoreCase("y"))
				rules.add(rule);
		}

		return UnoOfficialRules.getPack(rules.toArray(new UnoHouseRule[rules.size()]));
	}

	/**
	 * Creates a new {@link UnoConsoleGame} with a {@link UnoConsolePlayer} named "You"
	 * and a {@link UnoStrategicPlayer} named "Billy the StrategicUnoHand".
	 */
	public UnoConsoleGame() {
		super(new UnoConsolePlayer("You"), new UnoStrategicPlayer("Billy the StrategicUnoHand"),
			  UnoStandardDeck.getDeck(), 7, getRulePack());
	}

	/**
	 * The main method
	 *
	 * @param args
	 *            arguments (will be ignored)
	 */
	public static void main(String[] args) {
		UnoGame game = new UnoConsoleGame();

		UnoWinner winner = game.play();
		UnoPlayer winnerPlayer = winner.getWinner();
		if (winnerPlayer == null) {
			System.out.println("It's a draw!");

		} else {
			System.out.println(winnerPlayer.getName() + " won!");
		}
		System.out.print("Reason: ");
		switch (winner.getEndReason()) {
			case REQUESTED:
				System.out.println("you quit.");
				break;
			case FALLBACK:
				System.out.println("the draw pile was depleted and there weren't any cards in the discard pile.");
				break;
			case VICTORY:
				System.out.println("placed all cards.");
				break;
			case UNKNOWN:
				System.out.println("this shouldn't have happened!" +
					"Please send a log of the game to https://github.com/markozajc/JUNO/issues.");
				break;

		}
	}

	@Override
	public void onEvent(String format, Object... arguments) {
		System.out.printf(format, arguments);
		System.out.println();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
