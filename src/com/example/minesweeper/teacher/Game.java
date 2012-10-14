package com.example.minesweeper.teacher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class Game extends Activity {
	GameView game;
	Button hint;
	boolean guess;
	HintDialog hintDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);
		
		game = (GameView) findViewById(R.id.gameView1);
		hint = (Button) findViewById(R.id.button_hint);
		game.initGame(this);
		resetGuess();
		game.newGame();
		hintDialog = new HintDialog();
    }
    
    public void newGame(View view) {
    	resetGuess();
    	game.newGame();
    }
    
    public void hint(View view) {
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
    		hintDialog.setMessage(hintText);
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