/**
 * Handles the Gear pickup.
 */
package Team4450.Robot10;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import Team4450.Lib.LCD;
import Team4450.Lib.LaunchPad.LaunchPadControlIDs;
import Team4450.Lib.Util;
import Team4450.Lib.ValveDA;

public class GearPickup
{
	private Robot		robot;
	private Teleop		teleop;
	private CANTalon	motor = new CANTalon(7);
	private boolean		pickupDown = true, pickupOut = true;
	private ValveDA		pickupLiftValve = new ValveDA(6);
	private ValveDA		pickupDeployValve = new ValveDA(1, 0);
	private Thread		autoPickupThread;
	
	public GearPickup(Robot robot, Teleop teleop)
	{
		Util.consoleLog();
		
		this.robot = robot;
		this.teleop = teleop;

		robot.InitializeCANTalon((CANTalon) motor);

		motor.enableBrakeMode(true);
		
		stopMotor();
		
		pickupIn();
		
		raisePickup();
	}
	
	public void dispose()
	{
		Util.consoleLog();
		
		if (motor != null) motor.delete();
		if (pickupLiftValve != null) pickupLiftValve.dispose();
		if (pickupDeployValve != null) pickupDeployValve.dispose();
	}
	
	public void startMotorIn()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", true);

		motor.set(.50);
	}
	
	public void startMotorInSlow()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", true);

		motor.set(.50);
	}
	
	public void startMotorOut()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", true);
		
		motor.set(-.50);
	}

	public void stopMotor()
	{
		Util.consoleLog();

		// Note to students. This if block is important. This makes the toggle in teleop
		// work correctly if stopMotor is called from the auto pickup thread. If you don't
		// understand what is going on here, ASK!
		
		if (teleop != null)
		{
			if (teleop.launchPad !=  null ) teleop.launchPad.FindButton(LaunchPadControlIDs.BUTTON_YELLOW).latchedState = false;
		}

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

	/**
	 * Start auto gear pickup thread.
	 */
	public void StartAutoPickup()
	{
		Util.consoleLog();
		
		if (autoPickupThread != null) return;

		autoPickupThread = new AutoPickup();
		autoPickupThread.start();
	}
	
	/**
	 * Stops auto pickup thread.
	 */
	public void StopAutoPickup()
	{
		Util.consoleLog();

		if (autoPickupThread != null) autoPickupThread.interrupt();
		
		autoPickupThread = null;
	}

	//----------------------------------------
	// Automatic ball pickup thread.
	
	private class AutoPickup extends Thread
	{
		AutoPickup()
		{
			Util.consoleLog();
			
			this.setName("AutoGearPickup");
	    }
		
	    public void run()
	    {
	    	Util.consoleLog();
	    	
	    	try
	    	{
	    		lowerPickup();
	    		sleep(250);
	    		pickupOut();
	    		sleep(250);
	    		startMotorIn();
	    		sleep(250);
	    		
    	    	while (!isInterrupted() && motor.getOutputCurrent() < 10.0) // 5.0
    	    	{
    	            // We sleep since JS updates come from DS every 20ms or so. We wait 50ms so this thread
    	            // does not run at the same time as the teleop thread.
    	    		LCD.printLine(8, "gearmotor current=%f", motor.getOutputCurrent());
    	            sleep(50);
    	    	}
    	    	
    	    	if (!interrupted()) Util.consoleLog("  Gear detected");
    	    	
    	    	// We run the gear motor during and after picking gear up off the floor
    	    	// to suck the gear in as far as possible.
    	    	
    	    	startMotorInSlow();

    	    	sleep(500);

    	    	pickupIn();
    			raisePickup();

    	    	sleep(1000);

    	    	stopMotor();
	    	}
	    	catch (InterruptedException e) 
	    	{
		    	stopMotor();
		    	pickupIn();
				raisePickup();
	    	}
	    	catch (Throwable e) 
	    	{
	    		e.printStackTrace(Util.logPrintStream);

		    	stopMotor();
		    	pickupIn();
				raisePickup();
	    	}
			
			autoPickupThread = null;
	    }
	}	// end of AutoPickup thread class.

}
