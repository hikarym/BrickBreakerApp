package br.usp.ime.brickbreakerapp;

import java.io.IOException;
import br.usp.ime.brickbreakerapp.LevelParameters.ParametersConfig;
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
	public static final boolean SHOW_DEBUG_STUFF = false;       // enable on-screen debugging
	
	// Gameplay configurables.  
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
	 * 4: increases the size of the brick
	 * 5: increases the number of lives
	 */
	private static final int BRICK_EMPTY = 0;
	private static final int BRICK_NORMAL = 1;
	private static final int BRICK_2HITS = 2;
	private static final int BRICK_3HITS = 3;
	private static final int BRICK_ESPECIAL1 = 4;
	private static final int BRICK_ESPECIAL2 = 5;
	
	/*
	 * Value of each type of brick
	 * 1: normal,
	 * 2: the brick is destroyed with 2 hits,
	 * 3: the brick is destroyed with 3 hits,
	 * 4: increases the size of the brick
	 * 5: increases the number of lives
	 */
	
	private static final int[] VALUES_BRICKS_DEFAULTS = new int[]{100,200,300,300,300};
	
	// Number of brick states
	private static final int BRICK_STATES = 6;
	// Number of levels
	private static final int BRICK_LEVELS = 7;
	private int[][] mBrickStatesConfig = new int[BRICK_ROWS][BRICK_COLUMNS];
	private Bitmap[] mBMPBrickTexture = new Bitmap[BRICK_STATES - 1];
	private Bitmap[] mBMPBkgLevel = new Bitmap[BRICK_LEVELS];
	

	// In-memory saved game.  
	//The game is saved and restored whenever the Activity is paused and resumed
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
	

	private static final float PADDLE_VERTICAL_PERC = 8 / 100.0f;
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
	private String mButtonSettingsTextureImg = "drawable/settings";
	private String mButtonReloadLevelTextureImg = "drawable/reload";

	// Button size
	private static final int DEFAULT_BUTTON_WIDTH =
			(int) (ARENA_WIDTH * BUTTON_WIDTH_PERC * BUTTON_DEFAULT_WIDTH);
	private static final int DEFAULT_BUTTON_HEIGHT =
			(int) (ARENA_WIDTH * BUTTON_HEIGHT_PERC * BUTTON_DEFAULT_WIDTH);

	private Brick mBricks[][] = new Brick[BRICK_ROWS][BRICK_COLUMNS];
	private int mLiveBrickCount;

	private static final int DEFAULT_PADDLE_WIDTH =
			(int) (ARENA_WIDTH * PADDLE_WIDTH_PERC * PADDLE_DEFAULT_WIDTH);
	private TexturedBasicAlignedRect mPaddle;
	
	//Buttons
	private TexturedBasicAlignedRect mQuitButton;
	private TexturedBasicAlignedRect mNextLevelButton;
	private TexturedBasicAlignedRect mBack;
	private TexturedBasicAlignedRect mSettings;
	private TexturedBasicAlignedRect mReloadButton;
	
	private TexturedBasicAlignedRect mSettingsLayer;

	private static final int DEFAULT_BALL_DIAMETER = (int) (ARENA_WIDTH * BALL_WIDTH_PERC);
	private Ball mBall;

	private static final double NANOS_PER_SECOND = 1000000000.0;
	private static final double MAX_FRAME_DELTA_SEC = 0.5;
	private long mPrevFrameWhenNsec;

	/*
	 * Pause briefly on certain transitions, e.g. before launching a new ball after one was lost.
	 */
	private float mPauseDuration;

	//private int mDebugSlowMotionFrames;

	// If FRAME_RATE_SMOOTHING is true, then the rest of these fields matter.
	private static final boolean FRAME_RATE_SMOOTHING = false;
	private static final int RECENT_TIME_DELTA_COUNT = 5;
	double mRecentTimeDelta[] = new double[RECENT_TIME_DELTA_COUNT];
	int mRecentTimeDeltaNext;

	// Storage for collision detection results.
	private static final int HIT_FACE_NONE = 0;
	private static final int HIT_FACE_VERTICAL = 1;
	private static final int HIT_FACE_HORIZONTAL = 2;
	private static final int HIT_FACE_SHARPCORNER = 3;
	private BaseRect[] mPossibleCollisions =
			new BaseRect[BRICK_COLUMNS * BRICK_ROWS + NUM_BORDERS + NUM_SCORE_DIGITS + 1/*paddle*/];
	private float mHitDistanceTraveled;     // result from findFirstCollision()
	private float mHitXAdj, mHitYAdj;       // result from findFirstCollision()
	private int mHitFace;                   // result from findFirstCollision()
	//private OutlineAlignedRect mDebugCollisionRect;  // visual debugging
	
	// Game play state.	 
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
	
	//Events that can happen when the ball moves.
	private static final int EVENT_NONE = 0;
	private static final int EVENT_LAST_BRICK = 1;
	private static final int EVENT_BALL_LOST = 2;

	
	// Text message to display in the middle of the screen (e.g. "won" or "game over").	
	private static final float STATUS_MESSAGE_WIDTH_PERC = 85 / 100.0f;
	private TexturedAlignedRect mGameStatusMessages;
	private int mGameStatusMessageNum;
	private int mDebugFramedString;

	// Score display
	private static final int NUM_SCORE_DIGITS = 5;
	private TexturedAlignedRect[] mScoreDigits = new TexturedAlignedRect[NUM_SCORE_DIGITS];

	// Text resources, notably including an image texture for our various text strings.
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

	/**
	 * Resets game state to initial values.  
	 */
	private void reset() {

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
		//mGameLevel = 1;
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

		Log.d(TAG, "game saved");
	}

	/**
	 * Restores game state from save area.  
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
					if (bricks[i][j]==BRICK_EMPTY) {
						mLiveBrickCount--;						
					} 
				}
			}
			Log.d(TAG, "llive brickcount is " + mLiveBrickCount);

			mBall.setDirection(save.mBallXDirection, save.mBallYDirection);
			mBall.setPosition(save.mBallXPosition, save.mBallYPosition);
			mBall.setSpeed(save.mBallSpeed);
			movePaddle(save.mPaddlePosition);

			mGamePlayState = save.mGamePlayState;
			mGameLevel = save.
			mGameStatusMessageNum = save.mGameStatusMessageNum;
			mLivesRemaining = save.mLivesRemaining;
			mScore = save.mScore;
		}

		Log.d(TAG, "game restored");
		return true;
	}

	/**
	 * Performs some housekeeping after the Renderer surface has changed.
	 */
	public void surfaceChanged() {
		// Pause briefly.  This gives the user time to orient themselves after a screen
		// rotation or switching back from another app.
		setPauseTime(1.5f);

		// Reset this so we don't leap forward.  
		mPrevFrameWhenNsec = 0;

		// We need to draw the screen at least once
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
	 */
	public static void invalidateSavedGame() {
		synchronized (sSavedGame) {
			sSavedGame.mIsValid = false;
		}
	}

	/**
	 * Determines whether we have saved a game that can be resumed.  
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
		
		mBackgroundImg = new TexturedBasicAlignedRect();			
		
		// Temporary create a bitmap
		Bitmap bmp = getBitmapTexture(context, mBackgroundTextureImg);        

		TexturedBasicAlignedRect rectBack = new TexturedBasicAlignedRect();
		//Log.d(TAG, "paddle y=" + rect.getYPosition());
		rectBack.setPosition(ARENA_WIDTH/2, ARENA_HEIGHT/2);
		rectBack.setScale(ARENA_WIDTH - BORDER_WIDTH * 2, ARENA_HEIGHT - BORDER_WIDTH * 2);
		//rectBack.setColor(0.1f, 0.1f, 0.1f);
		rectBack.setTexture(bmp);

		mBackgroundImg = rectBack;
	}


	/**
	 * Draw the background of a game level
	 */
	void drawBackground() {
		mBackgroundImg.draw();
	}
	
	/**
	 * Get a bitmap object from a source
	 * @param context
	 * @param src i.e: "drawable/background_3" 
	 * @return a Bitmap object
	 */
	private Bitmap getBitmapTexture(Context context, String src){
		int id = context.getResources().getIdentifier(src, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		return bmp;
	}
	
	/**
	 * Allocates the bricks, setting their sizes and positions and textures.  Sets mLiveBrickCount.
	 */
	void allocBricks(Context context) {		
		// Textures for different types of bricks
		// 1. texture for normal brick.
		// (The brick with this texture é destroyed with 1 hit)
		mBMPBrickTexture[0] = getBitmapTexture(context, mBrickNormalTextureImg);
		
		// 2. texture for rock brick
		// (The brick with this texture é destroyed with 2 hits)
		mBMPBrickTexture[1] = getBitmapTexture(context, mBrickRockTextureImg);
		
		// 3. texture for mix brick
		// (The brick with this texture é destroyed with 3 hit, and it reduces the 
		// speed of ball)
		mBMPBrickTexture[2] = getBitmapTexture(context, mBrickMixTextureImg);		

		// 4. texture for especial brick
		// (The brick with this texture é destroyed with 1 hit, and it increases
		// the size of the paddle)
		mBMPBrickTexture[3] = getBitmapTexture(context, mBrickEspecial1TextureImg);
		
		// 5. texture for especial brick
		// (The brick with this texture é destroyed with 1 hit, and increases 
		// one live more, that it says, one chance to play)
		mBMPBrickTexture[4] = getBitmapTexture(context, mBrickEspecial2TextureImg);
		
		//------------------------
		
		final float totalBrickWidth = ARENA_WIDTH - BORDER_WIDTH * 2;
		final float brickWidth = totalBrickWidth / BRICK_COLUMNS;
		final float totalBrickHeight = ARENA_HEIGHT * (BRICK_TOP_PERC - BRICK_BOTTOM_PERC);
		final float brickHeight = totalBrickHeight / BRICK_ROWS;
		final float zoneBottom = ARENA_HEIGHT * BRICK_BOTTOM_PERC;
		final float zoneLeft = BORDER_WIDTH;

		for (int r = 0; r < BRICK_ROWS; r++) {
			for (int c = 0; c < BRICK_COLUMNS; c++) {
				Brick brick = new Brick();
	
				float bottom = zoneBottom + r * brickHeight;
				float left = zoneLeft + c * brickWidth;
	
				// Brick position specifies the center point, so need to offset from bottom left.
				brick.setPosition(left + brickWidth / 2, bottom + brickHeight / 2);
	
				// Brick size is the size of the "brick zone", scaled down by a few % on each edge.
				brick.setScale(brickWidth * (1.0f - BRICK_HORIZONTAL_GAP_PERC),
						brickHeight * (1.0f - BRICK_VERTICAL_GAP_PERC));
				
				brick.setBrickState(mBrickStatesConfig[r][c]);
				if (mBrickStatesConfig[r][c]!=BRICK_EMPTY) {
					// The score value of brick is VALUE_BRICK_DEFAULT
					brick.setScoreValue(VALUES_BRICKS_DEFAULTS[mBrickStatesConfig[r][c]-1]);
					brick.setTexture(mBMPBrickTexture[mBrickStatesConfig[r][c]-1]);
					
					// counting the number of bricks to draw
					mLiveBrickCount ++;
				}				
				// Building the matrix of bricks
				mBricks[r][c] = brick;
			}
		}
		
	}

	/**
	 * Drawing the "live" bricks.
	 */
	void drawBricks() {
		for (int r = 0; r < BRICK_ROWS; r++) {
			for (int c = 0; c < BRICK_COLUMNS; c++) {
				Brick brick = mBricks[r][c];
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
		
		// play area
		rect = new BasicAlignedRect();
		rect.setPosition(ARENA_WIDTH/2, ARENA_HEIGHT/2);
		rect.setScale(ARENA_WIDTH, ARENA_HEIGHT);
		//rect.setColor(0.1f, 0.1f, 0.1f);
		rect.setColor(0.451f, 0.541f, 0.322f);
		mBackgroundColor = rect;

		// This rect is just off the bottom of area of game.  
		rect = new BasicAlignedRect();
		rect.setPosition(ARENA_WIDTH/2, -BORDER_WIDTH/2);//---------------------------------------------------
		rect.setScale(ARENA_WIDTH, BORDER_WIDTH);
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
		rect.setPosition(ARENA_WIDTH/2, ARENA_HEIGHT - BORDER_WIDTH/2);//--------------------------------------
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
		// Temporary create a bitmap
		Bitmap bmp = getBitmapTexture(context, mPaddleTextureImg);      

		rect.setScale(DEFAULT_PADDLE_WIDTH * mPaddleSizeMultiplier,
				ARENA_HEIGHT * PADDLE_HEIGHT_PERC);
		//rect.setColor(1.0f, 1.0f, 1.0f);        

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
	 * Moves the paddle to a new location.  
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
		// Temporary create a bitmap
		Bitmap bmp = getBitmapTexture(context, mBallTextureImg);
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
		Ball ball = mBall;
		float savedX = ball.getXPosition();
		float savedY = ball.getYPosition();
		float radius = ball.getRadius();

		float xpos = BORDER_WIDTH * 1.5f + radius;
		float ypos = ARENA_HEIGHT - (BORDER_WIDTH + radius * 1.2f);
		int lives = mLivesRemaining;
		boolean ballIsLive = (mGamePlayState != GAME_PAUSE && mGamePlayState != GAME_READY);
		if (ballIsLive) {
			// it decreasing  the number of "live" balls  whether the game is animated
			lives--;
		}
		
		Log.v(TAG, "ballIsLive: "+ String.valueOf(ballIsLive)+"|lives: "+String.valueOf(lives)
				+"|mLiveBrickCount: "+String.valueOf(mLiveBrickCount));

		for (int i = 0; i < lives; i++) {
			float jitterX = 0.0f;
			float jitterY = 0.0f;
			// Vibrate the "remaining lives" balls when we're almost out of lives.
			if (mLivesRemaining > 0 && mLivesRemaining < 3) {
				jitterX = (float) ((4 - mLivesRemaining) * (Math.random() - 0.5) * 2);
				jitterY = (float) ((4 - mLivesRemaining) * (Math.random() - 0.5) * 2);
			}
			// Draw the remaining balls
			ball.setPosition(xpos + jitterX, ypos + jitterY);
			ball.draw();
			//changing xpos to draw the new remaining ball
			xpos += radius * 2.2f ;
		}
		
		// drawing the ball is moving 
		ball.setPosition(savedX, savedY);
		if (ballIsLive) {
			ball.draw();
		}
	}

	/**
	 * Creates objects required to display a numeric score.
	 */
	void allocScore() {
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
		mGameStatusMessages = new TexturedAlignedRect();
		mGameStatusMessages.setTexture(mTextRes.getTextureHandle(),
				mTextRes.getTextureWidth(), mTextRes.getTextureHeight());
		mGameStatusMessages.setPosition(ARENA_WIDTH / 2, ARENA_HEIGHT / 2);
	}

	/**
	 * If appropriate, draw a message in the middle of the screen.
	 * GAME OVER, WINNER, TOUCH SCREEN TO START, READY?
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
	 * Allocates the buttons.
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

		mQuitButton = rect;
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

		mNextLevelButton = rect;
	}
	
	void allocButtonReloadLevel(Context context) {

		// Show buttons exit, next level, restart level

		TexturedBasicAlignedRect rect = new TexturedBasicAlignedRect();
		float w = DEFAULT_BUTTON_WIDTH * mButtonMultiplier;
		float h = DEFAULT_BUTTON_HEIGHT * mButtonMultiplier;

		// --------------------Button Reload level
		int id = context.getResources().getIdentifier(
				mButtonReloadLevelTextureImg, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		
		rect.setScale(w, h);

		rect.setColor(1.0f, 1.0f, 1.0f);        // note color is cycled during pauses

		float pos_x = ARENA_WIDTH / 2.0f + w;
		float pos_y = ARENA_HEIGHT / 2.0f - h;

		rect.setPosition(pos_x, pos_y);
		//Log.d(TAG, "button x=" + String.valueof(pos_x));
		rect.setTexture(bmp);

		mReloadButton = rect;
	}

	void allocButtonSettings(Context context) {
		// Show button settings

		TexturedBasicAlignedRect rect = new TexturedBasicAlignedRect();
		float w = DEFAULT_BUTTON_WIDTH * mButtonMultiplier;
		float h = DEFAULT_BUTTON_HEIGHT * mButtonMultiplier;

		// --------------------Button Settings
		int id = context.getResources().getIdentifier(
				mButtonSettingsTextureImg, null, context.getPackageName());
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		
		rect.setScale(w, h);

		rect.setColor(1.0f, 1.0f, 1.0f);        // note color is cycled during pauses

		float pos_x = ARENA_WIDTH / 2.0f + w;
		float pos_y = ARENA_HEIGHT / 2.0f - h;

		rect.setPosition(pos_x, pos_y);
		//Log.d(TAG, "button x=" + String.valueof(pos_x));
		rect.setTexture(bmp);

		mSettings = rect;
	}
/*
	void allocLayerSettings(Context context) {

		TexturedBasicAlignedRect rect = new TexturedBasicAlignedRect();

		// --------------------Button Settings
		int id = context.getResources().getIdentifier(
				mLayerSettingsTextureImg, null, context.getPackageName());
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		
		rect.setPosition(-ARENA_WIDTH/2, -ARENA_HEIGHT/2);
		rect.setScale(ARENA_WIDTH, WINDOW_WIDTH - ARENA_HEIGHT);
		rect.setColor(0.1f, 0.1f, 0.1f);
		rect.setTexture(bmp);
		
		mSettingsLayer = rect;
	}
*/
	//---Draw the layer that will be used for settings and the part that can move the paddle
	void drawSettingsLayer() {
		mSettingsLayer.draw();
		mSettings.draw();
	}
	
	/**
	 * Draw the buttons for GAME OVER and WINNER screen
	 */
	void drawButtons(){
		Log.v(TAG, "mGameStatusMessageNum: " + String.valueOf(mGameStatusMessageNum));
		switch (mGameStatusMessageNum) {
		case TextResources.GAME_OVER:
			mQuitButton.draw();
			mReloadButton.draw();
			break;
		case TextResources.WINNER:
			mQuitButton.draw();
			mNextLevelButton.draw();
			break;
		default:
			break;
		}
	}	

	/**
	 * Sets the pause time.  
	 */
	void setPauseTime(float durationMsec) {
		mPauseDuration = durationMsec;
	}

	/**
	 * Updates all game state for the next frame.  
	 */
	void calculateNextFrame(Context context) {
		Log.v(TAG, "calcula frame");
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
			int event = moveBall(context, deltaSec);
			Log.v(TAG, "EVENT dep: "+String.valueOf(event));
			switch (event) {
			case EVENT_LAST_BRICK:
				mGamePlayState = GAME_WON;
				setPauseTime(1.5f);
				SoundResources.play(SoundResources.WINNER_MUSIC);
				break;
			case EVENT_BALL_LOST:
				if (--mLivesRemaining == 0) {
					// game over
					SoundResources.play(SoundResources.GAME_OVER_MUSIC);
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
				//SoundResources.play(SoundResources.GAME_OVER_MUSIC);
				//setPauseTime(1.5f);
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
	private int moveBall(Context context, double deltaSec) {
		int event = EVENT_NONE;

		float radius = mBall.getRadius();
		float distance = (float) (mBall.getSpeed() * deltaSec);
		
		while (distance > 0.0f) {
			float curX = mBall.getXPosition();
			float curY = mBall.getYPosition();
			float dirX = mBall.getXDirection();
			float dirY = mBall.getYDirection();
			float finalX = curX + dirX * distance;
			float finalY = curY + dirY * distance;
			float left, right, top, bottom;
			
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
					
					if (hit instanceof Brick) {
						Brick brick = (Brick) hit;
						//brick.setAlive(false);
						
						int brickState = brick.getBrickState();
						if (brickState == BRICK_ESPECIAL1) {
							//increases the  size paddle
							brickState = 0;
							setPaddleSizeMultiplier(mPaddleSizeMultiplier * 1.5f);
							allocPaddle(context);  
						}
						else if (brickState == BRICK_ESPECIAL2) {
							// increases the number of lives
							brickState= 0;
							mLivesRemaining ++;
							//setBallMaximumSpeed(mBallInitialSpeed * 3);
							setBallSizeMultiplier(mBallSizeMultiplier * 1.2f);
							
							//TexturedAlignedRect.prepareToDraw();
							//allocBall(context);
							Log.v(TAG, "EVENT ant: "+String.valueOf(event));
					        //drawBall();        
					        //TexturedAlignedRect.finishedDrawing();
							
						}
						else{
							brickState --;
						}
						brick.setBrickState(brickState);
						
						if(!brick.isAlive()){
							mLiveBrickCount--;
							
							mScore += brick.getScoreValue() * mScoreMultiplier;
							// explosion effect
							SoundResources.play(SoundResources.BRICK_HIT);
							Log.v(TAG, "score value"+brick.getScoreValue()+ "| score mult"+ String.valueOf(mScoreMultiplier));
						}else{
							// normal effect
							SoundResources.play(SoundResources.BRICK_NORMAL_HIT);
						}
						
						if (mLiveBrickCount == 0) {
							Log.d(TAG, "*** won ***");
							event = EVENT_LAST_BRICK;
							distance = 0.0f;
						}
						
						Log.v(TAG, "EVENT ant 2: "+String.valueOf(event));
						
					} else if (hit == mPaddle) {
						if (mHitFace == HIT_FACE_HORIZONTAL) {
							float paddleWidth = mPaddle.getXScale();
							float paddleLeft = mPaddle.getXPosition() - paddleWidth / 2;
							float hitAdjust = (newPosX - paddleLeft) / paddleWidth;
							
							if (hitAdjust < 0.0f) {
								hitAdjust = 0.0f;
							}
							if (hitAdjust > 1.0f) {
								hitAdjust = 1.0f;
							}
							
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
								if (newDirY < 0) {
									maxRatio = -maxRatio;
								}
								newDirY = Math.abs(newDirX) / maxRatio;
							}
						}

						SoundResources.play(SoundResources.PADDLE_HIT);
					} else if (hit == mBorders[BOTTOM_BORDER]) {
						event = EVENT_BALL_LOST;						
						distance = 0.0f;
						SoundResources.play(SoundResources.BALL_LOST);
						
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

					
				}
			}

			if (hits == 0) {
				// hit nothing, move ball to final position and bail
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
		
		float checkLeft = targLeft > left ? targLeft : left;
		float checkRight = targRight < right ? targRight : right;
		float checkTop = targBottom > bottom ? targBottom : bottom;
		float checkBottom = targTop < top ? targTop : top;

		if (checkRight > checkLeft && checkBottom > checkTop) {
			return true;
		}
		return false;
	}

	
	private BaseRect findFirstCollision(BaseRect[] rects, final int numRects, final float curX,
			final float curY, final float dirX, final float dirY, final float distance,
			final float radius) {

		
		final float MAX_STEP = 2.0f;
		final float MIN_STEP = 0.001f;

		float radiusSq = radius * radius;
		int faceHit = HIT_FACE_NONE;
		int faceToAdjust = HIT_FACE_NONE;
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

					// Adjust whichever requires the least movement to guarantee we're no
					// longer colliding.
					if (xdist < ydist) {
						faceToAdjust = HIT_FACE_HORIZONTAL;
					} else {
						faceToAdjust = HIT_FACE_VERTICAL;
					}
				}
				

				/*
				 * Collision! */
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
	 * Game state storage.  
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
		resetPaddle();
		mGamePlayState = GAME_READY;
		mIsAnimating = true;
	}

	public boolean isGamePaused(){
		return (mGamePlayState == GAME_PAUSE) ? true : false;
	}	
	
	public void gameOptions(BrickBreakerSurfaceView surfaceView, Context context, 
			float arenaX, float arenaY, boolean isGameLost){
		float posXTouch = arenaX;
		float posYTouch = ARENA_HEIGHT - arenaY;
		
		Log.v(TAG, "arenaX=" + (int) arenaX + " --> arenaY=" + (int) posYTouch);
		
		//
		float minXQuitBu = mQuitButton.getXPosition() - mQuitButton.getXScale()/2;
		float maxXQuitBu = mQuitButton.getXPosition() + mQuitButton.getXScale()/2;		
		float minYQuitBu = mQuitButton.getYPosition() - mQuitButton.getYScale()/2;
		float maxYQuitBu = mQuitButton.getYPosition() + mQuitButton.getYScale()/2;
		
		float minXReloadBu = mReloadButton.getXPosition() - mReloadButton.getXScale()/2;
		float maxXReloadBu = mReloadButton.getXPosition() + mReloadButton.getXScale()/2;	
		float minYReloadBu = mReloadButton.getYPosition() - mReloadButton.getYScale()/2;
		float maxYReloadBu = mReloadButton.getYPosition() + mReloadButton.getYScale()/2;
		
		float minXNextBu = mNextLevelButton.getXPosition() - mNextLevelButton.getXScale()/2;
		float maxXNextBu = mNextLevelButton.getXPosition() + mNextLevelButton.getXScale()/2;		
		float minYNextBu = mNextLevelButton.getYPosition() - mNextLevelButton.getYScale()/2;
		float maxYNextBu = mNextLevelButton.getYPosition() + mNextLevelButton.getYScale()/2;
		
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
				(minYReloadBu <= posYTouch && posYTouch<= maxYReloadBu) && isGameLost){
			// Touch on RESTART button (THE SAME LEVEL)
			Log.v(TAG, "Restart the same game level");
			BrickBreakerActivity.invalidateSavedGame();						
			allocBricks(context);
			allocScore();
	        reset();
	        // Continue the game
        	surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
			
		}		
		else if ((minXNextBu <= posXTouch && posXTouch<= maxXNextBu) && 
				(minYNextBu <= posYTouch && posYTouch<= maxYNextBu) && !isGameLost) {
			// Touch  on Next Button
			Log.v(TAG, "Next level");
			//restore 
			//BrickBreakerActivity.invalidateSavedGame();
			//reset();
			if (mGameLevel< LevelsFragment.MAX_LEVEL) {
				int newGameLevel = mGameLevel + 1;
				
				// Configure the next level of game
				ParametersConfig param = LevelParameters.configLevelParameters(newGameLevel);				

				mBrickStatesConfig = Library.buildBrickStatesConfig(BRICK_ROWS, BRICK_COLUMNS, param.configStr);				
		        setBallSizeMultiplier(param.ballSize);
		        setPaddleSizeMultiplier(param.paddleSize);
		        setScoreMultiplier(param.scoreMultiplier);
		        setMaxLives(param.maxLives);
		        setBallInitialSpeed(param.minSpeed);
		        setBallMaximumSpeed(param.maxSpeed);
		        setGameLevel(newGameLevel);
		        setBrickStatesConfig(mBrickStatesConfig);
		        setBackgroundLevel(param.backgroundTextureImg);
		        
		        allocBackground(context);
		        allocBackground(context);
		        allocBricks(context);
		        allocPaddle(context);
		        allocBall(context);		        
		        allocScore();
		        
		        reset();
		        
		        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
			}
		}
	}
	
}
