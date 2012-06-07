import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;


public class ColorTest {

	public static void main(String args[]) throws Exception
	{
		TouchSensor touchSensor = new TouchSensor(SensorPort.S3);
		
		while(!Button.ESCAPE.isDown())
		{
			while(touchSensor.isPressed())
			{
				LCD.drawString("Sensor pressed", 0, 0);
			}
			LCD.clear();
		}
		
	}
	
}
