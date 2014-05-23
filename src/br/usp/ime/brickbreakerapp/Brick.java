package br.usp.ime.brickbreakerapp;

/**
 * Represents an immobile, destructible brick.
 */
public class Brick extends TexturedBasicAlignedRect {
    /*
     * It's worth noting that the position, size, color, and score value of a brick is fixed,
     * and could be computed on the fly while drawing.  We don't need a Brick object per brick;
     * all we really need is a bit vector that tells us whether or not brick N is alive.  We
     * can draw all bricks with a single BasicAlignedRect that we reposition.
     *
     * Implementing bricks this way would require significantly less storage but additional
     * computation per frame.  It's also a less-general solution, making it less desirable
     * for a demo app.
     */

    //private boolean mAlive = false;
    private int mPoints = 0;
    /*
     * Type of brick
     * 0:dead, 1:normal, 2: especial brick 'rock'(destroyed with 2 hits )
     * 3:especial brick(steel)(destroyed with 3 hits)
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
