package br.usp.ime.brickbreakerapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
	
	private static final String TAG = MainActivity.TAG;
	private static View mMainView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "MainFragment.onCreateView");
		
		mMainView = inflater.inflate(R.layout.fragment_main,
				container, false);
		
		setUp();
		
		return mMainView;
    }
	
	@Override
	public void onResume() {
		Log.d(TAG, "MainFragment.onResume");
		
		super.onResume();
		
		updateControls();
	}
	
    private void setUp() {
		Log.d(TAG, "MainFragment.setUp");
		
		TextView textViewTitleOption = (TextView) mMainView.findViewById(R.id.textViewTitleMain);
		textViewTitleOption.setTextColor(Color.RED);
		
    	TextView btPlay = (TextView) mMainView.findViewById(R.id.btPlay);
    	btPlay.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			TextView tv = (TextView) v.findViewById(R.id.btPlay);

    			if (MotionEvent.ACTION_DOWN == event.getAction()) {
    				tv.setTextColor(Color.MAGENTA);
    				tv.setTextSize(40);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(Color.BLACK);
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
    				tv.setTextColor(Color.GREEN);
    				tv.setTextSize(30);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(Color.BLACK);
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
    				tv.setTextColor(Color.BLUE);
    				tv.setTextSize(30);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(Color.BLACK);
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
    				tv.setTextColor(Color.YELLOW);
    				tv.setTextSize(30);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(Color.BLACK);
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
    				tv.setTextColor(Color.GRAY);
    				tv.setTextSize(30);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextColor(Color.BLACK);
    				tv.setTextSize(22);
    			}
    			
    			// So it can be handled by onClick
    			return false;
    		}
		});
    }

	//---Sets the state of the UI controls to match our internal state
	private static void updateControls() {
		Log.d(TAG, "MainFragment.updateControls");
		
		boolean isSoundEnabled = MainActivity.getBooPref(
				MainActivity.SOUND_EFFECTS_ENABLED_KEY, MainActivity.DEFAULT_SOUND_EFFECTS_STATUS);
		
		TextView btPlay = (TextView) mMainView.findViewById(R.id.btPlay);
		btPlay.setSoundEffectsEnabled(isSoundEnabled);
		
    	TextView btLevels = (TextView) mMainView.findViewById(R.id.btLevels);
		btLevels.setSoundEffectsEnabled(isSoundEnabled);
		
    	TextView btOption = (TextView) mMainView.findViewById(R.id.btOption);
    	btOption.setSoundEffectsEnabled(isSoundEnabled);
    	
    	TextView btRanking = (TextView) mMainView.findViewById(R.id.btRanking);
    	btRanking.setSoundEffectsEnabled(isSoundEnabled);
    	
    	TextView btExit = (TextView) mMainView.findViewById(R.id.btExit);
    	btExit.setSoundEffectsEnabled(isSoundEnabled);
    	
		/*
		
		Button resume = (Button) findViewById(R.id.button_resumeGame);
		resume.setEnabled(BrickBreakerActivity.canResumeFromSave());
		resume.setSoundEffectsEnabled(isSoundEnabled);
		
		*/
		
	}
}