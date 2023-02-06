package robot_war_summative;

import becker.robots.*;
import java.awt.Color;

/**
 * Creating a basic fighter robot.
 * @author Kevin D
 */

public class SwiftFighterRobot extends FighterRobot {
	private int health;

	/**
	 * Constructor to set up the robot.
	 * @param city The city the robot is in.
	 * @param street The robot's starting street.
	 * @param avenue The robot's starting avenue.
	 * @param dir The robot's starting direction.
	 * @param id The robot's ID.
	 * @param health The robot's health.
	 */
	public SwiftFighterRobot(City city, int street, int avenue, Direction dir, int id, int health) {
		super(city, street, avenue, dir, id, 3, 1, 6); // Attack = 3, defence = 1, movement = 6
		this.health = health;
		this.setLabel();
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
		// Setting the target based on whichever opponent has the lowest health.
		int targetIndex = this.setTarget(data);
		int[] moves = this.moveAmount(energy, data[targetIndex]);

		// If it can, attack the target.
		if(moves[2] == 1 && energy > 0)
			return new TurnRequest(this.getAvenue() + moves[0], this.getStreet() + moves[1], data[targetIndex].getID(), this.getAttack());
		// Otherwise, move towards the target.
		else {
			return new TurnRequest(this.getAvenue() + moves[0], this.getStreet() + moves[1], -1, 0);
		}
	}

	/**
	 * Calculates the amount to move in each direction towards a target without using up all the energy.
	 * @param energy The robot's current energy.
	 * @param target The target's OppData record.
	 * @return An array containing vertical and horizontal movement amounts, as well as an integer indicating whether or not it can attack the target after moving.
	 */
	private int[] moveAmount(int energy, OppData target) {
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
	 * Determins the current target.
	 * @param data An array of opponents.
	 * @return i The index of the target in the sorted array.
	 */
	private int setTarget(OppData[] data) {
		// Sorting the array by distance.
		data = this.sortOpp(data);

		// Choosing the first opponent that's not itself or dead.
		for(int i = 0; i < data.length; i++) {
			if(data[i].getID() != this.getID() && data[i].getHealth() > 0) {
				return i;
			}
		}

		return 0;
	}

	/**
	 * Sorting an OppData array via insertion sort, based on distance from closest to farthest.
	 * @param data An array of opponents.
	 * @return The sorted array.
	 */
	private OppData[] sortOpp(OppData[] data) {
		// Sorting the array.
		for(int i = 1; i < data.length; i++) {
			int check = i;

			// Keeps on moving an oppponent towards the front of the array if it's closer than the one before it.
			while(data[check].getHealth() < data[check - 1].getHealth()) {
				this.swap(data, check, check - 1);
				check -= 1;

				// If the current opponent was moved to the start of the array, move on and start checking the next opponent.
				if(check == 0)
					break;
			}
		}

		return data;
	}

	/**
	 * Swaps two OppData records in an array.
	 * @param data An array of opponents.
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
			this.setColor(Color.YELLOW);
	}

	/**
	 * Updates the robot's health.
	 */
	public void battleResult(int healthLost, int oppID, int oppHealthLost, int numRoundsFought) {
		this.health -= healthLost;
	}
}
