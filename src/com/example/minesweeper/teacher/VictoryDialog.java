package com.example.minesweeper.teacher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Simple alert dialog with a positive button. Used for displaying a message
 * when the game is won.
 * 
 * @author Bryan
 * 
 */
public class VictoryDialog extends DialogFragment {
	String message;

	public void setMessage(String m) {
		message = m;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message).setPositiveButton("Okay",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();
	}
}
