/**
 * Handles the Gear pickup.
 */
package Team4450.Robot10;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import Team4450.Lib.LCD;
import Team4450.Lib.LaunchPad.LaunchPadControlIDs;
import Team4450.Lib.Util;

public class GearPickup
{
	private Robot		robot;
	private Teleop		teleop;
	private boolean		pickupDown = true, pickupOut = true;
	private Thread		autoPickupThread;
	
	public GearPickup(Robot robot, Teleop teleop)
	{
		Util.consoleLog();
		
		this.robot = robot;
		this.teleop = teleop;

		Devices.InitializeCANTalon((WPI_TalonSRX) Devices.gearMotor);

		Devices.gearMotor.setNeutralMode(NeutralMode.Brake);
		
		stopMotor();
		
		pickupIn();
		
		raisePickup();
	}
	
	public void dispose()
	{
		Util.consoleLog();
	}
	
	public void startMotorIn()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", true);

		Devices.gearMotor.set(.50);
	}
	
	public void startMotorInSlow()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", true);

		Devices.gearMotor.set(.50);
	}
	
	public void startMotorOut()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("GearPickupMotor", true);
		
		Devices.gearMotor.set(-.50);
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
		
		Devices.gearMotor.set(0);
	}

	public boolean isRunning()
	{
		if (Devices.gearMotor.get() != 0)
			return true;
		else
			return false;
	}
	
	public void lowerPickup()
	{
		Util.consoleLog();

		if (isPickupDown()) return;
		
		pickupDown = true;
		
		Devices.pickupLiftValve.SetB();
		
		SmartDashboard.putBoolean("GearPickupDown", pickupDown);
	}
	
	public void raisePickup()
	{
		Util.consoleLog();

		if (!isPickupDown()) return;
		
		pickupDown = false;
		
		Devices.pickupLiftValve.SetA();
		
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
		
		Devices.pickupDeployValve.SetA();
		
		SmartDashboard.putBoolean("GearPickupDown", pickupOut);
	}
	
	public void pickupOut()
	{
		Util.consoleLog();

		if (isPickupOut()) return;
		
		pickupOut = true;
		
		Devices.pickupDeployValve.SetB();
		
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
	    		
    	    	while (!isInterrupted() && Devices.gearMotor.getOutputCurrent() < 10.0) // 5.0
    	    	{
    	            // We sleep since JS updates come from DS every 20ms or so. We wait 50ms so this thread
    	            // does not run at the same time as the teleop thread.
    	    		LCD.printLine(8, "gearmotor current=%f", Devices.gearMotor.getOutputCurrent());
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
