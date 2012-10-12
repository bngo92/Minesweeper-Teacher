package com.example.minesweeper.teacher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Spinner;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void startGame(View view) {
    	Intent intent = new Intent(this, Game.class);
    	Spinner spinner = (Spinner) findViewById(R.id.spinner1);
    	int index = spinner.getSelectedItemPosition();
    	int[] size;
    	if (index == 0) {
    		int temp[] = {8, 8, 10};
    		size = temp;
    	} else if (index == 1) {
    		int temp[] = {16, 16, 40};
    		size = temp;
    	} else {
    		int temp[] = {16, 30, 99};
    		size = temp;
    	}
    	intent.putExtra("size", size);
    	startActivity(intent);
    }
}
