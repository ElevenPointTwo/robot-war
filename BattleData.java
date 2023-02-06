package robot_war_summative;

/**
 * Creating a class to extend OppData in order to store more information.
 * @author Kevin D
 */

public class BattleData extends OppData {
	private int healthLost = 0, roundsFought = 0, maxMoves = 0;

	/**
	 * Constructor to set up a BattleData record.
	 * @param id The ID of the opponent.
	 * @param a The opponent's avenue.
	 * @param s The opponent's street.
	 * @param health The opponent's health.
	 */
	public BattleData(int id, int a, int s, int health) {
		super(id, a, s, health);
	}
	
	/**
	 * Returns the total health lost from previous encounters with this opponent.
	 * @return The health lost.
	 */
	public int getHealthLost() {
		return this.healthLost;
	}
	
	/**
	 * Adding to the total amount of health lost from encounters with this opponent.
	 * @param loss The amount of health lost from this round.
	 */
	public void addHealthLost(int loss) {
		this.healthLost += loss;
	}
	
	/**
	 * Returns the number of times the fighter has fought this opponent before.
	 * @return The number of fights.
	 */
	public int getRoundsFought() {
		return this.roundsFought;
	}
	
	/**
	 * Adds one round to the number of times that the fighter has fought this opponent.
	 */
	public void addRoundsFought() {
		this.roundsFought++;
	}
	
	/**
	 * Returns the potential maximum number of movement points for this opponent.
	 * @return The max number of moves.
	 */
	public int getMaxMoves() {
		return this.maxMoves;
	}
	
	/**
	 * Updating the known maximum number of moves of an opponent in order to determine the maximum bounds of its other points.
	 * @param moves The number of moves the opponent moved this round.
	 */
	public void setMaxMoves(int moves) {
		// If this round's moves is greater than the current known maxMoves, update it.
		if(moves > this.maxMoves) {
			this.maxMoves = moves;
		}
	}
	
	public String toString() {
		return "**ID: " + this.getID() + " HL: " + this.getHealthLost() + " (" + this.getAvenue() + ", " + this.getStreet() + ") H: " + this.getHealth() + " R: " + this.getRoundsFought() + " M: " + this.getMaxMoves() + "**";
	}
}
