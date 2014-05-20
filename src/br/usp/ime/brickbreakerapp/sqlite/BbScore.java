package br.usp.ime.brickbreakerapp.sqlite;

public class BbScore {
	private int id;
	private String username;
	private int rank;
	private int score;

	public BbScore() {
		this.id = -1;
		this.username = null;
		this.rank = -1;
		this.score = -1; // score == -1 if the player haven't played yet
	}
	
	public BbScore(int id, int rank, String username, int score) {
		this.id = id;
		this.username = username;
		this.rank = rank;
		this.score = score;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	@Override
	public String toString() {
		return "BbScore [rank=" + rank + ", username=" + username + ", score=" + score+ "]";
	}
}
