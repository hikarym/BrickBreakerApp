package br.usp.ime.brickbreakerapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.GridView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
	
	private Context mContext;
	private Integer[] mImageIDs;
	public ImageAdapter(Context c, Integer[] imageIDs){
		mContext = c;
		mImageIDs = imageIDs;
	}
	
	//---returns the number of images---
	public int getCount() {
		return mImageIDs.length;
	}
	
	//---returns the ID of an item---
	public Object getItem(int position) {
		return position;
	}
	
	public long getItemId(int position) {
		return position;
	}
	
	//---returns an ImageView view---
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		
		if (convertView == null) {
			textView = new TextView(mContext);
			textView.setLayoutParams(new GridView.LayoutParams(185, 185));
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(Color.DKGRAY);
			textView.setPadding(5, 5, 5, 5);
		}
		
		else
			textView = (TextView) convertView;
		
		textView.setBackgroundResource(mImageIDs[position]);
		
		SpannableString ss1 = new SpannableString("Level "+ String.valueOf(position + 1));
		ss1.setSpan(new StyleSpan(Typeface.BOLD), 0, ss1.length(), 0);
		textView.setText(ss1);
		
		return textView;
	}
}
