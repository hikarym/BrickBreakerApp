package br.usp.ime.brickbreakerapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RankingFragment extends Fragment {
	public static final String TAG = MainActivity.TAG;
	
	private View mRankingView;
	private TableLayout mRankingTable;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "RankingFragment.onCreateView");
		
		mRankingView = inflater.inflate(
				R.layout.fragment_ranking, container, false);
		
		mRankingTable = (TableLayout) mRankingView.findViewById(R.id.rankingTable);
		mRankingTable.setGravity(Gravity.CENTER);
		
		createTableTitle();
		createTableRows();
		
		return mRankingView;
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "RankingFragment.onResume");
		
		super.onResume();
		
		updateControls();
	}
	
	private void createTableTitle() {
		Log.d(TAG, "RankingFragment.createTableTitle()");
		
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

		rankTextView.setTextColor(Color.WHITE);
		usernameTextView.setTextColor(Color.WHITE);
		scoreTextView.setTextColor(Color.WHITE);

		// add the TextViews to the new TableRow
		row.addView(rankTextView);
		row.addView(usernameTextView);
		row.addView(scoreTextView);
		
		mRankingTable.addView(row);
	}
	
	private void createTableRows() {
		Log.d(TAG, "RankingFragment.createTableRows()");
		
		Cursor cursor;
		int rank = 0;
		String username;
		String score;
		
		cursor = MainActivity.getBbSQliteHelper().getAllScoresInfo();
		
		while (cursor.moveToNext()) {
			rank++;
			username = cursor.getString(1);
			score = cursor.getString(2);
			
			createSingleTableRow("" + rank, username, score);
		}
		
		cursor.close();
	}
	
	private void createSingleTableRow(String rank, String username, String score) {
		Log.d(TAG, "RankingFragment.createSingleTableRow()");
		
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

		rankTextView.setTextColor(Color.BLACK);
		usernameTextView.setTextColor(Color.BLACK);
		scoreTextView.setTextColor(Color.BLACK);

		// add the TextViews to the new TableRow
		row.addView(rankTextView);
		row.addView(usernameTextView);
		row.addView(scoreTextView);
		
		mRankingTable.addView(row);
	}
	
	//---Sets the state of the UI controls to match our internal state
	private void updateControls() {
		Log.d(TAG, "LevelsFragment.updateControls");
		
		boolean isSoundEnabled = MainActivity.getBooPref(
				MainActivity.SOUND_EFFECTS_ENABLED_KEY, MainActivity.DEFAULT_SOUND_EFFECTS_STATUS);
		
		Button btBackRanking = (Button) mRankingView.findViewById(R.id.btBackRanking);
		btBackRanking.setSoundEffectsEnabled(isSoundEnabled);
	}
}