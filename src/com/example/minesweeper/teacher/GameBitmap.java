package com.example.minesweeper.teacher;

import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GameBitmap {
	private Resources mRes;
	private HashMap<Integer, Bitmap> map;
	
	GameBitmap(Resources res) {
		mRes = res;
		map = new HashMap<Integer, Bitmap>();
	}
	
	public Bitmap getBitmap(int id) {
		if (!map.containsKey(id))
			map.put(id, BitmapFactory.decodeResource(mRes, id));
		return map.get(id);
	}

}
