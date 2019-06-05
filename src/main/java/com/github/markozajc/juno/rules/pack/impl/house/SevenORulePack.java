package com.github.markozajc.juno.rules.pack.impl.house;

import com.github.markozajc.juno.cards.impl.UnoNumericCard;
import com.github.markozajc.juno.game.UnoGame;
import com.github.markozajc.juno.hands.UnoHand;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.rules.types.UnoGameFlowRule;
import com.github.markozajc.juno.rules.types.flow.UnoInitializationConclusion;

public class SevenORulePack {

	public class HandSwappingRule implements UnoGameFlowRule {

		@Override
		public UnoInitializationConclusion initializationPhase(UnoPlayer player, UnoGame game) {
			if (game.getTopCard() instanceof UnoNumericCard && (((UnoNumericCard) game.getTopCard()).getNumber() == 0
					|| ((UnoNumericCard) game.getTopCard()).getNumber() == 7)) {
				// If the top card is a numeric card with a seven or a zero

				UnoPlayer foe = game.nextPlayer(player);
				UnoHand playerHand = player.getHand();
				UnoHand foeHand = foe.getHand();
				player.setHand(foeHand);
				foe.setHand(playerHand);
				// Swap hands

				game.onEvent("Swapped cards", (Object[]) null);
			}

			return UnoInitializationConclusion.NOTHING;
		}

	}

}
