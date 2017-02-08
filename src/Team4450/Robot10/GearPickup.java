/**
 * Handles the Gear pickup.
 */
package Team4450.Robot10;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import Team4450.Lib.Util;

public class GearPickup
{
	private Robot		robot;
	private Talon		motor = new Talon(1);

	public GearPickup(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		stop();
	}
	
	public void dispose()
	{
		Util.consoleLog();
		
		if (motor != null) motor.free();
	}
	
	public void startIn()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", true);

		motor.set(.50);
	}
	
	public void startOut()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", true);
		
		motor.set(-.20);
	}

	public void stop()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", false);
		
		motor.set(0);
	}

	public boolean isRunning()
	{
		if (motor.get() != 0)
			return true;
		else
			return false;
	}
	
	public void lowerPickup()
	{
		Util.consoleLog();

		if (isPickupDown()) return;
		
		SmartDashboard.putBoolean("GearPickupDown", true);
	}
	
	public void raisePickup()
	{
		Util.consoleLog();

		if (!isPickupDown()) return;
		
		SmartDashboard.putBoolean("GearPickupDown", false);
	}
	
	public boolean isPickupDown()
	{
		return false;
	}
}
