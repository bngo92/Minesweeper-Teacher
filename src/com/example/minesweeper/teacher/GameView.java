package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.Collections;
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
	private Random rand;
	
	private int maxHeight;
	private int maxWidth;
	
	private ArrayList<ArrayList<Tile>> mGrid;	
	private ArrayList<Tile> mMines;
	
	private int height;
	private int width;
	private int mines;
	private int size;
	
	private int mCount;
	private boolean mStart;
	private boolean mActive;
	private Handler mTimer;
	private int mSeconds;

	private int mR;
	private int mC;
	private int offset_row;
	private int offset_col;
	
	TextView tvtime;
	TextView tvcount;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// round up
		maxWidth = (MeasureSpec.getSize(widthMeasureSpec) + size - 1)/ size;
		maxHeight = (MeasureSpec.getSize(heightMeasureSpec) + size - 1)/ size;
	}
	
	public GameView(Context context) {
		super(context);
	}	
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setSize(int[] size) {
		height = size[0];
		width = size[1];
		mines = size[2];
	}
	
	public void setTextView(TextView time, TextView count) {
		tvtime = time;
		tvcount = count;
	}
	
	public void scroll(int r, int c) {
		offset_row += r;
		offset_row = Math.min(height - maxHeight, offset_row);
		offset_row = Math.max(0, offset_row);
		
		offset_col += c;
		offset_col = Math.min(width - maxWidth, offset_col);
		offset_col = Math.max(0, offset_col);
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

		mMines = new ArrayList<Tile>(mines);
		mGrid = new ArrayList<ArrayList<Tile>>(height);
		for (int r = 0; r < height; r++) {
			mGrid.add(new ArrayList<Tile>(width));
			for (int c = 0; c < width; c++)
				mGrid.get(r).add(new Tile(getResources()));
		}

		// Handle input events
		this.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// Store x and y coordinates for the following listener
				mR = (int) event.getY() / size + offset_row;
				mC = (int) event.getX() / size + offset_col;

				// Ignore touch if the game isn't active or the touch is out of bounds
				if (!mActive || mR >= height || mC >= width)
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
				mGrid.get(mR).get(mC).toggleFlag();
				return true;
			}
			
		});
		
		this.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Tile tile = mGrid.get(mR).get(mC);
				
				if (tile.isRevealed() && tile.getMines() == countFlags(mR, mC))
					revealSurrounding(mR, mC);
				else
					reveal(mR, mC);
				
				if (mCount == 0) {
					stopTimer();
					mActive = false;
				}
			}
			
		});
	}
	
	public void newGame() {
		offset_row = 0;
		offset_col = 0;
		
		// Clear mines and grid
		mMines.clear();
		for (ArrayList<Tile> arrayList : mGrid)
			for (Tile tile : arrayList)
				tile.reset();
		
		// Randomize location of mines
		ArrayList<Pair> grid = new ArrayList<Pair>(height * width);
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				grid.add(new Pair(r, c));
			}
		}
		
		Collections.shuffle(grid);
		for (int i = 0; i < mines; i++) {
			Pair p = grid.get(i);
			mGrid.get(p.first).get(p.second).setMine();
			mMines.add(mGrid.get(p.first).get(p.second));
		}
		
		// Count mines
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				if (mGrid.get(r).get(c).isMine())
					continue;
				
				int nMines = 0;
				for (int rr = Math.max(r-1, 0); rr < Math.min(r+2, height); rr++)
					for (int cc = Math.max(c-1, 0); cc < Math.min(c+2, width); cc++)
						if (mGrid.get(rr).get(cc).isMine())
							nMines++;
				mGrid.get(r).get(c).setMines(nMines);
			}
		}

		mCount = height * width - mines;
		mActive = true;
		mStart = false;
		
		// Update text view;
		resetTimer();
		updateCount();
	}
	
	public void reveal(int r, int c) {
		Tile tile = mGrid.get(r).get(c);
		
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
			updateCount();
			if (tile.isZero())
				revealSurrounding(r, c);
		}
	}

	public void revealSurrounding(int r, int c) {
		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++)
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++)
				reveal(i, j);
	}	
	
	public int countFlags(int r, int c) {
		int count = 0;
		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++) {
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++) {
				if (mGrid.get(i).get(j).isFlagged())
					count++;
			}
		}
		return count;
	}

	public void doDraw(Canvas canvas) {
		for (int r = 0; r < Math.min(height, maxHeight); r++)
			for (int c = 0; c < Math.min(width, maxWidth); c++)
				canvas.drawBitmap(mGrid.get(r+offset_row).get(c+offset_col).getBitmap(),
						c*size, r*size, null);
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
	
	public void hint() {
		// 1. Check if the number of adjacent mines equals the number of adjacent flags
		// Uncover them.
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				if (!mGrid.get(r).get(c).isZero() && mGrid.get(r).get(c).isRevealed() && 
						mGrid.get(r).get(c).getMines() == countFlags(r, c)) {
					int temp = mCount;
					revealSurrounding(r, c);
					if (temp != mCount)
						return;
				}
			}
		}
		
		// 2. Check if the number of adjacent mines equals the number of uncovered tiles
		// Flag them
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				if (mGrid.get(r).get(c).isZero() || mGrid.get(r).get(c).isFlagged() || 
						!mGrid.get(r).get(c).isRevealed())
					continue;
				
				if (mGrid.get(r).get(c).getMines() == countRevealed(r, c)) {
					boolean success = false;
					for (int rr = Math.max(r-1, 0); rr < Math.min(r+2, height); rr++)
						for (int cc = Math.max(c-1, 0); cc < Math.min(c+2, width); cc++)
							if (!mGrid.get(rr).get(cc).isRevealed() && 
									!mGrid.get(rr).get(cc).isFlagged()) {
								mGrid.get(rr).get(cc).toggleFlag();
								success = true;
							}
					if (success)
						return;
				}
			}
		}
		
		guess();
	}
	
	public void guess() {
		ArrayList<Pair> a = new ArrayList<Pair>(height * width);
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (!mGrid.get(r).get(c).isRevealed())
					a.add(new Pair(r, c));
		int n = rand.nextInt(a.size());
		int r = a.get(n).first;
		int c = a.get(n).second;
		reveal(r, c);
		return;
	}
	
	public int countRevealed(int r, int c) {
		int count = 0;
		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++) {
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++) {
				if (!mGrid.get(i).get(j).isRevealed())
					count++;
			}
		}
		return count;
	}
	
}
