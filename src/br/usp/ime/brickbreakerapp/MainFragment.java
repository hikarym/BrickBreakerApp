package br.usp.ime.brickbreakerapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainFragment extends Fragment implements OnItemSelectedListener {
	
	private static final String TAG = "MainFragment";
	
	public MainFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main,
				container, false);
		
		setUpButtons(rootView);
		
		return rootView;
    }

    private void setUpButtons(View rootView) {/*
    	Button btPlay = (Button) rootView.findViewById(R.id.btPlay);
    	btPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.onClickPlay();
			}
		});
		
    	Button btLevels = (Button) rootView.findViewById(R.id.btLevels);
		btLevels.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.onClickLevels();
			}
		});
		
    	Button btOption = (Button) rootView.findViewById(R.id.btOption);
    	btOption.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.onClickOption();
			}
		});

    	Button btRanking = (Button) rootView.findViewById(R.id.btRanking);
    	btRanking.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.onClickRanking();
			}
		});
    	
    	Button btExit = (Button) rootView.findViewById(R.id.btExit);
    	btExit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.onClickExit();
				getActivity().finish();
			}
		});*/
    }
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
}