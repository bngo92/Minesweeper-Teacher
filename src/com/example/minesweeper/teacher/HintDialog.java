package com.example.minesweeper.teacher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class HintDialog extends DialogFragment {
	Game game;
	String message;
	
	public void setMessage(Game g, String m) {
		game = g;
		message = m;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				game.game.hint();
			}
		})
		.setNegativeButton("Undo", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				game.game.clearQueue(game);
			}
		});
		// Create the AlertDialog object and return it
		return builder.create();
	}
}
