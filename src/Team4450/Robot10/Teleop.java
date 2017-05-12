
package Team4450.Robot10;

import java.lang.Math;

import Team4450.Lib.*;
import Team4450.Lib.JoyStick.*;
import Team4450.Lib.LaunchPad.*;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

class Teleop
{
	private final Robot 		robot;
	public  JoyStick			rightStick, leftStick, utilityStick;
	public  LaunchPad			launchPad;
	private	GearBox				gearBox;
	private boolean				autoTarget, invertDrive, altDriveMode;
	private BallPickup			ballPickup;
	private Shooter				shooter;
	private GearPickup			gearPickup;
	private Vision				vision;
	
	// Wheel encoder is plugged into dio port 1 - orange=+5v blue=signal, dio port 2 black=gnd yellow=signal. 
	private Encoder				encoder = new Encoder(3, 4, true, EncodingType.k4X);

	// Encoder ribbon cable to dio ports: ribbon wire 2 = orange, 5 = yellow, 7 = blue, 10 = black

	// Constructor.
	
	Teleop(Robot robot)
	{
		Util.consoleLog();

		this.robot = robot;
		
		gearBox = new GearBox(robot);
		
		ballPickup = new BallPickup(robot);
		
		shooter = new Shooter(robot);
		
		gearPickup = new GearPickup(robot, this);
				
		vision = Vision.getInstance(robot);
	}

	// Free all objects that need it.
	
	void dispose()
	{
		Util.consoleLog();
		
		if (leftStick != null) leftStick.dispose();
		if (rightStick != null) rightStick.dispose();
		if (utilityStick != null) utilityStick.dispose();
		if (launchPad != null) launchPad.dispose();
		if (gearBox != null) gearBox.dispose();
		if (ballPickup != null) ballPickup.dispose();
		if (shooter != null) shooter.dispose();
		if (gearPickup != null) gearPickup.dispose();
		if (encoder != null) encoder.free();
	}

