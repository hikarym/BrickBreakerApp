package br.usp.ime.brickbreakerapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service  implements MediaPlayer.OnErrorListener {
	public final String TAG = "MusicService";
	
	private final IBinder mBinder = new ServiceBinder();
	private MediaPlayer mPlayer;
	private int length = 0;
	
	public MusicService() {
		
	}
	
	public class ServiceBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mPlayer = MediaPlayer.create(this, R.raw.music_destination);
		mPlayer.setOnErrorListener(this);
		
		if (mPlayer!= null) {
			mPlayer.setLooping(true);
			mPlayer.setVolume(100,100);
		}
		
		mPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				onError(mPlayer, what, extra);
				return true;
			}
		});
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		mPlayer.start();
		
		return START_STICKY;
	}
	
	public void pauseMusic() {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
			length=mPlayer.getCurrentPosition();
		}
	}
	
	public void resumeMusic() {
		if (mPlayer.isPlaying()==false) {
			mPlayer.seekTo(length);
			mPlayer.start();
		}
	}
	
	public void stopMusic() {
		mPlayer.stop();
		mPlayer.release();
		mPlayer = null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (mPlayer != null) {
			try {
				mPlayer.stop();
				mPlayer.release();
			} finally {
				mPlayer = null;
			}
		}
	}
	
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(TAG, "Music player failed");
		
		if (mPlayer != null) {
			try {
				mPlayer.stop();
				mPlayer.release();
			} finally {
				mPlayer = null;
			}
		}
		
		return false;
	}
}