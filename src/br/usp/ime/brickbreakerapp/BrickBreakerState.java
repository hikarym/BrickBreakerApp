package br.usp.ime.brickbreakerapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.Log;

/*
 * Primary class for the game
 */
public class BrickBreakerState {
	private static final String TAG = MainActivity.TAG;
	public static final boolean DEBUG_COLLISIONS = false;       // enable increased logging
	public static final boolean SHOW_DEBUG_STUFF = false;       // enable on-screen debugging
	
	// Gameplay configurables.  These may not be changed while the game is in progress, and
	// changing a value invalidates the saved game.	
	private int mMaxLives = 3;
	private int mBallInitialSpeed = 300;
	private int mBallMaximumSpeed = 800;
	private float mBallSizeMultiplier = 1.0f;
	private float mPaddleSizeMultiplier = 1.0f;
	private float mScoreMultiplier = 1.0f;
	private float mButtonMultiplier = 1.0f;
	
	/*
	 * State of each brick
	 * 0: empty,
	 * 1: normal,
	 * 2: the brick is destroyed with 2 hits,
	 * 3: the brick is destroyed with 3 hits,
	 * 4: if this brick is hit, then the game finishes)
	 */
	private static final int BRICK_EMPTY = 0;
	private static final int BRICK_NORMAL = 1;
	private static final int BRICK_2HITS = 2;
	private static final int BRICK_3HITS = 3;
	private static final int BRICK_ESPECIAL1 = 4;
	private static final int BRICK_ESPECIAL2 = 5;
	
	// Number of brick states
	private static final int BRICK_STATES = 6;
	// Number of levels
	private static final int BRICK_LEVELS = 6;
	private int[][] mBrickStatesConfig = new int[BRICK_ROWS][BRICK_COLUMNS];
	private Bitmap[] mBMPBrickTexture = new Bitmap[BRICK_STATES - 1];
	private Bitmap[] mBMPBkgLevel = new Bitmap[BRICK_LEVELS];
	

	// In-memory saved game.  The game is saved and restored whenever the Activity is paused
	// and resumed.  This should be the only static variable in BrickBreakerState.
	private static SavedGame sSavedGame = new SavedGame();


	static final float ARENA_WIDTH = 768.0f;
	static final float ARENA_HEIGHT = 1024.0f;


	private static final float BRICK_TOP_PERC = 85 / 100.0f;
	private static final float BRICK_BOTTOM_PERC = 43 / 100.0f;
	private static final float BORDER_WIDTH_PERC = 2 / 100.0f;
	public static final int BRICK_COLUMNS = 9;//12
	public static final int BRICK_ROWS = 6;//8
	

	private static final float BORDER_WIDTH = (int) (BORDER_WIDTH_PERC * ARENA_WIDTH);
	private static final float SCORE_TOP = ARENA_HEIGHT - BORDER_WIDTH * 2;
	private static final float SCORE_RIGHT = ARENA_WIDTH - BORDER_WIDTH * 2;
	private static final float SCORE_HEIGHT_PERC = 5 / 100.0f;
	//space between columns(in percentage) 
	private static final float BRICK_HORIZONTAL_GAP_PERC = 20 / 100.0f;
	//space between rows(in percentage) 
	private static final float BRICK_VERTICAL_GAP_PERC = 20 / 100.0f;// 50 /  100.0f
	

	private static final float PADDLE_VERTICAL_PERC = 12 / 100.0f;
	private static final float PADDLE_HEIGHT_PERC = 3 / 100.0f; //1/100.0f
	private static final float PADDLE_WIDTH_PERC = 2 / 100.0f;
	private static final int PADDLE_DEFAULT_WIDTH = 8; //6
	private static final float BALL_WIDTH_PERC = 5.5f / 100.0f; //2.5f / 100.0f;
	private static final float BUTTON_WIDTH_PERC = 2 / 100.0f;
	private static final float BUTTON_HEIGHT_PERC = 2 / 100.0f;
	private static final float BUTTON_DEFAULT_WIDTH = 8;

	private static final int NUM_BORDERS = 4;
	private static final int BOTTOM_BORDER = 0;
	private BasicAlignedRect mBorders[] = new BasicAlignedRect[NUM_BORDERS];
	private BasicAlignedRect mBackgroundColor;
	private TexturedBasicAlignedRect mBackgroundImg;
	private TexturedBasicAlignedRect mButtonRestart;
	private TexturedBasicAlignedRect mButtonMenu;
	private TexturedBasicAlignedRect mButtonQuit;
	/*Images for textures*/
	private String mBrickNormalTextureImg = "drawable/brick_normal";
	private String mBrickRockTextureImg = "drawable/brick_rock";
	private String mBrickMixTextureImg = "drawable/brick_mix";
	private String mBrickEspecial1TextureImg = "drawable/brick_especial_1";
	private String mBrickEspecial2TextureImg = "drawable/brick_pig_small";
	private String mBackgroundTextureImg = "drawable/background_3";
	private String mPaddleTextureImg = "drawable/paddle";
	private String mBallTextureImg = "drawable/ball_angry_red";
	private String mButtonQuitTextureImg = "drawable/exit";
	private String mButtonNextLevelTextureImg = "drawable/next";
	private String mButtonBackTextureImg = "drawable/back";
	private String mButtonSettingsTextureImg = "drawable/settings";
	private String mButtonReloadLevelTextureImg = "drawable/reload";

	// Button size
	private static final int DEFAULT_BUTTON_WIDTH = (int) (ARENA_WIDTH * BUTTON_WIDTH_PERC * BUTTON_DEFAULT_WIDTH);
	private static final int DEFAULT_BUTTON_HEIGHT = (int) (ARENA_WIDTH * BUTTON_HEIGHT_PERC * BUTTON_DEFAULT_WIDTH);

	private Brick mBricks[][] = new Brick[BRICK_ROWS][BRICK_COLUMNS];
	private int mLiveBrickCount;

	private static final int DEFAULT_PADDLE_WIDTH =
			(int) (ARENA_WIDTH * PADDLE_WIDTH_PERC * PADDLE_DEFAULT_WIDTH);
	private TexturedBasicAlignedRect mPaddle;
	//Buttons 
	private TexturedBasicAlignedRect mQuit;
	private TexturedBasicAlignedRect mNextLevel;
	private TexturedBasicAlignedRect mBack;
	private TexturedBasicAlignedRect mSettings;
	private TexturedBasicAlignedRect mReload;

	private static final int DEFAULT_BALL_DIAMETER = (int) (ARENA_WIDTH * BALL_WIDTH_PERC);
	private Ball mBall;

	private static final double NANOS_PER_SECOND = 1000000000.0;
	private static final double MAX_FRAME_DELTA_SEC = 0.5;
	private long mPrevFrameWhenNsec;

	/*
	 * Pause briefly on certain transitions, e.g. before launching a new ball after one was lost.
	 */
	private float mPauseDuration;

	private int mDebugSlowMotionFrames;

	// If FRAME_RATE_SMOOTHING is true, then the rest of these fields matter.
	private static final boolean FRAME_RATE_SMOOTHING = false;
	private static final int RECENT_TIME_DELTA_COUNT = 5;
	double mRecentTimeDelta[] = new double[RECENT_TIME_DELTA_COUNT];
	int mRecentTimeDeltaNext;

	/*
	 * Storage for collision detection results.
	 */
	private static final int HIT_FACE_NONE = 0;
	private static final int HIT_FACE_VERTICAL = 1;
	private static final int HIT_FACE_HORIZONTAL = 2;
	private static final int HIT_FACE_SHARPCORNER = 3;
	private BaseRect[] mPossibleCollisions =
			new BaseRect[BRICK_COLUMNS * BRICK_ROWS + NUM_BORDERS + NUM_SCORE_DIGITS + 1/*paddle*/];
	private float mHitDistanceTraveled;     // result from findFirstCollision()
	private float mHitXAdj, mHitYAdj;       // result from findFirstCollision()
	private int mHitFace;                   // result from findFirstCollision()
	private OutlineAlignedRect mDebugCollisionRect;  // visual debugging

	/*
	 * Game play state.
	 */
	public static final int GAME_INITIALIZING = 0;
	public static final int GAME_READY = 1;
	public static final int GAME_PLAYING = 2;
	public static final int GAME_WON = 3;
	public static final int GAME_LOST = 4;
	public static final int GAME_PAUSE = 5;
	private int mGamePlayState;

	private boolean mIsAnimating;
	private int mLivesRemaining;
	private int mScore;
	// Level of the game
	private int mGameLevel = 1;
	// Brick configuration in strings
	private String[] mBricksConfigStrings = new String[BRICK_ROWS];

	/*
	 * Events that can happen when the ball moves.
	 */
	private static final int EVENT_NONE = 0;
	private static final int EVENT_LAST_BRICK = 1;
	private static final int EVENT_BALL_LOST = 2;

	/*
	 * Text message to display in the middle of the screen (e.g. "won" or "game over").
	 */
	private static final float STATUS_MESSAGE_WIDTH_PERC = 85 / 100.0f;
	private TexturedAlignedRect mGameStatusMessages;
	private int mGameStatusMessageNum;
	private int mDebugFramedString;

	/* Score display */
	private static final int NUM_SCORE_DIGITS = 5;
	private TexturedAlignedRect[] mScoreDigits = new TexturedAlignedRect[NUM_SCORE_DIGITS];

	/* Text resources, notably including an image texture for our various text strings. */
	private TextResources mTextRes;

