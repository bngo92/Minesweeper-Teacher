package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

/**
 * Core gaming object.
 * 
 * @author Bryan
 * 
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	private Game parent;

	private GameThread gameThread;
	private BitmapCache bitmapCache;
	private Timer gameTimer;

	private int maxHeight;
	private int maxWidth;
	private int offset_row;
	private int offset_col;
	private LinkedList<TileAction> hintQueue;

	private Grid mGrid;

	private int tileSize;

	private int count;
	private int flagCount;
	/** True until game is over from clicking on a mine or winning. */
	boolean gameOver;

	private int mR;
	private int mC;

	private TextView textViewTime;
	private TextView textViewCount;
	VictoryDialog victoryDialog;
	private int height;
	private int width;
	private int mines;
	Random rand;

	public GameView(Context context) {
		super(context);
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Initializes helper objects and input event listeners for game.
	 * 
	 * @param parent
	 *            Parent of view for accessing other UI elements.
	 */
	public void initGame(final Game parent) {
		this.parent = parent;
		textViewTime = (TextView) parent.findViewById(R.id.textViewTime);
		textViewCount = (TextView) parent.findViewById(R.id.textViewCount);
		height = parent.getIntent().getExtras().getInt("height");
		width = parent.getIntent().getExtras().getInt("width");
		mines = parent.getIntent().getExtras().getInt("mines");
		victoryDialog = new VictoryDialog();

		getHolder().addCallback(this);
		gameThread = new GameThread(this);
		gameTimer = new Timer(textViewTime);
		bitmapCache = new BitmapCache(getResources());
		tileSize = bitmapCache.getBitmap(R.drawable.mine).getWidth();
		hintQueue = new LinkedList<TileAction>();

		mGrid = new HintGrid(height, width, mines);

		// Handle input events
		this.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// Turn off automatic solver
				gameTimer.removeCallbacks(parent.findHint);
				gameTimer.removeCallbacks(parent.processHint);
				clearHintQueue();

				// Ignore touch if game has ended
				if (gameOver)
					return true;

				// Store x and y coordinates for the following listener
				mR = (int) event.getY() / tileSize + offset_row;
				mC = (int) event.getX() / tileSize + offset_col;

				// Ignore touch if the game isn't active or the touch is out of
				// bounds
				if (mR >= height || mC >= width)
					return true;

				// Start time during the first touch
				// TODO: change when panning is enabled
				if (!gameTimer.isOn()) {
					gameTimer.startTimer();
				}

				// Process the following listener
				return false;
			}

		});

		this.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				flag(mR, mC);
				return true;
			}

		});

		this.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				gameClick(mR, mC);
			}

		});
	}

	/** Reset and randomizes grid and resets counters and timers. */
	public void newGame() {
		// Reset position
		offset_row = 0;
		offset_col = 0;

		mGrid.randomize();
		count = mGrid.getCount();
		flagCount = mGrid.getFlagCount();

		gameOver = false;

		// Update text views
		gameTimer.resetTimer();
		updateCount();
	}

	public void gameClick(int r, int c) {
		parent.stop();
		click(r, c);
	}

	public void hintClick(int r, int c) {
		click(r, c);
	}

	/**
	 * Clicks grid at location. If mine is clicked, stop game. Update counter.
	 * 
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 */
	public void click(int r, int c) {
		if (gameOver)
			return;

		count = mGrid.click(r, c);
		if (count == -1) {
			gameTimer.stopTimer();
			gameOver = true;
			return;
		}
		updateCount();
	}

	/**
	 * Flags grid at location. Updates flag count.
	 * 
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 */
	public void flag(int r, int c) {
		flagCount = mGrid.flag(r, c);
		updateCount();
	}

	/**
	 * If hints are queued, process them. Otherwise, try to queue hints. If no
	 * hints are found, indicate to caller that you should guess.
	 * 
	 * @return null if game is not active or hints are queued, Game.GUESS if
	 *         hint could not be found, hint tooltip text
	 */
	public String hint() {
		// Start timer if hint button is pressed
		if (!gameTimer.isOn()) {
			gameTimer.startTimer();
		}

		if (gameOver)
			return null;

		String hint = mGrid.findHint(hintQueue);
		if (hint != null)
			return hint;

		return Game.GUESS;
	}

	/**
	 * Processes hint queue and performs the corresponding action at the stored
	 * coordinates.
	 * 
	 * @return true if a hint was performed, false if not
	 */
	public boolean processHintQueue() {
		if (hintQueue.isEmpty())
			return false;

		while (!hintQueue.isEmpty()) {
			TileAction tileAction = hintQueue.pop();
			switch (tileAction.action) {
			case CLICK:
				hintClick(tileAction.r, tileAction.c);
				break;
			case FLAG:
				flag(tileAction.r, tileAction.c);
				break;
			}
			scrollTo(tileAction.r, tileAction.c);
		}
		return true;
	}

	/** Call method when automatic solver is stopped to notify parent Game. */
	public void clearHintQueue() {
		hintQueue.clear();
		parent.resetGuess();
	}

	/**
	 * Locate unrevealed tiles and randomly add one to click into the hint
	 * queue.
	 */
	public void guess() {
		// Start time during the first touch
		if (!gameTimer.isOn()) {
			gameTimer.startTimer();
		}

		// Build candidates for guessing
		ArrayList<Pair<Integer, Integer>> candidates = mGrid
				.getGuessCandidates();

		if (rand == null)
			rand = new Random();

		int n = rand.nextInt(candidates.size());
		Pair<Integer, Integer> guess = candidates.get(n);
		gameClick(guess.first, guess.second);
		return;
	}

	/**
	 * Update text views displays of counts of non-mine and mine tiles. Ends
	 * game and displays victory message if non-mile tiles reaches 0.
	 */
	public void updateCount() {
		textViewCount.setText(count + "/" + flagCount);

		if (count == 0) {
			gameTimer.stopTimer();
			gameOver = false;
			victoryDialog.setMessage("You win!\nTime: " + gameTimer.getTime());
			victoryDialog.show(parent.getFragmentManager(), "");
		}
	}

	/**
	 * Scroll in specified direction.
	 * 
	 * @param r
	 *            rows to move over
	 * @param c
	 *            columns to move over
	 */
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

	/**
	 * Center GameView at specified location
	 * 
	 * @param r
	 *            row to scroll to
	 * @param c
	 *            column to scroll to
	 */
	public void scrollTo(int r, int c) {
		scroll(r - maxHeight / 2 - offset_row, c - maxWidth / 2 - offset_col);
	}

	/**
	 * Return default bitmap when not drawing a tile.
	 * 
	 * @return default Bitmap object
	 */
	public Bitmap getBitmap() {
		return bitmapCache.getBitmap(R.drawable.def);
	}

	/**
	 * Return bitmap corresponding to the tile's states.
	 * 
	 * @param tile
	 *            tile to draw
	 * @return <li>Bitmap object of the tile
	 */
	public Bitmap getBitmap(Tile tile) {
		int id;
		if (tile.isExploded()) {
			id = R.drawable.mine_clicked;
		} else if (tile.isFlagged()) {
			if (gameOver && !tile.isMine()) {
				id = R.drawable.bad_flag;
			} else {
				id = R.drawable.flag;
			}
		} else if (!tile.isRevealed()) {
			id = R.drawable.unknown;
		} else
			switch (tile.getMines()) {
			case -1:
				id = R.drawable.mine;
				break;
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
				id = R.drawable.miner;
				break;
			}
		return bitmapCache.getBitmap(id);
	}

	public void doDraw(Canvas canvas) {
		ArrayList<ArrayList<Tile>> grid = mGrid.getGrid();
		for (int r = 0; r < Math.min(height, maxHeight + 1); r++) {
			if (r + offset_row >= height) {
				for (int c = 0; c < Math.min(width, maxWidth + 1); c++)
					canvas.drawBitmap(getBitmap(), c * tileSize, r * tileSize,
							null);
				break;
			}
			ArrayList<Tile> row = grid.get(r + offset_row);
			for (int c = 0; c < Math.min(width, maxWidth + 1); c++) {
				if (c + offset_col >= width) {
					canvas.drawBitmap(getBitmap(), c * tileSize, r * tileSize,
							null);
					break;
				}
				Tile tile = row.get(c + offset_col);
				canvas.drawBitmap(getBitmap(tile), c * tileSize, r * tileSize,
						null);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (tileSize == 0)
			tileSize = 40;
		maxWidth = MeasureSpec.getSize(widthMeasureSpec) / tileSize;
		maxHeight = MeasureSpec.getSize(heightMeasureSpec) / tileSize;
	}

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (!gameThread.isAlive()) {
			gameThread = new GameThread(this);
			gameThread.setRunning(true);
			gameThread.start();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (gameThread.isAlive()) {
			gameThread.setRunning(false);
		}
	}

}
