package com.example.minesweeper.teacher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void pickDifficulty(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        final Context context = this;
        
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
        	
			@Override
			public boolean onMenuItemClick(MenuItem item) {
        		int height;
        		int width;
        		int mines;
        		
            	switch (item.getItemId()) {
    	        	case R.id.difficulty_beginner:
    	        		height = 8;
    	        		width = 8;
    	        		mines = 10;
    	        		break;
    	        	case R.id.difficulty_intermediate:
    	        		height = 16;
    	        		width = 16;
    	        		mines = 40;
    	        		break;
    	        	case R.id.difficulty_advanced:
    	        		height = 16;
    	        		width = 30;
    	        		mines = 99;
    	        		break;
    	    		default:
    	    			return false;
            	}
            	
            	Intent intent = new Intent(context, Game.class);
            	intent.putExtra("height", height);
            	intent.putExtra("width", width);
            	intent.putExtra("mines", mines);
            	startActivity(intent);
            	return true;
			}
        });
        
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_difficulty, popup.getMenu());
        popup.show();
    }
    
}
