import java.awt.Rectangle;
import java.util.Vector;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.addon.NXTCam;
import lejos.robotics.navigation.DifferentialPilot;

//76, 27 - 44,47 in claw
//12, 78 - 32,15 left max
//156,85 - 12,20 right claw

//144x50

//40*30 cm square that ball is visible and bigger than 10x10 pixels

public class Main {

	private static int claw = 60;

	private static DifferentialPilot pilot = new DifferentialPilot(4.4, 13.5,
			Motor.C, Motor.A);
	private static NXTCam cam = new NXTCam(SensorPort.S2);
	private static Vector<Rectangle> rect;
	private static Rectangle grabArea = new Rectangle(10, 80, 145, 50);
	private static Rectangle driveForwardArea = new Rectangle(10, 130, 145, 200);
	private static Rectangle gotBallArea = new Rectangle(70, 70, 60, 60);
	private static TouchSensor touchSensor = new TouchSensor(SensorPort.S3);

	private static boolean clawIsClosed = false;

	public static enum searchState {
		RIGHT, LEFT, FORWARD
	}

	private static searchState state;

	private static int noOfRectangles;

	public static void main(String args[]) throws Exception {
		Motor.B.setSpeed(180);
		cam.enableTracking(true);
		Thread.sleep(1000);
		pilot.setTravelSpeed(5);
		noOfRectangles = 0;
		state = searchState.LEFT;
		while (!Button.ESCAPE.isDown()) {
			refreshView();
			LCD.drawString("Objects: " + cam.getNumberOfObjects(), 0, 0);

			// if (rect.elementAt(0) != null) {
			// LCD.drawInt(rect.elementAt(0).x, 0, 1);
			// LCD.drawChar(',', 3, 1);
			// LCD.drawInt(rect.elementAt(0).y, 4, 1);
			//
			// LCD.drawInt(rect.elementAt(0).height, 0, 2);
			// LCD.drawChar(',', 3, 2);
			// LCD.drawInt(rect.elementAt(0).width, 4, 2);
			// }
			if (/*!ballIsInClaw() &&*/ !clawIsClosed) {
				if (!isBallWithinGrasp()) {
					if (!isBallFound()) {
						switch (state) {
						case FORWARD:
							pilot.rotate(45);
							pilot.travel(10);
							state = searchState.LEFT;
							break;
						case LEFT:
							pilot.rotate(45);
							state = searchState.RIGHT;
							break;
						case RIGHT:
							pilot.rotate(-90);
							state = searchState.FORWARD;
							break;
						}
						LCD.drawString("Objects: " + cam.getNumberOfObjects(),
								0, 0);
					}
				}
			} else {
				while (!touchSensor.isPressed()) {
					pilot.backward();
				}
				pilot.travel(10);
				openClaw();
				pilot.stop();
				while (!touchSensor.isPressed())
					;
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

	public static boolean isBallFound() {
		for (int i = 0; i < noOfRectangles; i++) {
			if (driveForwardArea.contains(rect.elementAt(i))) {
				return true;
			}
		}
		return false;
	}

	public static void refreshView() {
		rect = new Vector<Rectangle>();
		noOfRectangles = cam.getNumberOfObjects();
		for (int i = 0; i < noOfRectangles; i++) {
			rect.addElement(cam.getRectangle(i));
		}
	}

	public static boolean ballIsInClaw() {
		for (int i = 0; i < noOfRectangles; i++) {
			if (gotBallArea.contains(rect.elementAt(i))) {
				return true;
			}
		}
		return false;
	}
}
