package br.usp.ime.brickbreakerapp;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;

public class LevelsFragment extends Fragment {
	public static final String TAG = "LevelsFragment";

    public static final int MIN_LEVEL = 1;
	public static final int MAX_LEVEL = 7;
	
	private static Integer[] mImageIDs = {
			            R.drawable.background_1,
			            R.drawable.background_2,
			            R.drawable.background_3,
			            R.drawable.background_4,
			            R.drawable.background_5,
			            R.drawable.background_6,
			            R.drawable.background_7
					};
	
	private View mLevelsView;
	private GridView mGridView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(MainActivity.TAG, TAG + ".onCreateView");
		
		mLevelsView = inflater.inflate(
				R.layout.fragment_levels, container, false);
		
		mGridView = (GridView) mLevelsView.findViewById(R.id.gridviewLevels);
		mGridView.setGravity(Gravity.CENTER);
		
		setUpGridView();
		
		return mLevelsView;
	}
	
	@Override
	public void onResume() {
		Log.d(MainActivity.TAG, TAG + ".onResume");
		
		super.onResume();
		
		updateControls();
	}
	
	private void setUpGridView() {
		Log.d(MainActivity.TAG, TAG + ".setUpGridView");
		
		mGridView.setAdapter(new ImageAdapter(getActivity().getApplicationContext(), mImageIDs));
		
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
				int position, long id) {
					Intent intent = null;
					
					position++;
					if(position >= MIN_LEVEL && position <= MAX_LEVEL){
						BrickBreakerActivity.setLevelIndex(position);
						intent = new Intent(getActivity(), BrickBreakerActivity.class);
				        startActivity(intent);
					}
					else{
						AlertDialog.Builder builder = new AlertDialog.Builder(
								new ContextThemeWrapper(getActivity(), android.R.style.Theme_Dialog));
						
						builder.setTitle("Error on load!");
						builder.setIcon(android.R.drawable.ic_dialog_alert);
						
						builder.setMessage("Level " + (position) + " not found!");
						builder.setPositiveButton(R.string.ok, null);
						builder.show();
					}
					
				}
		});
	}
	
	//---Sets the state of the UI controls to match our internal state
	private void updateControls() {
		Log.d(MainActivity.TAG, TAG + ".updateControls");
		
		boolean isSoundEnabled = MainActivity.getBooPref(
				MainActivity.SFX_ENABLED_KEY, MainActivity.DEFAULT_SFX_STATUS);
		
		Button btBackLevels = (Button) mLevelsView.findViewById(R.id.btBackLevels);
		btBackLevels.setSoundEffectsEnabled(isSoundEnabled);
		
		mGridView.setSoundEffectsEnabled(isSoundEnabled);
	}
	
}