package robot_war_summative;

import becker.robots.*;
import java.awt.Color;
import java.util.Random;

/**
 * Creating a tank fighter robot.
 * @author Kevin D
 */

public class TankFighterRobot extends FighterRobot {
	private int health, parkx, parky;
	Random generator = new Random();

	/**
	 * Constructor to set up the robot.
	 * @param city The city the robot is in.
	 * @param street The robot's starting street.
	 * @param avenue The robot's starting avenue.
	 * @param dir The robot's starting direction.
	 * @param id The robot's ID.
	 * @param health The robot's health.
	 */
	public TankFighterRobot(City city, int street, int avenue, Direction dir, int id, int health) {
		super(city, street, avenue, dir, id, 3, 6, 1); // Attack = 3, defence = 6, movement = 1
		this.health = health;
		this.setLabel();

		// Generating a random parking spot near the middle of the arena.
		this.parkx = generator.nextInt(BattleManager.WIDTH/2) + BattleManager.WIDTH/4;
		this.parky = generator.nextInt(BattleManager.HEIGHT/2) + BattleManager.HEIGHT/4;
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
		int targetIndex;

		// Moving to the parking spot if it's not yet there.
		if(!this.atParking()) {
			if(this.getAvenue() > parkx)
				return new TurnRequest(this.getAvenue() - 1, this.getStreet(), -1, 0);
			else if(this.getAvenue() < parkx)
				return new TurnRequest(this.getAvenue() + 1, this.getStreet(), -1, 0);
			else if(this.getStreet() > parky)
				return new TurnRequest(this.getAvenue(), this.getStreet() - 1, -1, 0);
			else
				return new TurnRequest(this.getAvenue(), this.getStreet() + 1, -1, 0);
		}

		// If other opponents are also on its parking spot, it will attack the one with the lowest health.
		targetIndex = this.canAttack(data);
		if(targetIndex != -1)
			return new TurnRequest(this.getAvenue(), this.getStreet(), data[targetIndex].getID(), 3);

		// Otherwise, do nothing.
		return new TurnRequest(this.getAvenue(), this.getStreet(), -1, 0);
	}

	/**
	 * Checks whether the TankFighterRobot is at its parking location.
	 * @return True if yes, false if no.
	 */
	private boolean atParking() {
		if(this.getAvenue() == this.parkx && this.getStreet() == this.parky)
			return true;
		else
			return false;
	}

	/**
	 * Determines whether or not the TFR can attack an opponent by checking if there's any on its intersection.
	 * @param data An array of opponents.
	 * @return It'll return the array index of an opponent if yes, and -1 if no.
	 */
	private int canAttack(OppData[] data) {
		data = this.sortOpp(data);

		// Finding the first opponent that it can engage. Since data was sorted, this is also the fighter with the lowest health.
		for(int i = 0; i < data.length; i++) {
			if(data[i].getAvenue() == this.parkx && data[i].getStreet() == this.parky && data[i].getHealth() > 0 && data[i].getID() != this.getID())
				return i;
		}

		return -1;
	}

	/**
	 * Sorting an OppData array via insertion sort, based on health from lowest to highest.
	 * @param data An array of opponents.
	 * @return The sorted array.
	 */
	private OppData[] sortOpp(OppData[] data) {
		// Sorting the array.
		for(int i = 1; i < data.length; i++) {
			int check = i;

			// Keeps on moving an element towards the front of the array if it's smaller than the one before it.
			while(data[check].getHealth() < data[check - 1].getHealth()) {
				this.swap(data, check, check - 1);
				check -= 1;
				
				// If the current element was moved to the start of the array, move on and start checking the next element.
				if(check == 0)
					break;
			}
		}

		return data;
	}

	/**
	 * Swaps two OppData records in an array.
	 * @param data The array containing the OppData.
	 * @param index1 The index of the first record.
	 * @param index2 THe index of the second record.
	 */
	private void swap(OppData[] data, int index1, int index2) {
		OppData temp = data[index1];
		data[index1] = data[index2];
		data[index2] = temp;
	}

	/**
	 * Overriding the setLable method to display health and turn the robot black once it's dead.
	 */
	public void setLabel() {
		this.setLabel(this.health + " " + this.getID());

		if(this.health == 0)
			this.setColor(Color.BLACK);
		else
			this.setColor(Color.GREEN);
	}

	/**
	 * Updates the robot's health.
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health -= healthLost;
	}
}
