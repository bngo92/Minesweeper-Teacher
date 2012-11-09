package com.example.minesweeper.teacher;

import android.os.Handler;
import android.widget.TextView;

public class Timer extends Handler {
	final int UPDATE_TIME = 10;
	
	private TextView textView;
	private int mMillis;
	
	Timer(TextView tv) {
		textView = tv;
		mMillis = 0;
	}

	public void startTimer() {
		resetTimer();
		removeCallbacks(updateTimer);
		postDelayed(updateTimer, UPDATE_TIME);
	}
	
	public void stopTimer() {
		removeCallbacks(updateTimer);
	}
	
	private Runnable updateTimer = new Runnable() {
		public void run() {	
			mMillis += UPDATE_TIME;
			textView.setText(getTime());
			postDelayed(updateTimer, UPDATE_TIME);
		}
	};
	
	public void resetTimer() {
		removeCallbacks(updateTimer);
		mMillis = 0;
		textView.setText("0.00");
	}
	
	public String getTime() {
		return String.format("%.2f", mMillis/1000.);
	}
}
