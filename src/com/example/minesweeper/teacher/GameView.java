package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private GameThread mThread;
	//private int mX;
	//private int mY;
	Bitmap mBitmap;
	
	int width = 9;
	int height = 9;
	int mines = 10;
	int size = 48;
	int count;
	int empty;
	private ArrayList<ArrayList<Tile>> mGrid;
	
	public GameView(Context context) {
		super(context);
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.my_image);
		getHolder().addCallback(this);
		mThread = new GameThread(this);
		
		newGame();
	}
	
	public void newGame() {
		mGrid = new ArrayList<ArrayList<Tile>>(width);
		for (int i = 0; i < width; i++) {
			mGrid.add(new ArrayList<Tile>(height));
			for (int j = 0; j < height; j++)
				mGrid.get(i).add(new Tile(getResources(), i*size, j*size));
		}
		
		Random random = new Random();
		int nMines = 0;
		while (nMines < mines) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			if (!mGrid.get(x).get(y).isMine()) {
				mGrid.get(x).get(y).setMine(-1);
				nMines++;
			}	
		}
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (mGrid.get(i).get(j).isMine())
					continue;
				nMines = 0;
				for (int ii = Math.max(i-1, 0); ii < Math.min(i+2, width); ii++)
					for (int jj = Math.max(j-1, 0); jj < Math.min(j+2, height); jj++)
						if (mGrid.get(ii).get(jj).isMine())
							nMines++;
				mGrid.get(i).get(j).setMine(nMines);
			}
		}
		
		count = 0;
		empty = width*height - count;
	}
	
	public void doDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		for (ArrayList<Tile> arrayList : mGrid)
			for (Tile tile : arrayList)
				tile.doDraw(canvas);
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	public void surfaceCreated(SurfaceHolder arg0) {
		if (!mThread.isAlive()) {
			mThread = new GameThread(this);
			mThread.setRunning(true);
			mThread.start();
		}
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (mThread.isAlive()) {
			if (mThread.isAlive()) {
				mThread.setRunning(false);
			}
		}
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX() / size;
		int y = (int) event.getY() / size;
		if (!mGrid.get(x).get(y).isRevealed() && mGrid.get(x).get(y).isZero())
			revealZero(x, y);
		if (mGrid.get(x).get(y).reveal()) {
			count++;
			if (count == empty)
				newGame();
		}
		else
			newGame();
			
		return super.onTouchEvent(event);
	}
	
	public void revealZero(int x, int y) {
		mGrid.get(x).get(y).reveal();
		for (int i = Math.max(x-1, 0); i < Math.min(x+2, width); i++)
			for (int j = Math.max(y-1, 0); j < Math.min(y+2, width); j++) {
				if (!mGrid.get(i).get(j).isRevealed() && mGrid.get(i).get(j).isZero())
					revealZero(i, j);
				else
					mGrid.get(i).get(j).reveal();
			}
	}
}