	public void setGamePlayState(int state){
		mGamePlayState = state; 
	}

	public int getGamePlayState(){
		return mGamePlayState;
	}
	
	public void setGameLevel(int level){
		mGameLevel = level; 
	}

	public int getGameLevel(){
		return mGameLevel;
	}
	
	public BrickBreakerState() {
		//Values default for brick States		
		// NIVEL I: NORMAL BRICKS
		//String[] configStr = new String[]{"111111111","111111111", "111111111", "111111111", "111111111", "111111111"};
		// NIVEL II: Letter I
		//String[] configStr = new String[]{"001111100","001111100", "000232000", "000232000", "001111100", "001111100"};
		// NIVEL III: FACE
		//String[] configStr = new String[]{"000111000", "111000111", "011111110", "111414111", "101111101", "000101000"};		
		// NIVEL IV: CASTLE
		//String[] configStr = new String[]{"021222120", "021222120", "021222120", "021111120", "222222222", "220222522"};
		// NIVEL V : (SNAKE)
		//String[] configStr = new String[]{"333033303", "202020202", "202020202", "202020202", "202020202", "303330333"};
		// NIVEL VI : USP
		//String[] configStr = new String[]{"222222222", "111333100", "101003100", "101333111", "101300101", "101333111"};
		//buildBrickStatesConfig(configStr);
	}
	
	
	
	/**
	 * Build a brick configuration of the game
	 * (Each brick must be a value between 0 e 4) 
	 * @param configStr: array[001111100, 001111100, 000232000, 000232000, 001111100, 001111100])
	 * (The array must be BRICK_ROWS elements and 
	 * each string must be have BRICK_COLUMNS characters)
	 * 
	 */
	private void buildBrickStatesConfig(String[] configStr){
		
		for (int i = 0; i < BRICK_ROWS; i++) {
			
			for (int j = 0; j < BRICK_COLUMNS; j++) {				
				mBrickStatesConfig[i][j] = Integer.parseInt(
						String.valueOf(configStr[i].charAt(j)));
			}
		}
		
	}
	
	public void setBackgroundLevel(String src){
		mBackgroundTextureImg = src;
	}

	public void setMaxLives(int maxLives) {
		mMaxLives = maxLives;
	}
	public void setBallInitialSpeed(int speed) {
		mBallInitialSpeed = speed;
	}
	public void setBallMaximumSpeed(int speed) {
		mBallMaximumSpeed = speed;
	}
	public void setBallSizeMultiplier(float mult) {
		mBallSizeMultiplier = mult;
	}
	public void setPaddleSizeMultiplier(float mult) {
		mPaddleSizeMultiplier = mult;
	}
	public void setScoreMultiplier(float mult) {
		mScoreMultiplier = mult;
	}
	/**
	 * Set brick configuration in an matrix
	 * i.e.: int[][]
	 * [[1 1 1 1 1 1 1 1 1],[1 1 1 1 1 1 1 1 1], [1 1 1 1 1 1 1 1 1], 
	 *  [1 1 1 1 1 1 1 1 1],[1 1 1 1 1 1 1 1 1], [1 1 1 1 1 1 1 1 1]]
	 * @param brickStatesConfig
	 */
	public void setBrickStatesConfig(int[][] brickStatesConfig){
		mBrickStatesConfig = brickStatesConfig;
	}
	
	private int genRandomNumber(int min, int max){
		//int minR = 1;
		//int maxR = BMPs.length;
		return min + (int)(Math.random() * ((max - min) + 1));
	}

	/**
	 * Resets game state to initial values.  Does not reallocate any storage or access saved
	 * game state.
	 */
	private void reset() {
		/*
		 * This is called when we're asked to restore a game, but no saved game exists.  The
		 * various objects (e.g. bricks) have already been initialized.  If a saved game
		 * does exist, we'll never call here, so don't treat this like a constructor.
		 */

		//mGamePlayState = GAME_INITIALIZING;
		mGamePlayState = GAME_PAUSE;
		//mIsAnimating = true;
		mIsAnimating = false;
		//mGameStatusMessageNum = TextResources.NO_MESSAGE;
		mGameStatusMessageNum = TextResources.READY;
		mPrevFrameWhenNsec = 0;
		mPauseDuration = 0.0f;
		mRecentTimeDeltaNext = -1;
		mLivesRemaining = mMaxLives;
		mScore = 0;
		mGameLevel = 1;
		resetBall();
		//mLiveBrickCount = 0;      // initialized by allocBricks
		resetPaddle();
	}

	/**
	 * Moves the ball to its start position, resetting direction and speed to initial values.
	 */
	private void resetBall() {
		mBall.setDirection(-0.3f, -1.0f);
		mBall.setSpeed(mBallInitialSpeed);

		mBall.setPosition(ARENA_WIDTH / 2.0f + 45, ARENA_HEIGHT * BRICK_BOTTOM_PERC - 100);
	}
	
	/**
	 * Moves the paddle to its start position.
	 */
	private void resetPaddle(){
		mPaddle.setPosition(ARENA_WIDTH / 2.0f, ARENA_HEIGHT * PADDLE_VERTICAL_PERC);
		//rect.setPosition();
	}
	
	/**
	 * Reset score
	 */
	private void resetScore(){
		//mScore
		//mScoreDigits.
	}
	
	/**
	 * Reset the bricks distribution
	 */
	private void resetBricks(){
		
	}

	/**
	 * Saves game state into static storage.
	 */
	public void save() {

		synchronized (sSavedGame) {
			SavedGame save = sSavedGame;

			int[][] bricks = new int[BRICK_ROWS][BRICK_COLUMNS];
			for (int i = 0; i < BRICK_ROWS; i++) {
				for (int j = 0; j < BRICK_COLUMNS; j++) {
					bricks[i][j] = mBricks[i][j].getBrickState();
				}
			}
			save.mBricksState = bricks;

			save.mBallXDirection = mBall.getXDirection();
			save.mBallYDirection = mBall.getYDirection();
			save.mBallXPosition = mBall.getXPosition();
			save.mBallYPosition = mBall.getYPosition();
			save.mBallSpeed = mBall.getSpeed();
			save.mPaddlePosition = mPaddle.getXPosition();

			save.mGamePlayState = mGamePlayState;
			save.mGameStatusMessageNum = mGameStatusMessageNum;
			save.mLivesRemaining = mLivesRemaining;
			save.mScore = mScore;

			save.mIsValid = true;
		}

		//Log.d(TAG, "game saved");
	}

	/**
	 * Restores game state from save area.  If no saved game is available, we just reset
	 * the values.
	 *
	 * @return true if we restored from a saved game.
	 */
	public boolean restore() {
		synchronized (sSavedGame) {
			SavedGame save = sSavedGame;
			if (!save.mIsValid) {
				Log.d(TAG, "No valid saved game found");
				reset();
				save();     // initialize save area
				return false;
			}
			int[][] bricks = save.mBricksState;
			for (int i = 0; i < BRICK_ROWS; i++) {
				for (int j = 0; j < BRICK_COLUMNS; j++) {	
					mBricks[i][j].setBrickState(bricks[i][j]);
					if (bricks[i][j]!=0) {
						// board creation sets all bricks to "live", don't need to setAlive() here						
					} else {
						//mBricks[i][j].setBrickState(0);
						mLiveBrickCount--;
					}
				}
			}
			Log.d(TAG, "live brickcount is " + mLiveBrickCount);

			mBall.setDirection(save.mBallXDirection, save.mBallYDirection);
			mBall.setPosition(save.mBallXPosition, save.mBallYPosition);
			mBall.setSpeed(save.mBallSpeed);
			movePaddle(save.mPaddlePosition);

			mGamePlayState = save.mGamePlayState;
			mGameStatusMessageNum = save.mGameStatusMessageNum;
			mLivesRemaining = save.mLivesRemaining;
			mScore = save.mScore;
		}

		Log.d(TAG, "game restored");
		return true;
	}

	/**
	 * Performs some housekeeping after the Renderer surface has changed.
	 * <p>
	 * This is called after a screen rotation or when returning to the app from the home screen.
	 */
	public void surfaceChanged() {
		// Pause briefly.  This gives the user time to orient themselves after a screen
		// rotation or switching back from another app.
		setPauseTime(1.5f);

		// Reset this so we don't leap forward.  (Not strictly necessary because of the
		// game pause we set above -- we don't advance the ball state on the first frames we
		// draw, so this will reset naturally.)
		mPrevFrameWhenNsec = 0;

		// We need to draw the screen at least once, so set this whether or not we're actually
		// animating.  If we're in a "game over" state, this will go back to "false" right away.
		mIsAnimating = true;
	}

	/**
	 * Sets the TextResources object that the game will use.
	 */
	public void setTextResources(TextResources textRes) {
		mTextRes = textRes;
	}

	/**
	 * Marks the saved game as invalid.
	 * <p>
	 * May be called from a non-Renderer thread.
	 */
	public static void invalidateSavedGame() {
		synchronized (sSavedGame) {
			sSavedGame.mIsValid = false;
		}
	}

	/**
	 * Determines whether we have saved a game that can be resumed.  We would need to have a valid
	 * saved game and be playing or about to play.
	 * <p>
	 * May be called from a non-Renderer thread.
	 */
	public static boolean canResumeFromSave() {
		synchronized (sSavedGame) {
			//Log.d(TAG, "canResume: valid=" + sSavedGame.mIsValid
			//        + " state=" + sSavedGame.mGamePlayState);
			return sSavedGame.mIsValid &&
					(sSavedGame.mGamePlayState == GAME_PLAYING ||
					sSavedGame.mGamePlayState == GAME_READY);
		}
	}

