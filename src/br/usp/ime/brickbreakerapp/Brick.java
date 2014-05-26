package br.usp.ime.brickbreakerapp;

/**
 * Represents an immobile, destructible brick.
 */
public class Brick extends TexturedBasicAlignedRect {

    //private boolean mAlive = false;
    private int mPoints = 0;
    /*
     * Type of brick
     * 0:dead, 1:normal, 2: especial brick 'rock'(destroyed with 2 hits )
     * 3:especial brick(steel)(destroyed with 3 hits)
     * 4:it increases the size of paddle
     * 5:it gives one opportunity to play
     */
    private int mBrickState = 0; 
    
    /**
     * Returns the state of brick
     */
    public int getBrickState() {
        return mBrickState;
    }

    /**
     * Sets the brick liveness.
     */
    public void setBrickState(int state) {
        mBrickState = state;
    }

    /**
     * Gets the brick's point value.
     */
    public int getScoreValue() {
        return mPoints;
    }

    /**
     * Sets the brick's point value.
     */
    public void setScoreValue(int points) {
        mPoints = points;
    }
    
    /**
     * Verify whether a brick is alive
     */
    public boolean isAlive(){
    	return (mBrickState!=0? true:false);
    }
    
}
