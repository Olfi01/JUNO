package com.github.markozajc.juno.rules.impl.flow;

import static com.github.markozajc.juno.cards.UnoCardColor.WILD;
import static com.github.markozajc.juno.rules.types.flow.UnoInitializationConclusion.NOTHING;

import com.github.markozajc.juno.cards.*;
import com.github.markozajc.juno.game.UnoGame;
import com.github.markozajc.juno.players.UnoPlayer;
import com.github.markozajc.juno.rules.types.UnoGameFlowRule;
import com.github.markozajc.juno.rules.types.flow.UnoInitializationConclusion;

/**
 * The game flow rule responsible for letting hands change the color of wild
 * {@link UnoCard}s.
 *
 * @author Marko Zajc
 */
public class ColorChoosingRule implements UnoGameFlowRule {

	private static final String INVALID_COLOR = "%s tries to set an invalid color.";
	private static final String COLOR_CHANGED = "%s sets the color to %s.";

	@Override
	public UnoInitializationConclusion initializationPhase(UnoPlayer player, UnoGame game) {
		if (game.getTopCard() != null && game.getTopCard().getColor() == WILD && !game.getTopCard().isOpen()) {
			UnoCardColor color = game.getTopCard().getPlacer().chooseColor(game);

			if (color == WILD) {
				game.onEvent(INVALID_COLOR, game.getTopCard().getPlacer().getName());
				return new UnoInitializationConclusion(true, false);
			}

			game.getTopCard().setColorMask(color);
			game.onEvent(COLOR_CHANGED, game.getTopCard().getPlacer().getName(), color.toString());
		}

		return NOTHING;
	}

}
