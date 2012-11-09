package com.example.minesweeper.teacher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;

public class Game extends Activity {
	GameView game;
	Button hint;
	boolean guess;
	HintDialog hintDialog;
	VictoryDialog victoryDialog;
	Handler mTimer;
	int delay = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);
		
		game = (GameView) findViewById(R.id.gameView1);
		hint = (Button) findViewById(R.id.button_hint);
		victoryDialog = new VictoryDialog();
		game.initGame(this);
		resetGuess();
		game.newGame();
		
		hintDialog = new HintDialog();
		mTimer = new Handler();
		hint.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				mTimer.post(solve);
				return true;
			}
			
		});
    }
    
	Runnable solve = new Runnable() {
		public void run() {
			String s = game.hint();
			if (s != null && s != "guess") {
				mTimer.postDelayed(reveal, delay);
			}
		}
	};
	
	Runnable reveal = new Runnable() {
		public void run() {
			game.hint();
			mTimer.postDelayed(solve, delay);
		}
	};
	
    public void newGame(View view) {
    	resetGuess();
    	game.newGame();
    }
    
    public void hint(View view) {
    	mTimer.removeCallbacks(solve);
    	mTimer.removeCallbacks(reveal);
    	if (guess) {
    		game.hint();
    		resetGuess();
    		return;
    	} 
    	
    	String hintText = game.hint();
    	if (hintText == null) {
    		return;
    	} else if (hintText == "guess") {
    		hint.setText(R.string.button_guess);
    		guess = true;
    	} else {
    		hintDialog.setMessage(this, hintText);
    		hintDialog.show(getFragmentManager(), "");
    	}
    }
    
    public void resetGuess() {
		hint.setText(R.string.button_hint);
		guess = false;
    }
    
    public void scrollLeft(View view) {
    	game.scroll(0, -1);
    }
    
    public void scrollDown(View view) {
    	game.scroll(1, 0);
    }
    
    public void scrollUp(View view) {
    	game.scroll(-1, 0);
    }
    
    public void scrollRight(View view) {
    	game.scroll(0, 1);
    }

}