package robot_war_summative;

import becker.robots.*;
import java.awt.Color;

/**
 * Trying to create a smart fighter robot.
 * @author Kevin D
 */

public class DengFighterRobot extends FighterRobot {
	// Variables and constants used to store information the DFR needs, such as the number of opponents to test attack and the rounds to test attack them.
	private int health, testNum; 
	private final int TEST_ATTACKS = 2, TEST_ROUNDS = 3; 
	private BattleData[] battleRecord = new BattleData[BattleManager.NUM_PLAYERS];
	private boolean setup = false, firstTarget = false;

	/**
	 * Constructor to set up the robot.
	 * @param city The city the robot is in.
	 * @param street The robot's starting street.
	 * @param avenue The robot's starting avenue.
	 * @param dir The robot's starting direction.
	 * @param id The robot's ID.
	 * @param health The robot's health.
	 */
	public DengFighterRobot(City city, int street, int avenue, Direction dir, int id, int health) {
		super(city, street, avenue, dir, id, 4, 4, 2); // Attack = 4, defence = 4, movement = 2
		this.health = health;
		this.setLabel();

		// Initializing the battleRecord array when the DFR is created.
		for(int i = 0; i < BattleManager.NUM_PLAYERS; i++) {
			if(i != this.getID()) {
				battleRecord[i] = new BattleData(i, -1, -1, 0);
			}
			else { // It uses -2 as to not conflict with the -1 ID used for not attacking rounds.
				battleRecord[i] = new BattleData(-2, 0, 0, 0);
			}
		}

		// Setting the number of opponents it will test attack, up to a maximum of three.
		if(this.battleRecord.length - 1 >= 3) {
			this.testNum = 3;
		}
		else {
			this.testNum = this.battleRecord.length - 1;
		}

	}

	/**
	 * Moves the robot to the specified location.
	 * @param avenue The destination avenue.
	 * @param street The destination street.
	 */
	public void goToLocation(int avenue, int street) {
		// Calculating displacements.
		int x, y;
		x = avenue - this.getAvenue();
		y = street - this.getStreet();

		// Horizontal movement.
		if(x > 0)
			this.turnTo(Direction.EAST);
		else if (x < 0)
			this.turnTo(Direction.WEST);
		this.move(Math.abs(x));

		// Vertical movement.
		if(y > 0)
			this.turnTo(Direction.SOUTH);
		else if (y < 0)
			this.turnTo(Direction.NORTH);
		this.move(Math.abs(y));
	}

	/**
	 * Turns the robot towards the specified direction.
	 * @param dir The direction to turn towards.
	 */
	private void turnTo(Direction dir) {
		/* This works by assigning each direction a number to make it easier to figure out how to turn. 
		 * north = 0, east = 1, south = 2, west = 3.
		 * It then calculates the starting direction subtracted by the end direction. 
		 * For example, north -> east (0 - 1) and east -> south (1 - 2) both have a result of -1,
		 * and both only involve a right turn. */
		int start, end; 

		// Converting directions to numbers.
		if(this.getDirection() == Direction.NORTH)
			start = 0;
		else if(this.getDirection() == Direction.EAST)
			start = 1;
		else if(this.getDirection() == Direction.SOUTH)
			start = 2;
		else
			start = 3;

		if(dir == Direction.NORTH)
			end = 0;
		else if(dir == Direction.EAST)
			end = 1;
		else if(dir == Direction.SOUTH)
			end = 2;
		else
			end = 3;

		// Turning based on the results.
		if(start - end == -1 || start - end == 3) 
			this.turnRight();
		else if(start - end == 1 || start - end == -3)
			this.turnLeft();
		else if(Math.abs(start - end) == 2)
			this.turnAround();
	}

