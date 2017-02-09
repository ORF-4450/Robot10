/**
 * Manage gearbox shifting.
 */
package Team4450.Robot10;

import Team4450.Lib.*;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GearBox
{
	private Robot		robot;
	private boolean		lowSpeed, neutral, pto;
	private ValveDA		highLowValve = new ValveDA(0);
	private ValveDA		ptoValve = new ValveDA(2);
	private ValveSA		neutralValve = new ValveSA(4);
	
	public GearBox	(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		disengagePTO();
		
		lowSpeed();
	}
	
	public void dispose()
	{
		Util.consoleLog();
		
		if (highLowValve != null) highLowValve.dispose();
		if (ptoValve != null) ptoValve.dispose();
		if (neutralValve != null) neutralValve.dispose();
	}
	
	private void displayStatus()
	{
		Util.consoleLog("low=%b, neutral=%b, pto=%b", lowSpeed, neutral, pto);
		
		SmartDashboard.putBoolean("LowSpeed", lowSpeed);
		SmartDashboard.putBoolean("Neutral", neutral);
		SmartDashboard.putBoolean("PTO", pto);
	}

	public void lowSpeed()
	{
		Util.consoleLog();

		lowSpeed = true;
		neutral = false;
		
		highLowValve.SetA();
		
		displayStatus();
	}

	public void highSpeed()
	{
		Util.consoleLog();

		lowSpeed = false;
		neutral = false;
		
		highLowValve.SetB();
		
		displayStatus();
	}

	public void neutral()
	{
		Util.consoleLog();

		neutral = true;
		lowSpeed = false;
		
		displayStatus();
	}

	public void engagePTO()
	{
		Util.consoleLog();

		pto = true;
		
		ptoValve.SetA();
		
		displayStatus();
	}

	public void disengagePTO()
	{
		Util.consoleLog();

		pto = false;
		
		ptoValve.SetB();
		
		displayStatus();
	}
}
