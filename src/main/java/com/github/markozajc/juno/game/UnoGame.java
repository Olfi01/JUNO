package com.github.markozajc.juno.game;

import java.io.PrintStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.github.markozajc.juno.cards.UnoCard;
import com.github.markozajc.juno.decks.UnoDeck;
import com.github.markozajc.juno.hands.UnoHand;
import com.github.markozajc.juno.piles.impl.UnoDiscardPile;
import com.github.markozajc.juno.piles.impl.UnoDrawPile;
import com.github.markozajc.juno.rules.UnoRule;
import com.github.markozajc.juno.rules.pack.UnoRulePack;

/**
 * A class representing a game of UNO. {@link UnoGame} is the thing that controls the
 * flow and actions that take place in a round of UNO. A {@link UnoGame} utilizes
 * {@link UnoRule}s to do stuff and an implementation of this that does it is
 * {@link UnoControlledGame}.
 *
 * @author Marko Zajc
 */
public abstract class UnoGame {

	/**
	 * A list of players in an UNO game.
	 *
	 * @author Marko Zajc
	 */
	public enum UnoPlayer {
		/**
		 * The first player. This player gets to place a card first.
		 */
		PLAYER1,
		/**
		 * The second player.
		 */
		PLAYER2,
		/**
		 * Nobody. This is used to signal a tie by {@link UnoGame#playGame()}.
		 */
		NOBODY;
	}

	/**
	 * First player's hand.
	 */
	@Nonnull
	public final UnoHand playerOneHand;
	/**
	 * Second player's hand.
	 */
	@Nonnull
	public final UnoHand playerTwoHand;

	private final int cardAmount;

	private UnoDrawPile draw;

	@Nonnull
	private final UnoDiscardPile discard = new UnoDiscardPile();

	@Nonnull
	private final UnoRulePack rules;

	@Nonnull
	private final UnoDeck deck;

	private UnoCard topCard;

	/**
	 * Returns the other {@link UnoPlayer}.
	 *
	 * @param player
	 *            the {@link UnoPlayer} to reverse
	 * @return the other {@link UnoPlayer}
	 */
	@Nonnull
	private static UnoPlayer reversePlayer(UnoPlayer player) {
		return player.equals(UnoPlayer.PLAYER1) ? UnoPlayer.PLAYER2 : UnoPlayer.PLAYER1;
	}

	/**
	 * Returns hand belonging to the given {@link UnoPlayer}.
	 *
	 * @param player
	 *            the {@link UnoPlayer}
	 * @param playerOneHand
	 *            player one's hand
	 * @param playerTwoHand
	 *            player two's hand
	 * @return {@code player}'s hand
	 */
	@SuppressWarnings("null")
	@Nonnull
	private static UnoHand decideHand(UnoPlayer player, UnoHand playerOneHand, UnoHand playerTwoHand) {
		return player.equals(UnoPlayer.PLAYER1) ? playerOneHand : playerTwoHand;
	}

	/**
	 * Creates a new UNO game.
	 *
	 * @param playerOneHand
	 *            first player's hand
	 * @param playerTwoHand
	 *            second player's hand
	 * @param deck
	 *            the {@link UnoDeck} to use
	 * @param cardAmount
	 *            the amount of card each player gets initially
	 * @param rules
	 *            the {@link UnoRulePack} for this {@link UnoGame}
	 */
	public UnoGame(@Nonnull UnoHand playerOneHand, @Nonnull UnoHand playerTwoHand, @Nonnull UnoDeck deck,
			@Nonnegative int cardAmount, @Nonnull UnoRulePack rules) {
		this.playerOneHand = playerOneHand;
		this.playerTwoHand = playerTwoHand;
		this.deck = deck;
		this.cardAmount = cardAmount;
		this.rules = rules;
	}

	private void init() {
		this.draw = new UnoDrawPile(this.deck);
		// Creates the draw pile

		this.discard.clear();
		this.playerOneHand.clear();
		this.playerTwoHand.clear();
		// Clears every other pile

		this.discard.add(this.draw.drawInitalCard());
		// Draws the initial card

		this.playerOneHand.draw(this, this.cardAmount);
		this.playerTwoHand.draw(this, this.cardAmount);
		// Deals the cards
	}

	/**
	 * Updates the {@link UnoCard} returned by {@link #getTopCard()}.
	 */
	private void updateTopCard() {
		this.topCard = this.discard.getTop();
	}

	/**
	 * Merges the discard pile into the draw pile. This is to be called by the
	 * implementation when the draw pile gets empty.
	 */
	public void discardIntoDraw() {
		this.draw.mergeResetShuffle(this.discard.createDrawPile());
	}

	/**
	 * Plays a hand. This method should get a card to place from a hand
	 *
	 * @param hand
	 */
	protected abstract void playHand(@Nonnull UnoHand hand);

	/**
	 * Checks whether a hand has won. Criteria for winning in UNO is
	 * <ul>
	 * <li>Having no more cards ({@link UnoHand#getSize()} {@code == 0})
	 * <li>The top {@link UnoCard} being played ({@link UnoCard#isPlayed()})
	 * </ul>
	 *
	 * @param hand
	 * @param discard
	 * @return
	 */
	private static boolean checkVictory(UnoHand hand, UnoDiscardPile discard) {
		return hand.getSize() == 0 && discard.getTop().isPlayed();
	}

