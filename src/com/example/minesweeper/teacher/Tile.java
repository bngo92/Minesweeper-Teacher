package com.example.minesweeper.teacher;

public class Tile {
	private int mR;
	private int mC;
	private int mMines;
	private boolean mRevealed;
	private boolean mFlagged;
	
	public enum State { NONE, REVEAL, REVEAL_SURROUNDING, FLAG, EXPLODE };
	private State mState;
	
	public Tile(int r, int c) {
		mR = r;
		mC = c;
		reset();
	}
	
	public void reset() {
		mRevealed = false;
		mFlagged = false;
		mMines = 0;
		mState = State.NONE;
	}
	
	public Pair<Integer, Integer> getCoords() {
		return new Pair<Integer, Integer>(mR, mC);
	}
	
	public int getMines() {
		return mMines;
	}
	
	public State getState() {
		return mState;
	}
	
	public void setState(State state) {
		mState = state;
	}
	
	public void setMine() {
		mMines = -1;
	}
	
	public void setMines(int mines) {
		mMines = mines;
	}
	
	public void setFlag(boolean b) {
		mFlagged = b;
	}
	
	public int toggleFlag() {
		if (mRevealed)
			return 0;
		mFlagged = !mFlagged;
		if (mFlagged)
			return 1;
		return -1;
	}
	
	public void reveal() {
		mRevealed = true;
	}

	public boolean isMine() {
		if (mMines == -1)
			return true;
		return false;
	}
	
	public boolean isZero() {
		if (mMines == 0)
			return true;
		return false;
	}
	
	public boolean isRevealed() {
		return mRevealed;
	}
	
	public boolean isFlagged() {
		return mFlagged;
	}
	
}
