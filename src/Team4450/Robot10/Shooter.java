/**
 * Handles the Shooter.
 */
package Team4450.Robot10;

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import Team4450.Lib.Util;

public class Shooter
{
	private Robot		robot;
	public Talon		motor = new Talon(1);
	private Talon		feederMotor = new Talon(3), indexerMotor = new Talon(2);

	// Shooter Wheel quad encoder is plugged into dio port 3 - orange=+5v blue=signal, dio port 4 - black=gnd yellow=signal. 
	//public Encoder		encoder = new Encoder(3, 4, true, EncodingType.k4X);
	
	// Touchless Encoder single channel on dio port 0.
	public Counter		tlEncoder = new Counter(0);

	// Robot PID defaults. These look like static constants but you must instantiate
	// this class to set up these items for comp or clone robot before accessing them.
	public double					SHOOTER_LOW_POWER, SHOOTER_HIGH_POWER;
	public double					SHOOTER_LOW_RPM, SHOOTER_HIGH_RPM;
	public double					PVALUE, IVALUE, DVALUE; 

	private final PIDController		shooterPidController;
	//public ShooterSpeedSource		shooterSpeedSource = new ShooterSpeedSource(encoder);
	public ShooterSpeedSource		shooterSpeedSource = new ShooterSpeedSource(tlEncoder);

	public Shooter(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
		
		//encoder.reset();
		tlEncoder.reset();
		
		// This is distance per pulse and our distance is 1 revolution since we want to measure
		// rpm. We determined there are 1024 pulses in a rev so 1/1024 = .000976 rev per pulse.
		//encoder.setDistancePerPulse(.000976);
		
		// This is distance per pulse and our distance is 1 revolution since we want to measure
		// rpm. We know the touchless encoder pulses once per revolution.
		tlEncoder.setDistancePerPulse(1);
		
		// Tells encoder to supply the rate as the input to any PID controller source.
		//encoder.setPIDSourceType(PIDSourceType.kRate);
		tlEncoder.setPIDSourceType(PIDSourceType.kRate);

		// Create PIDController using our custom PIDSource and SpeedController classes.
		shooterPidController = new PIDController(0.0, 0.0, 0.0, shooterSpeedSource, motor);

		// Handle the fact that the pickup motor is a CANTalon on competition robot
		// and a pwm Talon on clone. Set PID defaults. Note that this sets the default
		// values which are reflected on the DS. Changes there change the values in use
		// by the running program.
		
		stop();
		
		if (robot.isComp) 
		{
			// Competition robot PID defaults.
			SHOOTER_LOW_POWER = .50;
			SHOOTER_HIGH_POWER = .45;
			SHOOTER_LOW_RPM = 2500;
			SHOOTER_HIGH_RPM = 3200;

			PVALUE = .0025;
			IVALUE = .0025;
			DVALUE = .005;	//3; 
		}
		else
		{
			// Clone robot PID defaults.
			SHOOTER_LOW_POWER = .50;
			SHOOTER_HIGH_POWER = .45;
			SHOOTER_LOW_RPM = 2500;
			SHOOTER_HIGH_RPM = 3200;

			PVALUE = .0025; 
			IVALUE = .0025;
			DVALUE = .003; 
		}
	}
	
	public void dispose()
	{
		Util.consoleLog();

		if (shooterPidController != null)
		{
			shooterPidController.disable();
			shooterPidController.free();
		}
		
		if (motor != null) motor.free();
		//if (encoder != null) encoder.free();
		if (tlEncoder != null) tlEncoder.free();
		if (feederMotor != null) feederMotor.free();
		if (indexerMotor != null) indexerMotor.free();
	}
	
//	public void start()
//	{
//		Util.consoleLog();
//
//		SmartDashboard.putBoolean("ShooterMotor", true);
//		
//		motor.set(.80);
//	}

	/**
	 * Start shooter motors.
	 * @param power Power level to use 0.0 to +1.0. If PID is enabled, and the power is equal to
	 * the low or high power constant, PID will be used to hold the low or high RPM as set on the
	 * driver station and with the P I D values set on the DS. If any other value or PID is off,
	 * the power value is used directly on the motors.
	 */
	public void start(double power)
	{
		Util.consoleLog("%.2f", power);
	
		if (SmartDashboard.getBoolean("PIDEnabled", false))
		{
    		if (power == SHOOTER_LOW_POWER)
    			// When shooting at low power, we will attempt to maintain a constant wheel speed (rpm)
    			// using pid controller measuring rpm via the encoder. RPM determined experimentally
    			// by setting motors to the low power value and seeing what rpm results.
    			// This call starts the pid controller and turns shooter motor control over to it.
    			// The pid will run the motors on its own until disabled.
    			holdShooterRPM(SmartDashboard.getNumber("LowSetting", SHOOTER_LOW_RPM));
    		else if (power == SHOOTER_HIGH_POWER)
    			// We later decided to use pid for high power shot when high power was reduced from 100%.
    			holdShooterRPM(SmartDashboard.getNumber("HighSetting", SHOOTER_HIGH_RPM));
    		else
    			// Set power directly for any value other than the defined high and low values.
    			motor.set(power);
		}
		else
			motor.set(power);
			
		SmartDashboard.putBoolean("ShooterMotor", true);
	}