	/**
	 * Used as a last resort case when both the {@link UnoDiscardPile} and the
	 * {@link UnoDrawPile} are empty. Either indicates a catastrophic failure in the card
	 * economy (the least likely), that the {@link UnoGame} implementation handled the
	 * two piles improperly (unlikely), that both of the {@link UnoHand}s malfunctioned
	 * (unlikely) or that both {@link UnoHand}s were intentionally made to just draw
	 * cards (the most likely).
	 *
	 * @return the fallback winner
	 */
	private final UnoPlayer fallbackVictory() {
		if (this.playerOneHand.getSize() < this.playerTwoHand.getSize()) {
			return UnoPlayer.PLAYER1;
			// P1 has less cards

		} else if (this.playerTwoHand.getSize() < this.playerOneHand.getSize()) {
			return UnoPlayer.PLAYER2;
			// P2 has less cards
		} else {
			return UnoPlayer.NOBODY;
			// P1 and P2 have the same amount of cards (tie)
		}
	}

	/**
	 * Plays a game of UNO.
	 *
	 * @return the winner {@link UnoPlayer}
	 */
	@Nonnull
	public UnoPlayer playGame() {
		init();
		// Initiates game

		UnoPlayer winner = null;
		for (UnoPlayer player = UnoPlayer.PLAYER1; winner == null; player = reversePlayer(player)) {
			UnoPlayer reversePlayer = reversePlayer(player);
			// Gets the other player

			winner = playAndCheckHand(decideHand(player, this.playerOneHand, this.playerTwoHand), player,
				decideHand(reversePlayer, this.playerOneHand, this.playerTwoHand), reversePlayer);
			// Plays the hand and checks both

			if (this.discard.getSize() <= 1 && this.draw.getSize() == 0)
				winner = fallbackVictory();
			// Fallback method used in the case of both piles getting empty. Do note that the
			// game can not continue at this point so a winner
			// must be chosen.
		}
		// Iterates over all players until a winner is declared

		return winner;
	}

	/**
	 * Plays a hand and check whether either of the {@link UnoPlayer}s has won.
	 *
	 * @param playerHand
	 *            player's hand
	 * @param foeHand
	 *            foe's hand
	 * @param player
	 *            the {@link UnoPlayer} that owns the {@code playerHand}
	 * @param foe
	 *            the {@link UnoPlayer} that owns the {@code foeHand}
	 * @return the victor or {@code null} if nobody has won yet
	 */
	private UnoPlayer playAndCheckHand(@Nonnull UnoHand playerHand, @Nonnull UnoPlayer player, @Nonnull UnoHand foeHand, @Nonnull UnoPlayer foe) {
		updateTopCard();
		// Updates the top card

		playHand(playerHand);
		// Plays player's hand

		if (checkVictory(playerHand, this.discard))
			return player;
		// Checks whether whether player has won

		if (checkVictory(foeHand, this.discard))
			return foe;
		// Checks whether whether the foe has won (only ran if the top card was a draw card
		// and player
		// didn't defend)

		return null;
	}

	/**
	 * @return the {@link UnoRulePack} in use by this {@link UnoGame}
	 */
	@Nonnull
	public UnoRulePack getRules() {
		return this.rules;
	}

	/**
	 * The top {@link UnoCard} of the discard pile. This is the preferred method of
	 * obtaining the top {@link UnoCard} as it fetches and stores the top card at the end
	 * of each turn, thus only having to actually get it from the {@link UnoDiscardPile}
	 * pile once, reducing response time a little.
	 *
	 * @return the {@link UnoCard} that's on top of the discard pile
	 */
	public UnoCard getTopCard() {
		return this.topCard;
	}

	/**
	 * Called when an event (such as a card being placed, the color of the top card being
	 * changed, etc) occurs. The given format can be formatted with
	 * {@link String#format(String, Object...)} or
	 * {@link PrintStream#printf(String, Object...)} (or a similar method).
	 *
	 * @param format
	 *            the format of the message
	 * @param arguments
	 *            arguments for the format
	 */
	public abstract void onEvent(String format, Object... arguments);

	/**
	 * Returns the draw pile. This is where cards are drawn from by hands. The discard
	 * pile is shuffled and merged into this when this gets empty.
	 *
	 * @return the {@link UnoDrawPile}
	 */
	@Nonnull
	public UnoDrawPile getDraw() {
		UnoDrawPile drawPile = this.draw;
		if (drawPile == null)
			throw new IllegalStateException(
					"The draw pile is null - please play at least one game to initialize the piles.");

		return drawPile;
	}

	/**
	 * Returns the discard pile. This is where hands place their cards.
	 *
	 * @return the {@link UnoDiscardPile}
	 */
	@Nonnull
	public UnoDiscardPile getDiscard() {
		return this.discard;
	}

	/**
	 * Returns the deck in use. The deck is only used during the draw pile initialization
	 * and always remains the same.
	 *
	 * @return the {@link UnoDeck}
	 */
	@Nonnull
	public UnoDeck getDeck() {
		return this.deck;
	}

}
