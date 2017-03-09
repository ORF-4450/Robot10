
package Team4450.Robot10;

import Team4450.Lib.*;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous
{
	private final Robot	robot;
	private final int	program = (int) SmartDashboard.getNumber("AutoProgramSelect");
	private GearPickup	gearPickup;
	private GearBox		gearBox;
	private Vision		vision;
	
	//	encoder is plugged into dio port 2 - orange=+5v blue=signal, dio port 3 black=gnd yellow=signal. 
	private Encoder		encoder = new Encoder(1, 2, true, EncodingType.k4X);

	Autonomous(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		// Create instance of GearBox to initialize it properly for autonomous.
		
		gearBox = new GearBox(robot);

		gearPickup = new GearPickup(robot, null);
		
		vision = Vision.getInstance(robot);
	}

	public void dispose()
	{
		Util.consoleLog();
		
		if (encoder != null) encoder.free();
		if (gearBox != null) gearBox.dispose();
		if (gearPickup != null) gearPickup.dispose();
	}

	public void execute()
	{
		Util.consoleLog("Alliance=%s, Location=%d, Program=%d, FMS=%b", robot.alliance.name(), robot.location, program, robot.ds.isFMSAttached());
		LCD.printLine(2, "Alliance=%s, Location=%d, FMS=%b, Program=%d", robot.alliance.name(), robot.location, robot.ds.isFMSAttached(), program);

		robot.robotDrive.setSafetyEnabled(false);

		// Initialize encoder.
		encoder.reset();
        
        // Set gyro/NavX to heading 0.
        //robot.gyro.reset();
		robot.navx.resetYaw();
		
        // Wait to start motors so gyro will be zero before first movement.
        Timer.delay(.50);

		switch (program)
		{
			case 0:		// No auto program.
				break;
				
			case 1:		// Drive forward to line and stop.
				autoDrive(-.70, 9000, true);
				
				break;
				
			case 2:		// Place gear center start.
				placeGearCenter(5800, false);
				
				break;
				
			case 3:		// Place gear left start.
				placeGearFromSide(true, false);
				
				break;
				
			case 4:		// Place gear right start.
				placeGearFromSide(false, false);
				
				break;
				
			case 5:		// Place gear left start with vision.
				placeGearFromSide(true, true);
				
				break;
				
			case 6:		// Place gear right start with vision.
				placeGearFromSide(false, true);
				
				break;
				
			case 7:		// TestDrive backward and stop.
				autoDrive(.70, 9000, true);
				
				break;
		}
		
		Util.consoleLog("end");
	}

	private void placeGearCenter(int encoderCounts, boolean useVision)
	{
		Util.consoleLog("%d", encoderCounts);
		
		// Drive forward to peg and stop.
		
		if (useVision)
			autoDrive(-.60, encoderCounts, true);
		else
			autoDriveVision(-.60, encoderCounts, true);
		
		// Start gear pickup motor in reverse.
		
		gearPickup.startMotorOut();
		
		Timer.delay(.500);
		
		// Drive backward a bit.

		autoDrive(.50, 1000, true);
		
		gearPickup.stopMotor();
	}
	
	private void placeGearFromSide(boolean leftSide, boolean useVision)
	{
		Util.consoleLog("left side=%b", leftSide);
		
		// Drive forward to be on a 55 degree angle with side peg and stop.
		
		if (leftSide)
			autoDrive(-.50, 5600, true);
		else
			autoDrive(-.50, 5600, true);
		
		// rotate as right or left 90 degrees.
		
		if (leftSide)
			// Rotate right.
			autoRotate(-.60, 55);
		else
			// Rotate left
			autoRotate(.60, 55);
		
		// Place gear.
		
		placeGearCenter(5300, useVision);
	}
	
	// Auto drive in set direction and power for specified encoder count. Stops
	// with or without brakes on CAN bus drive system. Uses gyro/NavX to go straight.
	
	private void autoDrive(double power, int encoderCounts, boolean enableBrakes)
	{
		int		angle;
		double	gain = .03;
		
		Util.consoleLog("pwr=%f, count=%d, brakes=%b", power, encoderCounts, enableBrakes);

		if (robot.isComp) robot.SetCANTalonBrakeMode(enableBrakes);

		encoder.reset();
		robot.navx.resetYaw();
		
		while (robot.isAutonomous() && Math.abs(encoder.get()) < encoderCounts) 
		{
			LCD.printLine(3, "encoder=%d", encoder.get());
			
			// Angle is negative if robot veering left, positive if veering right when going forward.
			// It is opposite when going backward. Note that for this robot, - power means forward and
			// + power means backward.
			
			//angle = (int) robot.gyro.getAngle();
			angle = (int) robot.navx.getYaw();

			LCD.printLine(5, "angle=%d", angle);
			
			// Invert angle for backwards.
			
			if (power > 0) angle = -angle;
			
			//Util.consoleLog("angle=%d", angle);
			
			// Note we invert sign on the angle because we want the robot to turn in the opposite
			// direction than it is currently going to correct it. So a + angle says robot is veering
			// right so we set the turn value to - because - is a turn left which corrects our right
			// drift.
			
			robot.robotDrive.drive(power, -angle * gain);
			
			Timer.delay(.020);
		}

		robot.robotDrive.tankDrive(0, 0, true);				
	}
	
	// Auto rotate left or right the specified angle. Left/right from robots forward view.
	// Turn right, power is -
	// Turn left, power is +
	// angle of rotation is always +.
	
	private void autoRotate(double power, int angle)
	{
		Util.consoleLog("pwr=%.3f  angle=%d", power, angle);
		
		robot.navx.resetYaw();
		
		robot.robotDrive.tankDrive(power, -power);

		while (robot.isAutonomous() && Math.abs((int) robot.navx.getYaw()) < angle) {Timer.delay(.020);} 
		
		robot.robotDrive.tankDrive(0, 0);
	}
	
	// Auto drive in set direction and power for specified encoder count. Stops
	// with or without brakes on CAN bus drive system. Uses vision to drive to spring.
	
	private void autoDriveVision(double power, int encoderCounts, boolean enableBrakes)
	{
		int		angle;
		double	gain = .03;
		
		Util.consoleLog("pwr=%f, count=%d, brakes=%b", power, encoderCounts, enableBrakes);

		if (robot.isComp) robot.SetCANTalonBrakeMode(enableBrakes);

		encoder.reset();
		
		while (robot.isAutonomous() && Math.abs(encoder.get()) < encoderCounts) 
		{
			LCD.printLine(3, "encoder=%d", encoder.get());
			
			// Angle is negative if robot veering left, positive if veering right when going forward.
			// It is opposite when going backward. Note that for this robot, - power means forward and
			// + power means backward.
			
			if (vision.SeekPegOffset())
				angle = vision.getPegOffset();
			else
				angle = 0;
			
			LCD.printLine(5, "angle=%d", (int) angle);
			
			// Invert angle for backwards.
			
			if (power > 0) angle = -angle;
			
			//Util.consoleLog("angle=%d", angle);
			
			// Note we invert sign on the angle because we want the robot to turn in the opposite
			// direction than it is currently going to correct it. So a + angle says robot is veering
			// right so we set the turn value to - because - is a turn left which corrects our right
			// drift.
			
			robot.robotDrive.drive(power, -angle * gain);
			
			Timer.delay(.020);
		}

		robot.robotDrive.tankDrive(0, 0, true);				
	}
}