	/**
	 * Overriding the takeTurn method to decide what to do for the current round.
	 * @param energy The robot's current energy.
	 * @param data An array of opponents.
	 */
	public TurnRequest takeTurn(int energy, OppData[] data) {
		BattleData currTarget = null;
		int[] moves = new int[3];

		// Updating data.
		for(int i = 0; i < this.battleRecord.length; i++) {
			// Since battleRecord will be sorted by something other than ID, it needs to use the opponent's
			// ID to locate the same opponent in the OppData array.
			BattleData current = this.battleRecord[i];
			int oppID = current.getID(); 

			if(oppID >= 0) {
				// Updating maxMoves based on the opponent's stored and current coordinates. The if statement is so that
				// it won't try to calculate maxMoves on its first round, when all the BattleData records haven't been updated yet and have coordinates of (-1, -1).
				if(current.getAvenue() != -1 && current.getStreet() != -1) {
					current.setMaxMoves(this.calcDistance(current.getAvenue(), current.getStreet(), data[oppID].getAvenue(), data[oppID].getStreet()));
				}

				current.setAvenue(data[oppID].getAvenue());
				current.setStreet(data[oppID].getStreet());
				current.setHealth(data[oppID].getHealth());
			}
		}

		// Setting the target. Since sortBD is only called when it resets the target, the current target will always be at the front of the array 
		// so the DFR will continue to attack that target until it's dead.
		this.setTarget();
		currTarget = battleRecord[0];

		// If the DFR hasn't finished setup yet but the next target (which is also the closest) is too far away, it'll just work with what it has.
		if(!this.setup && this.calcDistance(currTarget) > 6 && this.hasData() > 0) {
			System.out.println("TARGET TOO FAR, SETUP COMPLETE");
			this.setup = true;

			// Sorting the battleRecord again, but this time based on calcStrength as the setup phase has been finished.
			this.sortBD();
			currTarget = battleRecord[0];
		}
		
		displayArray(battleRecord);
		System.out.println("Curr target: " + currTarget.getID());

		moves = this.moveAmount(energy, currTarget);

		// Attack the target if it is in range.
		if(moves[2] == 1 && energy > 20) {
			int rounds;

			// It will attack less rounds during the setup phase, but will be more aggressive afterwards.
			if(!setup)
				rounds = this.TEST_ROUNDS;
			else
				rounds = this.getAttack();

			return new TurnRequest(this.getAvenue() + moves[0], this.getStreet() + moves[1], currTarget.getID(), rounds);
		}
		// If its energy is less than 20, do nothing.
		else if(energy <= 20) {
			return new TurnRequest(this.getAvenue(), this.getStreet(), -1, 0);
		}
		// Otherwise, move towards the target.
		else
			return new TurnRequest(this.getAvenue() + moves[0], this.getStreet() + moves[1], -1, 0);
	}

	/**
	 * Calculates the amount to move in each direction towards a target without using up all the energy.
	 * @param energy The robot's current energy.
	 * @param target The target's BattleData record.
	 * @return An array containing vertical and horizontal movement amounts, as well as an integer indicating whether or not it can attack the target after moving.
	 */
	private int[] moveAmount(int energy, BattleData target) {
		int[] moves = new int[3];
		int x, y, maxMoves, currEnergy = energy, movex = 0, movey = 0;

		// Calculating the distance to the target.
		x = target.getAvenue() - this.getAvenue();
		y = target.getStreet() - this.getStreet();

		// Calculating the maximum amount it can move.
		// If there's more than enough energy to move, set maxMoves equal to the move points.
		if(energy > this.getNumMoves() * 5) 
			maxMoves = this.getNumMoves();
		else if(energy < 5)
			maxMoves = 0;
		// If energy is a multiple of 5, reduce maxMoves by 1 as to not use up all the energy.
		else if(energy % 5 == 0)
			maxMoves = energy/5 - 1;
		// If energy is not a multiple of 5, use integer division so some energy will be left.
		else 
			maxMoves = energy/5;

		// Matching x coordinates, moving as far as it can or as far as it needs to.
		if(x != 0) { 
			if(Math.abs(x) >= maxMoves) {
				currEnergy -= maxMoves * 5;
				if(x > 0)
					movex = maxMoves;
				else
					movex = -maxMoves;
			}
			else {
				currEnergy -= Math.abs(x) * 5;
				movex = x;
			}
		}

		// Recalculating maxMoves based on remaining energy and how far it's already moved.
		if(currEnergy > (this.getNumMoves() - Math.abs(movex)) * 5)
			maxMoves = this.getNumMoves() - Math.abs(movex);
		else if(currEnergy < 5)
			maxMoves = 0;
		else if(currEnergy % 5 == 0)
			maxMoves = currEnergy/5 - 1;
		else
			maxMoves = currEnergy/5;

		// Matching y coordinates, based on how far it can or how far it has to go.
		if(y != 0) { 
			if(Math.abs(y) >= maxMoves) {
				if(y > 0)
					movey = maxMoves;
				else
					movey = -maxMoves;
			}
			else
				movey = y;
		}

		// Returning results.
		moves[0] = movex; 
		moves[1] = movey;

		// If it can reach the target in this round, the third index will be 1.
		// Otherwise, it'll be -1 to indicate the target can't be attacked this round.
		if(this.getAvenue() + movex == target.getAvenue() && this.getStreet() + movey == target.getStreet())
			moves[2] = 1;
		else
			moves[2] = -1;

		return moves;
	}

