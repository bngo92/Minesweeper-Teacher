package com.example.minesweeper.teacher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import com.example.minesweeper.teacher.TileAction.Action;

/**
 * Extension of the core grid with hint functionality.
 * 
 * @author Bryan
 * 
 */
public class HintGrid extends Grid {
	Pair<Integer, Integer> lastClicked;

	public HintGrid(int height, int width, int mines) {
		super(height, width, mines);
		lastClicked = new Pair<Integer, Integer>(0, 0);
	}

	public int click(int r, int c) {
		lastClicked = new Pair<Integer, Integer>(r, c);
		return super.click(r, c);
	}

	/** Breadth first search through grid for hint. */
	public String findHint(LinkedList<TileAction> queue) {
		int r = lastClicked.first;
		int c = lastClicked.second;
		String hint = hint(queue, r, c);
		if (hint != null)
			return hint;

		int n = 1;
		boolean found;
		do {
			found = false;
			r = lastClicked.first - n;
			c = lastClicked.second - n;
			if (r >= 0) {
				for (int i = 0; i < 2 * n; i++) {
					int cc = c + i;
					if (cc < 0 || cc >= width)
						continue;
					found = true;
					hint = hint(queue, r, cc);
					if (hint != null)
						return hint;
				}
			}
			r = lastClicked.first - n;
			c = lastClicked.second + n;
			if (c < width) {
				for (int i = 0; i < 2 * n; i++) {
					int rr = r + i;
					if (rr < 0 || rr >= height)
						continue;
					found = true;
					hint = hint(queue, rr, c);
					if (hint != null)
						return hint;
				}
			}
			r = lastClicked.first + n;
			c = lastClicked.second + n;
			if (r < height) {
				for (int i = 0; i < 2 * n; i++) {
					int cc = c - i;
					if (cc < 0 || cc >= width)
						continue;
					found = true;
					hint = hint(queue, r, cc);
					if (hint != null)
						return hint;
				}
			}
			r = lastClicked.first + n;
			c = lastClicked.second - n;
			if (c >= 0) {
				for (int i = 0; i < 2 * n; i++) {
					int rr = r - i;
					if (rr < 0 || rr >= height)
						continue;
					found = true;
					hint = hint(queue, rr, c);
					if (hint != null)
						return hint;
				}
			}
			n++;
		} while (found);
		return null;
	}

	/**
	 * Checks location if any hint can be performed. Adds hint to queue if
	 * found.
	 * 
	 * @param queue
	 *            Queue to add hint to
	 * @param r
	 *            row of tile to check
	 * @param c
	 *            column of tile to check
	 * @return <li>hint text output from the detecting algorithm
	 */
	public String hint(LinkedList<TileAction> queue, int r, int c) {
		String hint = revealAlgorithm(queue, r, c);
		if (hint != null)
			return hint;
		hint = flagAlgorithm(queue, r, c);
		if (hint != null)
			return hint;
		hint = pairAlgorithm(queue, r, c);
		if (hint != null)
			return hint;
		// hint = tripleAlgorithm(r, c);
		// if (hint != null)
		// return hint;
		return null;
	}

	/**
	 * Check if the number of adjacent mines equals the number of adjacent flags
	 * 
	 * @param hintQueue
	 *            queue to populate with hint tiles
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 * @return hint text
	 */
	public String revealAlgorithm(LinkedList<TileAction> hintQueue, int r, int c) {
		Tile tile = getTile(r, c);
		if (!tile.isRevealed() || tile.isZero() || countRevealed(r, c) == 0
				|| countUnrevealed(r, c) == 0 || countMines(r, c) != 0)
			return null;

		hintQueue.push(new TileAction(r, c, Action.CLICK));

		return String.format(Locale.getDefault(),
				"(%d,%d) has %d mines and %d flags."
						+ "Reveal the remaining tiles.", r, c, tile.getMines(),
				tile.getMines());
	}

	/**
	 * Check if the number of adjacent mines equals the number of uncovered
	 * tiles
	 * 
	 * @param hintQueue
	 *            queue to populate with hint tiles
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 * @return hint text
	 */
	public String flagAlgorithm(LinkedList<TileAction> queue, int r, int c) {
		Tile tile = getTile(r, c);
		if (tile.isZero() || tile.isFlagged() || !tile.isRevealed())
			return null;

		int mines = countMines(r, c);
		int unrevealed = countUnrevealed(r, c);
		if (mines != unrevealed)
			return null;

		boolean success = false;
		for (int rr = Math.max(r - 1, 0); rr < Math.min(r + 2, height); rr++) {
			for (int cc = Math.max(c - 1, 0); cc < Math.min(c + 2, width); cc++) {

				if (rr == r && cc == c)
					continue;

				Tile subtile = getTile(rr, cc);
				if (subtile.isRevealed() || subtile.isFlagged())
					continue;

				queue.push(new TileAction(rr, cc, TileAction.Action.FLAG));
				success = true;
			}
		}
		if (success) {
			return String.format(Locale.getDefault(),
					"(%d,%d) has %d remaining mines and %d remaining tiles. "
							+ "Flag them all.", r, c, tile.getMines(),
					tile.getMines());
		}
		return null;
	}

