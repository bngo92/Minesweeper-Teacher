package com.example.minesweeper.teacher;


public class Tile {
	private int mMines;
	private boolean mRevealed;
	private boolean mFlagged;
	
	public Tile() {
		reset();
	}
	
	public void reset() {
		mRevealed = false;
		mFlagged = false;
		mMines = 0;
	}
	
	public int getMines() {
		return mMines;
	}
	
	public void setMine() {
		mMines = -1;
	}
	
	public void setMines(int mines) {
		mMines = mines;
	}
	
	public void toggleFlag() {
		if (mRevealed)
			return;
		mFlagged = !mFlagged;
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