	void OperatorControl()
	{
		double	rightY = 0, leftY = 0, utilX = 0, rightX = 0, leftX = 0;
		double	gain = .01;
		boolean	steeringAssistMode = false;
		int		angle;
		
        // Motor safety turned off during initialization.
        robot.robotDrive.setSafetyEnabled(false);

		Util.consoleLog();
		
		LCD.printLine(1, "Mode: OperatorControl");
		LCD.printLine(2, "All=%s, Start=%d, FMS=%b", robot.alliance.name(), robot.location, robot.ds.isFMSAttached());
		
		// Configure LaunchPad and Joystick event handlers.
		
		launchPad = new LaunchPad(robot.launchPad, LaunchPadControlIDs.BUTTON_BLUE, this);
		
		LaunchPadControl lpControl = launchPad.AddControl(LaunchPadControlIDs.ROCKER_LEFT_BACK);
		lpControl.controlType = LaunchPadControlTypes.SWITCH;
		lpControl = launchPad.AddControl(LaunchPadControlIDs.ROCKER_LEFT_FRONT);
		lpControl.controlType = LaunchPadControlTypes.SWITCH;

		launchPad.AddControl(LaunchPadControlIDs.BUTTON_YELLOW);
		launchPad.AddControl(LaunchPadControlIDs.BUTTON_RED_RIGHT);
		launchPad.AddControl(LaunchPadControlIDs.BUTTON_RED);
		launchPad.AddControl(LaunchPadControlIDs.BUTTON_BLUE_RIGHT);
        launchPad.addLaunchPadEventListener(new LaunchPadListener());
        launchPad.Start();

		leftStick = new JoyStick(robot.leftStick, "LeftStick", JoyStickButtonIDs.TRIGGER, this);
		leftStick.addJoyStickEventListener(new LeftStickListener());
        leftStick.Start();
        
		rightStick = new JoyStick(robot.rightStick, "RightStick", JoyStickButtonIDs.TOP_LEFT, this);
		rightStick.AddButton(JoyStickButtonIDs.TRIGGER);
		rightStick.AddButton(JoyStickButtonIDs.TOP_BACK);
        rightStick.addJoyStickEventListener(new RightStickListener());
        rightStick.Start();
        
		utilityStick = new JoyStick(robot.utilityStick, "UtilityStick", JoyStickButtonIDs.TRIGGER, this);
		utilityStick.AddButton(JoyStickButtonIDs.TOP_LEFT);
		utilityStick.AddButton(JoyStickButtonIDs.TOP_RIGHT);
		utilityStick.AddButton(JoyStickButtonIDs.TOP_MIDDLE);
		utilityStick.AddButton(JoyStickButtonIDs.TOP_BACK);
        utilityStick.addJoyStickEventListener(new UtilityStickListener());
        utilityStick.Start();
        
        // Tighten up dead zone for smoother climber movement.
        utilityStick.deadZone = .05;

		// Set CAN Talon brake mode by rocker switch setting.
        // We do this here so that the Utility stick thread has time to read the initial state
        // of the rocker switch.
        if (robot.isComp) robot.SetCANTalonBrakeMode(lpControl.latchedState);
        
        // Set gyro/Navx to heading 0.
        //robot.gyro.reset();
        robot.navx.resetYaw();
        
        robot.navx.setHeading(90);
        
        // Reset encoder.
        encoder.reset();

        // Motor safety turned on.
        robot.robotDrive.setSafetyEnabled(true);
        
		// Driving loop runs until teleop is over.

		while (robot.isEnabled() && robot.isOperatorControl())
		{
			// Get joystick deflection and feed to robot drive object
			// using calls to our JoyStick class.

			if (gearBox.isPTO())
			{
				rightY = stickLogCorrection(rightStick.GetY());		// fwd/back right
				
				leftY = stickLogCorrection85(utilityStick.GetY());	// up/down
				
				if (leftY < 0) leftY = 0;
			} 
// Not inverting controls at this time. Do not do this!			
//			else if (invertDrive)
//			{
//				rightY = stickLogCorrection(rightStick.GetY() * -1);	// fwd/back right
//    			leftY = stickLogCorrection(leftStick.GetY() * -1);		// fwd/back left
//			}
			else
			{
				rightY = stickLogCorrection(rightStick.GetY());	// fwd/back
    			leftY = stickLogCorrection(leftStick.GetY());	// fwd/back

    			rightX = stickLogCorrection(rightStick.GetX());	// left/right
    			leftX = stickLogCorrection(leftStick.GetX());	// left/right
			}
			
			utilX = utilityStick.GetX();
			
			LCD.printLine(3, "distance=%.2f", robot.monitorDistanceThread.getRangeInches());
			LCD.printLine(4, "leftY=%.4f  rightY=%.4f  utilX=%.4f", leftY, rightY, utilX);
			LCD.printLine(5, "encoder=%d,  shootenc=%d", encoder.get(), shooter.tlEncoder.get());
			//LCD.printLine(5, "gyroAngle=%d, gyroRate=%d", (int) robot.gyro.getAngle(), (int) robot.gyro.getRate());
			LCD.printLine(6, "yaw=%.2f, total=%.2f, rate=%.2f, hdng=%.2f", robot.navx.getYaw(), robot.navx.getTotalYaw(), 
					robot.navx.getYawRate(), robot.navx.getHeading());
			LCD.printLine(7, "shootenc=%d rpm=%.0f pwr=%.2f", shooter.shooterSpeedSource.get(), 
					shooter.shooterSpeedSource.getRate() * 60, shooter.motor.get());

			// Set wheel motors.
			// Do not feed JS input to robotDrive if we are controlling the motors in automatic functions.

			//if (!autoTarget) robot.robotDrive.tankDrive(leftY, rightY);
			
			// Two drive modes, full tank and alternate. Switch on right stick trigger.
			
			if (!autoTarget) 
			{
				if (altDriveMode)
//				{	// single stick drive on right with rotate on left
//					if (rightY == 0)
//						robot.robotDrive.tankDrive(leftX, -leftX);	// Left stick rotate when right stick at zero.
//					else
//						robot.robotDrive.drive(rightY, rightX);		// Right stick fwd/back, arc on left/right.
//				}
								
//				{	// two stick with fwd/back on left stick, left/right on right.
//					robot.robotDrive.drive(leftY, rightX);		// Right stick fwd/back, arc on left/right.
//				}
				
				{	// normal tank with straight drive assist when sticks within 10% of each other.
					if (leftRightEqual(leftY, rightY, 10) && Math.abs(rightY) > .50)
					{
						if (!steeringAssistMode) robot.navx.resetYaw();

						// Angle is negative if robot veering left, positive if veering right when going forward.
						// It is opposite when going backward. Note that for this robot, - power means forward and
						// + power means backward.
						
						angle = (int) robot.navx.getYaw();

						LCD.printLine(5, "angle=%d", angle);
						
						// Invert angle for backwards.
						
						if (rightY > 0) angle = -angle;
						
						//Util.consoleLog("angle=%d", angle);
						
						// Note we invert sign on the angle because we want the robot to turn in the opposite
						// direction than it is currently going to correct it. So a + angle says robot is veering
						// right so we set the turn value to - because - is a turn left which corrects our right
						// drift.
						
						robot.robotDrive.drive(rightY, -angle * gain);

						steeringAssistMode = true;
					}
					else
					{
						steeringAssistMode = false;
						robot.robotDrive.tankDrive(leftY, rightY);		// Normal tank drive.
					}
					
				  SmartDashboard.putBoolean("Overload", steeringAssistMode);
				}
				else
					robot.robotDrive.tankDrive(leftY, rightY);		// Normal tank drive.
			}
			
			// Update the robot heading indicator on the DS.
			
	   		SmartDashboard.putNumber("Gyro", robot.navx.getHeading());

			// End of driving loop.
			
			Timer.delay(.020);	// wait 20ms for update from driver station.
		}
		
		// End of teleop mode.
		
		Util.consoleLog("end");
	}

