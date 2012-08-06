package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private GameThread mThread;

	private ArrayList<ArrayList<Tile>> mGrid;	
	private ArrayList<Tile> mMines;
	
	private int width = 9;
	private int height = 9;
	private int mines = 10;
	private int size;
	private Random rand;
	private int maxX;
	int maxY;
	
	private int mCount;
	private boolean mStart;
	private boolean mActive;
	private long mStartTime;
	
	TextView tvtime;
	TextView tvcount;
	
	public GameView(Context context) {
		super(context);
		initGame();
	}	
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGame();
	}
	
	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGame();
	}
	
	public int getCount() {
		return mCount;
	}
	
	public void setTextView(TextView time, TextView count) {
		tvtime = time;
		tvcount = count;
	}
	
	public void updateCount() {
		tvcount.setText("" + mCount);
	}

	public void initGame() {
		getHolder().addCallback(this);
		mThread = new GameThread(this);
		
		mGrid = new ArrayList<ArrayList<Tile>>(width);
		mMines = new ArrayList<Tile>(mines);

		size = BitmapFactory.decodeResource(getResources(), R.drawable.mined).getWidth();
		maxX = width;
		maxY = height;
		rand = new Random();
		
		for (int i = 0; i < width; i++) {
			mGrid.add(new ArrayList<Tile>(height));
			for (int j = 0; j < height; j++)
				mGrid.get(i).add(new Tile(getResources(), i*size, j*size));
		}
		
	}
	
	public void newGame() {
		for (ArrayList<Tile> arrayList : mGrid)
			for (Tile tile : arrayList)
				tile.reset();
		mMines.clear();
		
		while (mMines.size() < mines) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			if (!mGrid.get(x).get(y).isMine()) {
				mGrid.get(x).get(y).setMine(-1);
				mMines.add(mGrid.get(x).get(y));
			}	
		}
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (mGrid.get(i).get(j).isMine())
					continue;
				int nMines = 0;
				for (int ii = Math.max(i-1, 0); ii < Math.min(i+2, width); ii++)
					for (int jj = Math.max(j-1, 0); jj < Math.min(j+2, height); jj++)
						if (mGrid.get(ii).get(jj).isMine())
							nMines++;
				mGrid.get(i).get(j).setMine(nMines);
			}
		}

		mCount = width * height - mines;
		mActive = true;
		mStart = false;
		tvtime.setText("" + 0);
		updateCount();
	}
	
	public void doDraw(Canvas canvas) {
		updateTime();
		canvas.drawColor(Color.BLACK);
		synchronized (mGrid) {
			for (ArrayList<Tile> arrayList : mGrid)
				for (Tile tile : arrayList)
					tile.doDraw(canvas);
		}
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
			mThread.setRunning(false);
		}
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX() / size;
		int y = (int) event.getY() / size;
		
		if (!mActive || x >= maxX || y >= maxY)
			return super.onTouchEvent(event);
		
		//if (!mThread.getRunning())
			//mThread.setRunning(true);
		if (!mStart) {
			mStart = true;
			mStartTime = System.currentTimeMillis();
		}
		
		synchronized (mGrid) {
			if (!mGrid.get(x).get(y).isRevealed()) {
				if (mGrid.get(x).get(y).reveal()) {
					mCount--;
					
					if (mGrid.get(x).get(y).isZero())
						revealZero(x, y);
					
					updateCount();
					if (mCount == 0) {
						//mThread.setRunning(false);
						mActive = false;
					}
				}
				
				else {
					for (Tile tile : mMines)
						tile.reveal();
					//mThread.setRunning(false);
					mActive = false;
				}
			} 
		}
			
		return super.onTouchEvent(event);
	}
	
	public void revealZero(int x, int y) {
		for (int i = Math.max(x-1, 0); i < Math.min(x+2, width); i++) {
			for (int j = Math.max(y-1, 0); j < Math.min(y+2, width); j++) {
				if (!mGrid.get(i).get(j).isRevealed()) {
					mGrid.get(i).get(j).reveal();
					mCount--;
					
					if (mGrid.get(i).get(j).isZero())
						revealZero(i, j);
				}
			}
		}
	}
	
	public void updateTime() {
		if (mStart && mActive)
			//tvtime.setText("" + (System.currentTimeMillis() - mStartTime) / 1000)
			;
	}
}