	/*
	 * 3. Check if a neighbor has a subset of tiles /** Check if all your tiles
	 * are shared by a neighbor. If the neighbor has more surrounding mines and
	 * the difference is equal to the number of not shared tiles, flag those
	 * tiles. Otherwise, if the neighbor has the same amount of surrounding
	 * mines, click the tiles not shared
	 * 
	 * @param hintQueue queue to populate with hint tiles
	 * 
	 * @param r row of tile
	 * 
	 * @param c column of tile
	 * 
	 * @return hint text
	 */
	public String pairAlgorithm(LinkedList<TileAction> queue, int r, int c) {
		Tile tile = getTile(r, c);
		if (tile.isZero() || tile.isFlagged() || !tile.isRevealed()
				|| countUnrevealed(r, c) == 0)
			return null;

		ArrayList<Tile> tile_neighbors = getUnrevealedNeighbors(r, c);
		for (int rr = Math.max(r - 2, 0); rr < Math.min(r + 3, height); rr++) {
			for (int cc = Math.max(c - 2, 0); cc < Math.min(c + 3, width); cc++) {

				if (rr == r && cc == c)
					continue;

				Tile subtile = getTile(rr, cc);
				if (subtile.isZero() || !subtile.isRevealed()
						|| subtile.isFlagged() || countUnrevealed(rr, cc) == 0)
					continue;

				ArrayList<Tile> subtile_neighbors = getUnrevealedNeighbors(rr,
						cc);
				Pair<ArrayList<Tile>, ArrayList<Tile>> neighbors = splitNeighbors(
						tile_neighbors, subtile_neighbors);
				ArrayList<Tile> tile_shared = neighbors.first;
				ArrayList<Tile> tile_unique = neighbors.second;
				if (tile_unique.isEmpty())
					continue;

				int success = 0;
				String s = "";
				if (countMines(r, c) == tile_unique.size() + countMines(rr, cc)) {
					/*
					 * 1? 1? 2? -> 2? 2? 2F
					 */
					s = "mines";
					for (Tile t : tile_unique) {
						if (t.isFlagged())
							continue;

						Pair<Integer, Integer> coords = t.getCoords();
						queue.push(new TileAction(coords.first, coords.second,
								TileAction.Action.FLAG));
						success++;
					}
				} else if (tile_shared.containsAll(subtile_neighbors)
						&& countMines(r, c) == countMines(rr, cc)) {
					/*
					 * 1? 1? 1? -> 1? 2? 2R
					 */
					s = "not mines";
					for (Tile t : tile_unique) {
						Pair<Integer, Integer> coords = t.getCoords();
						queue.push(new TileAction(coords.first, coords.second,
								TileAction.Action.CLICK));
						success++;
					}
				}
				if (success != 0) {
					return String.format(Locale.getDefault(),
							"(%d,%d) and (%d,%d) share %d mines across %d tiles."
									+ "The remaining %d are %s.", r, c, rr, cc,
							countMines(rr, cc), subtile_neighbors.size(),
							success, s);
				}
			}
		}
		return null;
	}

	/*
	 * TODO: 01? 01? 12? -> 12? ??? ??R
	 */
	public String tripleAlgorithm(LinkedList<TileAction> queue, int r, int c) {
		Tile tile = getTile(r, c);
		if (tile.isZero() || tile.isFlagged() || !tile.isRevealed()
				|| countUnrevealed(r, c) == 0)
			return null;

		ArrayList<Tile> candidates = getRevealedNeighbors(r, c);
		ArrayList<Tile> tile_neighbors = getUnrevealedNeighbors(r, c);
		for (int i = 0; i < candidates.size(); i++) {
			for (int j = i + 1; j < candidates.size(); j++) {
				Pair<Integer, Integer> tile1_coords = candidates.get(i)
						.getCoords();
				Pair<Integer, Integer> tile2_coords = candidates.get(j)
						.getCoords();
				if ((countMines(r, c)
						- countMines(tile1_coords.first, tile1_coords.second) - countMines(
							tile2_coords.first, tile2_coords.second)) != 0)
					continue;

				ArrayList<Tile> tile1_neighbors = getUnrevealedNeighbors(
						tile1_coords.first, tile1_coords.second);
				ArrayList<Tile> tile2_neighbors = getUnrevealedNeighbors(
						tile2_coords.first, tile2_coords.second);

				for (Tile t : tile1_neighbors) {
					if (tile2_neighbors.contains(t))
						continue;
				}
				for (Tile t : tile2_neighbors) {
					if (tile1_neighbors.contains(t))
						continue;
				}

				ArrayList<Tile> a = new ArrayList<Tile>();
				for (Tile t : tile_neighbors) {
					if (!tile1_neighbors.contains(t)
							&& !tile2_neighbors.contains(t))
						a.add(t);
				}
				if (a.isEmpty()) {
					continue;
				}
				for (Tile t : a) {
					Pair<Integer, Integer> coords = t.getCoords();
					queue.push(new TileAction(coords.first, coords.second,
							TileAction.Action.CLICK));
					return String.format(Locale.getDefault(),
							"(%d,%d) and (%d,%d) have taken all of (%d,%d)'s mines."
									+ "The remaining locations are clear.",
							tile1_coords.first, tile1_coords.second,
							tile2_coords.first, tile2_coords.second, r, c);
				}
			}
		}
		return null;
	}

