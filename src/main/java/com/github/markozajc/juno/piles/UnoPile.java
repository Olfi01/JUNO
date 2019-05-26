package com.github.markozajc.juno.piles;

import java.util.List;

import com.github.markozajc.juno.cards.UnoCard;
import com.github.markozajc.juno.hands.UnoHand;
import com.github.markozajc.juno.piles.impl.UnoDiscardPile;
import com.github.markozajc.juno.piles.impl.UnoDrawPile;

/**
 * A interface representing a pile of cards. Normally a UNO games has two regular
 * piles ({@link UnoDrawPile} and {@link UnoDiscardPile}) and {@link UnoHand}s, which
 * also implement this.
 *
 * @author Marko Zajc
 */
public interface UnoPile {

	/**
	 * @return either a modifiable clone or an unmodifiable list of this pile's cards
	 */
	public List<UnoCard> getCards();

	/**
	 * @return this pile's size
	 */
	public int getSize();

}
