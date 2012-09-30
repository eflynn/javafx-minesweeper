package org.foobar.minesweeper.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public enum SquareType {
	BLANK, FLAG, MINE, HITMINE, WRONGMINE, ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(
			4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8);

	private final int mineCount;

	private SquareType(int mineCount) {
		this.mineCount = mineCount;
	}

	private SquareType() {
		this(-1);
	}

	public boolean isRevealable() {
		return this == BLANK;
	}

	public boolean hasMineCount() {
		return mineCount != -1;
	}

	public int getMineCount() {
		checkState(hasMineCount());

		return mineCount;
	}

	/**
	 * Returns default value if not a member.
	 * 
	 * @param defaultValue
	 * @return
	 */
	public int getMineCountOr(int defaultValue) {
		return hasMineCount() ? mineCount : defaultValue;
	}

	public static SquareType of(int numMines) {
		checkArgument(numMines >= 0 && numMines <= 8,
				"numMines (%d) is out of bounds", numMines);

		return SquareType.values()[ZERO.ordinal() + numMines];
	}
}
