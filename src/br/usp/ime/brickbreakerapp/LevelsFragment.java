package br.usp.ime.brickbreakerapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

public class LevelsFragment extends Fragment {
	public static final String TAG = "LevelsFragment";

	private int nLevels;
	private List<String> levelList;
	
	private View mLevelsView;
	private GridView mGridView;
	
	public LevelsFragment() {
		nLevels = 10;
		
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
		
		mGridView = (GridView) mLevelsView.findViewById(R.id.gridview);
		
		return mLevelsView;
	}
	
	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		
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
						default:
							Toast.makeText(getActivity(), "Level not found!", Toast.LENGTH_SHORT).show();
							
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
}