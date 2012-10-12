package com.example.minesweeper.teacher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class Game extends Activity {
	
	TextView time;
	TextView count;
	GameView game;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);
		
		int size[] = getIntent().getExtras().getIntArray("size");
		game = (GameView) findViewById(R.id.gameView1);
		time = (TextView) findViewById(R.id.textViewTime);
		count = (TextView) findViewById(R.id.textViewCount);
		game.setTextView(time, count);
		game.setSize(size);
		game.initGame();
		game.newGame();
    }
    
    public void newGame(View view) {
    	game.newGame();
    }
    
    public void hint(View view) {
    	game.hint();
    }
}