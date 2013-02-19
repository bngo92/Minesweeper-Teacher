package com.example.minesweeper.teacher;

/**
 * Tile Object
 * 
 * <p>
 * Holds various states of a tile
 * 
 * @author Bryan
 * 
 */
public class Tile {
	private int mR;
	private int mC;
	private int mMines;
	private boolean mRevealed;
	private boolean mFlagged;
	private boolean mExploded;
	private boolean mHighlighted;

	/**
	 * Constructor.
	 * 
	 * @param r
	 *            (required) row of tile
	 * @param c
	 *            (required) column of tile
	 */
	public Tile(int r, int c) {
		mR = r;
		mC = c;
		reset();
	}

	/** Reset states to default. */
	public void reset() {
		mRevealed = false;
		mFlagged = false;
		mExploded = false;
		mHighlighted = false;
		mMines = 0;
	}

	/** Return row and column coordinates. */
	public Pair<Integer, Integer> getCoords() {
		return new Pair<Integer, Integer>(mR, mC);
	}

	/** Return count of surrounding mines */
	public int getMines() {
		return mMines;
	}

	/** Return if mine was clicked */
	public boolean isExploded() {
		return mExploded;
	}

	/** Explode mine. */
	public void explode() {
		mExploded = true;
	}

	/** Set mine underneath tile. */
	public void setMine() {
		mMines = -1;
	}

	/** Update number of surrounding mines. */
	public void updateMines(int mines) {
		mMines = mines;
	}

	/**
	 * Toggles flag if tile has not been revealed yet.
	 * 
	 * @return 0 if flag can't be toggled, 1 if tile is now flagged, -1 if tile
	 *         is now unflagged
	 */
	public int toggleFlag() {
		if (mRevealed)
			return 0;
		mFlagged = !mFlagged;
		if (mFlagged)
			return 1;
		return -1;
	}

	/**
	 * Reveals tile
	 * 
	 * @return 0 if already revealed, 1 if newly revealed
	 */
	public int reveal() {
		if (mRevealed)
			return 0;
		mRevealed = true;
		return 1;
	}

	/**
	 * 
	 * @return if tile contains a mine
	 */
	public boolean isMine() {
		if (mMines == -1)
			return true;
		return false;
	}

	/**
	 * 
	 * @return if tile is not surrounded by any mines
	 */
	public boolean isZero() {
		if (mMines == 0)
			return true;
		return false;
	}

	/**
	 * 
	 * @return if tile has been revealed
	 */
	public boolean isRevealed() {
		return mRevealed;
	}

	/**
	 * 
	 * @return if tile has been flagged
	 */
	public boolean isFlagged() {
		return mFlagged;
	}

	/**
	 * 
	 * @return if tile has been highlighted
	 */
	public boolean isHighlighted() {
		return mHighlighted;
	}

	public void setHighlighted(boolean highlighted) {
		mHighlighted = highlighted;
	}

}
