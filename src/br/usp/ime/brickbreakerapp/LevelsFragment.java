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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

public class LevelsFragment extends Fragment {
	public static final String TAG = "LevelsFragment";

	private int nLevels;
	private List<String> levelList;
	
	private View mLevelsView;
	private GridView mGridView;
	
	Integer[] imageIDs = {
            R.drawable.background_3,
            R.drawable.background_4,
            R.drawable.background_5,
            R.drawable.background_6,
            R.drawable.background_7,
            R.drawable.background_8
    };
	
	public LevelsFragment() {
		nLevels = 6;
		
		levelList = new ArrayList<String>();
		
		for(int i = 1; i <= nLevels; i++)
			levelList.add("Level " + i);
		
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
		
		mLevelsView = inflater.inflate(
				R.layout.fragment_levels, container, false);
		
		mGridView = (GridView) mLevelsView.findViewById(R.id.gridviewLevels);
		mGridView.setGravity(Gravity.CENTER);
		
		return mLevelsView;
	}
	
	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		
		//ArrayAdapter<String> levelAdapter = new ArrayAdapter<String>(
			//	getActivity(), android.R.layout.simple_list_item_1, levelList);
		
		
		mGridView.setAdapter(new ImageAdapter(getActivity().getApplicationContext(), 
				imageIDs));
		
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
	
	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
	}
	
	private static class Level {
		public String mLiveBricks;
		public float background;

	}
	
}