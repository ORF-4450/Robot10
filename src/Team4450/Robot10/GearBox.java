/**
 * Manage gearbox shifting.
 */
package Team4450.Robot10;

import Team4450.Lib.*;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GearBox
{
	private Robot		robot;
	private boolean		lowSpeed, neutral, pto, highSpeed, neutralSupported = true;
	
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
	}
	
	private void displayStatus()
	{
		Util.consoleLog("low=%b, neutral=%b, pto=%b", lowSpeed, neutral, pto);
		
		SmartDashboard.putBoolean("Low", lowSpeed);
		SmartDashboard.putBoolean("High", highSpeed);
		SmartDashboard.putBoolean("Neutral", neutral);
		SmartDashboard.putBoolean("PTO", pto);
	}

	/**
	 * Set gearboxes into low speed.
	 */
	public void lowSpeed()
	{
		Util.consoleLog();

		highSpeed = false;
		neutral = false;

		Devices.highLowValve.SetA();

		if (neutralSupported) Devices.neutralValve.Open();
		
		lowSpeed = true;
		
		displayStatus();
	}

	/**
	 * Set gearboxes into high speed.
	 */
	public void highSpeed()
	{
		Util.consoleLog();

		neutral = false;
		lowSpeed = false;
		
		if (neutralSupported) Devices.neutralValve.Close();

		Devices.highLowValve.SetB();
		
		highSpeed = true;
		
		displayStatus();
	}

	/**
	 * Set gearboxes into neutral.
	 */
	public void neutral()
	{
		Util.consoleLog();

		neutral = false;

		if (neutralSupported)
		{
			Devices.neutralValve.Close();
			
			if (lowSpeed) Devices.highLowValve.SetB();

			Devices.highLowValve.SetA();
			
			neutral = true;
			lowSpeed = false;
			highSpeed = false;
		}
		
		displayStatus();
	}

	/**
	 * Engage PTO drive.
	 */
	public void engagePTO()
	{
		Util.consoleLog();
		
		neutral();
		
		Devices.ptoValve.SetA();

		pto = true;
		
		displayStatus();
	}

	/**
	 * Disengage PTO drive.
	 */
	public void disengagePTO()
	{
		Util.consoleLog();

		pto = false;
		
		Devices.ptoValve.SetB();
		
		lowSpeed();
		
		//displayStatus();
	}
	
	/**
	 * Return state of PTO drive.
	 * @return True if PTO engaged, false if not.
	 */
	public boolean isPTO()
	{
		return pto;
	}
	
	/**
	 * Return low speed state.
	 * @return True if low speed.
	 */
	public boolean isLowSpeed()
	{
		return lowSpeed;
	}
	
	/**
	 * Return high speed state.
	 * @return True if high speed.
	 */
	public boolean isHighSpeed()
	{
		return highSpeed;
	}
	
	/**
	 * Return neutral state.
	 * @return True if gearbox in neutral, false if not.
	 */
	public boolean isNeutral()
	{
		return neutral;
	}
	
	/**
	 * Return availability of neutral state.
	 * @return True if gearbox has neutral support, false if not.
	 */
	public boolean isNeutralSupported()
	{
		return neutralSupported;
	}
}
