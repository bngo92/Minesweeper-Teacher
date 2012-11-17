package com.example.minesweeper.teacher;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Thread for drawing and updating the canvas.
 * @author Bryan
 *
 */
public class GameThread extends Thread {
	private GameView mGameView;
	private SurfaceHolder mHolder;
	private boolean mRun = false;
	
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
		while (mRun) {
			canvas = mHolder.lockCanvas();
			if (canvas != null) {
				mGameView.doDraw(canvas);
				mHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

}
