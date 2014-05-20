package br.usp.ime.brickbreakerapp;

import android.app.DialogFragment;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RankingFragment extends DialogFragment {
	public static final String TAG = "RankingFragment";
	
	private View mRankingView;
	private TableLayout mRankingTable;
	
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
		
		mRankingView = inflater.inflate(
				R.layout.fragment_ranking, container, false);
		
		mRankingTable = (TableLayout) mRankingView.findViewById(R.id.rankingTable);
		mRankingTable.setGravity(Gravity.CENTER);
		
		createTableTitle();
		createTableRows();
		
		Button button = (Button) mRankingView.findViewById(R.id.btRankingClose);
		button.setBackgroundResource(android.R.drawable.btn_dialog);
		
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Close dialog
            	getDialog().dismiss();
            }
        });
        
		return mRankingView;
	}
	
	private void createTableTitle() {
		Log.d(TAG, "createTableTitle()");
		
		// create a new TableRow
		TableRow row = new TableRow(getActivity());
		row.setGravity(Gravity.CENTER);
		row.setPadding(0, 0, 0, 16);
		
		TextView rankTextView = new TextView(getActivity());
		TextView usernameTextView = new TextView(getActivity());
		TextView scoreTextView = new TextView(getActivity());

		rankTextView.setText("Rank");
		usernameTextView.setText("Username");
		scoreTextView.setText("Score");

		rankTextView.setTextSize(20);
		usernameTextView.setTextSize(20);
		scoreTextView.setTextSize(20);

		rankTextView.setGravity(Gravity.CENTER);
		usernameTextView.setGravity(Gravity.CENTER);
		scoreTextView.setGravity(Gravity.CENTER);

		rankTextView.setTextColor(0xFFFFFFFF);
		usernameTextView.setTextColor(0xFFFFFFFF);
		scoreTextView.setTextColor(0xFFFFFFFF);

		// add the TextViews to the new TableRow
		row.addView(rankTextView);
		row.addView(usernameTextView);
		row.addView(scoreTextView);
		
		mRankingTable.addView(row);
	}
	
	private void createTableRows() {
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
		
		cursor.close();
	}
	
	private void createSingleTableRow(String rank, String username, String score) {
		Log.d(TAG, "createSingleTableRow()");
		
		// create a new TableRow
		TableRow row = new TableRow(getActivity());
		
		TextView rankTextView = new TextView(getActivity());
		TextView usernameTextView = new TextView(getActivity());
		TextView scoreTextView = new TextView(getActivity());
		
		rankTextView.setText(rank);
		usernameTextView.setText(username);
		scoreTextView.setText(score);
		
		rankTextView.setTextSize(20);
		usernameTextView.setTextSize(20);
		scoreTextView.setTextSize(20);

		rankTextView.setGravity(Gravity.CENTER);
		usernameTextView.setGravity(Gravity.CENTER);
		scoreTextView.setGravity(Gravity.CENTER);

		rankTextView.setTextColor(0xFFFFFFFF);
		usernameTextView.setTextColor(0xFFFFFFFF);
		scoreTextView.setTextColor(0xFFFFFFFF);

		// add the TextViews to the new TableRow
		row.addView(rankTextView);
		row.addView(usernameTextView);
		row.addView(scoreTextView);
		
		mRankingTable.addView(row);
	}

}