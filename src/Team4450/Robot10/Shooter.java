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
	private Talon		motor = new Talon(1), feederMotor = new Talon(2), indexerMotor = new Talon(3);

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
		if (feederMotor != null) feederMotor.free();
		if (indexerMotor != null) indexerMotor.free();
	}
	
	public void start()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("ShooterMotor", true);
		
		motor.set(.80);
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
	
	public void startFeeding()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("DispenserMotor", true);
		
		feederMotor.set(.50);
		indexerMotor.set(-.50);
	}

	public void stopFeeding()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("DispenserMotor", false);

		feederMotor.set(0);
		indexerMotor.set(0);
	}

	public boolean isFeeding()
	{
		if (feederMotor.get() != 0)
			return true;
		else
			return false;
	}

}
