package com.github.markozajc.juno.rules.impl.placement;

import static com.github.markozajc.juno.cards.UnoCardColor.WILD;
import static com.github.markozajc.juno.rules.types.UnoCardPlacementRule.PlacementClearance.*;

import com.github.markozajc.juno.cards.*;
import com.github.markozajc.juno.hands.UnoHand;
import com.github.markozajc.juno.rules.pack.UnoRulePack;
import com.github.markozajc.juno.rules.types.UnoCardPlacementRule;

/**
 * {@link UnoCardColor}-based rules for all {@link UnoCard}s.
 *
 * @author Marko Zajc
 */
public class ColorPlacementRules {

	private ColorPlacementRules() {}

	private static UnoRulePack pack;

	private static void createPack() {
		pack = new UnoRulePack(new ColorPlacementRule(), new WildColorPlacementRule());
	}

	/**
	 * A color-based placement rule that allows cards of the same color to be placed atop
	 * of each other and neutrals others.
	 *
	 * @author Marko Zajc
	 */
	public static class ColorPlacementRule implements UnoCardPlacementRule {

		@Override
		public PlacementClearance canBePlaced(UnoCard target, UnoCard card, UnoHand hand) {
			if (target.getColor() == card.getColor())
				return ALLOWED;

			return NEUTRAL;
		}

	}

	/**
	 * A color-based placement rule that allows a wild card to be placed atop of anything
	 * and neutrals others.
	 *
	 * @author Marko Zajc
	 */
	public static class WildColorPlacementRule implements UnoCardPlacementRule {

		@Override
		public PlacementClearance canBePlaced(UnoCard target, UnoCard card, UnoHand hand) {
			if (card.getOriginalColor() == WILD)
				return ALLOWED;

			return NEUTRAL;
		}

	}

	/**
	 * @return {@link UnoRulePack} of the official color placement rules.
	 */
	public static UnoRulePack getPack() {
		if (pack == null)
			createPack();

		return pack;
	}

}
