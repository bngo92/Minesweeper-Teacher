package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Container of Tile objects.
 * @author Bryan
 *
 */
public class Grid {
	protected ArrayList<ArrayList<Tile>> mGrid;	
	protected ArrayList<Tile> mMines;
	
	protected int height;
	protected int width;
	protected int mines;
	
	private int mCount;
	private int mFlagCount;
	
	/**
	 * Initialize grid with specified dimensions
	 * @param height number of rows
	 * @param width number of columns
	 * @param mines number of mines
	 */
	public Grid(int height, int width, int mines) {
		this.height = height;
		this.width = width;
		this.mines = mines;
		
		// Setup grid
		mMines = new ArrayList<Tile>(mines);
		mGrid = new ArrayList<ArrayList<Tile>>(height);
		for (int r = 0; r < height; r++) {
			mGrid.add(new ArrayList<Tile>(width));
			for (int c = 0; c < width; c++)
				mGrid.get(r).add(new Tile(r, c));
		}
	}
	
	/**
	 * Populate grid with random mines.
	 */
	public void randomize() {
		// Clear mines and grid
		mMines.clear();
		for (ArrayList<Tile> arrayList : mGrid)
			for (Tile tile : arrayList)
				tile.reset();
		
		// Randomize location of mines
		ArrayList<Pair<Integer, Integer>> grid = new ArrayList<Pair<Integer, Integer>>(height * width);
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				grid.add(new Pair<Integer, Integer>(r, c));
			}
		}
		
		Collections.shuffle(grid);
		for (int i = 0; i < mines; i++) {
			Pair<Integer, Integer> p = grid.get(i);
			getTile(p.first, p.second).setMine();
			mMines.add(getTile(p.first, p.second));
		}
		
		// Update mine count
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				if (mGrid.get(r).get(c).isMine()) {
					continue;
				}
				
				int nMines = 0;
				for (int rr = Math.max(r-1, 0); rr < Math.min(r+2, height); rr++) {
					for (int cc = Math.max(c-1, 0); cc < Math.min(c+2, width); cc++) {
						if (mGrid.get(rr).get(cc).isMine()) {
							nMines++;
						}
					}
				}
				mGrid.get(r).get(c).updateMines(nMines);
			}
		}
		
		mCount = height * width - mines;
		mFlagCount = 0;
	}
	
	/** Return grid object. */
	public ArrayList<ArrayList<Tile>> getGrid() {
		return mGrid;
	}
	
	/**
	 * Return tile at coordinates.
	 * @param r row of tile
	 * @param c column of tile
	 * @return <li>Tile object at the location
	 */
	public Tile getTile(int r, int c) {
		return mGrid.get(r).get(c);
	}
	
	/**
	 * Perform a click action on the tile at the location.
	 * @param r row of tile
	 * @param c column of tile
	 * @return -1 if clicked on a mine, count of unrevealed tiles on the grid otherwise
	 */
	public int click(int r, int c) {
		Tile tile = getTile(r, c);
		if (tile.isRevealed() && tile.getMines() == countFlags(r, c)) {
			if (!revealSurrounding(r, c)) {
				return -1;
			}
			return mCount;
		}
		
		return reveal(r, c);
	}
	
	/**
	 * Perform a flag action on the tile at the location.
	 * @param r row of tile
	 * @param c column of tile
	 * @return <li>number of flags on the grid
	 */
	public int flag(int r, int c) {
		mFlagCount += getTile(r, c).toggleFlag();
		return mFlagCount;
	}
	
	/**
	 * Attempts to reveal tile.
	 * Activates all mines if a mine is clicked.
	 * Attempts to reveal surroundings if possible.
	 * @param r row of tile to reveal
	 * @param c column of tile to reveal
	 * @return -1 if a mine was revealed, count of unrevealed tiles otherwise
	 */
	public int reveal(int r, int c) {
		Tile tile = getTile(r, c);
		
		// Don't reveal flagged tiles
		if (tile.isFlagged() || tile.isRevealed())
			return mCount;

		// Try revealing tile
		// Exit if mines is hit
		mCount -= tile.reveal();
		if (tile.isMine()) {
			tile.explode();
			for (Tile mine : mMines)
				mine.reveal();
			return -1;
		}

		if (tile.isZero()) {
			if (!revealSurrounding(r, c)) {
				return -1;
			}
		}

		return mCount;
	}

	/**
	 * Reveal the tiles surrounding the tile chosen
	 * @param r row of the tile
	 * @param c row of the column
	 * @return false if mine has been revealed, true otherwise
	 */
	public boolean revealSurrounding(int r, int c) {
		boolean explode = false;
		for (int i = Math.max(r-1, 0); i < Math.min(r+2, height); i++) {
			for (int j = Math.max(c-1, 0); j < Math.min(c+2, width); j++) {
				if (i == r && j == c)
					continue;
				
				if (reveal(i, j) == -1) {
					explode = true;
				}
			}
		}
		
		return !explode;
	}	

	/**
	 * Count the number of flags surrounding a tile.
	 * @param r row of tile
	 * @param c column of tile
	 * @return <li>number of flags surrounding the tile at the location
	 */
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
	
	/** Return the number of unrevealed tiles on the grid. */
	public int getCount() {
		return mCount;
	}
	
	/** Return the number of flags on the grid. */
	public int getFlagCount() {
		return mFlagCount;
	}

	/** Do not process hints. */
	public String findHint(LinkedList<TileAction> queue) {
		// TODO Auto-generated method stub
		return null;
	}

	/** Do not process hints. */
	public ArrayList<Pair<Integer, Integer>> getGuessCandidates() {
		// TODO Auto-generated method stub
		return null;
	}
}