	/**
	 * Gets the score from a completed game.
	 * @return The score, or -1 if the current save state doesn't hold a completed game.
	 */
	public static int getFinalScore() {
		synchronized (sSavedGame) {
			if (sSavedGame.mIsValid &&
					(sSavedGame.mGamePlayState == GAME_WON ||
					sSavedGame.mGamePlayState == GAME_LOST)) {
				return sSavedGame.mScore;
			} else {
				Log.d(TAG, "No score: valid=" + sSavedGame.mIsValid
				        + " state=" + sSavedGame.mGamePlayState);
				return -1;
			}
		}
	}

	/**
	 * Returns true if we want the system to call our draw methods.
	 */
	public boolean isAnimating() {
		return mIsAnimating;
	}

	/**
	 * Allocates the background texture to game
	 */
	void allocBackground(Context context) {
		/*
		 * The messages (e.g. "won" and "lost") are stored in the same texture, so the choice
		 * of which text to show is determined by the texture coordinates stored in the
		 * TexturedAlignedRect.  We can update those without causing an allocation, so there's
		 * no need to allocate a separate drawable rect for every possible message.
		 */

		mBackgroundImg = new TexturedBasicAlignedRect();
		int id = context.getResources().getIdentifier(mBackgroundTextureImg, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);        

		TexturedBasicAlignedRect rectBack = new TexturedBasicAlignedRect();
		//Log.d(TAG, "paddle y=" + rect.getYPosition());
		rectBack.setPosition(ARENA_WIDTH/2, ARENA_HEIGHT/2);
		rectBack.setScale(ARENA_WIDTH - BORDER_WIDTH * 2, ARENA_HEIGHT - BORDER_WIDTH * 2);
		//rectBack.setColor(0.1f, 0.1f, 0.1f);
		rectBack.setTexture(bmp);


		mBackgroundImg = rectBack;
	}


	/**
	 * If appropriate, draw a message in the middle of the screen.
	 */
	void drawBackground() {
		mBackgroundImg.draw();              
	}

