package com.github.markozajc.juno.game;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.markozajc.juno.cards.UnoCard;
import com.github.markozajc.juno.cards.UnoCardColor;
import com.github.markozajc.juno.cards.UnoStandardDeck;
import com.github.markozajc.juno.cards.impl.UnoActionCard;
import com.github.markozajc.juno.cards.impl.UnoDrawCard;
import com.github.markozajc.juno.cards.impl.UnoWildCard;
import com.github.markozajc.juno.hands.UnoHand;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules;
import com.github.markozajc.juno.utils.UnoRuleUtils;

/**
 * A {@link UnoGame} implementation that endorses the official UNO rules (see
 * "Specification" in the README for the details). This implementation works only
 * with the official set of {@link UnoCard}s. It also has a few events that you can
 * extend.
 *
 * @author Marko Zajc
 * @deprecated Only supports a hard-coded set of rules and does not utilize some of
 *             the newer utilities. Use {@link UnoControlledGame} instead.
 */
@Deprecated
public abstract class BasicUnoGame extends UnoGame {

	/**
	 * Called when a hand is placed to the discard pile.
	 *
	 * @param hand
	 *            the card's placer
	 * @param card
	 *            the {@link UnoCard}
	 */
	@SuppressWarnings("unused")
	protected void onCardPlaced(UnoHand hand, UnoCard card) {}

	/**
	 * Called when a hand changes the color (using a wild card).
	 *
	 * @param hand
	 *            the {@link UnoHand} that has changed the color
	 * @param newColor
	 *            the new {@link UnoCardColor}
	 */
	@SuppressWarnings("unused")
	protected void onColorChanged(UnoHand hand, UnoCardColor newColor) {}

	/**
	 * Called when the discard pile is merged and shuffled into the draw pile.
	 */
	protected void onPileShuffle() {}

	/**
	 * Called when a hand draws cards from the draw pile.
	 *
	 * @param hand
	 *            the {@link UnoHand} drawing the cards
	 * @param quantity
	 *            the quantity of drawn cards
	 */
	@SuppressWarnings("unused")
	protected void onDrawCards(UnoHand hand, int quantity) {}

	/**
	 * Called when a {@link UnoHand} tries to place an illegal {@link UnoCard}. This
	 * means that the card is either not compatible with the top card or the hand does
	 * not actually possess the card it tried to place.
	 *
	 * @param hand
	 *            the offending {@link UnoHand}
	 * @param card
	 *            the invalid {@link UnoCard}
	 */
	@SuppressWarnings("unused")
	protected void onInvalidCard(UnoHand hand, UnoCard card) {}

	/**
	 * Called when an invalid {@link UnoCardColor} request has been made. This means that
	 * a hand has returned {@link UnoCardColor#WILD} on
	 * {@link UnoHand#chooseColor(UnoGame)}. {@link UnoHand#chooseColor(UnoGame)}.
	 *
	 * @param hand
	 *            the offending {@link UnoHand}
	 * @param color
	 *            the invalid {@link UnoCardColor}
	 */
	@SuppressWarnings("unused")
	protected void onInvalidColor(UnoHand hand, UnoCardColor color) {}

	/**
	 * Creates a new {@link BasicUnoGame}
	 *
	 * @param first
	 *            the first {@link UnoPlayer}
	 * @param second
	 *            the second {@link UnoPlayer}
	 */
	public BasicUnoGame(@Nonnull UnoPlayer first, @Nonnull UnoPlayer second) {
		super(first, second, new UnoStandardDeck(), 7, UnoOfficialRules.getPack());
	}

	private void changeColor(UnoHand hand, UnoCard card) {
		if (!card.getColor().equals(UnoCardColor.WILD))
			return;

		UnoCardColor color = hand.chooseColor(this);
		while (color == UnoCardColor.WILD) {
			onInvalidColor(hand, color);
			color = hand.chooseColor(this);
		}
		onColorChanged(hand, color);

		card.setColorMask(color);
	}

	@SuppressWarnings("null")
	@Override
	protected void turn(UnoPlayer player) {
		start: while (true) {
			UnoCard topCard = this.getDiscard().getTop();
			boolean hasToDraw = topCard instanceof UnoDrawCard && !((UnoDrawCard) topCard).isPlayed();

			if (!hasToDraw)
				changeColor(player.getHand(), topCard);

			boolean drawn = false;
			while (true) {
				UnoCard card = player.getHand().playCard(this, nextPlayer(player));

				if (card == null && !drawn) {
					// In case hand throws nothing

					int draw = 1;
					if (hasToDraw) {
						draw = this.getDiscard().getConsecutiveDraw();
						this.getDiscard().markTopPlayed();
					}

					if (this.getDraw().getSize() < draw) {
						if (this.getDiscard().getSize() < draw /* minimum draw requirement */ + 1 /* the top card */)
							return;

						onPileShuffle();
						discardIntoDraw();
					}

					List<UnoCard> drawnCards = player.getHand().draw(this, draw);
					onDrawCards(player.getHand(), draw);
					if (drawnCards.size() == 1 && UnoRuleUtils
							.combinedPlacementAnalysis(topCard, drawnCards, this.getRules(), player.getHand())
							.size() == 1) {
						drawn = true;
						continue;
					}

					return;
				} else if (card == null && drawn) {
					return;
				}

				if (UnoRuleUtils
						.combinedPlacementAnalysis(topCard, Arrays.asList(card), this.getRules(), player.getHand())
						.isEmpty() || !player.getHand().getCards().remove(card)) {
					// Should be checked by Hand implementation in the first place
					onInvalidCard(player.getHand(), card);
					continue;
				}
				// Checks if the card is valid and exists

				onCardPlaced(player.getHand(), card);

				if (card instanceof UnoWildCard)
					changeColor(player.getHand(), card);

				this.getDiscard().add(card);

				if (card instanceof UnoActionCard && player.getHand().getSize() > 0)
					continue start;

				break;
			}

			break;
		}
	}

}
