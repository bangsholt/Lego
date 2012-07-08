import java.awt.Rectangle;
import java.util.Vector;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.addon.NXTCam;
import lejos.robotics.navigation.DifferentialPilot;

public class Main {

	private static int claw = 60;

	private static DifferentialPilot pilot = new DifferentialPilot(4.4, 14,
			Motor.C, Motor.A);
	
	//Vector to hold all the rectangles that the camera detects
	private static Vector<Rectangle> rect;
	//grabArea is the area where the claws will close on the ball
	private static Rectangle grabArea = new Rectangle(25, 50, 150, 70);
	//driveForwardArea is the area in which the ball has to be for the robot to go forward
	private static Rectangle driveForwardArea = new Rectangle(40, 0, 80, 150);
	//leftForwardArea is the area where the robot will turn to the left
	private static Rectangle leftForwardArea = new Rectangle(0, 0, 30, 250);
	//rightForwardArea is the area where the robot will turn to the right
	private static Rectangle rightForwardArea = new Rectangle(120, 0, 20, 250);
	//The sensors connected to the robot
	private static LightSensor lightSensor = new LightSensor(SensorPort.S1);
	private static NXTCam cam = new NXTCam(SensorPort.S2);
	private static TouchSensor touchSensor = new TouchSensor(SensorPort.S3);
	//Boolean used to tell if the claw is closed or not
	private static boolean clawIsClosed = false;
	//An enum used to iterate through the different states in turning the robot to find the ball
	public static enum searchState {
		//This is the 3 steps it take to turn to the right
		RIGHT1, RIGHT2, RIGHT3, 
		//And the three steps returning
		RETRIGHT1, RETRIGHT2, RETRIGHT3, 
		//Going to the left
		LEFT1, LEFT2, LEFT3,
		//And back
		RETLEFT1, RETLEFT2, RETLEFT3, 
		//So it can go forward again
		FORWARD
	}
	//The current state of where the robot should turn
	private static searchState state = searchState.RIGHT1;
	//How many rectangles are there currently
	private static int noOfRectangles;

