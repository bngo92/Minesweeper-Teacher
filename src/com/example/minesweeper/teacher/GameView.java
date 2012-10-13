package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private GameThread mThread;
	private BitmapCache mBitmap;
	private Timer mTimer;
	
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

	private int mR;
	private int mC;
	private int offset_row;
	private int offset_col;
	
	TextView tvtime;
	TextView tvcount;
	
	public GameView(Context context) {
		super(context);
	}	
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void initGame(final Game parent) {
		int[] gameSize = parent.getIntent().getExtras().getIntArray("size");
		height = gameSize[0];
		width = gameSize[1];
		mines = gameSize[2];
		tvtime = (TextView) parent.findViewById(R.id.textViewTime);
		tvcount = (TextView) parent.findViewById(R.id.textViewCount);
		
		getHolder().addCallback(this);
		mThread = new GameThread(this);
		mTimer = new Timer(tvtime);
		mBitmap = new BitmapCache(getResources());
		size = mBitmap.getBitmap(R.drawable.mine).getWidth();

		// Setup grid
		mMines = new ArrayList<Tile>(mines);
		mGrid = new ArrayList<ArrayList<Tile>>(height);
		for (int r = 0; r < height; r++) {
			mGrid.add(new ArrayList<Tile>(width));
			for (int c = 0; c < width; c++)
				mGrid.get(r).add(new Tile());
		}

		// Handle input events
		this.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				parent.resetGuess();
				
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
					mTimer.startTimer();
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
			}
			
		});
	}
	
	public void newGame() {
		// Reset position
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
		
		// Update mine count
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
		mTimer.resetTimer();
		updateCount();
	}
	
	public int reveal(int r, int c) {
		Tile tile = mGrid.get(r).get(c);
		
		if (tile.isFlagged() || tile.isRevealed())
			return 0;

		tile.reveal();
		if (tile.isMine()) {
			mTimer.stopTimer();
			mActive = false;
			for (Tile mine : mMines)
				mine.reveal();
			return 0;
		}
		
		mCount--;
		updateCount();
		
		int n = 1;
		if (tile.isZero())
			n += revealSurrounding(r, c);
		return n;
	}

	public int revealSurrounding(int r, int c) {
		int n = 0;
		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++)
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++)
				n += reveal(i, j);
		return n;
	}	
	
	public int countFlags(int r, int c) {
		int count = 0;
		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++) {
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++) {
				if (i == r && j == c)
					continue;
				if (mGrid.get(i).get(j).isFlagged())
					count++;
			}
		}
		return count;
	}

	public int countRevealed(int r, int c) {
		int count = 0;
		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++) {
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++) {
				if (i == r && j == c)
					continue;
				if (mGrid.get(i).get(j).isRevealed())
					count++;
			}
		}
		return count;
	}
	
	public int countUnrevealed(int r, int c) {
		int count = 0;
		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++) {
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++) {
				if (i == r && j == c)
					continue;
				if (!mGrid.get(i).get(j).isRevealed())
					count++;
			}
		}
		return count;
	}
	
	public ArrayList<Tile> getUnrevealedNeighbors(int r, int c) {
		ArrayList<Tile> a = new ArrayList<Tile>();

		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++) {
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++) {
				if (i == r && j == c)
					continue;
				if (!mGrid.get(i).get(j).isRevealed())
					a.add(mGrid.get(i).get(j));
			}
		}
		
		return a;
	}
	
	public boolean hint() {
		// Start time during the first touch
		if (!mStart) {
			mStart = true;
			mTimer.startTimer();
		}
		
		if (!mActive)
			return true;
		
		// 1. Check if the number of adjacent mines equals the number of adjacent flags
		// Uncover them.
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				Tile tile = mGrid.get(r).get(c);
				if (tile.isRevealed() && !tile.isZero() && 
						countRevealed(r, c) != 0 && 
						tile.getMines() == countFlags(r, c)) {
					if (revealSurrounding(r, c) != 0) {
						scroll(r - maxHeight/2 - offset_row, c - maxWidth/2 - offset_col);
						return true;
					}
				}
			}
		}
		
		// 2. Check if the number of adjacent mines equals the number of uncovered tiles
		// Flag them
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				if (mGrid.get(r).get(c).isZero() || 
						mGrid.get(r).get(c).isFlagged() || 
						!mGrid.get(r).get(c).isRevealed())
					continue;
				
				if (mGrid.get(r).get(c).getMines() == countUnrevealed(r, c)) {
					boolean success = false;
					for (int rr = Math.max(r-1, 0); rr < Math.min(r+2, height); rr++)
						for (int cc = Math.max(c-1, 0); cc < Math.min(c+2, width); cc++)
							if (!mGrid.get(rr).get(cc).isRevealed() && 
									!mGrid.get(rr).get(cc).isFlagged()) {
								scroll(r - maxHeight/2 - offset_row, c - maxWidth/2 - offset_col);
								mGrid.get(rr).get(cc).toggleFlag();
								
								success = true;
							}
					if (success)
						return true;
				}
			}
		}
		
		return false;
	}
	
	public void guess() {
		// Start time during the first touch
		if (!mStart) {
			mStart = true;
			mTimer.startTimer();
		}
		
		// Build candidates for guessing
		ArrayList<Pair> a = new ArrayList<Pair>(height * width);
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (!mGrid.get(r).get(c).isRevealed() && 
						!mGrid.get(r).get(c).isFlagged())
					a.add(new Pair(r, c));
		
		Random rand = new Random();
		int n = rand.nextInt(a.size());
		int r = a.get(n).first;
		int c = a.get(n).second;
		
		scroll(r - maxHeight/2 - offset_row, c - maxWidth/2 - offset_col);
		reveal(r, c);
		return;
	}

	public void doDraw(Canvas canvas) {
		for (int r = 0; r < Math.min(height, maxHeight+1); r++) {
			if (r + offset_row >= height) {
				for (int c = 0; c < Math.min(width, maxWidth+1); c++)
					canvas.drawBitmap(getBitmap(),
							c*size, r*size, null);
				break;
			}
			ArrayList<Tile> row = mGrid.get(r+offset_row);
			for (int c = 0; c < Math.min(width, maxWidth+1); c++) {
				if (c + offset_col >= width) {
					canvas.drawBitmap(getBitmap(),
							c*size, r*size, null);
					break;
				}
				Tile tile = row.get(c+offset_col);
				canvas.drawBitmap(getBitmap(tile),
						c*size, r*size, null);
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// round up
		maxWidth = MeasureSpec.getSize(widthMeasureSpec) / size;
		maxHeight = MeasureSpec.getSize(heightMeasureSpec) / size;
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
	
	public Bitmap getBitmap() {
		return mBitmap.getBitmap(R.drawable.mine);
	}
	
	public Bitmap getBitmap(Tile tile) {
		int id;
		if (!tile.isRevealed()) {
			if (tile.isFlagged())
				id = R.drawable.minef;
			else
				id = R.drawable.mined;
		}
		else
			switch (tile.getMines()) {
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
		return mBitmap.getBitmap(id);
	}
	
	public void scroll(int r, int c) {
		offset_row += r;
		offset_row = Math.min(height - maxHeight, offset_row);
		offset_row = Math.max(0, offset_row);
		
		offset_col += c;
		offset_col = Math.min(width - maxWidth, offset_col);
		offset_col = Math.max(0, offset_col);
	}
	
	public void updateCount() {
		tvcount.setText("Tiles Left: " + mCount);
		
		if (mCount == 0) {
			mTimer.stopTimer();
			mActive = false;
		}
	}
	
}
