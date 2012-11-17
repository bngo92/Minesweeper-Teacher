package com.example.minesweeper.teacher;

import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Caches bitmap objects.
 * @author Bryan
 *
 */
public class BitmapCache {
	private Resources res;
	private HashMap<Integer, Bitmap> map;
	
	/**
	 * Initializes cache with resource object to decode bitmap id's.
	 * @param res The resources object containing the image data
	 */
	public BitmapCache(Resources res) {
		this.res = res;
		map = new HashMap<Integer, Bitmap>();
	}
	
	/**
	 * Caches any new bitmaps and returns the bitmap object associated with the id.
	 * @param id The resource id of the image data
	 * @return <li>The decoded bitmap of the corresponding image id
	 */
	public Bitmap getBitmap(int id) {
		if (!map.containsKey(id))
			map.put(id, BitmapFactory.decodeResource(res, id));
		return map.get(id);
	}

}
