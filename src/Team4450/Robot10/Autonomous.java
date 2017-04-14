
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
	private Shooter		shooter;
	private BallPickup	ballPickup;
	
	//	encoder is plugged into dio port 1 - orange=+5v blue=signal, dio port 2 black=gnd yellow=signal. 
	private Encoder		encoder = new Encoder(3, 4, true, EncodingType.k4X);

	Autonomous(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		// Create instance of GearBox to initialize it properly for autonomous.
		
		gearBox = new GearBox(robot);

		gearPickup = new GearPickup(robot, null);
		
		vision = Vision.getInstance(robot);
		
		shooter = new Shooter(robot);
		
		ballPickup = new BallPickup(robot);
	}

	public void dispose()
	{
		Util.consoleLog();
		
		if (encoder != null) encoder.free();
		if (gearBox != null) gearBox.dispose();
		if (gearPickup != null) gearPickup.dispose();
		if (shooter != null) shooter.dispose();
		if (ballPickup != null) ballPickup.dispose();
	}
	
	private boolean isAutoActive()
	{
		return robot.isEnabled() && robot.isAutonomous();
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
        //Timer.delay(.50);

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
				
			case 7:		// Drive then shoot, left start.
				autoShoot(true);
				
				break;

			case 8:		// Drive then shoot, right start.
				autoShoot(false);
				
				break;
				
			case 9:		// Place gear center start with vision.
				placeGearCenter(5500, true);
				
				break;
				
			case 10:	// Test.
				//autoDrive(-.50, 9000, true);
				//autoDriveVision(-.40, 3000, true);
				//autoShoot();
				
				break;
		}
		
		Util.consoleLog("end");
	}

	private void placeGearCenter(int encoderCounts, boolean useVision)
	{
		Util.consoleLog("%d  vision=%b", encoderCounts, useVision);
		
		// Drive forward to peg and stop.
		
		if (useVision)
			autoDriveVision(-.50, encoderCounts, true);
		else
			autoDrive(-.60, encoderCounts, true);
		
		if (!isAutoActive()) return;
		
		// Start gear pickup motor in reverse.

		gearPickup.startMotorOut();
		
		Timer.delay(.5);
		
		gearPickup.lowerPickup();
		
		Timer.delay(.5);
		
		// Drive backward a bit.

		autoDrive(.30, 1000, true);
		
		gearPickup.stopMotor();
	}
	
	private void placeGearFromSide(boolean leftSide, boolean useVision)
	{
		Util.consoleLog("left side=%b  vision=%b", leftSide, useVision);
		
		// Drive forward to be on a 55 degree angle with side peg and stop.
		
		if (leftSide)
			autoDrive(-.50, 5600, true);
		else
			autoDrive(-.50, 5600, true);
		
		if (!isAutoActive()) return;
		
		// rotate as right or left 90 degrees.
		
		if (leftSide)
			// Rotate right.
			autoRotate(-.60, 55);
		else
			// Rotate left
			autoRotate(.60, 55);
		
		if (!isAutoActive()) return;
		
		// Lef the camera image settle down.
		
		if (useVision) Timer.delay(.25);
		
		// Place gear.
		
		placeGearCenter(5300, useVision);
		
		// Move and shoot balls if on left side.
		
//		if (leftSide)
//		{
//			autoRotate(.60, 20);
//			
//			shooter.start(shooter.SHOOTER_HIGH_POWER);
//			
//			autoDrive(.80, 6200, true);
//			
//			shooter.startFeeding();
//		}
	}
	
	private void autoShoot(boolean left)
	{
		double power = -.60;
		int		encoderCountsFwd = 5400, encoderCountsBack = 3700, angle = 31;
		
		Util.consoleLog("pwr=%.2f  encf=%d  encb=%d  angle=%d", power, encoderCountsFwd, encoderCountsBack, angle);
	
		// Drive forward to cross base line.
		
		autoDrive(power, encoderCountsFwd, true);
		
		// Turn to point at boiler.
		
		if (left)
			autoRotate(power, angle);	
		else
			autoRotate(-power, angle);
		
		if (!isAutoActive()) return;
		
		// Back up to shooting location;
		
		autoDrive(-power, encoderCountsBack, true);
		
		if (!isAutoActive()) return;
		
		// Shoot balls.
		
		gearPickup.lowerPickup();
		
		shooter.start(shooter.SHOOTER_HIGH_POWER);
		
		Timer.delay(3);
		
		ballPickup.start();
		
		shooter.startFeeding();
		
		// Run until auto is over.
		
		while (isAutoActive()) Timer.delay(1);
	}
	
	// Auto drive in set direction and power for specified encoder count. Stops
	// with or without brakes on CAN bus drive system. Uses gyro/NavX to go straight.
	
	private void autoDrive(double power, int encoderCounts, boolean enableBrakes)
	{
		int		angle;
		double	gain = .03;
		
		Util.consoleLog("pwr=%.2f, count=%d, brakes=%b", power, encoderCounts, enableBrakes);

		if (robot.isComp) robot.SetCANTalonBrakeMode(enableBrakes);

		encoder.reset();
		robot.navx.resetYaw();
		
		while (isAutoActive() && Math.abs(encoder.get()) < encoderCounts) 
		{
			LCD.printLine(4, "encoder=%d", encoder.get());
			
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
		
		Util.consoleLog("end: actual count=%d", Math.abs(encoder.get()));
	}
	
	// Auto rotate left or right the specified angle. Left/right from robots forward view.
	// Turn right, power is -
	// Turn left, power is +
	// angle of rotation is always +.
	
	private void autoRotate(double power, int angle)
	{
		Util.consoleLog("pwr=%.2f  angle=%d", power, angle);
		
		robot.navx.resetYaw();
		
		robot.robotDrive.tankDrive(power, -power);

		while (isAutoActive() && Math.abs((int) robot.navx.getYaw()) < angle) {Timer.delay(.020);} 
		
		robot.robotDrive.tankDrive(0, 0);
	}
	
	// Auto drive in current direction and power for specified encoder count. Stops
	// with or without brakes on CAN bus drive system. Uses vision to drive to spring
	// targets. Vision code returns pixel offset of peg from the center of the camera
	// image. We use that offset to drive with curve. We further use the pixel distance
	// between the target rectangles to determine the distance to the peg.
	
	private void autoDriveVision(double power, int encoderCounts, boolean enableBrakes)
	{
		int		distance, prevDistance = 0;
		double	pegOffset, gain = .002, power2 = power, delay = .10;	//.25;
		boolean	driving = true;
		
		Util.consoleLog("pwr=%.2f, count=%d, brakes=%b", power, encoderCounts, enableBrakes);

		if (robot.isComp) robot.SetCANTalonBrakeMode(enableBrakes);

		encoder.reset();
		
		robot.monitorDistanceThread.setDelay(delay);
		
		while (driving) 
		{
			LCD.printLine(4, "encoder=%d", encoder.get());
			
			// pegOffset is negative if robot veering right, positive if veering left when going forward.
			// It is opposite when going backward. Note that for this robot, - power means forward and
			// + power means backward. If we get successful image evaluation, use the pixel offset from
			// center as direction control. If not, set pegOffset = 0 to drive straight on current heading.
			
			if (vision.SeekPegOffset())
			{
				pegOffset = vision.getPegOffset();
				
				distance = vision.getDistance();
				
				SmartDashboard.putBoolean("TargetLocked", true);
			}
			else
			{
				pegOffset = 0;
				distance = 0;
				SmartDashboard.putBoolean("TargetLocked", false);
			}
			
			// If we have the distance between target rectangles we can determine from that if we
			// should stop. If we don't have distance between rectangles, fall back to encoder counts.
			// Also, while monitoring distance, if distance is less than previous distance then we are
			// probably so close to target we are not getting true distance so we should stop.
			
			if (distance != 0)
			{
				if (distance > 150 && distance <= prevDistance)
					driving = false;
				else
					driving = isAutoActive() && distance < 195;
				
				if (!driving) 
				{
					Util.consoleLog("stop driving, distance=%d", distance);
					continue;
				}
			}
			else
			{
				driving = isAutoActive() && Math.abs(encoder.get()) < encoderCounts;
				
				if (!driving) 
				{
					Util.consoleLog("stop driving, encoder=%d", Math.abs(encoder.get()));
					continue;
				}
			}
			
			prevDistance = distance;
			
			// If no distance, we are driving on encoder so use very short delay. If we have distance
			// measurement, then use longer delay between image evaluations. When close to the target
			// distance, increase the rate of image checks and drop the speed. Also, when far from the
			// target we need a small gain so that robot responds minimally to being off center. When
			// we get close we need to up the gain so the robot responds more quickly to off center.
			
			if (distance != 0 && distance < 100)
			{
				delay = .10;	//.25;
				power2 = power;
			}
			else if (distance != 0)
			{
				delay = .10;
				gain = .005;
				power2 = power / 2;
			}
			
			// Invert offset for backwards.
			
			if (power > 0) pegOffset = -pegOffset;
			
			// Gain value controls sensitivity to the offset and maps the offset to range -1 to +1. 
			// Cap the offset in range -1 to +1.
			
			pegOffset = pegOffset * gain;
			
			if (pegOffset < -1)
				pegOffset = -1;
			else if (pegOffset > 1)
				pegOffset = 1;
			
			Util.consoleLog("power=%.2f  offset=%.3f  dist=%d  delay=%.2f  usd=%.2f", power2, pegOffset, distance, delay,
							robot.monitorDistanceThread.getRangeInches());
			
			LCD.printLine(5, "power=%.2f  offset=%.3f  dist=%d  delay=%.2f  usd=%.2f", power2, pegOffset, distance, delay,
							robot.monitorDistanceThread.getRangeInches());

			// Offset is + if robot veering right, so we invert to - because - curve is to the left.
			// Offset is - if robot veering left, so we invert to + because + curve is to the right.
			
			robot.robotDrive.drive(power2, -pegOffset);
			
			Timer.delay(delay);
		}	// end of while (driving).

		robot.robotDrive.tankDrive(0, 0, true);				
		
		robot.monitorDistanceThread.setDelay(1.0);
	}
}