	/**
	 * Sets the current target.
	 */
	private void setTarget() {
		// If it has data on an equal or greater number of opponents than needed or if it has data on every remaining opponent, the setup process is finished.
		if(!this.setup && (this.hasData() >= this.testNum || this.hasData() == this.isAlive())) {
			System.out.println("DONE SETUP");
			this.setup = true;
			this.sortBD();
		}
		// If it's still in the setup phase, it will target the closest opponent. There are two circumstances when it needs to sort and choose a new target: 
		// when choosing the first target or when the current target has been fought more than TEST_ATTACKS number of times.
		else if(!this.setup && (this.battleRecord[0].getRoundsFought() >= this.TEST_ATTACKS || !this.firstTarget)) {
			System.out.println("NOT SETUP, NEXT TARGET");
			this.sortBD();
		}
		// If every fighter that it had data on is dead, restart the setup process.
		else if(this.setup && this.hasData() == 0) { 
			System.out.println("OUT OF DATA, RESTART SETUP");
			this.setup = false;
			this.sortBD();
		}
		// If its previous target is dead, resort the battleRecord.
		else if(this.battleRecord[0].getHealth() == 0) {
			System.out.println("TARGET DEAD, RESET TARGET");
			this.sortBD();
		}
	}

	/**
	 * Counts how many currently alive opponents that it has fought at least TEST_ATTACKS times.
	 * @return The number of opponents that it has data on.
	 */
	private int hasData() {
		int counter = 0;
		for(int i = 0; i < this.battleRecord.length; i++) {
			if(this.battleRecord[i].getRoundsFought() >= this.TEST_ATTACKS && this.battleRecord[i].getHealth() > 0) {
				counter++;
			}
		}

		return counter;
	}

	/**
	 * Counts how many opponents are still alive.
	 * @return The number of alive opponents.
	 */
	private int isAlive() {
		int counter = 0;
		for(int i = 0; i < this.battleRecord.length; i++) {
			if(this.battleRecord[i].getHealth() > 0) {
				counter++;
			}
		}

		return counter;
	}

	/**
	 * Sorts the battleRecord array depending on the current situation.
	 */
	private void sortBD() {
		System.out.println("SORTING");
		for(int i = 1; i < this.battleRecord.length; i++) {
			int check = i;

			// Determining how to sort. In the setup phase, it'll target the closest opponent.
			if(!setup) {

				// All opponents that are dead or have already been fought TEST_ATTACKS times will be sorted to the back. Otherwise, the array will be sorted by distance.
				while(this.battleRecord[check - 1].getHealth() == 0 || this.battleRecord[check - 1].getRoundsFought() >= this.TEST_ATTACKS || (this.calcDistance(this.battleRecord[check]) < this.calcDistance(this.battleRecord[check - 1]) && this.isValid(this.battleRecord[check]))) {
					this.swap(this.battleRecord, check, check - 1);
					check -= 1;

					// If the current opponent was moved to the start of the array, move on and start checking the next opponent.
					if(check == 0)
						break;
				}
			}
			
			/* If not in the setup phase, the DFR will pick the weakest opponent based on four levels of determination:
			 * 1. Health
			 * 2. Distance
			 * 3. Average health lost from previous encounters
			 * 4. Its possible max attack and defence point sum, as determined by observing its movements and storing data in its BattleData record.
			 * For example, if an opponent moves 5 squares in one round, that means the sum of its attack and defence points can be a maximum of 5. 
			 * 
			 * Adding these various attributes provides a broader understanding of an opponent. The lower any one (and the sum) is, the better. 
			 * It'll also prioritize opponents with rounds fought as there is data about them. */
			else {
				while(this.battleRecord[check - 1].getHealth() == 0 || (this.calcStrength(this.battleRecord[check]) < this.calcStrength(this.battleRecord[check - 1]) && this.battleRecord[check].getHealth() > 0)) {
					this.swap(battleRecord, check, check - 1);
					check -= 1;
					if(check == 0)
						break;
				}
			}
		}
	}

