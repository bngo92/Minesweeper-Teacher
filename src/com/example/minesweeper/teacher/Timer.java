package com.example.minesweeper.teacher;

import java.util.Locale;

import android.os.Handler;
import android.widget.TextView;

/**
 * Simple timer object.
 * 
 * @author Bryan
 * 
 */
public class Timer extends Handler {
	/** How often timer is updated. */
	final int UPDATE_TIME = 10;

	private TextView textView;
	private boolean on;
	private long startTime;

	/**
	 * Constructor.
	 * 
	 * @param textView
	 *            TextView object to write to.
	 */
	Timer(TextView textView) {
		this.textView = textView;
		resetTimer();
	}

	/** Rest timer. */
	public void resetTimer() {
		removeCallbacks(updateTimer);

		on = false;
		textView.setText("0.00");
	}

	/** Start timer. */
	public void startTimer() {
		resetTimer();

		on = true;
		startTime = System.currentTimeMillis();
		postDelayed(updateTimer, UPDATE_TIME);
	}

	/** Update timer and write time to textView. */
	private Runnable updateTimer = new Runnable() {
		public void run() {
			textView.setText(getTime());
			postDelayed(updateTimer, UPDATE_TIME);
		}
	};

	/** Stop timer. */
	public void stopTimer() {
		removeCallbacks(updateTimer);
	}

	/** Return string of elapsed time with 2 decimal points. */
	public String getTime() {
		return String.format(Locale.getDefault(), "%.2f",
				(System.currentTimeMillis() - startTime) / 1000.);
	}

	/** Return whether timer has been started or not. */
	public boolean isOn() {
		return on;
	}
}
