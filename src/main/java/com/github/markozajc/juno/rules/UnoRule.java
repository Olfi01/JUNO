package com.github.markozajc.juno.rules;

/**
 * A meta-interface for UNO rules.
 *
 * @author Marko Zajc
 */
public interface UnoRule {

	public enum ConflictResolution {

		FAIL,
		REPLACE,
		BACKOFF

	}

}
