package com.example.minesweeper.teacher;

import android.os.Handler;
import android.widget.TextView;

public class Timer extends Handler {
	private TextView textView;
	private int mSeconds;
	
	Timer(TextView tv) {
		textView = tv;
		mSeconds = 0;
	}

	public void startTimer() {
		resetTimer();
		removeCallbacks(updateTimer);
		postDelayed(updateTimer, 1000);
	}
	
	public void stopTimer() {
		removeCallbacks(updateTimer);
	}
	
	private Runnable updateTimer = new Runnable() {
		public void run() {
			mSeconds++;
			textView.setText("" + mSeconds);
			postDelayed(updateTimer, 1000);
		}
	};
	
	public void resetTimer() {
		mSeconds = 0;
		textView.setText("0");
	}
}
