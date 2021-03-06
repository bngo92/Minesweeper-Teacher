package com.example.minesweeper.teacher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * Container for all game elements.
 * <p>Responsible for setting up new games and processing hints.
 * @author Bryan
 *
 */
public class Game extends Activity {
	GameView gameView;
	Button hint;

	/** Automatic solver thread. */
	Handler hintTimer;
	HintDialog hintDialog;

	boolean guess;
	final int delay = 0;
	final static String GUESS = "guess";

    /**
     * Locates and initializes UI elements and game parameters.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);
		
		gameView = (GameView) findViewById(R.id.gameView1);
		hint = (Button) findViewById(R.id.button_hint);

		hintTimer = new Handler();
		hintDialog = new HintDialog();

		hint.setOnLongClickListener(new OnLongClickListener() {

			/** Start automatic solver when hint button is long clicked. */
			public boolean onLongClick(View v) {
				hintTimer.post(findHint);
				return true;
			}
			
		});
		
		gameView.initGame(this);
		resetGuess();
		gameView.newGame();
    }
    
	Runnable findHint = new Runnable() {
		/** 
		 * Starting point for automatic solver.
		 * 
		 * <p>Finds game actions that can be performed. 
		 * Queues found actions after delay. 
		 */
		public void run() {
			String s = gameView.hint();
			if (s != null && s != GUESS) {
				hintTimer.postDelayed(processHint, delay);
			}
		}
	};
	
	Runnable processHint = new Runnable() {
		/** 
		 * Performed queued actions. 
		 * Queues finding more actions to be performed after delay. 
		 */
		public void run() {
			gameView.hint();
			hintTimer.postDelayed(findHint, delay);
		}
	};
	
	/**
	 * Resets UI elements and game state.
	 * @param view
	 */
    public void newGame(View view) {
    	resetGuess();
    	gameView.newGame();
    }
    
    /**
     * Method is called when hint button is clicked.
     * <p>If currently guessing, a guess is performed.
     * Otherwise, if a hint can be found, a hint is stored in the queue
     * and a hint dialog is shown.
     * If not, turn on guessing.
     * <p>Cancels automatic solver if active.
     * @param view
     */
    public void hint(View view) {
    	hintTimer.removeCallbacks(findHint);
    	hintTimer.removeCallbacks(processHint);

    	// Try processing hint queue
    	if (gameView.processHintQueue())
    		return;
    	
    	// Guess if guess state has been previously set
    	if (guess) {
    		gameView.guess();
    		resetGuess();
    		return;
    	}
    	
    	String hintText = gameView.hint();
    	if (hintText == null) {
    		return;
    	} else if (hintText == GUESS) {
    		// Set hint button to guess state
    		hint.setText(R.string.button_guess);
    		guess = true;
    	} else {
    		hintDialog.setMessage(this, hintText);
    		hintDialog.show(getFragmentManager(), "");
    	}
    }
    
    /** Reset hint button text and turn off guessing. */
    public void resetGuess() {
		hint.setText(R.string.button_hint);
		guess = false;
    }
    
    public void scrollLeft(View view) {
    	gameView.scroll(0, -1);
    }
    
    public void scrollDown(View view) {
    	gameView.scroll(1, 0);
    }
    
    public void scrollUp(View view) {
    	gameView.scroll(-1, 0);
    }
    
    public void scrollRight(View view) {
    	gameView.scroll(0, 1);
    }

}