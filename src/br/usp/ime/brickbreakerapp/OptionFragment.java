package br.usp.ime.brickbreakerapp;

import java.util.ArrayList;
import java.util.List;

import br.usp.ime.brickbreakerapp.sqlite.BbSQliteHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.Toast;

public class OptionFragment extends Fragment {
	public static final String TAG = "OptionFragment";
	
	private View mOptionView;

    // Enabled Sounds effects of game 
    private static boolean statusSoundEffects;
    
	public OptionFragment() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "onCreateView");
		
		mOptionView = inflater.inflate(
				R.layout.fragment_option, container, false);
		
		Button btChangeUsername = (Button) mOptionView.findViewById(R.id.btChangeUsername);
		btChangeUsername.setText("User: " + getActivity().getResources().getString(R.string.default_user));
    	
		return mOptionView;
	}
	
	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		
	}
	
	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
	}

    //---Gets sound effect status
    public static boolean getSoundEffects() {
        return statusSoundEffects;
    }

    //---Enables or disables sound effects
    public static void setSoundEffects(boolean soundEffectsEnabled) {
    	statusSoundEffects = soundEffectsEnabled;
    	/*
    	CheckBox soundEffectsCheckBox = (CheckBox) mOptionView.findViewById(R.id.checkSound);
        soundEffectsEnabled.setChecked(BrickBreakerActivity.getSoundEffectsEnabled());*/
    }
    
    public static class resetScoresFragment extends DialogFragment {
    	//private final BbSQliteHelper mAppDB = new BbSQliteHelper(getActivity());
        
    	public static resetScoresFragment newInstance(int title) {
    		resetScoresFragment frag = new resetScoresFragment();
    		Bundle args = new Bundle();
    		args.putInt("title", title);
    		frag.setArguments(args);
    		
    		return frag;
		}
    	
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
    		int title = getArguments().getInt("title");
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(
    				new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog))
		            .setIcon(android.R.drawable.ic_dialog_alert)
		            .setTitle(title)
		            .setMessage(R.string.msg_reset_scores)
		            .setCancelable(true)
		            .setNegativeButton(R.string.action_cancel, null)
		            .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
		    			@Override
		                public void onClick(DialogInterface dialog, int id) {
		                    // Reset scores
		            		MainActivity.mAppDB.dropDBTable();
		            		MainActivity.mAppDB.createDBTable();
		            		
		    				Toast.makeText(getActivity(),
		    						"All scores have been deleted",
		    						Toast.LENGTH_SHORT).show();
		    				
		            		//mAppDB.close();
		                }
		            });
		            /*
		            .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
		    			@Override
		                public void onClick(DialogInterface dialog, int id) {
		                    // Reset scores
		    				MainActivity.mAppDB.dropDBTable();
		    				MainActivity.mAppDB.createDBTable();
		    				
		            		//mAppDB.close();
		                }
		            });
    		*/
    		return builder.create();
        }
    }
}