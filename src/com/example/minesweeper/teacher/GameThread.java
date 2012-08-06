package com.example.minesweeper.teacher;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
	private GameView mGameView;
	private SurfaceHolder mHolder;
	private boolean mRun = false;
	private long mStartTime;
	private int mElapsed;
	
	public GameThread(GameView gameView) {
		mGameView = gameView;
		mHolder = mGameView.getHolder();
	}
	
	public boolean getRunning () {
		return mRun;
	}
	
	public void setRunning(boolean run) {
		mRun = run;
	}
	
	@Override
	public void run() {
		Canvas canvas = null;
		mStartTime = System.currentTimeMillis();
		while (mRun) {
			canvas = mHolder.lockCanvas();
			if (canvas != null) {
				//mGameView.updateTime(mElapsed);
				Paint mPaint = new Paint();
				mPaint.setColor(Color.RED);
				canvas.drawText("" + mElapsed, 0, mGameView.maxY, mPaint);
				mGameView.doDraw(canvas);
				mElapsed = (int) (System.currentTimeMillis() - mStartTime);
				mHolder.unlockCanvasAndPost(canvas);
			}
			mStartTime = System.currentTimeMillis();
		}
	}

}
