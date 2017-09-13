/**
 * Handles the Ball Pickup.
 */
package Team4450.Robot10;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import Team4450.Lib.*;

public class BallPickup
{
	private Robot		robot;
	
	public BallPickup(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		stop();
	}
	
	public void dispose()
	{
		Util.consoleLog();
		
	}
	
	public void start()
	{
		Util.consoleLog();
		
		SmartDashboard.putBoolean("BallPickupMotor", true);

		Devices.ballMotor.set(.80);
	}

	public void stop()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("BallPickupMotor", false);

		Devices.ballMotor.set(0);
	}

	public boolean isRunning()
	{
		if (Devices.ballMotor.get() != 0)
			return true;
		else
			return false;
	}
}
