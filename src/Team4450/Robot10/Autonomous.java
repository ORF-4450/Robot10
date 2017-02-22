
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
	
	// encoder is plugged into dio port 2 - orange=+5v blue=signal, dio port 3 black=gnd yellow=signal. 
	private Encoder		encoder = new Encoder(1, 2, true, EncodingType.k4X);

	Autonomous(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
	}

	public void dispose()
	{
		Util.consoleLog();
		
		encoder.free();
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
				autoDrive(-.50, 9000, true);
				
				break;
		}
		
		Util.consoleLog("end");
	}

	// Auto drive in set direction and power for specified encoder count. Stops
	// with or without brakes on CAN bus drive system. Uses gyro/NavX to go straight.
	
	private void autoDrive(double power, int encoderCounts, boolean enableBrakes)
	{
		int		angle;
		double	gain = .03;
		
		Util.consoleLog("pwr=%f, count=%d, coast=%b", power, encoderCounts, enableBrakes);

		if (robot.isComp) robot.SetCANTalonBrakeMode(enableBrakes);
		
		while (robot.isAutonomous() && Math.abs(encoder.get()) < encoderCounts) 
		{
			LCD.printLine(3, "encoder=%d", encoder.get());
			//LCD.printLine(5, "gyroAngle=%d, gyroRate=%d", (int) robot.gyro.getAngle(), (int) robot.gyro.getRate());
			LCD.printLine(5, "gyroAngle=%d", (int) robot.navx.getYaw());
			
			// Angle is negative if robot veering left, positive if veering right.
			
			//angle = (int) robot.gyro.getAngle();
			angle = (int) robot.navx.getYaw();
			
			//Util.consoleLog("angle=%d", angle);
			
			robot.robotDrive.drive(power, -angle * gain);
			
			Timer.delay(.020);
		}

		robot.robotDrive.tankDrive(0, 0, true);				
	}
}