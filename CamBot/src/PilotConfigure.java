import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.robotics.navigation.DifferentialPilot;

public class PilotConfigure {

	private static DifferentialPilot pilot = new DifferentialPilot(4.4, 14,
			Motor.C, Motor.A);
	private static LightSensor lightSensor = new LightSensor(SensorPort.S1);
	private static TouchSensor touchSensor = new TouchSensor(SensorPort.S3);

	public static void main(String args[]) throws Exception {
		Motor.A.resetTachoCount();
		Motor.C.resetTachoCount();
		pilot.setRotateSpeed(60);
		pilot.setTravelSpeed(15);

		LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 0);
		LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 1);

		while (!Button.ESCAPE.isDown()) {
			LCD.drawString("value:" + lightSensor.readValue(), 0, 0);
			while(lightSensor.readValue()>40){
			pilot.backward();
			}
			pilot.stop();

			int RightTacho = (Motor.A.getTachoCount());
			int LeftTacho = (Motor.C.getTachoCount());

			LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 0);
			LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 1);

			int rotate = 0;
			if (RightTacho > LeftTacho) {
				rotate = RightTacho*2 - LeftTacho;
				Motor.A.rotateTo(rotate);
			} else {
				rotate = LeftTacho*2 - RightTacho;
				Motor.C.rotateTo(rotate);
			}
			LCD.clear();
			LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 0);
			LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 1);

			pilot.travel(10);
			LCD.drawString("Diff=" + rotate, 0, 2);
			LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 3);
			LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 4);
			LCD.clear();
			Motor.A.resetTachoCount();
			Motor.C.resetTachoCount();
			LCD.drawString("Right=" + Motor.A.getTachoCount(), 0, 0);
			LCD.drawString("Left=" + Motor.C.getTachoCount(), 0, 1);

		}
	}

}
