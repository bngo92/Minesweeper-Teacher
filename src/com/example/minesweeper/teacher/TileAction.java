package com.example.minesweeper.teacher;

/**
 * Object for storing hints. Saves row, column, and the hint action to perform.
 * 
 * @author Bryan
 * 
 */
public class TileAction {
	enum Action {
		CLICK, FLAG
	};

	Tile tile;
	Action action;

	public TileAction(Tile t, Action a) {
		tile = t;
		action = a;
	}
}
