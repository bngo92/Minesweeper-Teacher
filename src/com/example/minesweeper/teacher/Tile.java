package com.example.minesweeper.teacher;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Tile {
	private int mX;
	private int mY;
	
	private int mMines;
	private Resources mRes;
	private Bitmap mBitmap;
	private boolean mRevealed;
	
	public Tile(Resources res, int x, int y) {
		mRes = res;
		mX = x;
		mY = y;
		reset();
	}
	
	public void reset() {
		mBitmap = BitmapFactory.decodeResource(mRes, R.drawable.mined);
		mRevealed = false;
		mMines = 0;
	}
	
	public void doDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, mX, mY, null);
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
	
	public void setMine(int mines) {
		mMines = mines;
	}
	
	public boolean reveal() {
		mRevealed = true;
		int id;
		switch (mMines) {
		case 0:
			id = R.drawable.mine0;
			break;
		case 1:
			id = R.drawable.mine1;
			break;
		case 2:
			id = R.drawable.mine2;
			break;
		case 3:
			id = R.drawable.mine3;
			break;
		case 4:
			id = R.drawable.mine4;
			break;
		case 5:
			id = R.drawable.mine5;
			break;
		case 6:
			id = R.drawable.mine6;
			break;
		case 7:
			id = R.drawable.mine7;
			break;
		case 8:
			id = R.drawable.mine8;
			break;
		default:
			id = R.drawable.mine;
			break;
		}
		mBitmap = BitmapFactory.decodeResource(mRes, id);
		if (mMines == -1)
			return false;
		return true;
	}

}
