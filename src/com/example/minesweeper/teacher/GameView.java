package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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

import com.example.minesweeper.teacher.Tile.State;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private GameThread mThread;
	private BitmapCache mBitmap;
	private Timer mTimer;

	private int height;
	private int width;
	private int maxHeight;
	private int maxWidth;
	private int offset_row;
	private int offset_col;
	private LinkedList<Tile> queue;
	
	private ArrayList<ArrayList<Tile>> mGrid;	
	private ArrayList<Tile> mMines;
	
	private int mines;
	private int size;
	
	private int mCount;
	private boolean mStart;
	private boolean mActive;

	private int mR;
	private int mC;
	
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
		queue = new LinkedList<Tile>();

		// Setup grid
		mMines = new ArrayList<Tile>(mines);
		mGrid = new ArrayList<ArrayList<Tile>>(height);
		for (int r = 0; r < height; r++) {
			mGrid.add(new ArrayList<Tile>(width));
			for (int c = 0; c < width; c++)
				mGrid.get(r).add(new Tile(r, c));
		}

		// Handle input events
		this.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				while (!queue.isEmpty()) {
					Tile tile = queue.pop();
					tile.setState(State.NONE);
				}
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
	
	public String hint() {
		// Start time during the first touch
		if (!mStart) {
			mStart = true;
			mTimer.startTimer();
		}
		
		if (!mActive)
			return null;
		
		if (!queue.isEmpty()) {
			while (!queue.isEmpty()) {
				Tile tile = queue.pop();
				Pair coords = tile.getCoords();
				switch (tile.getState()) {
				case REVEAL:
					reveal(coords.first, coords.second);
					break;
				case REVEAL_SURROUNDING:
					revealSurrounding(coords.first, coords.second);
					break;
				case FLAG:
					tile.setFlag(true);
					break;
				default:
					break;
				}
				tile.setState(State.NONE);
			}
			return null;
		}
		
		String hint;
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				hint = revealAlgorithm(r, c);
				if (hint != null)
					return hint;
				hint = flagAlgorithm(r, c);
				if (hint != null)
					return hint;
				hint = pairAlgorithm(r, c);
				if (hint != null)
					return hint;
			}
		}
		guess();
		return "guess";
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

		scrollToHint(r, c);
		mGrid.get(r).get(c).setState(State.REVEAL);
		queue.push(mGrid.get(r).get(c));
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
		if (tile.getState() != State.NONE) {
			id = R.drawable.miner;
		} else if (!tile.isRevealed()) {
			if (tile.isFlagged())
				id = R.drawable.minef;
			else
				id = R.drawable.mined;
		} else
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
		int row = offset_row + r;
		if (row > height - maxHeight)
			row = height - maxHeight;
		if (row < 0)
			row = 0;
		offset_row = row;

		int col = offset_col + c;
		if (col > width - maxWidth)
			col = width - maxWidth;
		if (col < 0)
			col = 0;
		offset_col = col;
	}
	
	public void updateCount() {
		tvcount.setText("Tiles Left: " + mCount);
		
		if (mCount == 0) {
			mTimer.stopTimer();
			mActive = false;
		}
	}
	
	public Tile getTile(int r, int c) {
		return mGrid.get(r).get(c);
	}
	
	//public class Hint {
		/* 1. Check if the number of adjacent mines equals the number of adjacent flags
		 * Uncover them.
		 *
		 * ?F?		UFU
		 * ?1?	->	U1U
		 * ???		UUU
		 */
		public String revealAlgorithm(int r, int c) {
			Tile tile = getTile(r, c);
			if (!tile.isRevealed() || 
					tile.isZero() || 
					countRevealed(r, c) == 0 ||
					countUnrevealed(r, c) == 0 ||
					countMines(r, c) != 0)
				return null;
			
			tile.setState(State.REVEAL_SURROUNDING);
			queue.push(tile);
			
			scrollToHint(r, c);
			return String.format("(%d,%d) has %d mines and %d flags.", r, c, tile.getMines(), tile.getMines());
		}

		/* 2. Check if the number of adjacent mines equals the number of uncovered tiles
		 * Flag them
		 * 
		 * 2?		2F
		 * 2?	->	2F
		 * ??		??
		 */
		public String flagAlgorithm(int r, int c) {
			Tile tile = getTile(r, c);
			if (tile.isZero() || 
					tile.isFlagged() || 
					!tile.isRevealed())
				return null;
				
			int mines = countMines(r, c);
			int unrevealed = countUnrevealed(r, c);
			if (mines != unrevealed)
				return null;

			boolean success = false;
			for (int rr = Math.max(r-1, 0); rr < Math.min(r+2, height); rr++) {
				for (int cc = Math.max(c-1, 0); cc < Math.min(c+2, width); cc++) {
					
					if (rr == r && cc == c)
						continue;
					
					Tile subtile = getTile(rr, cc);
					if (subtile.isRevealed() ||
							subtile.isFlagged()) 
						continue;
					
					subtile.setState(State.FLAG);
					queue.push(subtile);
					success = true;
				}
			}
			if (success) {
				scrollToHint(r, c);
				return String.format("(%d,%d) has %d mines and %d uncovered tiles.", r, c, tile.getMines(), tile.getMines());
			}
			return null;
		}

		/* 3. Check if a neighbor has a subset of tiles
		 * Uncover or flag the rest
		 * 
		 * 1?		1?
		 * 1?	->	1?
		 * 1?		1U
		 * 
		 * 1?		1?
		 * 2?	->	2?
		 * 2?		2F
		 */
		public String pairAlgorithm(int r, int c) {
			Tile tile = getTile(r, c);
			if (tile.isZero() || 
					tile.isFlagged() || 
					!tile.isRevealed() ||
					countUnrevealed(r, c) == 0)
				return null;
			
			ArrayList<Tile> tile_neighbors = getUnrevealedNeighbors(r, c);
			for (int rr = Math.max(r-1, 0); rr < Math.min(r+2, height); rr++) {
				for (int cc = Math.max(c-1, 0); cc < Math.min(c+2, width); cc++) {
					
					if (rr == r && cc == c)
						continue;
					
					Tile subtile = getTile(rr, cc);
					if (subtile.isZero() || 
							!subtile.isRevealed() || 
							subtile.isFlagged() ||
							countUnrevealed(rr, cc) == 0)
						continue;
					
					ArrayList<Tile> subtile_neighbors = getUnrevealedNeighbors(rr, cc);
					ArrayList<Tile> tile_unique = removeSubset(tile_neighbors, subtile_neighbors);
					if (tile_unique.isEmpty())
						continue;
					
					int success = 0;
					String s = "";
					int mine_difference = countMines(r, c) - countMines(rr, cc);
					if (mine_difference == tile_unique.size()) {
						s = "mines";
						for (Tile t: tile_unique) {
							if (t.isFlagged()) 
								continue; 
							
							t.setState(State.FLAG);
							queue.push(t);
							success++;
						}
					} else if (countMines(r, c) == countMines(rr, cc) && mine_difference < tile_unique.size()) {
						s = "not mines";
						for (Tile t: tile_unique) {
							t.setState(State.REVEAL);
							queue.push(t);
							success++;
						}
					}
					if (success != 0) {
						scrollToHint(r, c);
						return String.format("(%d,%d) and (%d,%d) share %d tiles." +
								"The remaining %d are %s.", 
								r, c, rr, cc, 
								subtile_neighbors.size(),
								success, s);
					}
				}
			}
			return null;
		}

		private int countRevealed(int r, int c) {
			int count = 0;
			for (int rr = Math.max(r-1, 0); rr < Math.min(r+2, height); rr++) {
				for (int cc = Math.max(c-1, 0); cc < Math.min(c+2, width); cc++) {
					if (rr == r && cc == c)
						continue;

					Tile tile = getTile(r, c);
					if (tile.isRevealed() || 
							tile.isFlagged())
						count++;
				}
			}
			return count;
		}
		
		private int countUnrevealed(int r, int c) {
			int count = 0;
			for (int rr = Math.max(r-1, 0); rr < Math.min(r+2, height); rr++) {
				for (int cc = Math.max(c-1, 0); cc < Math.min(c+2, width); cc++) {
					if (rr == r && cc == c)
						continue;

					Tile tile = getTile(rr, cc);
					if (!tile.isRevealed() &&
							!tile.isFlagged())
						count++;
				}
			}
			return count;
		}
		
		private int countMines(int r, int c) {
			return getTile(r, c).getMines() - countFlags(r, c);
		}	
		
		private ArrayList<Tile> getUnrevealedNeighbors(int r, int c) {
			ArrayList<Tile> a = new ArrayList<Tile>();

			for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++) {
				for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++) {
					if (i == r && j == c)
						continue;
					
					Tile tile = getTile(i, j);
					if (!tile.isRevealed() && !tile.isFlagged())
						a.add(tile);
				}
			}
			
			return a;
		}
		
		private ArrayList<Tile> removeSubset(final ArrayList<Tile> a1, ArrayList<Tile> a2) {
			if (a2.size() > a1.size())
				return new ArrayList<Tile>();
			
			for (Tile tile : a2) {
				if (a1.contains(tile))
					a1.remove(tile);
				else
					return new ArrayList<Tile>();
			}
			
			return a1;
		}
		
		public void scrollToHint(int r, int c) {
			scroll(r - maxHeight/2 - offset_row, c - maxWidth/2 - offset_col);
		}
	//}
}