	private Bitmap getBitmapTexture(Context context, String src){
		int id = context.getResources().getIdentifier(src, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		return bmp;
	}
	/**
	 * Allocates the bricks, setting their sizes and positions.  Sets mLiveBrickCount.
	 */
	void allocBricks(Context context) {		
		// 1. texture for normal brick
		mBMPBrickTexture[0] = getBitmapTexture(context, mBrickNormalTextureImg);
		
		// 2. texture for rock brick
		mBMPBrickTexture[1] = getBitmapTexture(context, mBrickRockTextureImg);
		
		// 3. texture for mix brick
		mBMPBrickTexture[2] = getBitmapTexture(context, mBrickMixTextureImg);		

		// 4. texture for especial brick
		mBMPBrickTexture[3] = getBitmapTexture(context, mBrickEspecial1TextureImg);
		
		// 5. texture for especial brick
		mBMPBrickTexture[4] = getBitmapTexture(context, mBrickEspecial2TextureImg);
		
		//------------------------
		
		final float totalBrickWidth = ARENA_WIDTH - BORDER_WIDTH * 2;
		final float brickWidth = totalBrickWidth / BRICK_COLUMNS;
		//final float brickWidth = 100;
		final float totalBrickHeight = ARENA_HEIGHT * (BRICK_TOP_PERC - BRICK_BOTTOM_PERC);
		final float brickHeight = totalBrickHeight / BRICK_ROWS;
		//final float brickHeight = 100;

		final float zoneBottom = ARENA_HEIGHT * BRICK_BOTTOM_PERC;
		final float zoneLeft = BORDER_WIDTH;

		for (int i = 0; i < BRICK_ROWS; i++) {
			for (int j = 0; j < BRICK_COLUMNS; j++) {
				Brick brick = new Brick();
	
				//int row = i / BRICK_COLUMNS;
				//int col = i % BRICK_COLUMNS;
				int row = i;
				int col = j;
	
				float bottom = zoneBottom + row * brickHeight;
				float left = zoneLeft + col * brickWidth;
	
				// Brick position specifies the center point, so need to offset from bottom left.
				brick.setPosition(left + brickWidth / 2, bottom + brickHeight / 2);
	
				// Brick size is the size of the "brick zone", scaled down by a few % on each edge.
				brick.setScale(brickWidth * (1.0f - BRICK_HORIZONTAL_GAP_PERC),
						brickHeight * (1.0f - BRICK_VERTICAL_GAP_PERC));
	
				// Score is based on row, with lower bricks being worth less than the top bricks.
				// The point value here is for a game at normal difficulty.  We multiply by 100
				// because that makes everything MORE EXCITING!!!
				brick.setScoreValue((row + 1) * 100);
				//int minR = 1;
				//int maxR = BMPs.length;
				//int ind = minR + (int)(Math.random() * ((maxR - minR) + 1));
				//Log.v(TAG, "ind texture:"+ ind);
				//brick.setBrickState(ind);
				//brick.setTexture(BMPs[ind - 1]);
				brick.setBrickState(mBrickStatesConfig[i][j]);
				if (mBrickStatesConfig[i][j]!=BRICK_EMPTY) {
					brick.setTexture(mBMPBrickTexture[mBrickStatesConfig[i][j]-1]);
				}				
	
				mBricks[i][j] = brick;
			}
		}

		//Log.d(TAG, "Brick zw=" + brickWidth + " zh=" + brickHeight
		//        + " w=" + mBricks[0].getXScale() + " h=" + mBricks[0].getYScale()
		//        + " gapw=" + (brickWidth - mBricks[0].getXScale())
		//        + " gaph=" + (brickHeight - mBricks[0].getYScale()));

		if (false) {
			// The maximum possible score determines how many digits we need to display.
			int max = 0;
			for (int j = 0; j < BRICK_ROWS; j++) {
				max += (j+1) * BRICK_COLUMNS;
			}
			Log.d(TAG, "max score on 'normal' is " + (max * 100));
		}


		mLiveBrickCount = BRICK_ROWS * BRICK_COLUMNS;
	}

	/**
	 * Draws the "live" bricks.
	 */
	void drawBricks() {
		for (int i = 0; i < BRICK_ROWS; i++) {
			for (int j = 0; j < BRICK_COLUMNS; j++) {
				Brick brick = mBricks[i][j];
				//if brick isn't configured as empty, the we can draw it 
				if (brick.getBrickState()!=BRICK_EMPTY) {
					//if the brick was not destroyed
					brick.draw();
				}
			}
		}
	}

	/**
	 * Allocates the rects that define the borders and background.
	 */
	void allocBorders() {
		BasicAlignedRect rect;

		// Need one rect that covers the entire play area (i.e. viewport) in the background color.
		// (We could tighten this up a bit so we don't get overdrawn by the borders, but that's
		// a minor concern.)
		rect = new BasicAlignedRect();
		rect.setPosition(ARENA_WIDTH/2, ARENA_HEIGHT/2);
		rect.setScale(ARENA_WIDTH, ARENA_HEIGHT);
		//rect.setColor(0.1f, 0.1f, 0.1f);
		rect.setColor(0.451f, 0.541f, 0.322f);
		mBackgroundColor = rect;

		// This rect is just off the bottom of the arena.  If we collide with it, the ball is
		// lost.  This must be BOTTOM_BORDER (zero).
		rect = new BasicAlignedRect();
		rect.setPosition(ARENA_WIDTH/2, -BORDER_WIDTH/2);
		rect.setScale(ARENA_WIDTH, BORDER_WIDTH);
		//rect.setColor(1.0f, 0.65f, 0.0f);
		//rect.setColor(0.451f, 0.541f, 0.322f);
		rect.setColor(0.1f, 0.1f, 0.1f);
		mBorders[BOTTOM_BORDER] = rect;

		// Need one rect each for left / right / top.
		rect = new BasicAlignedRect();
		rect.setPosition(BORDER_WIDTH/2, ARENA_HEIGHT/2);
		rect.setScale(BORDER_WIDTH, ARENA_HEIGHT);
		rect.setColor(0.6f, 0.6f, 0.6f);
		mBorders[1] = rect;

		rect = new BasicAlignedRect();
		rect.setPosition(ARENA_WIDTH - BORDER_WIDTH/2, ARENA_HEIGHT/2);
		rect.setScale(BORDER_WIDTH, ARENA_HEIGHT);
		rect.setColor(0.6f, 0.6f, 0.6f);
		mBorders[2] = rect;

		rect = new BasicAlignedRect();
		rect.setPosition(ARENA_WIDTH/2, ARENA_HEIGHT - BORDER_WIDTH/2);
		rect.setScale(ARENA_WIDTH - BORDER_WIDTH*2, BORDER_WIDTH);
		rect.setColor(0.6f, 0.6f, 0.6f);
		mBorders[3] = rect;
	}

	/**
	 * Draws the border and background rects.
	 */
	void drawBorders() {
		mBackgroundColor.draw();
		for (int i = 0; i < mBorders.length; i++) {
			mBorders[i].draw();
		}
	}

	/**
	 * Creates the paddle.
	 */
	void allocPaddle(Context context) {
		TexturedBasicAlignedRect rect = new TexturedBasicAlignedRect();
		int id = context.getResources().getIdentifier(mPaddleTextureImg, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);

		rect.setScale(DEFAULT_PADDLE_WIDTH * mPaddleSizeMultiplier,
				ARENA_HEIGHT * PADDLE_HEIGHT_PERC);
		rect.setColor(1.0f, 1.0f, 1.0f);        // note color is cycled during pauses

		rect.setPosition(ARENA_WIDTH / 2.0f, ARENA_HEIGHT * PADDLE_VERTICAL_PERC);
		//Log.d(TAG, "paddle y=" + rect.getYPosition());
		rect.setTexture(bmp);

		mPaddle = rect;
	}

	/**
	 * Draws the paddle.
	 */
	void drawPaddle() {
		mPaddle.draw();
	}

	/**
	 * Moves the paddle to a new location.  The requested position is expressed in arena
	 * coordinates, but does not need to be clamped to the viewable region.
	 * <p>
	 * The final position may be slightly different due to collisions with walls or
	 * side-contact with the ball.
	 */
	void movePaddle(float arenaX) {

		float paddleWidth = mPaddle.getXScale() / 2;
		final float minX = BORDER_WIDTH + paddleWidth;
		final float maxX = ARENA_WIDTH - BORDER_WIDTH - paddleWidth;

		if (arenaX < minX) {
			arenaX = minX;
		} else if (arenaX > maxX) {
			arenaX = maxX;
		}

		mPaddle.setXPosition(arenaX);
	}

	/**
	 * Creates the ball.
	 */
	void allocBall(Context context) {
		Ball ball = new Ball();
		int id = context.getResources().getIdentifier(mBallTextureImg, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);

		int diameter = (int) (DEFAULT_BALL_DIAMETER * mBallSizeMultiplier);
		// ovals don't work right -- collision detection requires a circle
		ball.setScale(diameter, diameter);
		ball.setTexture(bmp);
		mBall = ball;
	}

	/**
	 * Draws the "live" ball and the remaining-lives display.
	 */
	void drawBall() {
		/*
		 * We use the lone mBall object to draw all instances of the ball.  We just move it
		 * around for each instance.
		 */

		Ball ball = mBall;
		float savedX = ball.getXPosition();
		float savedY = ball.getYPosition();
		float radius = ball.getRadius();

		float xpos = BORDER_WIDTH * 2 + radius;
		float ypos = BORDER_WIDTH + radius;
		int lives = mLivesRemaining;
		boolean ballIsLive = (mGamePlayState != GAME_PAUSE && mGamePlayState != GAME_READY);
		if (ballIsLive) {
			// In READY state we show the "live" ball next to the "remaining" balls, rather than
			// in the play area.
			lives--;
		}

		for (int i = 0; i < lives; i++) {
			// Vibrate the "remaining lives" balls when we're almost out of bricks.  It's
			// kind of silly, but it's easy to do.
			float jitterX = 0.0f;
			float jitterY = 0.0f;
			if (mLiveBrickCount > 0 && mLiveBrickCount < 4) {
				jitterX = (float) ((4 - mLiveBrickCount) * (Math.random() - 0.5) * 2);
				jitterY = (float) ((4 - mLiveBrickCount) * (Math.random() - 0.5) * 2);
			}
			ball.setPosition(xpos + jitterX, ypos + jitterY);
			ball.draw();

			xpos += radius * 3;
		}

		ball.setPosition(savedX, savedY);
		if (ballIsLive) {
			ball.draw();
		}
	}

	/**
	 * Creates objects required to display a numeric score.
	 */
	void allocScore() {
		/*
		 * The score digits occupy a fixed position at the top right of the screen.  They're
		 * actually part of the arena, and sit "under" the ball.  (We could, in fact, have the
		 * ball collide with them.)
		 *
		 * We want to use fixed-size cells for the digits.  Each digit has a different width
		 * though (which is somewhat true even if we use a monospace font -- a '1' can measure
		 * narrower than an '8' because the text metrics ignore the padding).  We want to run
		 * through and figure out what the widest glyph is, and use that as the cell width.
		 *
		 * The basic plan is to find the widest glyph, scale it up to match the height we
		 * want, and use that as the size of a cell.  The digits are drawn scaled up to that
		 * height, with the width increased proportionally (a given digit may not fill the
		 * entire width of the cell).
		 */

		int maxWidth = 0;
		Rect widest = null;
		for (int i = 0 ; i < 10; i++) {
			Rect boundsRect = mTextRes.getTextureRect(TextResources.DIGIT_START + i);
			int rectWidth = boundsRect.width();
			if (maxWidth < rectWidth) {
				maxWidth = rectWidth;
				widest = boundsRect;
			}
		}

		float widthHeightRatio = (float) widest.width() / widest.height();
		float cellHeight = ARENA_HEIGHT * SCORE_HEIGHT_PERC;
		float cellWidth = cellHeight * widthHeightRatio * 1.05f; // add 5% spacing between digits

		// Note these are laid out from right to left, i.e. mScoreDigits[0] is the 1s digit.
		float top = SCORE_TOP;
		float right = SCORE_RIGHT;
		for (int i = 0; i < NUM_SCORE_DIGITS; i++) {
			mScoreDigits[i] = new TexturedAlignedRect();
			mScoreDigits[i].setTexture(mTextRes.getTextureHandle(),
					mTextRes.getTextureWidth(), mTextRes.getTextureHeight());
			mScoreDigits[i].setPosition(SCORE_RIGHT - (i * cellWidth) - cellWidth/2,
					SCORE_TOP - cellHeight/2);
		}
	}

	/**
	 * Draws the current score.
	 */
	void drawScore() {
		float cellHeight = ARENA_HEIGHT * SCORE_HEIGHT_PERC;
		int score = mScore;
		for (int i = 0; i < NUM_SCORE_DIGITS; i++) {
			int val = score % 10;
			Rect boundsRect = mTextRes.getTextureRect(TextResources.DIGIT_START + val);
			float ratio = cellHeight / boundsRect.height();

			TexturedAlignedRect scoreCell = mScoreDigits[i];
			scoreCell.setTextureCoords(boundsRect);
			scoreCell.setScale(boundsRect.width() * ratio,  cellHeight);
			scoreCell.draw();

			score /= 10;
		}
	}

	/**
	 * Creates storage for a message to display in the middle of the screen.
	 */
	void allocMessages() {
		/*
		 * The messages (e.g. "won" and "lost") are stored in the same texture, so the choice
		 * of which text to show is determined by the texture coordinates stored in the
		 * TexturedAlignedRect.  We can update those without causing an allocation, so there's
		 * no need to allocate a separate drawable rect for every possible message.
		 */

		mGameStatusMessages = new TexturedAlignedRect();
		mGameStatusMessages.setTexture(mTextRes.getTextureHandle(),
				mTextRes.getTextureWidth(), mTextRes.getTextureHeight());
		mGameStatusMessages.setPosition(ARENA_WIDTH / 2, ARENA_HEIGHT / 2);
	}

	/**
	 * If appropriate, draw a message in the middle of the screen.
	 */
	void drawMessages() {
		//Log.v(TAG,"MESSAGE NUM "+ String.valueOf(mGameStatusMessageNum) + "state: "+String.valueOf(mGamePlayState));
		if ((mGameStatusMessageNum != TextResources.NO_MESSAGE)) {
			//Show messages before start the game, after losing, after winning.  
			TexturedAlignedRect msgBox = mGameStatusMessages;

			Rect boundsRect = mTextRes.getTextureRect(mGameStatusMessageNum);
			msgBox.setTextureCoords(boundsRect);

			float scale = (ARENA_WIDTH * STATUS_MESSAGE_WIDTH_PERC) / boundsRect.width();
			msgBox.setScale(boundsRect.width() * scale, boundsRect.height() * scale);
			msgBox.draw();            

		} 
	}

	/**
	 * Allocates Buttons 
	 */
	/**
	 * Creates the buttons.
	 */
	void allocButtonQuit(Context context) {

		// Show buttons exit, next level, restart level

		TexturedBasicAlignedRect rect = new TexturedBasicAlignedRect();
		float w = DEFAULT_BUTTON_WIDTH * mButtonMultiplier;
		float h = DEFAULT_BUTTON_HEIGHT * mButtonMultiplier;
		
		// --------------------Button quit
		int id = context.getResources().getIdentifier(mButtonQuitTextureImg, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		
		rect.setScale(w, h);

		rect.setColor(1.0f, 1.0f, 1.0f);        // note color is cycled during pauses

		float pos_x = ARENA_WIDTH / 2.0f - w;
		float pos_y = ARENA_HEIGHT / 2.0f - h;

		rect.setPosition(pos_x, pos_y );
		//Log.d(TAG, "button x=" + String.valueof(pos_x));
		rect.setTexture(bmp);

		mQuit = rect;

		

	}
	
	void allocButtonNextLevel(Context context) {

		// Show buttons exit, next level, restart level

		TexturedBasicAlignedRect rect = new TexturedBasicAlignedRect();
		float w = DEFAULT_BUTTON_WIDTH * mButtonMultiplier;
		float h = DEFAULT_BUTTON_HEIGHT * mButtonMultiplier;

		// --------------------Button Next level
		int id = context.getResources().getIdentifier(mButtonNextLevelTextureImg, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		
		rect.setScale(w, h);

		rect.setColor(1.0f, 1.0f, 1.0f);        // note color is cycled during pauses

		float pos_x = ARENA_WIDTH / 2.0f + w ;
		float pos_y = ARENA_HEIGHT / 2.0f - h;

		rect.setPosition(pos_x, pos_y);
		//Log.d(TAG, "button x=" + String.valueof(pos_x));
		rect.setTexture(bmp);

		mNextLevel = rect;
	}
	
	void allocButtonReloadLevel(Context context) {

		// Show buttons exit, next level, restart level

		TexturedBasicAlignedRect rect = new TexturedBasicAlignedRect();
		float w = DEFAULT_BUTTON_WIDTH * mButtonMultiplier;
		float h = DEFAULT_BUTTON_HEIGHT * mButtonMultiplier;

		// --------------------Button Reload level
		int id = context.getResources().getIdentifier(mButtonReloadLevelTextureImg, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		
		rect.setScale(w, h);

		rect.setColor(1.0f, 1.0f, 1.0f);        // note color is cycled during pauses

		float pos_x = ARENA_WIDTH / 2.0f + w;
		float pos_y = ARENA_HEIGHT / 2.0f - h;

		rect.setPosition(pos_x, pos_y);
		//Log.d(TAG, "button x=" + String.valueof(pos_x));
		rect.setTexture(bmp);

		mReload = rect;

	}

	/**
	 * Draw the buttons for GAME OVER and WINNER screen
	 */
	void drawButtons(){
		Log.v(TAG, "mGameStatusMessageNum: "+String.valueOf(mGameStatusMessageNum));
		switch (mGameStatusMessageNum) {
		case TextResources.GAME_OVER:
			mQuit.draw();
			mReload.draw();
			break;
		case TextResources.WINNER:
			mQuit.draw();
			mNextLevel.draw();
			break;
		default:
			break;
		}
	}


	/**
	 * Allocates shapes that we use for "visual debugging".
	 */
	void allocDebugStuff() {
		mDebugCollisionRect = new OutlineAlignedRect();
		mDebugCollisionRect.setColor(1.0f, 0.0f, 0.0f);
	}

	/**
	 * Renders debug features.
	 * <p>
	 * This function is allowed to violate the "don't allocate objects" rule.
	 */
	void drawDebugStuff() {
		if (!SHOW_DEBUG_STUFF) {
			return;
		}

		// Draw a red outline rectangle around the ball.  This shows the area that was
		// examined for collisions during the "coarse" pass.
		if (true) {
			OutlineAlignedRect.prepareToDraw();
			mDebugCollisionRect.draw();
			OutlineAlignedRect.finishedDrawing();
		}

		// Draw the entire message texture so we can see what it looks like.
		if (true) {
			int textureWidth = mTextRes.getTextureWidth();
			int textureHeight = mTextRes.getTextureHeight();
			float scale = (ARENA_WIDTH * STATUS_MESSAGE_WIDTH_PERC) / textureWidth;

			// Draw an orange rect around the texture.
			OutlineAlignedRect outline = new OutlineAlignedRect();
			outline.setPosition(ARENA_WIDTH / 2, ARENA_HEIGHT / 2);
			outline.setScale(textureWidth * scale + 2, textureHeight * scale + 2);
			outline.setColor(1.0f, 0.65f, 0.0f);
			OutlineAlignedRect.prepareToDraw();
			outline.draw();
			OutlineAlignedRect.finishedDrawing();

			// Draw the full texture.  Note you can set the background to opaque white in
			// TextResources to see what the drop shadow looks like.
			Rect boundsRect = new Rect(0, 0, textureWidth, textureHeight);
			TexturedAlignedRect msgBox = mGameStatusMessages;
			msgBox.setTextureCoords(boundsRect);
			msgBox.setScale(textureWidth * scale, textureHeight * scale);
			TexturedAlignedRect.prepareToDraw();
			msgBox.draw();
			TexturedAlignedRect.finishedDrawing();

			// Draw a rectangle around each individual text item.  We draw a different one each
			// time to get a flicker effect, so it doesn't fully obscure the text.
			if (true) {
				outline.setColor(1.0f, 1.0f, 1.0f);
				int stringNum = mDebugFramedString;
				mDebugFramedString = (mDebugFramedString + 1) % TextResources.getNumStrings();
				boundsRect = mTextRes.getTextureRect(stringNum);
				// The bounds rect is in bitmap coordinates, with (0,0) in the top left.  Translate
				// it to an offset from the center of the bitmap, and find the center of the rect.
				float boundsCenterX = boundsRect.exactCenterX()- (textureWidth / 2);
				float boundsCenterY = boundsRect.exactCenterY() - (textureHeight / 2);
				// Now scale it to arena coordinates, using the same scale factor we used to
				// draw the texture with all the messages, and translate it to the center of
				// the arena.  We need to invert Y to match GL conventions.
				boundsCenterX = ARENA_WIDTH / 2 + (boundsCenterX * scale);
				boundsCenterY = ARENA_HEIGHT / 2 - (boundsCenterY * scale);
				// Set the values and draw the rect.
				outline.setPosition(boundsCenterX, boundsCenterY);
				outline.setScale(boundsRect.width() * scale, boundsRect.height() * scale);
				OutlineAlignedRect.prepareToDraw();
				outline.draw();
				OutlineAlignedRect.finishedDrawing();
			}
		}
	}

	/**
	 * Sets the pause time.  The game will continue to execute and render, but won't advance
	 * game state.  Used at the start of the game to give the user a chance to orient
	 * themselves to the board.
	 * <p>
	 * May also be handy during debugging to see stuff (like the ball at the instant of a
	 * collision) without fully stopping the game.
	 */
	void setPauseTime(float durationMsec) {
		mPauseDuration = durationMsec;
	}

	/**
	 * Updates all game state for the next frame.  This primarily consists of moving the ball
	 * and checking for collisions.
	 */
	void calculateNextFrame() {
		Log.v(TAG, "calcula frma");
		// First frame has no time delta, so make it a no-op.
		if (mPrevFrameWhenNsec == 0) {
			mPrevFrameWhenNsec = System.nanoTime();     // use monotonic clock
			mRecentTimeDeltaNext = -1;                  // reset saved values
			return;
		}

		long nowNsec = System.nanoTime();
		double curDeltaSec = (nowNsec - mPrevFrameWhenNsec) / NANOS_PER_SECOND;
		if (curDeltaSec > MAX_FRAME_DELTA_SEC) {
			// We went to sleep for an extended period.  Cap it at a reasonable limit.
			Log.d(TAG, "delta time was " + curDeltaSec + ", capping at " + MAX_FRAME_DELTA_SEC);
			curDeltaSec = MAX_FRAME_DELTA_SEC;
		}
		double deltaSec;

		if (FRAME_RATE_SMOOTHING) {
			if (mRecentTimeDeltaNext < 0) {
				// first time through, fill table with current value
				for (int i = 0; i < RECENT_TIME_DELTA_COUNT; i++) {
					mRecentTimeDelta[i] = curDeltaSec;
				}
				mRecentTimeDeltaNext = 0;
			}

			mRecentTimeDelta[mRecentTimeDeltaNext] = curDeltaSec;
			mRecentTimeDeltaNext = (mRecentTimeDeltaNext + 1) % RECENT_TIME_DELTA_COUNT;

			deltaSec = 0.0f;
			for (int i = 0; i < RECENT_TIME_DELTA_COUNT; i++) {
				deltaSec += mRecentTimeDelta[i];
			}
			deltaSec /= RECENT_TIME_DELTA_COUNT;
		} else {
			deltaSec = curDeltaSec;
		}

		boolean advanceFrame = true;

		// If we're in a pause, animate the color of the paddle, but don't advance any state.
		if (mPauseDuration > 0.0f) {
			advanceFrame = false;
			if (mPauseDuration > deltaSec) {
				mPauseDuration -= deltaSec;

				if (mGamePlayState == GAME_PLAYING) {
					// rotate through yellow, magenta, cyan
					float[] colors = mPaddle.getColor();
					if (colors[0] == 0.0f) {
						mPaddle.setColor(1.0f, 0.0f, 1.0f);
					} else if (colors[1] == 0.0f) {
						mPaddle.setColor(1.0f, 1.0f, 0.0f);
					} else {
						mPaddle.setColor(0.0f, 1.0f, 1.0f);
					}
				}
			} else {
				// leaving pause, restore paddle color to white
				mPauseDuration = 0.0f;
				mPaddle.setColor(1.0f, 1.0f, 1.0f);
			}
		}

		// Do something appropriate based on our current state.
		switch (mGamePlayState) {
		case GAME_INITIALIZING:
			//mGamePlayState = GAME_READY;
			//mGamePlayState = GAME_PAUSE;
			break;
		case GAME_READY:
			mGameStatusMessageNum = TextResources.READY;
			Log.v(TAG,"advanceFrame"+ String.valueOf(advanceFrame));
			if (advanceFrame) {
				// "ready" has expired, move ball to starting position
				mGamePlayState = GAME_PLAYING;
				mGameStatusMessageNum = TextResources.NO_MESSAGE;
				setPauseTime(0.5f);
				advanceFrame = false;
			}
			break;
		case GAME_WON:
			mGameStatusMessageNum = TextResources.WINNER;
			mIsAnimating = false;
			advanceFrame = false;
			break;
		case GAME_LOST:
			mGameStatusMessageNum = TextResources.GAME_OVER;
			mIsAnimating = false;
			advanceFrame = false;
			break;
		case GAME_PLAYING:
			break;
		case GAME_PAUSE:
			mIsAnimating = false;
			advanceFrame = false;
			break;
		default:
			Log.e(TAG, "GLITCH: bad state " + mGamePlayState);
			break;
		}

		// If we're playing, move the ball around.
		if (advanceFrame) {
			int event = moveBall(deltaSec);
			switch (event) {
			case EVENT_LAST_BRICK:
				mGamePlayState = GAME_WON;
				// We're already playing the brick sound; play the other three sounds
				// simultaneously.  Cheap substitute for an actual "victory" sound.
				SoundResources.play(SoundResources.PADDLE_HIT);
				SoundResources.play(SoundResources.WALL_HIT);
				SoundResources.play(SoundResources.BALL_LOST);
				break;
			case EVENT_BALL_LOST:
				if (--mLivesRemaining == 0) {
					// game over, man
					mGamePlayState = GAME_LOST;
				} else {
					// switch back to "ready" state, reset ball position
					//mGamePlayState = GAME_READY;
					mGamePlayState = GAME_PAUSE;
					mGameStatusMessageNum = TextResources.READY;
					setPauseTime(1.5f);
					resetBall();
				}
				break;
			case EVENT_NONE:
				break;
			default:
				throw new RuntimeException("bad game event: " + event);
			}
		}

		mPrevFrameWhenNsec = nowNsec;
	}

	/**
	 * Moves the ball, checking for and reporting collisions as we go.
	 *
	 * @return A value indicating special events (won game, lost ball).
	 */
	private int moveBall(double deltaSec) {
		/*
		 * Movement and collision detection is done with two checks, "coarse" and "fine".
		 *
		 * First, we take the current position of the ball, and compute where it will be
		 * for the next frame.  We compute a box that encloses both the current and next
		 * positions (an "axis-aligned bounding box", or AABB).  For every object in the list,
		 * including the borders and paddle, we do quick test for a collision.  If nothing
		 * matches, we just jump the ball forward.
		 *
		 * If we do get some matches, we need to do a finer-grained test to see if (a) we
		 * actually hit something, and (b) how far along the ball's path we were when we
		 * first collided.
		 *
		 * If we did hit something, we need to update the ball's motion vector based on which
		 * edge or corner we hit, and restart the whole process from the point of the collision.
		 * The ball is now moving in a different direction, so the "coarse" information we
		 * gathered previously is no longer valid.
		 *
		 * There can be multiple collisions in a single frame, and we need to catch them all.
		 *
		 * (Given an insanely fast-moving ball, or a ball with a really large radius, or various
		 * other crazy parameters, it's possible to hit every brick in a single frame.)
		 */

		int event = EVENT_NONE;

		float radius = mBall.getRadius();
		float distance = (float) (mBall.getSpeed() * deltaSec);
		//Log.d(TAG, "delta=" + deltaSec * 60.0f + " dist=" + distance);

		if (mDebugSlowMotionFrames > 0) {
			// Simulate a "slow motion" mode by reducing distance.  The reduction is constant
			// until the last 60 frames, which ramps the speed up gradually.
			final float SLOW_FACTOR = 8.0f;
			final float RAMP_FRAMES = 60.0f;
			float div;
			if (mDebugSlowMotionFrames > RAMP_FRAMES) {
				div = SLOW_FACTOR;
			} else {
				// At frame 60, we want the full slowdown.  At frame 0, we want no slowdown.
				// STEP is how much we want to subtract from SLOW_FACTOR at each step.
				final float STEP = (SLOW_FACTOR - 1.0f) / RAMP_FRAMES;

				div = SLOW_FACTOR - (STEP * (RAMP_FRAMES - mDebugSlowMotionFrames));
			}
			distance /= div;

			mDebugSlowMotionFrames--;
		}

		while (distance > 0.0f) {
			float curX = mBall.getXPosition();
			float curY = mBall.getYPosition();
			float dirX = mBall.getXDirection();
			float dirY = mBall.getYDirection();
			float finalX = curX + dirX * distance;
			float finalY = curY + dirY * distance;
			float left, right, top, bottom;

			/*
			 * Find the edges of the rectangle described by the ball's start and end position.
			 * The (x,y) values identify the center, so factor in the radius too.
			 *
			 * Per GL conventions, values get larger moving toward the top-right corner.
			 */
			if (curX < finalX) {
				left = curX - radius;
				right = finalX + radius;
			} else {
				left = finalX - radius;
				right = curX + radius;
			}
			if (curY < finalY) {
				bottom = curY - radius;
				top = finalY + radius;
			} else {
				bottom = finalY - radius;
				top = curY + radius;
			}
			/* debug */
			mDebugCollisionRect.setPosition((curX + finalX) / 2, (curY + finalY) / 2);
			mDebugCollisionRect.setScale(right - left, top - bottom);

			int hits = 0;

			// test bricks
			for (int i = 0; i < BRICK_ROWS; i++) {
				for (int j = 0; j < BRICK_COLUMNS; j++) {
					if (mBricks[i][j].getBrickState()!=0 &&
							checkCoarseCollision(mBricks[i][j], left, right, bottom, top)) {
						mPossibleCollisions[hits++] = mBricks[i][j];
					}
				}
			}

			// test borders
			for (int i = 0; i < NUM_BORDERS; i++) {
				if (checkCoarseCollision(mBorders[i], left, right, bottom, top)) {
					mPossibleCollisions[hits++] = mBorders[i];
				}
			}

			// test paddle
			if (checkCoarseCollision(mPaddle, left, right, bottom, top)) {
				mPossibleCollisions[hits++] = mPaddle;
			}

			// test score... because we can
			if (false) {
				for (int i = 0; i < NUM_SCORE_DIGITS; i++) {
					// It's possible to get the ball wedged up behind the score digits if they're
					// too far from the wall relative to the size of the ball.  (I haven't seen it
					// actually get stuck, but it's a possibility.)  To do this right, we need a
					// collision rect that covers the digits and extends all the way to the
					// borders, or some random jitter in the collision vector that ensures we
					// can't enter a stable state.
					if (checkCoarseCollision(mScoreDigits[i], left, right, bottom, top)) {
						mPossibleCollisions[hits++] = mScoreDigits[i];
					}
				}
			}

			if (hits != 0) {
				// may have hit something, look closer
				BaseRect hit = findFirstCollision(mPossibleCollisions, hits, curX, curY,
						dirX, dirY, distance, radius);

				if (hit == null) {
					// didn't actually hit, clear counter
					hits = 0;
				} else {
					if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) {
						if (mHitDistanceTraveled <= 0.0f) {
							Log.e(TAG, "GLITCH: collision detection didn't move the ball");
							mHitDistanceTraveled = distance;
						}
					}

					// Update posn for the actual distance traveled and the collision adjustment
					float newPosX = curX + dirX * mHitDistanceTraveled + mHitXAdj;
					float newPosY = curY + dirY * mHitDistanceTraveled + mHitYAdj;
					mBall.setPosition(newPosX, newPosY);
					if (DEBUG_COLLISIONS) {
						Log.d(TAG, "COL: intermediate cx=" + newPosX + " cy=" + newPosY);
					}

					// Update the direction vector based on the nature of the surface we
					// struck.  We will override this for collisions with the paddle.
					float newDirX = dirX;
					float newDirY = dirY;
					switch (mHitFace) {
					case HIT_FACE_HORIZONTAL:
						newDirY = -dirY;
						break;
					case HIT_FACE_VERTICAL:
						newDirX = -dirX;
						break;
					case HIT_FACE_SHARPCORNER:
						newDirX = -dirX;
						newDirY = -dirY;
						break;
					case HIT_FACE_NONE:
					default:
						Log.e(TAG, "GLITCH: unexpected hit face" + mHitFace);
						break;
					}


					/*
					 * Figure out what we hit, and react.  A conceptually cleaner way to do
					 * this would be to define a "collision" action on every BaseRect object,
					 * and call that.  This is very straightforward for the object state update
					 * handling (e.g. remove brick, make sound), but gets a little more
					 * complicated for collisions that don't follow the basic rules (e.g. hitting
					 * the paddle) or special events (like hitting the very last brick).  We're
					 * not trying to build a game engine, so we just use a big if-then-else.
					 *
					 * Playing a sound here may not be the best approach.  If the sound code
					 * takes a while to queue up sounds, we could stall the game/render thread
					 * and reduce our frame rate.  It might be better to queue up sounds on a
					 * separate thread.  However, unless the ball is moving at an absurd speed,
					 * we shouldn't be colliding with more than two objects in a single frame,
					 * so we shouldn't be stressing SoundPool much.
					 */
					if (hit instanceof Brick) {
						Brick brick = (Brick) hit;
						//brick.setAlive(false);
						int newBrickState = brick.getBrickState() - 1;
						brick.setBrickState(newBrickState);
						
						if(!brick.isAlive()){
							mLiveBrickCount--;
							mScore += brick.getScoreValue() * mScoreMultiplier;
						}
						if (mLiveBrickCount == 0) {
							Log.d(TAG, "*** won ***");
							event = EVENT_LAST_BRICK;
							distance = 0.0f;
						}
						SoundResources.play(SoundResources.BRICK_HIT);
						
					} else if (hit == mPaddle) {
						if (mHitFace == HIT_FACE_HORIZONTAL) {
							float paddleWidth = mPaddle.getXScale();
							float paddleLeft = mPaddle.getXPosition() - paddleWidth / 2;
							float hitAdjust = (newPosX - paddleLeft) / paddleWidth;

							// Adjust the ball's motion based on where it hit the paddle.
							//
							// hitPosn ranges from 0.0 to 1.0, with a little bit of overlap
							// because the ball is round (it's based on the ball's *center*,
							// not the actual point of impact on the paddle itself -- something
							// we could correct by getting additional data out of the collision
							// detection code, but we can just as easily clamp it).
							//
							// The location determines how we alter the X velocity.  We want
							// this to be more pronounced at the edges of the paddle, especially
							// if the ball is hitting the "outside edge".
							//
							// Direction is a vector, normalized by the "set direction" method.
							// We don't need to worry about dirX growing without bound.
							//
							// This bit of code has a substantial impact on the "feel" of
							// the game.  It could probably use more tweaking.
							if (hitAdjust < 0.0f) {
								hitAdjust = 0.0f;
							}
							if (hitAdjust > 1.0f) {
								hitAdjust = 1.0f;
							}
							int hitPercent = (int) (hitAdjust * 100.0f);
							hitAdjust -= 0.5f;
							if (Math.abs(hitAdjust) > 0.25) {   // outer 25% on each side
								if (dirX < 0 && hitAdjust > 0 || dirX > 0 && hitAdjust < 0) {
									//Log.d(TAG, "outside corner, big jump");
									hitAdjust *= 1.6;
								} else {
									//Log.d(TAG, "far corner, modest jump");
									hitAdjust *= 1.2;
								}
							}
							hitAdjust *= 1.25;
							//Log.d(TAG, " hitPerc=" + hitPercent + " hitAdj=" + hitAdjust
							//        + " old dir=" + dirX + "," + dirY);
							newDirX += hitAdjust;
							float maxRatio = 3.0f;
							if (Math.abs(newDirX) > Math.abs(newDirY) * maxRatio) {
								// Limit the angle so we don't get too crazily horizontal.  Note
								// the ball could be moving downward after a collision if we're
								// in "never lose" mode and we bounced off the bottom of the
								// paddle, so we can't assume newDirY is positive.
								//Log.d(TAG, "capping Y vel to " + maxRatio + ":1");
								if (newDirY < 0) {
									maxRatio = -maxRatio;
								}
								newDirY = Math.abs(newDirX) / maxRatio;
							}
						}

						SoundResources.play(SoundResources.PADDLE_HIT);
					} else if (hit == mBorders[BOTTOM_BORDER]) {
						// We hit the bottom border.  It might be a little weird visually to
						// bounce off of it when the ball is lost, so if we hit it we stop the
						// current frame of computation immediately.  (Moving the border farther
						// off screen doesn't work -- too far and there's a long delay waiting
						// for a slow ball to drain, too close and we still get the bounce effect
						// from a fast-moving ball.)
						event = EVENT_BALL_LOST;
						distance = 0.0f;
						SoundResources.play(SoundResources.BALL_LOST);

						/*if (!mNeverLoseBall) {
                            event = EVENT_BALL_LOST;
                            distance = 0.0f;
                            SoundResources.play(SoundResources.BALL_LOST);
                        } else {
                            mScore -= 500 * mScoreMultiplier;
                            if (mScore < 0) {
                                mScore = 0;
                            }
                            SoundResources.play(SoundResources.WALL_HIT);
                        }*/
					} else {
						// hit a border or a score digit
						SoundResources.play(SoundResources.WALL_HIT);
					}

					// Increase speed by 3% after each (super-elastic!) collision, capping
					// at the skill-level-dependent maximum speed.
					int speed = mBall.getSpeed();
					speed += (mBallMaximumSpeed - mBallInitialSpeed) * 3 / 100;
					if (speed > mBallMaximumSpeed) {
						speed = mBallMaximumSpeed;
					}
					mBall.setSpeed(speed);

					mBall.setDirection(newDirX, newDirY);
					distance -= mHitDistanceTraveled;

					if (DEBUG_COLLISIONS) {
						Log.d(TAG, "COL: remaining dist=" + distance + " new dirX=" +
								mBall.getXDirection() + " dirY=" + mBall.getYDirection());
					}
				}
			}

			if (hits == 0) {
				// hit nothing, move ball to final position and bail
				if (DEBUG_COLLISIONS) {
					Log.d(TAG, "COL: none (dist was " + distance + ")");
				}
				mBall.setPosition(finalX, finalY);
				distance = 0.0f;
			}
		}

		return event;
	}

	/**
	 * Determines whether the target object could possibly collide with a ball whose current
	 * and future position are enclosed by the l/r/b/t values.
	 *
	 * @return true if we might collide with this object.
	 */
	private boolean checkCoarseCollision(BaseRect target, float left, float right,
			float bottom, float top) {       

		// Convert position+scale into l/r/b/t.
		float xpos, ypos, xscale, yscale;
		float targLeft, targRight, targBottom, targTop;

		xpos = target.getXPosition();
		ypos = target.getYPosition();
		xscale = target.getXScale();
		yscale = target.getYScale();
		targLeft = xpos - xscale;
		targRight = xpos + xscale;
		targBottom = ypos - yscale;
		targTop = ypos + yscale;

		// If the smallest right is bigger than the biggest left, and the smallest bottom is
		// bigger than the biggest top, we overlap.
		//
		// FWIW, this is essentially an application of the Separating Axis Theorem for two
		// axis-aligned rects.
		float checkLeft = targLeft > left ? targLeft : left;
		float checkRight = targRight < right ? targRight : right;
		float checkTop = targBottom > bottom ? targBottom : bottom;
		float checkBottom = targTop < top ? targTop : top;

		if (checkRight > checkLeft && checkBottom > checkTop) {
			return true;
		}
		return false;
	}

	/**
	 * Tests for a collision with the rectangles in mPossibleCollisions as the ball travels from
	 * (curX,curY).
	 * <p>
	 * We can't return multiple values from a method call in Java.  We don't want to allocate
	 * storage for the return value on each frame (this being part of the main game loop).  We
	 * can define a class that holds all of the return values and allocate a single instance
	 * of it when BrickBreakerState is constructed, or just drop the values into dedicated return-value
	 * fields.  The latter is incrementally easier, so we return the object we hit, and store
	 * additional details in these fields:
	 * <ul>
	 * <li>mHitDistanceLeft - the amount of distance remaining to travel after impact
	 * <li>mHitFace - what face orientation we hit
	 * <li>mHitXAdj, mHitYAdj - position adjustment so objects won't intersect
	 * </ul>
	 *
	 * @param rects Array of rects to test against.
	 * @param numRects Number of rects in array.
	 * @param curX Current X position.
	 * @param curY Current Y position.
	 * @param dirX X component of normalized direction vector.
	 * @param dirY Y component of normalized direction vector.
	 * @param distance Distance to travel.
	 * @param radius Radius of the ball.
	 * @return The object we struck, or null if none.
	 */
	private BaseRect findFirstCollision(BaseRect[] rects, final int numRects, final float curX,
			final float curY, final float dirX, final float dirY, final float distance,
			final float radius) {

		// Maximum distance, in arena coordinates, we advance the ball on each iteration of
		// the loop.  If this is too small, we'll do a lot of unnecessary iterations.  If it's
		// too large (e.g. more than the ball's radius), the ball can end up inside an object,
		// or pass through one entirely.
		final float MAX_STEP = 2.0f;

		// Minimum distance.  After a collision the objects are just barely in contact, so at
		// each step we need to move a little or we'll double-collide.  The minimum exists to
		// ensure that we don't get hosed by floating point round-off error.
		final float MIN_STEP = 0.001f;

		float radiusSq = radius * radius;
		int faceHit = HIT_FACE_NONE;
		int faceToAdjust = HIT_FACE_NONE;
		float xadj = 0.0f;
		float yadj = 0.0f;
		float traveled = 0.0f;

		while (traveled < distance) {
			// Travel a bit.
			if (distance - traveled > MAX_STEP) {
				traveled += MAX_STEP;
			} else if (distance - traveled < MIN_STEP) {
				//Log.d(TAG, "WOW: skipping tiny step distance " + (distance - traveled));
				break;
			} else {
				traveled = distance;
			}
			float circleXWorld = curX + dirX * traveled;
			float circleYWorld = curY + dirY * traveled;

			for (int i = 0; i < numRects; i++) {
				BaseRect rect = rects[i];
				float rectXWorld = rect.getXPosition();
				float rectYWorld = rect.getYPosition();
				float rectXScaleHalf = rect.getXScale() / 2.0f;
				float rectYScaleHalf = rect.getYScale() / 2.0f;

				// Translate the circle so that it's in the first quadrant, with the center of the
				// rectangle at (0,0).
				float circleX = Math.abs(circleXWorld - rectXWorld);
				float circleY = Math.abs(circleYWorld - rectYWorld);

				if (circleX > rectXScaleHalf + radius || circleY > rectYScaleHalf + radius) {
					// Circle is too far from rect edge(s) to overlap.  No collision.
					continue;
				}

				/*
				 * Check to see if the center of the circle is inside the rect on one axis.  
				 */
				if (circleX <= rectXScaleHalf) {
					faceToAdjust = faceHit = HIT_FACE_HORIZONTAL;
				} else if (circleY <= rectYScaleHalf) {
					faceToAdjust = faceHit = HIT_FACE_VERTICAL;
				} else {
					// Check the distance from rect corner to center of circle.
					float xdist = circleX - rectXScaleHalf;
					float ydist = circleY - rectYScaleHalf;
					if (xdist*xdist + ydist*ydist > radiusSq) {
						// Not close enough.
						//Log.d(TAG, "COL: corner miss");
						continue;
					}

					float dirXSign = Math.signum(dirX);
					float dirYSign = Math.signum(dirY);
					float cornerXSign = Math.signum(rectXWorld - circleXWorld);
					float cornerYSign = Math.signum(rectYWorld - circleYWorld);

					String msg;
					if (dirXSign == cornerXSign && dirYSign == cornerYSign) {
						faceHit = HIT_FACE_SHARPCORNER;
						msg = "sharp";
						if (DEBUG_COLLISIONS) {
							// Sharp corners can be interesting.  Slow it down for a few
							// seconds.
							mDebugSlowMotionFrames = 240;
						}
					} else if (dirXSign == cornerXSign) {
						faceHit = HIT_FACE_VERTICAL;
						msg = "vert";
					} else if (dirYSign == cornerYSign) {
						faceHit = HIT_FACE_HORIZONTAL;
						msg = "horiz";
					} else {
						// This would mean we hit the far corner of the brick, i.e. the ball
						// passed completely through it.
						Log.w(TAG, "COL: impossible corner hit");
						faceHit = HIT_FACE_SHARPCORNER;
						msg = "???";
					}

					if (DEBUG_COLLISIONS) {
						Log.d(TAG, "COL: " + msg + "-corner hit xd=" + xdist + " yd=" + ydist
								+ " dir=" + dirXSign + "," + dirYSign
								+ " cor=" + cornerXSign + "," + cornerYSign);
					}

					// Adjust whichever requires the least movement to guarantee we're no
					// longer colliding.
					if (xdist < ydist) {
						faceToAdjust = HIT_FACE_HORIZONTAL;
					} else {
						faceToAdjust = HIT_FACE_VERTICAL;
					}
				}

				if (DEBUG_COLLISIONS) {
					String msg = "?";
					if (faceHit == HIT_FACE_SHARPCORNER) {
						msg = "corner";
					} else if (faceHit == HIT_FACE_HORIZONTAL) {
						msg = "horiz";
					} else if (faceHit == HIT_FACE_VERTICAL) {
						msg = "vert";
					}
					Log.d(TAG, "COL: " + msg + " hit " + rect.getClass().getSimpleName() +
							" cx=" + circleXWorld + " cy=" + circleYWorld +
							" rx=" + rectXWorld + " ry=" + rectYWorld +
							" rxh=" + rectXScaleHalf + " ryh=" + rectYScaleHalf);
				}

				/*
				 * Collision!
				 *
				 * Because we're moving in discrete steps rather than continuously, we will
				 * usually end up slightly embedded in the object.  If, after reversing direction,
				 * we subsequently step forward very slightly (assuming a non-destructable
				 * object like a wall), we will detect a second collision with the same object,
				 * and reverse direction back *into* the wall.  Visually, the ball will "stick"
				 * to the wall and vibrate.
				 *
				 * We need to back the ball out slightly.  Ideally we'd back it along the path
				 * the ball was traveling by just the right amount, but unless MAX_STEP is
				 * really large the difference between that and a minimum-distance axis-aligned
				 * shift is negligible -- and this is easier to compute.
				 *
				 * There's some risk that our adjustment will leave the ball trapped in a
				 * different object.  Since the ball is the only object that's moving, and the
				 * direction of adjustment shouldn't be too far from the angle of incidence, we
				 * shouldn't have this problem in practice.
				 *
				 * Note this leaves the ball just *barely* in contact with the object it hit,
				 * which means it's technically still colliding.  This won't cause us to
				 * collide again and reverse course back into the object because we will move
				 * the ball a nonzero distance away from the object before we check for another
				 * collision.  The use of MIN_STEP ensures that we won't fall victim to floating
				 * point round-off error.  (If we didn't want to guarantee movement, we could
				 * shift the ball a tiny bit farther so that it simply wasn't in contact.)
				 */
				float hitXAdj, hitYAdj;
				if (faceToAdjust == HIT_FACE_HORIZONTAL) {
					hitXAdj = 0.0f;
					hitYAdj = rectYScaleHalf + radius - circleY;
					if (BrickBreakerSurfaceRenderer.EXTRA_CHECK && hitYAdj < 0.0f) {
						Log.e(TAG, "HEY: horiz was neg");
					}
					if (circleYWorld < rectYWorld) {
						// ball is below rect, must be moving up, so adjust it down
						hitYAdj = -hitYAdj;
					}
				} else if (faceToAdjust == HIT_FACE_VERTICAL) {
					hitXAdj = rectXScaleHalf + radius - circleX;
					hitYAdj = 0.0f;
					if (BrickBreakerSurfaceRenderer.EXTRA_CHECK && hitXAdj < 0.0f) {
						Log.e(TAG, "HEY: vert was neg");
					}
					if (circleXWorld < rectXWorld) {
						// ball is left of rect, must be moving to right, so adjust it left
						hitXAdj = -hitXAdj;
					}
				} else {
					Log.w(TAG, "GLITCH: unexpected faceToAdjust " + faceToAdjust);
					hitXAdj = hitYAdj = 0.0f;
				}

				if (DEBUG_COLLISIONS) {
					Log.d(TAG, "COL:  r=" + radius + " trav=" + traveled +
							" xadj=" + hitXAdj + " yadj=" + hitYAdj);
				}
				mHitFace = faceHit;
				mHitDistanceTraveled = traveled;
				mHitXAdj = hitXAdj;
				mHitYAdj = hitYAdj;
				return rect;
			}
		}

		//Log.d(TAG, "COL: no collision");
		return null;
	}

	/**
	 * Game state storage.  Anything interesting gets copied in here.  If we wanted to save it
	 * to disk we could just serialize the object.
	 * <p>
	 * This is "organized" as a dumping ground for BrickBreakerState to use.
	 */
	private static class SavedGame {
		public int mBricksState[][];
		public float mBallXDirection, mBallYDirection;
		public float mBallXPosition, mBallYPosition;
		public int mBallSpeed;
		public float mPaddlePosition;
		public int mGamePlayState;
		public int mGameStatusMessageNum;
		public int mLivesRemaining;
		public int mScore;

		public boolean mIsValid = false;        // set when state has been written out
	}

	public void RestartGame(){
		mGamePlayState = GAME_READY;
		mIsAnimating = true;
	}

	public boolean isGamePaused(){
		return (mGamePlayState == GAME_PAUSE) ? true : false;
	}	
	
	public void gameOptions(BrickBreakerSurfaceView surfaceView, Context context, float arenaX, float arenaY){
		float posXTouch = arenaX;
		float posYTouch = ARENA_HEIGHT - arenaY;
		
		Log.v(TAG, "arenaX=" + (int) arenaX + " --> arenaY=" + (int) posYTouch);
		
		//
		float minXQuitBu = mQuit.getXPosition() - mQuit.getXScale()/2;
		float maxXQuitBu = mQuit.getXPosition() + mQuit.getXScale()/2;
		
		float minYQuitBu = mQuit.getYPosition() - mQuit.getYScale()/2;
		float maxYQuitBu = mQuit.getYPosition() + mQuit.getYScale()/2;
		
		float minXReloadBu = mReload.getXPosition() - mQuit.getXScale()/2;
		float maxXReloadBu = mReload.getXPosition() + mQuit.getXScale()/2;
		
		float minYReloadBu = mReload.getYPosition() - mReload.getYScale()/2;
		float maxYReloadBu = mReload.getYPosition() + mReload.getYScale()/2;
		
		float minXNextBu = mNextLevel.getXPosition() - mNextLevel.getXScale()/2;
		float maxXNextBu = mNextLevel.getXPosition() + mNextLevel.getXScale()/2;
		
		float minYNextBu = mNextLevel.getYPosition() - mNextLevel.getYScale()/2;
		float maxYNextBu = mNextLevel.getYPosition() + mNextLevel.getYScale()/2;
		
		if ((minXQuitBu <= posXTouch && posXTouch<= maxXQuitBu) && 
				(minYQuitBu <= posYTouch && posYTouch<= maxYQuitBu)){
			// Touch on quit button
			Log.v(TAG, "Show the menu game");
			 //Show frament_main layout
			Intent myIntent = new Intent(context,MainActivity.class);			
			context.startActivity(myIntent);	
			return;
			
		}	
		else if ((minXReloadBu <= posXTouch && posXTouch<= maxXReloadBu) && 
				(minYReloadBu <= posYTouch && posYTouch<= maxYReloadBu)){
			// Touch on Restart button
			Log.v(TAG, "Restart the game");
			//BrickBreakerActivity.invalidateSavedGame();
			reset();
			TexturedBasicAlignedRect.prepareToDraw();
			allocBricks(context);
	        //drawBricks();
	        TexturedBasicAlignedRect.finishedDrawing();
	        TexturedAlignedRect.prepareToDraw();
	        allocScore();
	        //drawScore();      
	        TexturedAlignedRect.finishedDrawing();
        	surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        	
			//Show frament_main layout
			Intent myIntent = new Intent(context,BrickBreakerActivity.class);
			//configuring the parameters to start the same  level
			context.startActivity(myIntent);
			
			
		}		
		else if ((minXNextBu <= posXTouch && posXTouch<= maxXNextBu) && 
				(minYNextBu <= posYTouch && posYTouch<= maxYNextBu)) {
			// Touch  on Next Button
			Log.v(TAG, "Next level");
			//restore 
			BrickBreakerActivity.invalidateSavedGame();
			reset();
        	surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        	RestartGame();
			 //Show frament_main layout
			Intent myIntent = new Intent(context, BrickBreakerActivity.class);
			//configurating the parameters for next level
			context.startActivity(myIntent);
			
	        
			
		}
	}
}
