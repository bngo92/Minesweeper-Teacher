package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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
	private Handler mTimer;
	private int mSeconds;
	
	int mX;
	int mY;
	
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
	
	public void setTextView(TextView time, TextView count) {
		tvtime = time;
		tvcount = count;
	}
	
	// Timer functions
	public void startTimer() {
		resetTimer();
		mTimer.removeCallbacks(updateTimer);
		mTimer.postDelayed(updateTimer, 1000);
	}
	
	public void stopTimer() {
		mTimer.removeCallbacks(updateTimer);
	}
	
	private Runnable updateTimer = new Runnable() {
		public void run() {
			mSeconds++;
			tvtime.setText("" + mSeconds);
			mTimer.postDelayed(updateTimer, 1000);
		}
	};
	
	public void resetTimer() {
		mSeconds = 0;
		tvtime.setText("" + 0);
	}
	
	public void updateCount() {
		tvcount.setText("" + mCount);
	}

	public void initGame() {
		getHolder().addCallback(this);
		mThread = new GameThread(this);
		
		rand = new Random();
		mTimer = new Handler();

		size = BitmapFactory.decodeResource(getResources(), R.drawable.mined).getWidth();
		maxX = width;
		maxY = height;

		mMines = new ArrayList<Tile>(mines);
		mGrid = new ArrayList<ArrayList<Tile>>(width);
		for (int i = 0; i < width; i++) {
			mGrid.add(new ArrayList<Tile>(height));
			for (int j = 0; j < height; j++)
				mGrid.get(i).add(new Tile(getResources()));
		}

		// Handle input events
		this.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// Store x and y coordinates for the following listener
				mX = (int) event.getX() / size;
				mY = (int) event.getY() / size;

				// Ignore touch if the game isn't active or the touch is out of bounds
				if (!mActive || mX >= maxX || mY >= maxY)
					return true;

				// Start time during the first touch
				// TODO: change when panning is enabled
				if (!mStart) {
					mStart = true;
					startTimer();
				}
				
				// Process the following listener 
				return false;
			}
			
		});
		
		this.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				mGrid.get(mX).get(mY).toggleFlag();
				invalidate();
				return true;
			}
			
		});
		
		this.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Tile tile = mGrid.get(mX).get(mY);
				
				if (tile.isRevealed() && tile.getMines() == countFlags(mX, mY))
					revealSurrounding(mX, mY);
				else
					reveal(mX, mY);
				
				updateCount();
				if (mCount == 0) {
					stopTimer();
					mActive = false;
				}
			}
			
		});
	}
	
	public void newGame() {
		// Clear mines and grid
		mMines.clear();
		for (ArrayList<Tile> arrayList : mGrid)
			for (Tile tile : arrayList)
				tile.reset();
		
		// Randomize location of mines
		while (mMines.size() < mines) {
			int x = rand.nextInt(width);
			int y = rand.nextInt(height);
			if (!mGrid.get(x).get(y).isMine()) {
				mGrid.get(x).get(y).setMine();
				mMines.add(mGrid.get(x).get(y));
			}	
		}
		
		// Count mines
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (mGrid.get(i).get(j).isMine())
					continue;
				
				int nMines = 0;
				for (int ii = Math.max(i-1, 0); ii < Math.min(i+2, width); ii++)
					for (int jj = Math.max(j-1, 0); jj < Math.min(j+2, height); jj++)
						if (mGrid.get(ii).get(jj).isMine())
							nMines++;
				mGrid.get(i).get(j).setMines(nMines);
			}
		}

		mCount = width * height - mines;
		mActive = true;
		mStart = false;
		
		// Update text view;
		resetTimer();
		updateCount();
	}
	
	public void reveal(int x, int y) {
		Tile tile = mGrid.get(x).get(y);
		
		if (tile.isFlagged() || tile.isRevealed())
			return;

		tile.reveal();
		if (tile.isMine()) {
			stopTimer();
			mActive = false;
			for (Tile mine : mMines)
				mine.reveal();
		} else {
			mCount--;
			if (tile.isZero())
				revealSurrounding(x, y);
		}
	}
	
	public void revealSurrounding(int x, int y) {
		for (int i = Math.max(x-1, 0); i < Math.min(x+2, width); i++)
			for (int j = Math.max(y-1, 0); j < Math.min(y+2, width); j++)
				reveal(i, j);
	}
	
	public int countFlags(int x, int y) {
		int count = 0;
		for (int i = Math.max(x-1, 0); i < Math.min(x+2, width); i++) {
			for (int j = Math.max(y-1, 0); j < Math.min(y+2, width); j++) {
				if (mGrid.get(i).get(j).isFlagged())
					count++;
			}
		}
		return count;
	}

	public void doDraw(Canvas canvas) {
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				canvas.drawBitmap(mGrid.get(i).get(j).getBitmap(),
						i * size , j * size, null);
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
	    if (!mThread.isAlive()) {
	        mThread = new GameThread(this);
	        mThread.setRunning(true);
	        mThread.start();
	    }
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	    if (mThread.isAlive()) {
	        mThread.setRunning(false);
	    }
	    
	}
	
}
