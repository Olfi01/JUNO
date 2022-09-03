package com.github.markozajc.juno.game;

import static com.github.markozajc.juno.utils.UnoRuleUtils.filterRuleKind;

import java.util.List;

import javax.annotation.*;

import com.github.markozajc.juno.cards.UnoCard;
import com.github.markozajc.juno.decks.UnoDeck;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.rules.UnoRule;
import com.github.markozajc.juno.rules.pack.UnoRulePack;
import com.github.markozajc.juno.rules.types.UnoGameFlowRule;

/**
 * An implementation of {@link UnoGame} that lets you control most of the things with
 * {@link UnoRule}s.
 *
 * @author Marko Zajc
 */
public abstract class UnoControlledGame extends UnoGame {

	/**
	 * Creates a new {@link UnoControlledGame}.
	 *
	 * @param first
	 *            the first {@link UnoPlayer}
	 * @param second
	 *            the second {@link UnoPlayer}
	 * @param unoDeck
	 *            the {@link UnoDeck} to use
	 * @param cardAmount
	 *            the amount of card each player gets initially
	 * @param rules
	 *            the {@link UnoRulePack} for this {@link UnoGame}
	 */
	@Deprecated
	protected UnoControlledGame(@Nonnull UnoPlayer first, @Nonnull UnoPlayer second, @Nonnull UnoDeck unoDeck,
								@Nonnegative int cardAmount, @Nonnull UnoRulePack rules) {
		super(first, second, unoDeck, cardAmount, rules);
	}

	/**
	 * Creates a new {@link UnoControlledGame}.
	 *
	 * @param unoDeck
	 *            the {@link UnoDeck} to use
	 * @param cardAmount
	 *            the amount of card each player gets initially
	 * @param rules
	 *            the {@link UnoRulePack} for this {@link UnoGame}
	 * @param players
	 * 			  the {@link UnoPlayer}s for this {@link UnoGame}
	 */
	protected UnoControlledGame(@Nonnull UnoDeck unoDeck,
								@Nonnegative int cardAmount, @Nonnull UnoRulePack rules, UnoPlayer... players) {
		super(unoDeck, cardAmount, rules, players);
	}

	@Override
	protected void turn(@Nonnull UnoPlayer player) {
		List<UnoGameFlowRule> rules = filterRuleKind(this.getRules().getRules(), UnoGameFlowRule.class);

		boolean skip = initializationPhase(player, this, rules);

		if (!skip)
			decisionPhase(player, this, rules);

	}

	private static boolean initializationPhase(@Nonnull UnoPlayer player, @Nonnull UnoGame game,
											   @Nonnull List<UnoGameFlowRule> rules) {
		boolean repeat = true;
		boolean loseATurn = false;
		while (repeat) {
			repeat = false;

			for (UnoGameFlowRule rule : rules) {
				var result = rule.initializationPhase(player, game);
				if (result.shouldRepeat())
					repeat = true;

				if (result.shouldLoseATurn())
					loseATurn = true;
			}
		}

		return loseATurn;
	}

	private static void decisionPhase(@Nonnull UnoPlayer player, @Nonnull UnoGame game,
									  @Nonnull List<UnoGameFlowRule> rules) {
		boolean repeatDecision = true;
		while (repeatDecision) {
			repeatDecision = false;

			UnoCard decision = player.playCard(game, game.getNextPlayer(player));

			if (game.isEndRequested())
				return;
			// No need to continue

			for (UnoGameFlowRule rule : rules) {
				var result = rule.decisionPhase(player, game, decision);
				if (result.shouldRepeat())
					repeatDecision = true;
				if (result.shouldReverseDirection())
					game.reverseDirection();
			}

		}
	}

}
