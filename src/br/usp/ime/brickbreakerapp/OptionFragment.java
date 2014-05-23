package br.usp.ime.brickbreakerapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class OptionFragment extends Fragment {
	public static final String TAG = "OptionFragment";
	
	// Fragment View
	private static View mOptionView;

	// Current user name
	private static String mCurrentUsername;
	
	public OptionFragment() {
		mCurrentUsername = MainActivity.getStrPref(MainActivity.USERNAME_KEY, MainActivity.DEFAULT_USERNAME);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(MainActivity.TAG, TAG + ".onCreateView");
		
		mOptionView = inflater.inflate(
				R.layout.fragment_option, container, false);
		
		setUpButtons();
		
		return mOptionView;
	}

	@Override
	public void onResume() {
		Log.d(MainActivity.TAG, TAG + ".onResume");
		
		super.onResume();
		
		updateControls();
	}
	
    //---Sets current user name
    public static void setCurrentUsername(String username) {
        mCurrentUsername = username;
    }
	
    //---Gets current user name
    public static String getCurrentUsername() {
        return mCurrentUsername;
    }
    
    private void setUpButtons() {
    	CheckBox soundEffectsEnabled = (CheckBox) mOptionView.findViewById(R.id.checkSound);
    	soundEffectsEnabled.setTextColor(Color.GREEN);
		soundEffectsEnabled.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			CheckBox cb = (CheckBox) v.findViewById(R.id.checkSound);

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
		
    	TextView btResetScore = (TextView) mOptionView.findViewById(R.id.btResetScore);
    	btResetScore.setOnTouchListener(new TextView.OnTouchListener() {
			
    		@Override
    		public boolean onTouch(View v, MotionEvent event) {
    			TextView tv = (TextView) v.findViewById(R.id.btResetScore);

    			if (MotionEvent.ACTION_DOWN == event.getAction()) {
    				tv.setTextSize(12);
    			}
    			
    			else if (MotionEvent.ACTION_UP == event.getAction()) {
    				tv.setTextSize(16);
    			}

    			// So it can be handled by onClick
    			return false;
    		}
		});
    }
    
/************************************* Handling saved preferences *********************************************/
    
	//---Sets the state of the UI controls to match our internal state
	protected void updateControls() {
		Log.d(MainActivity.TAG, TAG + ".updateControls");
		
		/*Spinner difficulty = (Spinner) findViewById(R.id.spinner_difficultyLevel);
		difficulty.setSelection(BrickBreakerActivity.getDifficultyIndex());
		
		CheckBox neverLoseBall = (CheckBox) findViewById(R.id.checkbox_neverLoseBall);
		neverLoseBall.setChecked(BrickBreakerActivity.getNeverLoseBall());
		*/
		
		//int box_id = getResources().getIdentifier("btn_check", "drawable", "android");
		int box_id = getResources().getIdentifier("btn_check_holo_dark", "drawable", "android");
		boolean isSoundEnabled = MainActivity.getBooPref(
				MainActivity.SOUND_EFFECTS_ENABLED_KEY, MainActivity.DEFAULT_SOUND_EFFECTS_STATUS);
		
		CheckBox soundEffectsEnabled = (CheckBox) mOptionView.findViewById(R.id.checkSound);
		soundEffectsEnabled.setChecked(isSoundEnabled);
		soundEffectsEnabled.setButtonDrawable(box_id);
		soundEffectsEnabled.setSoundEffectsEnabled(isSoundEnabled);
		
		mCurrentUsername = MainActivity.getStrPref(MainActivity.USERNAME_KEY, MainActivity.DEFAULT_USERNAME);
		
		Button btChangeUsername = (Button) mOptionView.findViewById(R.id.btChangeUsername);
		btChangeUsername.setText("User: " + mCurrentUsername);
		btChangeUsername.setSoundEffectsEnabled(isSoundEnabled);
		
		TextView btResetScore = (TextView) mOptionView.findViewById(R.id.btResetScore);
		btResetScore.setSoundEffectsEnabled(isSoundEnabled);
		
		Button btBackOption = (Button) mOptionView.findViewById(R.id.btBackOption);
		btBackOption.setSoundEffectsEnabled(isSoundEnabled);
	}
	
    public static class resetScoresFragment extends DialogFragment {
    	//private final BbSQliteHelper mAppDB = new BbSQliteHelper(getActivity());
        
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
    		return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Dialog))
			    		.setIcon(android.R.drawable.ic_dialog_alert)
			    		.setTitle(R.string.app_name)
			            .setMessage(R.string.msg_reset_scores)
			            .setNegativeButton(R.string.cancel, null)
			            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			    			@Override
			                public void onClick(DialogInterface dialog, int id) {
			                    // Reset scores
			            		MainActivity.getBbSQliteHelper().dropDatabaseTables();
			            		MainActivity.getBbSQliteHelper().createDatabaseTables();
			            		
			    				Toast.makeText(getActivity(),
			    						"All scores have been deleted",
			    						Toast.LENGTH_SHORT).show();
			                }
			            }).create();
        }
    }
}