package com.example.minesweeper.teacher;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Tile {
	private Resources mRes;
	private Bitmap mBitmap;
	
	private int mMines;
	private boolean mRevealed;
	private boolean mFlagged;
	
	public Tile(Resources res) {
		mRes = res;
		reset();
	}
	
	public void reset() {
		mRevealed = false;
		mFlagged = false;
		mMines = 0;
		updateBitmap();
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
		updateBitmap();
	}
	
	public void reveal() {
		mRevealed = true;
		updateBitmap();
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
	
	public Bitmap getBitmap() {
		return mBitmap;
	}
	
	public void updateBitmap() {
		int id;
		if (!mRevealed) {
			if (mFlagged)
				id = R.drawable.minef;
			else
				id = R.drawable.mined;
		}
		else
			switch (mMines) {
			case 0: id = R.drawable.mine0; break;
			case 1: id = R.drawable.mine1; break;
			case 2: id = R.drawable.mine2; break;
			case 3: id = R.drawable.mine3; break;
			case 4: id = R.drawable.mine4; break;
			case 5: id = R.drawable.mine5; break;
			case 6: id = R.drawable.mine6; break;
			case 7: id = R.drawable.mine7; break;
			case 8: id = R.drawable.mine8; break;
			default: id = R.drawable.mine; break;
			}
		mBitmap = BitmapFactory.decodeResource(mRes, id);
	}
	
}
