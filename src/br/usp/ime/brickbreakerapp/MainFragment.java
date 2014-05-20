package br.usp.ime.brickbreakerapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class MainFragment extends Fragment {
	
	public static final String TAG = "MainFragment";
	private View mMainView;
	
	public MainFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mMainView = inflater.inflate(R.layout.fragment_main,
				container, false);
		
		setUpButtons();
		
		return mMainView;
    }

    private void setUpButtons() {
    	TextView btPlay = (TextView) mMainView.findViewById(R.id.btPlay);
    	btPlay.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			TextView tv = (TextView) v.findViewById(R.id.btPlay);

    			if (MotionEvent.ACTION_DOWN == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.green));
    				tv.setTextSize(40);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.white));
    				tv.setTextSize(32);
    			}

    			// So it can be handled by onClick
    			return false;
    		}
		});
		
    	TextView btLevels = (TextView) mMainView.findViewById(R.id.btLevels);
		btLevels.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			TextView tv = (TextView) v.findViewById(R.id.btLevels);

    			if (MotionEvent.ACTION_DOWN == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.green));
    				tv.setTextSize(30);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.white));
    				tv.setTextSize(22);
    			}

    			// So it can be handled by onClick
    			return false;
    		}
		});
		
    	TextView btOption = (TextView) mMainView.findViewById(R.id.btOption);
    	btOption.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			TextView tv = (TextView) v.findViewById(R.id.btOption);

    			if (MotionEvent.ACTION_DOWN == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.green));
    				tv.setTextSize(30);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.white));
    				tv.setTextSize(22);
    			}

    			// So it can be handled by onClick
    			return false;
    		}
		});

    	TextView btRanking = (TextView) mMainView.findViewById(R.id.btRanking);
    	btRanking.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			TextView tv = (TextView) v.findViewById(R.id.btRanking);

    			if (MotionEvent.ACTION_DOWN == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.green));
    				tv.setTextSize(30);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.white));
    				tv.setTextSize(22);
    			}

    			// So it can be handled by onClick
    			return false;
    		}
		});
    	
    	TextView btExit = (TextView) mMainView.findViewById(R.id.btExit);
    	btExit.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			TextView tv = (TextView) v.findViewById(R.id.btExit);
    			
    			if (MotionEvent.ACTION_DOWN == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.red));
    				tv.setTextSize(30);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(getResources().getColor(R.color.white));
    				tv.setTextSize(22);
    			}
    			
    			// So it can be handled by onClick
    			return false;
    		}
		});
    }
}