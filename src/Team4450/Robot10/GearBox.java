/**
 * Manage gearbox shifting.
 */
package Team4450.Robot10;

import Team4450.Lib.*;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GearBox
{
	private Robot		robot;
	private boolean		lowSpeed, neutral, pto, neutralSupported = false;
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

	/**
	 * Set gearboxes into low speed.
	 */
	public void lowSpeed()
	{
		Util.consoleLog();

		neutral = false;

		highLowValve.SetA();

		if (neutralSupported)
		{
			// a delay may be needed here to let highLowValve move.
			neutralValve.Open();
		}
		
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

		if (neutralSupported)
		{
			neutralValve.Close();
			// may need a delay here to neutralValve close.
		}

		highLowValve.SetB();
		
		lowSpeed = false;
		
		displayStatus();
	}

	/**
	 * Set gearboxes into neutral.
	 */
	public void neutral()
	{
		Util.consoleLog();

		neutral = true;

		if (neutralSupported)
		{
			neutralValve.Close();
			
			if (lowSpeed) highLowValve.SetB();

			highLowValve.SetA();
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
		
		ptoValve.SetA();

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
		
		ptoValve.SetB();
		
		lowSpeed();
		
		displayStatus();
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
	 * Return low/high speed state.
	 * @return True if low speed, false if high speed.
	 */
	public boolean isLowSpeed()
	{
		return lowSpeed;
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
