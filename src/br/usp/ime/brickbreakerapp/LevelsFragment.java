package br.usp.ime.brickbreakerapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class LevelsFragment extends Fragment {
	public static final String TAG = MainActivity.TAG;

	private static int nLevels = 5;
	private List<String> levelList;
	
	private View mLevelsView;
	private GridView mGridView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "LevelsFragment.onCreate");
		
		super.onCreate(savedInstanceState);
		
		levelList = new ArrayList<String>();
		
		for(int i = 1; i <= nLevels; i++)
			levelList.add("Level " + i);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "LevelsFragment.onCreateView");
		
		mLevelsView = inflater.inflate(
				R.layout.fragment_levels, container, false);
		
		mGridView = (GridView) mLevelsView.findViewById(R.id.gridviewLevels);
		mGridView.setGravity(Gravity.CENTER);
		
		setUpGridView();
		
		return mLevelsView;
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "OptionFragment.onResume");
		
		super.onResume();
		
		updateControls();
	}
	
	private void setUpGridView() {
		Log.d(TAG, "LevelsFragment.setUpGridView");
		
		ArrayAdapter<String> levelAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_list_item_1, levelList);
		
		mGridView.setAdapter(levelAdapter);
		
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
				int position, long id) {
					Intent intent = null;
					
					switch (position) {
						case 0:
							intent = new Intent(getActivity(), BrickBreakerActivity.class);
					        startActivity(intent);
					        
							break;
						case 1:
							break;
						case 2:
							break;
						case 3:
							break;
						case 4:
							break;
						case 5:
							break;
						case 6:
							break;
						case 7:
							break;
						case 8:
							break;
						case 9:
							break;
						case 10:
							break;
						case 11:
							break;
						case 12:
							break;
						case 13:
							break;
						case 14:
							break;
						case 15:
							break;
						case 16:
							break;
						case 17:
							break;
						case 18:
							break;
						case 19:
							break;
						default:
							AlertDialog.Builder builder = new AlertDialog.Builder(
									new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog));
							
							builder.setTitle("Error on load!");
							builder.setIcon(android.R.drawable.ic_dialog_alert);
							
							builder.setMessage("Level " + (position + 1) + " not found!");
							builder.setPositiveButton(R.string.ok, null);
							builder.show();
							
							break;
					}
				}
		});
	}
	
	//---Sets the state of the UI controls to match our internal state
	private void updateControls() {
		Log.d(TAG, "LevelsFragment.updateControls");
		
		boolean isSoundEnabled = MainActivity.getBooPref(
				MainActivity.SOUND_EFFECTS_ENABLED_KEY, MainActivity.DEFAULT_SOUND_EFFECTS_STATUS);
		
		Button btBackLevels = (Button) mLevelsView.findViewById(R.id.btBackLevels);
		btBackLevels.setSoundEffectsEnabled(isSoundEnabled);
		
		mGridView.setSoundEffectsEnabled(isSoundEnabled);
	}
}