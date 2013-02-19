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

	int r;
	int c;
	Action action;

	public TileAction(int r, int c, Action action) {
		this.r = r;
		this.c = c;
		this.action = action;
	}
}
