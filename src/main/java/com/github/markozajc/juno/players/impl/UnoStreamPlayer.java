package com.github.markozajc.juno.players.impl;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.github.markozajc.juno.cards.*;
import com.github.markozajc.juno.cards.impl.UnoDrawCard;
import com.github.markozajc.juno.game.UnoGame;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.rules.pack.impl.house.UnoProgressiveRulePack;
import com.github.markozajc.juno.utils.UnoRuleUtils;

/**
 * A human-driven player that uses a {@link Scanner} to read input and sends the
 * output to the given {@link PrintStream}. Blocks invalid card and color placements
 * automatically. This is meant as an example hand.
 *
 * @author Marko Zajc
 */
public class UnoStreamPlayer extends UnoPlayer {

	private static final String INVALID_CHOICE_STRING = "Invalid choice!";

	private final Scanner scanner;
	private final PrintStream ps;

	/**
	 * Creates a new {@link UnoStreamPlayer}.
	 *
	 * @param name
	 *            this player's name
	 * @param is
	 *            {@link InputStream} to read from
	 * @param ps
	 *            {@link PrintStream} to write to
	 */
	public UnoStreamPlayer(@Nonnull String name, @Nonnull InputStream is, @Nonnull PrintStream ps) {
		super(name);
		this.scanner = new Scanner(is);
		this.ps = ps;
	}

	@SuppressWarnings("null")
	@Override
	public UnoCard playCard(UnoGame game, UnoPlayer next) {
		UnoCard top = game.getDiscard().getTop();
		List<UnoCard> possible =
			UnoRuleUtils.combinedPlacementAnalysis(top, this.getHand().getCards(), game.getRules(), this.getHand());

		this.ps.println("Choose a card: [" + next.getName() +
			" hand size: " +
			next.getHand().getSize() +
			" | Draw pile size: " +
			game.getDraw().getSize() +
			" | Discard pile size: " +
			game.getDiscard().getSize() +
			" | Top card: " +
			game.getDiscard().getTop() +
			"]");

		List<UnoDrawCard> drawCards = UnoProgressiveRulePack.getConsecutive(game.getDiscard());
		if (!drawCards.isEmpty()) {
			this.ps.println("0 - Draw " + drawCards.size() * drawCards.get(0).getAmount() +
				" cards from " +
				drawCards.size() +
				" " +
				top +
				(drawCards.size() == 1 ? "" : "s"));
		} else {
			this.ps.println("0 \u2022 Draw");
		}

		int i = 1;
		for (UnoCard card : this.getHand().getCards()) {
			if (possible.contains(card)) {
				this.ps.println(i + " \u2022 " + card);
			} else {
				this.ps.println(i + " - " + card);
			}

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			i++;
		}
		this.ps.println("q \u2022 Quit");

		while (true) {
			String nextLine = this.scanner.nextLine();
			if (nextLine.equalsIgnoreCase("rules")) {
				this.ps.println("Active rules: " + game.getRules()
					.getRules()
					.stream()
					.map(r -> r.getClass().getSimpleName())
					.collect(Collectors.joining(", ")));
				continue;
			}

			if ("q".equalsIgnoreCase(nextLine)) {
				game.endGame();
				return null;
			}

			int choice;
			try {
				choice = Integer.parseInt(nextLine);
			} catch (NumberFormatException e) {
				this.ps.println(INVALID_CHOICE_STRING);
				continue;
			}

			if (choice == 0)
				return null;

			if (choice > this.getCards().size()) {
				this.ps.println(INVALID_CHOICE_STRING);
				continue;
			}

			UnoCard card = this.getCards().get(choice - 1);

			if (!possible.contains(card)) {
				this.ps.println(INVALID_CHOICE_STRING);
				continue;
			}

			return card;
		}
	}

	@Override
	public UnoCardColor chooseColor(UnoGame game) {
		this.ps.println("Choose a color:");

		this.ps.println("0 \u2022 Yellow");
		this.ps.println("1 \u2022 Red");
		this.ps.println("2 \u2022 Green");
		this.ps.println("3 \u2022 Blue");

		while (true) {
			int choice;
			try {
				choice = Integer.parseInt(this.scanner.nextLine());
			} catch (NumberFormatException e) {
				this.ps.println(INVALID_CHOICE_STRING);
				continue;
			}

			switch (choice) {
				case 0:
					return UnoCardColor.YELLOW;
				case 1:
					return UnoCardColor.RED;
				case 2:
					return UnoCardColor.GREEN;
				case 3:
					return UnoCardColor.BLUE;
				default:
					break;
			}

			this.ps.println(INVALID_CHOICE_STRING);
		}
	}

	@Override
	public boolean shouldPlayDrawnCard(UnoGame game, UnoCard drawnCard, UnoPlayer next) {
		this.ps.println("You have drawn a " + drawnCard.toString() + ". Do you want to place it? [y/n]");
		return this.scanner.nextLine().equals("y");
	}

}
