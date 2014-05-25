package br.usp.ime.brickbreakerapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Generate and play sound data.
 * <p>
 * The initialize() method must be called before any sounds can be played.
 */
public class SoundResources implements SoundPool.OnLoadCompleteListener {
    private static final String TAG = MainActivity.TAG;

    // Pass these as arguments to playSound().
    public static final int BRICK_HIT = 0;
    public static final int PADDLE_HIT = 1;
    public static final int WALL_HIT = 2;
    public static final int BALL_LOST = 3;
    private static final int NUM_SOUNDS = 4;
    // Sources
    public static final String BRICK_HIT_ASSET = "explosion.ogg";
    public static final String PADDLE_HIT_ASSET = "laser.ogg";
    public static final String WALL_HIT_ASSET = "laser.ogg";
    public static final String BALL_LOST_ASSET = "alarm.ogg";

    // Singleton instance.
    private static SoundResources sSoundResources;
    // Handles reproduction of sounds.
    private static AssetManager assetManager;

    // Maximum simultaneous sounds.  
    private static final int MAX_STREAMS = 20;

    private static boolean sSoundEffectsEnabled = true;

    // The actual sound data.  Must be "final" for immutability guarantees.
    private final Sound[] mSounds = new Sound[NUM_SOUNDS];


    /**
     * Initializes global data.  We have a small, fixed set of sounds, so we just load them all
     * statically.  Call this when the game activity starts.
     * <p>
     * We need the application context to figure out where files will live.
     */
    public static synchronized void initialize(Context context) {

        if (sSoundResources == null) {
            File dir = context.getFilesDir();
            // The AssetManager handle reproduction of sounds
            assetManager = context.getAssets();
            sSoundResources = new SoundResources(dir);
            
        }
    }

    /**
     * Starts playing the specified sound.
     */
    public static void play(int soundNum) {

        if (SoundResources.sSoundEffectsEnabled) {
            SoundResources instance = sSoundResources;
            if (instance != null) {
                instance.mSounds[soundNum].play();
            }
        }
    }

    /**
     * Sets the "sound effects enabled" flag.  If disabled, sounds will still be loaded but
     * won't be played.
     */
    public static void setSoundEffectsEnabled(boolean enabled) {
        sSoundEffectsEnabled = enabled;
    }

    /**
     * Instantiate the SOUNDPOOL
     */
    private SoundResources(File privateDir) {
    	SoundPool soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(this);
        generateSoundFiles(soundPool, privateDir);        
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (status != 0) {
            Log.w(TAG, "onLoadComplete: pool=" + soundPool + " sampleId=" + sampleId
                    + " status=" + status);
        }
    }

    /**
     * Generates all sounds.
     */
    private void generateSoundFiles(SoundPool soundPool, File privateDir) {
        mSounds[BRICK_HIT] = generateSound(soundPool, BRICK_HIT_ASSET);
        mSounds[PADDLE_HIT] = generateSound(soundPool,PADDLE_HIT_ASSET);
        mSounds[WALL_HIT] = generateSound(soundPool, WALL_HIT_ASSET);
        mSounds[BALL_LOST] = generateSound(soundPool, BALL_LOST_ASSET);
    }

    /**
     * Generate a sound with specific characteristics.
     */
    private Sound generateSound(SoundPool soundPool, String asset) {
        
        //sound
    	int audio = -1;
        
    	try {
			// Loading the sound			
			AssetFileDescriptor sound2 = assetManager.openFd(asset);
			audio = soundPool.load(sound2, 1);
			
			Log.v(TAG, "aud:"+String.valueOf(audio));
			
		} catch (IOException e) {
			Log.v(TAG, "ERROR SOUND");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return new Sound(soundPool, audio);
    }   

    /**
     * A self-contained sound effect.
     */
    private static class Sound {
        private SoundPool mSoundPool;
        private int mSoundHandle;    // SoundPool handle
        private float mVolume = 1.0f;// 0.5f

        /**
         * Creates a new sound for a SoundPool entry.
         *
         * @param soundPool The SoundPool that holds the sound.
         * @param handle The handle for the sound within the SoundPool.
         */
        public Sound(SoundPool soundPool, int soundHandle) {
            mSoundPool = soundPool;
            mSoundHandle = soundHandle;
        }

        // Plays the sound.         
        public void play() {
            //Log.d(TAG, "SOUND: play '" + mName + "' @" + rate);
            mSoundPool.play(mSoundHandle, mVolume, mVolume, 1, 0, 1.0f);
            //mSoundPool.play(mSoundHandle, 1.0f, 1.0f, 1, 0, 1);
        }
    }
}
