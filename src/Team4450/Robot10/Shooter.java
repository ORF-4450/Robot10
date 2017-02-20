/**
 * Handles the Shooter.
 */
package Team4450.Robot10;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import Team4450.Lib.Util;

public class Shooter
{
	private Robot		robot;
	private Talon		motor = new Talon(1), dispenserMotor = new Talon(2);

	public Shooter(Robot robot)
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
	
	public void start()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("ShooterMotor", true);
		
		motor.set(.50);
	}

	public void stop()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("ShooterMotor", false);

		motor.set(0);
	}

	public boolean isRunning()
	{
		if (motor.get() != 0)
			return true;
		else
			return false;
	}
	
	public void startDispensing()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("DispenserMotor", true);
		
		dispenserMotor.set(.50);
	}

	public void stopDispensing()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("DispenserMotor", false);

		dispenserMotor.set(0);
	}

	public boolean isDispensing()
	{
		if (dispenserMotor.get() != 0)
			return true;
		else
			return false;
	}

}
