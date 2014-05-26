package br.usp.ime.brickbreakerapp;

public class LevelParameters {
	private static final String TAG = MainActivity.TAG;
    // configuration of bricks
	
	public static ParametersConfig configLevelParameters(int level){
		ParametersConfig param = new ParametersConfig();
        switch (level) {
            case 1:                     // easy
                param.ballSize = 1.2f; //2.0f
                param.paddleSize = 1.2f;//2.0f
                //Scale growth of score 
                param.scoreMultiplier = 0.75f;
                param.maxLives = 3;
                param.minSpeed = 200;
                param.maxSpeed = 500;//500
                
                // configuration of bricks
                // NIVEL I: NORMAL BRICKS
                param.configStr = new String[]{"111111111","101010101", "101040101", "101010101", "101010101", "111111111"};   
                //aram.configStr = new String[]{"000000000","000000000", "000000000", "000145550", "000000000", "000000000"};
                param.backgroundTextureImg = "drawable/background_1";
                break;
            case 2:                     // normal
            	param.ballSize = 1;
            	param.paddleSize = 1.0f;
            	param.scoreMultiplier = 1.0f;
            	param.maxLives = 3;
            	param.minSpeed = 300;
            	param.maxSpeed = 800;
                
                // configuration of bricks
        		// NIVEL II: Letter I
            	param.configStr = new String[]{"002222200","001111100", "000242000", "000232000", "001111100", "002222200"};
            	param.backgroundTextureImg = "drawable/background_2";
                break;
            case 3:                     // normal
            	param.ballSize = 1;
            	param.paddleSize = 1.0f;
            	param.scoreMultiplier = 1.0f;
            	param.maxLives = 2;
            	param.minSpeed = 300;
            	param.maxSpeed = 800;
                
                // configuration of bricks
        		// NIVEL III: FACE
            	//param.configStr = new String[]{"000111000", "111000111", "011111110", "111515111", "101111101", "000101000"};
            	param.configStr = new String[]{"000222000", "111000111", "011333110", "111535111", "102222201", "000202000"};
            	param.backgroundTextureImg = "drawable/background_3";
                break;
            case 4:                     // hard
            	param.ballSize = 1;
            	param.paddleSize = 1.0f;
            	param.scoreMultiplier = 1.0f;
            	param.maxLives = 3;
            	param.minSpeed = 300;
            	param.maxSpeed = 800;
                		
        		// NIVEL IV: CASTLE
            	param.configStr = new String[]{"021242120", "021222120", "021222120", "021111120", "222222222", "225222522"};
            	param.backgroundTextureImg = "drawable/background_4";
                break;
                
            case 5:                     // hard
            	param.ballSize = 1.0f;
            	param.paddleSize = 0.8f;
            	param.scoreMultiplier = 1.25f;
            	param.maxLives = 3;
            	param.minSpeed = 600;
            	param.maxSpeed = 1200;
                
                // configuration of bricks
        		// NIVEL V : (SNAKE)
            	param.configStr = new String[]{"533033304", "202020202", "202020202", "202020202", "202020202", "403330335"};
            	param.backgroundTextureImg = "drawable/background_5";
                break;
                
            case 6:                     // hard
            	param.ballSize = 1.0f;
            	param.paddleSize = 0.85f;
            	param.scoreMultiplier = 1.25f;
            	param.maxLives = 3;
            	param.minSpeed = 600;
            	param.maxSpeed = 1200;
                
                // configuration of bricks    
        		// NIVEL VI : USP
            	param.configStr = new String[]{"522212225", "111333100", "101003100", "101343111", "101300101", "101333111"};
            	param.backgroundTextureImg = "drawable/background_6";
                break;    
                
            case 7:                     // hard
            	param.ballSize = 1.0f;
            	param.paddleSize = 0.5f;
            	param.scoreMultiplier = 0.1f;
            	param.maxLives = 1;
            	param.minSpeed = 1000;
            	param.maxSpeed = 100000;
                
                // configuration of bricks    
        		// NIVEL VI : USP
            	param.configStr = new String[]{"222222222", "111333100", "101003100", "101333111", "101300101", "101333111"};  
            	param.backgroundTextureImg = "drawable/background_7";
                break;    
                
            default:
                throw new RuntimeException("bad difficulty index " + level);
        }
        return param;
	}
	
	public static class ParametersConfig{
		public float ballSize;
	    public float paddleSize;
	    public float scoreMultiplier;
	    public int maxLives;
	    public int minSpeed;
	    public int maxSpeed;
	    
	    // configuration of bricks
		public String [] configStr;
		public String backgroundTextureImg;
	}

}