	/**
	 * Checks if an opponent is "valid", meaning they're alive and hasn't been fought TEST_ATTACKS times yet.
	 * @param opp The BattleData record for that opponent.
	 * @return True if yes, false if no.
	 */
	private boolean isValid(BattleData opp) {
		if(opp.getHealth() > 0 && opp.getRoundsFought() < this.TEST_ATTACKS)
			return true;
		else
			return false;
	}

	/**
	 * Calculates the number of moves needed to reach an opponent.
	 * @param opp The BattleData record of that opponent.
	 * @return The number of moves needed to reach that opponent.
	 */
	private int calcDistance(BattleData opp) {
		return Math.abs(opp.getAvenue() - this.getAvenue()) + Math.abs(opp.getStreet() - this.getStreet());
	}

	/**
	 * Overloading the calcDistance method to work with coordinates.
	 * @param x1 The x coordinate of the first point.
	 * @param y1 The y coordinate of the first point.
	 * @param x2 The x coordiante of the second point.
	 * @param y2 The y coordinate of the second point.
	 * @return The number of moves between the two points.
	 */
	private int calcDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x2 - x1) + Math.abs(y2 - y1); 
	}

	/**
	 * Calculates the "strength" of an opponent based on various factors.
	 * @param opp The BattleData record of that opponent.
	 * @return Their "strength".
	 */
	private double calcStrength(BattleData opp) {
		// The values are multiplied by weighting (e.g. 0.8) based on their importance. 
		// For example, health isn't that important since it's not necessarily an indicator of strength it's not unusual for a fighter to come back and kill an opponent with higher health.
		if(opp.getRoundsFought() > 0)
			return this.calcDistance(opp) + opp.getHealth() * 0.8 + (double)opp.getHealthLost()/opp.getRoundsFought() * 0.4 + (10 - opp.getMaxMoves());
		// If this opponent hasn't been fought yet, meaning there's no data about it, calcStrength will return a large value so this record gets sorted to the back and the DFR will target opponents that it has experience with.
		else
			return 200;
	}

	/**
	 * Swaps two BattleData records in an array.
	 * @param battleRecord The array of BattleData records.
	 * @param index1 The index of the first record.
	 * @param index2 The index of the second record.
	 */
	private void swap(BattleData[] battleRecord, int index1, int index2) {
		BattleData temp = battleRecord[index1];
		battleRecord[index1] = battleRecord[index2];
		battleRecord[index2] = temp;
	}

	/**
	 * Overriding the setLable method to display health and turn the robot black once it's dead.
	 */
	public void setLabel() {
		this.setLabel(this.health + " " + this.getID());

		if(this.health == 0)
			this.setColor(Color.BLACK);
		else
			this.setColor(Color.CYAN);
	}

	/**
	 * Updates the robot's health, as well as the BattleData for the opponent it just fought.
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health -= healthLost;

		/* firstTarget is used to keep track of whether the DFR has found its first target or not. Before it has, the DFR 
		 * will be constantly moving towards the closest opponent, but once battleResult is called for an ID >= 0, meaning it
		 * either attacked or was attacked, then it should target whoever that opponent is as they are now the closest. */
		if(!this.firstTarget && oppID >= 0) {
			this.firstTarget = true;
			this.sortBD();
		}

		// Finding the opponent in battleRecord and updating its information.
		for(int i = 0; i < this.battleRecord.length; i++) {
			if(this.battleRecord[i].getID() == oppID && oppID >= 0) {
				this.battleRecord[i].addHealthLost(healthLost);
				this.battleRecord[i].addRoundsFought();
				break;
			}
		}
	}
	
	private void displayArray(BattleData[] battleRecord) {
		for(BattleData record : battleRecord)
			System.out.print(record + " S: " + calcStrength(record) + " ");
		System.out.println();
	}
}
