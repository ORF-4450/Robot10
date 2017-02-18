/**
 * Handles the Gear pickup.
 */
package Team4450.Robot10;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;

public class GearPickup
{
	private Robot		robot;
	private Talon		motor = new Talon(1);
	private boolean		pickupDown, pickupOut;
	private ValveDA		pickupLiftValve = new ValveDA(6);
	private ValveDA		pickupDeployValve = new ValveDA(1, 0);
	
	public GearPickup(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		stop();
		
		pickupIn();
		
		raisePickup();
	}
	
	public void dispose()
	{
		Util.consoleLog();
		
		if (motor != null) motor.free();
		if (pickupLiftValve != null) pickupLiftValve.dispose();
		if (pickupDeployValve != null) pickupDeployValve.dispose();
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
		
		pickupDown = true;
		
		pickupLiftValve.SetB();
		
		SmartDashboard.putBoolean("GearPickupDown", pickupDown);
	}
	
	public void raisePickup()
	{
		Util.consoleLog();

		if (!isPickupDown()) return;
		
		pickupDown = false;
		
		pickupLiftValve.SetA();
		
		SmartDashboard.putBoolean("GearPickupDown", pickupDown);
	}
	
	public boolean isPickupDown()
	{
		return pickupDown;
	}
	
	public void pickupIn()
	{
		Util.consoleLog();

		if (!isPickupOut()) return;
		
		pickupOut = false;
		
		pickupDeployValve.SetA();
		
		SmartDashboard.putBoolean("GearPickupDown", pickupOut);
	}
	
	public void pickupOut()
	{
		Util.consoleLog();

		if (isPickupOut()) return;
		
		pickupOut = true;
		
		pickupDeployValve.SetB();
		
		SmartDashboard.putBoolean("GearPickupDown", pickupOut);
	}
	
	public boolean isPickupOut()
	{
		return pickupOut;
	}
}
