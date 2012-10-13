package com.example.minesweeper.teacher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class Game extends Activity {
	
	TextView time;
	TextView count;
	GameView game;
	Button hint;
	boolean guess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);
		
		int size[] = getIntent().getExtras().getIntArray("size");
		guess = false;
		
		game = (GameView) findViewById(R.id.gameView1);
		time = (TextView) findViewById(R.id.textViewTime);
		count = (TextView) findViewById(R.id.textViewCount);
		hint = (Button) findViewById(R.id.button_hint);
		
		game.setTextView(time, count);
		game.setSize(size);
		game.initGame();
		game.newGame();
    }
    
    public void newGame(View view) {
    	game.newGame();
    }
    
    public void hint(View view) {
    	if (guess) {
    		game.guess();
    		hint.setText(R.string.button_hint);
    		guess = false;
    	} else if (!game.hint()) {
    		hint.setText(R.string.button_guess);
    		guess = true;
    	}
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