	public static void main(String args[]) throws Exception {
		//Reset in order to know how little or much to turn later on
		Motor.A.resetTachoCount();
		Motor.C.resetTachoCount();
		//Claw Motorspeed
		Motor.B.setSpeed(180);
		//Turn on the camera
		cam.enableTracking(true);
		//Give the camera a while to boot up properly(This is guaranteed to happen in ~100 ms
		//but cutting some serious slack is ok here)
		Thread.sleep(1000);
		//Set the robot to run 15 * wheel diameter per second
		pilot.setTravelSpeed(15);
		//Set the robot to rotate 60 degress per second
		pilot.setRotateSpeed(60);
		//Initialize noOfRectangles to be 0, as no reads have happened yet
		noOfRectangles = 0;
		//To make it possible to break the execution of the robot
		while (!Button.ESCAPE.isDown()) {
			//Turn on the camera - there is no telling for how long the robot has been waiting for this to happen
			cam.enableTracking(true);
			//Get the new objects that's tracked
			refreshView();
			//Output how many objects that currently are tracked
			LCD.drawString("Objects: " + cam.getNumberOfObjects(), 0, 0);
			//
			if (!clawIsClosed) {
				while (!isBallWithinGrasp()) {
					refreshView();

					isBallFound();
					LCD.drawString("Objects: " + cam.getNumberOfObjects(), 0, 0);
				}
			} else {

				while (lightSensor.readValue() > 40) {
					pilot.backward();
				}
				pilot.stop();

				int RightTacho = (Motor.A.getTachoCount());
				int LeftTacho = (Motor.C.getTachoCount());

				LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 0);
				LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 1);
				int rotate = 0;
				if (RightTacho > LeftTacho) {
					rotate = RightTacho * 2 - LeftTacho;
					Motor.C.rotateTo(rotate);
				} else {
					rotate = LeftTacho * 2 - RightTacho;
					Motor.A.rotateTo(rotate);
				}
				LCD.drawString("Diff=" + rotate, 0, 2);
				LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 3);
				LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 4);

				while (!touchSensor.isPressed()) {
					pilot.backward();
				}

				pilot.travel(10);
				openClaw();
				pilot.stop();
				state = searchState.RIGHT1;
				while (!touchSensor.isPressed()) {
					Motor.A.resetTachoCount();
					Motor.C.resetTachoCount();
					LCD.clear();
					LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 0);
					LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 1);
				}

				cam.enableTracking(true);
			}
			if (Button.RIGHT.isDown()) {
				openClaw();
			}
			if (Button.LEFT.isDown()) {
				closeClaw();
			}
		}
	}

	public static void goForward() {
		pilot.travel(5);
	}

	public static void scanForBall() {
		pilot.rotate(90);
		pilot.rotate(-180);
		pilot.rotate(90);
	}

	public static void openClaw() {
		clawIsClosed = false;
		Motor.B.rotate(claw);
	}

	public static void closeClaw() {
		clawIsClosed = true;
		Motor.B.rotate(-claw);
	}

	public static boolean isBallWithinGrasp() {
		for (int i = 0; i < noOfRectangles; i++) {
			if (grabArea.contains(rect.elementAt(i))) {
				closeClaw();
				return true;
			}
		}
		return false;
	}

	public static void isBallFound() {
		Rectangle ballRect = new Rectangle();
		for (int i = 0; i < noOfRectangles; i++) {
			Rectangle currRect = rect.elementAt(i);
			int area = currRect.height * currRect.width;
			if (area > (ballRect.height * ballRect.width)) {
				ballRect = currRect;
			}
		}

		LCD.drawInt(ballRect.x, 0, 1);
		LCD.drawChar(',', 3, 1);
		LCD.drawInt(ballRect.y, 4, 1);

		LCD.drawInt(ballRect.height, 0, 2);
		LCD.drawChar(',', 3, 2);
		LCD.drawInt(ballRect.width, 4, 2);

		LCD.clear();
		LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 0);
		LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 1);

		if (driveForwardArea.intersects(ballRect)) {
			pilot.forward();
		} else if (leftForwardArea.intersects(ballRect)) {
			pilot.rotateLeft();
		} else if (rightForwardArea.intersects(ballRect)) {
			pilot.rotateRight();
		} else {
			int rotate = 25;
			switch (state) {
			case FORWARD:
				pilot.travel(10);
				state = searchState.RIGHT1;
				break;
			case RIGHT1:
				pilot.rotate(-1 * rotate);
				state = searchState.RIGHT2;
				break;
			case RIGHT2:
				pilot.rotate(-1 * rotate);
				state = searchState.RIGHT3;
				break;
			case RIGHT3:
				pilot.rotate(-1 * rotate);
				state = searchState.RETRIGHT1;
				break;
			case RETRIGHT1:
				pilot.rotate(rotate);
				state = searchState.RETRIGHT2;
				break;
			case RETRIGHT2:
				pilot.rotate(rotate);
				state = searchState.RETRIGHT3;
				break;
			case RETRIGHT3:
				pilot.rotate(rotate);
				state = searchState.LEFT1;
				break;
			case LEFT1:
				pilot.rotate(rotate);
				state = searchState.LEFT2;
				break;
			case LEFT2:
				pilot.rotate(rotate);
				state = searchState.LEFT3;
				break;
			case LEFT3:
				pilot.rotate(rotate);
				state = searchState.RETLEFT1;
				break;
			case RETLEFT1:
				pilot.rotate(-1 * rotate);
				state = searchState.RETLEFT2;
				break;
			case RETLEFT2:
				pilot.rotate(-1 * rotate);
				state = searchState.RETLEFT3;
				break;
			case RETLEFT3:
				pilot.rotate(-1 * rotate);
				state = searchState.FORWARD;
				break;
			}
		}
	}

	public static void refreshView() {
		rect = new Vector<Rectangle>();
		noOfRectangles = cam.getNumberOfObjects();
		for (int i = 0; i < noOfRectangles; i++) {
			rect.addElement(cam.getRectangle(i));
		}
	}
}