	private boolean leftRightEqual(double left, double right, double percent)
	{
		//if (left == right) return true;
		
		if (Math.abs(left - right) <= (1 * (percent / 100))) return true;
		
		return false;
	}
	
	// Map joystick y value of 0.0-1.0 to the motor working power range of approx 0.5-1.0
	
	private double stickCorrection(double joystickValue)
	{
		if (joystickValue != 0)
		{
			if (joystickValue > 0)
				joystickValue = joystickValue / 1.5 + .4;
			else
				joystickValue = joystickValue / 1.5 - .4;
		}
		
		return joystickValue;
	}
	
	// Custom base logrithim.
	// Returns logrithim base of the value.
	
	private double baseLog(double base, double value)
	{
		return Math.log(value) / Math.log(base);
	}

	// Map joystick y value of 0.0 to 1.0 to the motor working power range of approx 0.5 to 1.0 using
	// logrithmic curve.
	
	private double stickLogCorrection(double joystickValue)
	{
		double base = Math.pow(2, 1/3) + Math.pow(2, 1/3);
		
		if (joystickValue > 0)
			joystickValue = baseLog(base, joystickValue + 1);
		else if (joystickValue < 0)
			joystickValue = -baseLog(base, -joystickValue + 1);
			
		return joystickValue;
	}
	
	private double stickLogCorrection85(double joystickValue)
	{
		double base = 2.22;
		
		if (joystickValue > 0)
			joystickValue = baseLog(base, joystickValue + 1);
		else if (joystickValue < 0)
			joystickValue = -baseLog(base, -joystickValue + 1);
			
		return joystickValue;
	}
	
	// Handle LaunchPad control events.
	
	public class LaunchPadListener implements LaunchPadEventListener 
	{
	    public void ButtonDown(LaunchPadEvent launchPadEvent) 
	    {
	    	LaunchPadControl	control = launchPadEvent.control;
	    	
			Util.consoleLog("%s, latchedState=%b", control.id.name(),  control.latchedState);
			
			switch(control.id)
			{
				case BUTTON_YELLOW:
    				if (launchPadEvent.control.latchedState)
    					gearPickup.StartAutoPickup();
        			else
        				gearPickup.StopAutoPickup();

    				break;
    				
				case BUTTON_BLUE:
    				if (launchPadEvent.control.latchedState)
    					gearBox.engagePTO();
        			else
        				gearBox.disengagePTO();

    				break;
    				
				case BUTTON_BLUE_RIGHT:
    				if (launchPadEvent.control.latchedState)
    					gearPickup.lowerPickup();
        			else
        				gearPickup.raisePickup();

					break;
    				
				case BUTTON_RED:
    				if (launchPadEvent.control.latchedState)
    					gearPickup.pickupOut();
        			else
        				gearPickup.pickupIn();

					break;
    				
				case BUTTON_RED_RIGHT:
    				if (launchPadEvent.control.latchedState)
    					gearPickup.pickupOut();
        			else
        				gearPickup.pickupIn();

					break;
				
				default:
					break;
			}
	    }
	    
	    public void ButtonUp(LaunchPadEvent launchPadEvent) 
	    {
	    	//Util.consoleLog("%s, latchedState=%b", launchPadEvent.control.name(),  launchPadEvent.control.latchedState);
	    }