	public void stop()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("ShooterMotor", false);
		
		shooterPidController.disable();

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
		
		feederMotor.set(-.35);	// comp=.30
		indexerMotor.set(-.80);	// comp=.50
	}
	
	public void startFeedingReverse()
	{
		Util.consoleLog();

		SmartDashboard.putBoolean("DispenserMotor", true);
		
		feederMotor.set(.20);
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
	
	/**
	 * Automatically hold shooter motor speed (rpm). Starts PID controller to
	 * manage motor power to maintain rpm target.
	 * @param rpm RPM to hold.
	 */
	private void holdShooterRPM(double rpm)
	{
		double pValue = SmartDashboard.getNumber("PValue", PVALUE);
		double iValue = SmartDashboard.getNumber("IValue", IVALUE);
		double dValue = SmartDashboard.getNumber("DValue", DVALUE);

		Util.consoleLog("rpm=%.0f  p=%.4f  i=%.4f  d=%.4f", rpm, pValue, iValue, dValue);
		
		// p,i,d values are a guess.
		// f value is the base motor speed, which is where (power) we start.
		// setpoint is target rpm converted to rev/sec.
		// The idea is that we apply power to get rpm up to set point and then maintain.
		//shooterPidController.setPID(0.001, 0.001, 0.0, 0.0); 
		
		shooterPidController.setPID(pValue, iValue, dValue, 0.0); 
		shooterPidController.setSetpoint(rpm / 60);		// setpoint is revolutions per second.
		shooterPidController.setPercentTolerance(5);	// 5% error.
		shooterPidController.setToleranceBuffer(4096);	// 4 seconds of averaging.
		shooterPidController.setContinuous();
		//shooterPidController.setOutputRange(.20, .90);
		shooterSpeedSource.reset();
		shooterPidController.enable();
	}

	// Encapsulate an encoder or counter so we could modify the rate returned to
	// the PID controller.
	public class ShooterSpeedSource implements PIDSource
	{
		private Encoder		encoder;
		private Counter		counter;
		private int			inversion = 1;
		private double		rpmAccumulator, rpmSampleCount;
		
		public ShooterSpeedSource(Encoder encoder)
		{
			this.encoder = encoder;
		}
		
		public ShooterSpeedSource(Counter counter)
		{
			this.counter = counter;
		}
		
		@Override
		public void setPIDSourceType(PIDSourceType pidSource)
		{
			if (encoder != null) encoder.setPIDSourceType(pidSource);
			if (counter != null) counter.setPIDSourceType(pidSource);
		}

		@Override
		public PIDSourceType getPIDSourceType()
		{
			if (encoder != null) return encoder.getPIDSourceType();
			if (counter != null) return counter.getPIDSourceType();

			return null;
		}
		
		public void setInverted(boolean inverted)
		{
			if (inverted)
				inversion = -1;
			else
				inversion = 1;
		}

		public int get()
		{
			if (encoder != null ) return encoder.get() * inversion;
			if (counter != null ) return counter.get() * inversion;
			
			return 0;
		}
		
		public double getRate()
		{
			// TODO: Some sort of smoothing could be done to damp out the
			// fluctuations in encoder rate.
			
//			if (rpmSampleCount > 2048) rpmAccumulator = rpmSampleCount = 0;
//			
//			rpmAccumulator += encoder.getRate();
//			rpmSampleCount += 1;
//			
//			return rpmAccumulator / rpmSampleCount;

			if (encoder != null) return encoder.getRate() * inversion;
			if (counter != null) return counter.getRate() * inversion;
			
			return 0;
		}
		
		/**
		 * Return the current rotational rate of the encoder or current value (count) to PID controllers.
		 * @return Encoder revolutions per second or current count.
		 */
		@Override
		public double pidGet()
		{
			if (encoder != null)
			{
    			if (encoder.getPIDSourceType() == PIDSourceType.kRate)
    				return getRate();
    			else
    				return get();
			}
			
			if (counter != null)
			{
    			if (counter.getPIDSourceType() == PIDSourceType.kRate)
    				return getRate();
    			else
    				return get();
			}
			
			return 0;
		}
		
		public void reset()
		{
			rpmAccumulator = rpmSampleCount = 0;
			
			if (encoder != null) encoder.reset();
			if (counter != null) counter.reset();
		}
	}
}
