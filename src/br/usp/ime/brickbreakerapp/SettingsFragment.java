package br.usp.ime.brickbreakerapp;

import android.app.DialogFragment;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SettingsFragment extends DialogFragment {
	public static final String TAG = "SettingsFragment";
	
	private View mSettingsView;
	private TableLayout mSettingsTable;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		//setStyle(style, theme)
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "onCreateView");
		
		//getDialog().setTitle(R.string.title_ranking);
		//getDialog().setTitle("Rank\tUsername\tScore");
		getDialog().setCancelable(true);
		
		mSettingsView = inflater.inflate(
				R.layout.fragment_settings, container, false);
		
		mSettingsTable = (TableLayout) mSettingsView.findViewById(R.id.settingsTable);
		mSettingsTable.setGravity(Gravity.CENTER);
		
		createTableTitle();
		//createTableRows();
		
		setUp();
		
		return mSettingsView;
	}

	@Override
	public void onResume() {
		Log.d(MainActivity.TAG, TAG + ".onResume");
		
		super.onResume();
		
		updateControls();
	}
	
    private void setUp() {
		Button button = (Button) mSettingsView.findViewById(R.id.btCloseSettings);
		button.setBackgroundResource(android.R.drawable.btn_dialog);
		
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Close dialog
            	getDialog().dismiss();
            }
        });
        
    	CheckBox soundEffectsEnabled = (CheckBox) mSettingsView.findViewById(R.id.checkSoundSettings);
    	soundEffectsEnabled.setTextColor(Color.GREEN);
		soundEffectsEnabled.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			CheckBox cb = (CheckBox) v.findViewById(R.id.checkSoundSettings);

    			if (MotionEvent.ACTION_DOWN == event.getAction()) {
    				cb.setTextSize(18);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				cb.setTextSize(16);
    			}

    			// So it can be handled by onClick
    			return false;
    		}
		});
    }
    
	//---Sets the state of the UI controls to match our internal state
	protected void updateControls() {
		Log.d(MainActivity.TAG, TAG + ".updateControls");
		
		//int box_id = getResources().getIdentifier("btn_check", "drawable", "android");
		int box_id = getResources().getIdentifier("btn_check_holo_dark", "drawable", "android");
		boolean isSoundEnabled = MainActivity.getBooPref(
				MainActivity.SFX_ENABLED_KEY, OptionFragment.DEFAULT_SFX_STATUS);
		
		CheckBox soundEffectsEnabled = (CheckBox) mSettingsView.findViewById(R.id.checkSoundSettings);
		soundEffectsEnabled.setChecked(isSoundEnabled);
		soundEffectsEnabled.setButtonDrawable(box_id);
		soundEffectsEnabled.setSoundEffectsEnabled(isSoundEnabled);
		
		Button btBackOption = (Button) mSettingsView.findViewById(R.id.btCloseSettings);
		btBackOption.setSoundEffectsEnabled(isSoundEnabled);
	}
	
	private void createTableTitle() {
		Log.d(TAG, "createTableTitle()");
		
		// create a new TableRow
		TableRow row = new TableRow(getActivity());
		row.setGravity(Gravity.CENTER);
		row.setPadding(0, 0, 0, 16);
		
		TextView brickTextView = new TextView(getActivity());
		TextView descriptionTextView = new TextView(getActivity());

		brickTextView.setText("Brick");
		descriptionTextView.setText("Description");

		brickTextView.setTextSize(20);
		descriptionTextView.setTextSize(20);

		brickTextView.setGravity(Gravity.CENTER);
		descriptionTextView.setGravity(Gravity.CENTER);

		brickTextView.setTextColor(Color.BLACK);
		descriptionTextView.setTextColor(Color.BLACK);

		// add the TextViews to the new TableRow
		row.addView(brickTextView);
		row.addView(descriptionTextView);
		
		mSettingsTable.addView(row);
	}
	
	private void createTableRows() {/*
		Log.d(TAG, "createTableRows()");
		
		Cursor cursor;
		int rank = 0;
		String username;
		String score;
		
		cursor = MainActivity.mAppDB.getAllScoresInfo();
		
		if (cursor != null) {
			while (cursor.moveToNext()) {
				rank++;
				username = cursor.getString(1);
				score = cursor.getString(2);
				
				createSingleTableRow("" + rank, username, score);
			}
		}
		
		cursor.close();*/
	}
	
	private void createSingleTableRow(String rank, String username, String score) {
		Log.d(TAG, "createSingleTableRow()");
		
		// create a new TableRow
		TableRow row = new TableRow(getActivity());
		
		TextView brickTextView = new TextView(getActivity());
		TextView descriptionTextView = new TextView(getActivity());
		
		brickTextView.setText(rank);
		descriptionTextView.setText(username);
		
		brickTextView.setTextSize(20);
		descriptionTextView.setTextSize(20);

		brickTextView.setGravity(Gravity.CENTER);
		descriptionTextView.setGravity(Gravity.CENTER);

		brickTextView.setTextColor(Color.BLACK);
		descriptionTextView.setTextColor(Color.BLACK);

		// add the TextViews to the new TableRow
		row.addView(brickTextView);
		row.addView(descriptionTextView);
		
		mSettingsTable.addView(row);
	}

}