	    public void SwitchChange(LaunchPadEvent launchPadEvent) 
	    {
	    	LaunchPadControl	control = launchPadEvent.control;
	    	
	    	Util.consoleLog("%s", control.id.name());

	    	switch(control.id)
	    	{
				// Set CAN Talon brake mmode.
	    		case ROCKER_LEFT_BACK:
    				if (control.latchedState)
    					robot.SetCANTalonBrakeMode(false);	// coast
    				else
    	    			robot.SetCANTalonBrakeMode(true);	// brake
    				
    				break;
    				
	    		case ROCKER_LEFT_FRONT:
					if (robot.cameraThread != null) robot.cameraThread.ChangeCamera();
					//invertDrive = !invertDrive;
	    			break;
	    			
	    		case ROCKER_RIGHT:
					if (robot.cameraThread != null) robot.cameraThread.ChangeCamera();
	    			break;
    				
				default:
					break;
	    	}
	    }
	}

	// Handle Right JoyStick Button events.
	
	private class RightStickListener implements JoyStickEventListener 
	{
		
	    public void ButtonDown(JoyStickEvent joyStickEvent) 
	    {
	    	int angle;
	    	
	    	JoyStickButton	button = joyStickEvent.button;
	    	
			Util.consoleLog("%s, latchedState=%b", button.id.name(),  button.latchedState);
			
			switch(button.id)
			{
				case TRIGGER:
					//if (robot.cameraThread != null) robot.cameraThread.ChangeCamera();
					altDriveMode = !altDriveMode;
					robot.navx.resetYaw();	// for heading testing only. Remove at end of test.
					
					break;
					
				case TOP_LEFT:
   					robot.cameraThread.ChangeCamera();
					//invertDrive = !invertDrive;
    				break;
    				
				case TOP_BACK:
					vision.SeekPegOffset();
					angle = vision.getPegOffset();
					Util.consoleLog("angle=%d", angle);
					break;
				
				default:
					break;
			}
	    }

	    public void ButtonUp(JoyStickEvent joyStickEvent) 
	    {
	    	//Util.consoleLog("%s", joyStickEvent.button.name());
	    }
	}

	// Handle Left JoyStick Button events.
	
	private class LeftStickListener implements JoyStickEventListener 
	{
	    public void ButtonDown(JoyStickEvent joyStickEvent) 
	    {
	    	JoyStickButton	button = joyStickEvent.button;
	    	
			Util.consoleLog("%s, latchedState=%b", button.id.name(),  button.latchedState);
			
			switch(button.id)
			{
				case TRIGGER:
					if (button.latchedState)
	    				gearBox.highSpeed();
	    			else
	    				gearBox.lowSpeed();

					break;
					
				default:
					break;
			}
	    }

	    public void ButtonUp(JoyStickEvent joyStickEvent) 
	    {
	    	//Util.consoleLog("%s", joyStickEvent.button.name());
	    }
	}

	// Handle Utility JoyStick Button events.
	
	private class UtilityStickListener implements JoyStickEventListener 
	{
	    public void ButtonDown(JoyStickEvent joyStickEvent) 
	    {
	    	JoyStickButton	button = joyStickEvent.button;
	    	
			Util.consoleLog("%s, latchedState=%b", button.id.name(),  button.latchedState);
			
			switch(button.id)
			{
				// Trigger starts shoot sequence.
				case TRIGGER:
					if (button.latchedState)
	    				shooter.startFeeding();
	    			else
	    				shooter.stopFeeding();

    				break;
    				
				case TOP_RIGHT:
					if (button.latchedState)
					{
	    				ballPickup.start();
	    				//if (!shooter.isRunning()) shooter.startFeedingReverse();
					}
	    			else
	    			{
	    				ballPickup.stop();
	    				if (!shooter.isRunning()) shooter.stopFeeding();
	    			}
					
					break;
					
				case TOP_LEFT:
					if (button.latchedState)
	    				shooter.start(shooter.SHOOTER_HIGH_POWER);
	    			else
	    				shooter.stop();

					break;
				
				case TOP_MIDDLE:
					if (button.latchedState)
	    				gearPickup.startMotorIn();
	    			else
	    				gearPickup.stopMotor();

					break;
					
				case TOP_BACK:
					if (button.latchedState)
	    				gearPickup.startMotorOut();
	    			else
	    				gearPickup.stopMotor();

					break;
					
				default:
					break;
			}
	    }

	    public void ButtonUp(JoyStickEvent joyStickEvent) 
	    {
	    	//Util.consoleLog("%s", joyStickEvent.button.id.name());
	    }
	}
}