	/**
	 * Get the number surrounding tiles that are revealed
	 * 
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 * @return number of revealed neighbors
	 */
	private int countRevealed(int r, int c) {
		int count = 0;
		for (int rr = Math.max(r - 1, 0); rr < Math.min(r + 2, height); rr++) {
			for (int cc = Math.max(c - 1, 0); cc < Math.min(c + 2, width); cc++) {
				if (rr == r && cc == c)
					continue;

				Tile tile = getTile(r, c);
				if (tile.isRevealed() || tile.isFlagged())
					count++;
			}
		}
		return count;
	}

	/**
	 * Get the number surrounding tiles that are not revealed
	 * 
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 * @return number of unrevealed neighbors
	 */
	private int countUnrevealed(int r, int c) {
		int count = 0;
		for (int rr = Math.max(r - 1, 0); rr < Math.min(r + 2, height); rr++) {
			for (int cc = Math.max(c - 1, 0); cc < Math.min(c + 2, width); cc++) {
				if (rr == r && cc == c)
					continue;

				Tile tile = getTile(rr, cc);
				if (!tile.isRevealed() && !tile.isFlagged())
					count++;
			}
		}
		return count;
	}

	/**
	 * Count the number of mines unaccounted for
	 * 
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 * @return number of mines that still need to be flagged
	 */
	private int countMines(int r, int c) {
		return getTile(r, c).getMines() - countFlags(r, c);
	}

	/**
	 * Get the surrounding tiles that are revealed
	 * 
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 * @return list of unrevealed neighbors
	 */
	private ArrayList<Tile> getRevealedNeighbors(int r, int c) {
		ArrayList<Tile> neighbors = new ArrayList<Tile>();

		for (int rr = Math.max(r - 1, 0); rr < Math.min(r + 2, height); rr++) {
			for (int cc = Math.max(c - 1, 0); cc < Math.min(c + 2, width); cc++) {
				if (rr == r && cc == c)
					continue;

				Tile tile = getTile(rr, cc);
				if (tile.isRevealed() && !tile.isZero())
					neighbors.add(tile);
			}
		}

		return neighbors;
	}

	/**
	 * Get the surrounding tiles that are not revealed
	 * 
	 * @param r
	 *            row of tile
	 * @param c
	 *            column of tile
	 * @return list of unrevealed neighbors
	 */
	private ArrayList<Tile> getUnrevealedNeighbors(int r, int c) {
		ArrayList<Tile> a = new ArrayList<Tile>();

		for (int rr = Math.max(r - 1, 0); rr < Math.min(r + 2, height); rr++) {
			for (int cc = Math.max(c - 1, 0); cc < Math.min(c + 2, width); cc++) {
				if (rr == r && cc == c)
					continue;

				Tile tile = getTile(rr, cc);
				if (!tile.isRevealed() && !tile.isFlagged())
					a.add(tile);
			}
		}

		return a;
	}

	/**
	 * Split list of tiles into tiles that are shared by both lists, and those
	 * that are not.
	 * 
	 * @param a1
	 *            list to split
	 * @param a2
	 *            list to compare against
	 * @return pair of a list of shared tiles and a list of not shared tiles
	 */
	private Pair<ArrayList<Tile>, ArrayList<Tile>> splitNeighbors(
			final ArrayList<Tile> a1, ArrayList<Tile> a2) {
		// if (a2.size() > a1.size())
		// return new ArrayList<Tile>();
		ArrayList<Tile> shared = new ArrayList<Tile>();
		ArrayList<Tile> unique = new ArrayList<Tile>();

		for (Tile tile : a1) {
			if (a2.contains(tile))
				shared.add(tile);
			else
				unique.add(tile);
		}

		return new Pair<ArrayList<Tile>, ArrayList<Tile>>(shared, unique);
	}

	/** Return all tiles that have not been revealed or flagged. */
	public ArrayList<Pair<Integer, Integer>> getGuessCandidates() {
		ArrayList<Pair<Integer, Integer>> a = new ArrayList<Pair<Integer, Integer>>(
				height * width);
		for (int r = 0; r < height; r++)
			for (int c = 0; c < width; c++)
				if (!mGrid.get(r).get(c).isRevealed()
						&& !mGrid.get(r).get(c).isFlagged())
					a.add(new Pair<Integer, Integer>(r, c));
		return a;
	}
}
