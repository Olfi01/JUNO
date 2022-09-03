package com.github.markozajc.juno.players.impl;

import static com.github.markozajc.juno.cards.UnoCardColor.WILD;
import static com.github.markozajc.juno.rules.pack.impl.UnoOfficialRules.UnoHouseRule.*;
import static com.github.markozajc.juno.utils.UnoRuleUtils.combinedPlacementAnalysis;
import static com.github.markozajc.juno.utils.UnoUtils.*;

import java.util.*;
import java.util.Map.Entry;

import javax.annotation.*;

import com.github.markozajc.juno.cards.*;
import com.github.markozajc.juno.cards.impl.*;
import com.github.markozajc.juno.game.UnoGame;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.utils.UnoUtils;

/**
 * An automated hand that uses strategic logic to decide cards and colors to return.
 * Is suitable for production so you may use it as a "CPU" opponent in your code.
 *
 * @author Marko Zajc
 */
public class UnoStrategicPlayer extends UnoPlayer {

	/**
	 * Creates a new {@link UnoStrategicPlayer}.
	 *
	 * @param name
	 *            this player's name
	 */
	public UnoStrategicPlayer(@Nonnull String name) {
		super(name);
	}

	private static final int DRAW_CARD_THRESHOLD = 3;

	@Nullable
	private static List<UnoNumericCard> sevenoFilter(List<UnoCard> cards) {
		var applicable = new ArrayList<>(filterKind(UnoNumericCard.class, cards));
		applicable.removeIf(r -> r.getNumber() == 0 || r.getNumber() == 7);
		return applicable;
	}

	@Nullable
	private static UnoNumericCard sevenoStrategy(List<UnoCard> possiblePlacements,
												 List<Entry<Long, UnoCardColor>> colorAnalysis, UnoPlayer us,
												 UnoPlayer foe) {
		if (us.getHand().getSize() - 1 <= foe.getHand().getSize())
			return null;

		return chooseBestColorCard(sevenoFilter(possiblePlacements), colorAnalysis);
	}

	@Nullable
	private static UnoDrawCard chooseDrawCard(List<UnoCard> possiblePlacements,
											  List<Entry<Long, UnoCardColor>> colorAnalysis, UnoGame game,
											  UnoPlayer next) {

		boolean shouldPlay = false;
		if (game.getHouseRules().contains(PROGRESSIVE)) {
			// Progressive UNO is enabled
			shouldPlay = game.getTopCard() instanceof UnoDrawCard || next.getHand().getSize() <= DRAW_CARD_THRESHOLD;

			// Only place if "attacked" or if opponent's amount of card is smaller than the
			// threshold

		} else {
			shouldPlay = true;
			// Always play, there's nothing to worry about
		}

		if (!shouldPlay)
			return null;

		return simpleChooseCard(possiblePlacements, colorAnalysis, UnoDrawCard.class);
	}

	@Nullable
	private static <T extends UnoCard> T simpleChooseCard(List<UnoCard> possiblePlacements,
														  List<Entry<Long, UnoCardColor>> colorAnalysis,
														  Class<T> type) {
		return chooseBestColorCard(filterKind(type, possiblePlacements), colorAnalysis);
	}

	/**
	 * Does some strategic thinking; uses the analysis of this hand's cards and tries to
	 * place the card that has the color of most cards. If, for example, the hand has two
	 * blue and one red card, this will prefer the blue cards.
	 *
	 * @param <T>
	 *            type of the {@link UnoCard} to return
	 * @param possiblePlacements
	 *            all possible cards
	 * @param colorAnalysis
	 *            color analysis of the entire hand
	 *            ({@link UnoUtils#analyzeColors(List)})
	 * @param cardType
	 *
	 * @return the best possible card or {@code null} if there are no cards of the
	 *         requested kind
	 */
	@Nullable
	private static <T extends UnoCard> T chooseBestColorCard(List<T> possiblePlacements,
															 List<Entry<Long, UnoCardColor>> colorAnalysis) {
		if (possiblePlacements.isEmpty())
			return null;
		// In case there's no card of the requested kind

		for (Entry<Long, UnoCardColor> color : colorAnalysis) {
			if (color.getValue() == WILD)
				continue;
			// Skips the wild cards because it might be a good idea to save them for later

			List<T> matches = getColorCards(color.getValue(), possiblePlacements);
			// Gets the cards of

			if (!matches.isEmpty())
				return matches.get(0);
		}

		return possiblePlacements.get(0);
		// Fallback method
	}

	@SuppressWarnings("null")
	@Override
	public UnoCard playCard(UnoGame game, UnoPlayer next) {
		UnoCard top = game.getDiscard().getTop();
		List<UnoCard> possible = combinedPlacementAnalysis(top, this.getCards(), game.getRules(), this.getHand());

		if (possible.isEmpty())
			return null;
		// Draws a card if no other option is possible

		var colorAnalysis = analyzeColors(getCards());
		// Analyzes the colors
		if (game.getHouseRules().contains(SEVENO)) {
			UnoNumericCard sevenoCard = sevenoStrategy(possible, colorAnalysis, this, next);
			if (sevenoCard != null)
				return sevenoCard;
		}

		var drawCard = chooseDrawCard(possible, colorAnalysis, game, next);
		if (drawCard != null)
			return drawCard;
		// Places a draw card if necessary

		if (game.getPlayers().size() == 2) {
			var skipCard = simpleChooseCard(possible, colorAnalysis, UnoSkipCard.class);
			if (skipCard != null)
				return skipCard;

			var reverseCard = simpleChooseCard(possible, colorAnalysis, UnoReverseCard.class);
			if (reverseCard != null)
				return skipCard;
		}
		// Places an action card (skip or reverse) if there are two players

		List<UnoCard> possibleNumeric;
		if (game.getHouseRules().contains(SEVENO) && this.getHand().getSize() - 1 >= next.getHand().getSize()) {
			possibleNumeric = new ArrayList<>(possible);
			possibleNumeric.removeAll(sevenoFilter(possible));
		} else {
			possibleNumeric = possible;
		}
		// Remove the sevens and zeros from the list of possible cards (numeric card chooser)
		// if we do not want to place them

		var numericCard = simpleChooseCard(possibleNumeric, colorAnalysis, UnoNumericCard.class);
		if (numericCard != null)
			return numericCard;
		// Places a numeric card if possible

		var wildCard = filterKind(UnoWildCard.class, possible).stream().findFirst().orElse(null);
		if (wildCard != null)
			return wildCard;
		// places a Wild card if available

		return possible.get(0);
		// Places the first possible card in case none of the above choosers manage to choose
		// a viable card
	}

	@SuppressWarnings("null")
	@Override
	public UnoCardColor chooseColor(UnoGame game) {
		return analyzeColors(this.getCards()).stream()
			.filter(p -> p.getValue() != WILD)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Couldn't choose a color (UnoUtils malfunctioned!)"))
			.getValue();
	}

	@Override
	public boolean shouldPlayDrawnCard(UnoGame game, UnoCard drawnCard, UnoPlayer next) {
		return Objects.equals(playCard(game, next), drawnCard);
	}

}
