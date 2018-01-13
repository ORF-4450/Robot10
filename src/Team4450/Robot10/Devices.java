package Team4450.Robot10;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import Team4450.Lib.*;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Ultrasonic;

public class Devices
{
	  // Motor CAN ID/PWM port assignments (1=left-front, 2=left-rear, 3=right-front, 4=right-rear)
	  private static WPI_TalonSRX	LFCanTalon, LRCanTalon, RFCanTalon, RRCanTalon, LSlaveCanTalon, RSlaveCanTalon;
	  //Talon					LFPwmTalon, LRPwmTalon, RFPwmTalon, RRPwmTalon;
	  
	  public static DifferentialDrive	robotDrive;

	  public final static Joystick      utilityStick = new Joystick(2);	
	  public final static Joystick      leftStick = new Joystick(0);	
	  public final static Joystick		rightStick = new Joystick(1);	
	  public final static Joystick		launchPad = new Joystick(3);

	  public final static Compressor	compressor = new Compressor(0);	// Compressor class represents the PCM. There are 2.
	  public final static Compressor	compressor1 = new Compressor(1);
	  
	  public final static AnalogInput	pressureSensor = new AnalogInput(0);
	  public final static Ultrasonic	distanceSensor = new Ultrasonic(5, 6);	// Digital I/O ports.
	  
	  public final static ValveDA		unusedValve = new ValveDA(1, 3);
	  public final static ValveDA		pickupLiftValve = new ValveDA(6);
	  public final static ValveDA		pickupDeployValve = new ValveDA(1, 0);
	  public final static ValveDA		highLowValve = new ValveDA(0);
	  public final static ValveDA		ptoValve = new ValveDA(2);
	  public final static ValveDA		neutralValve = new ValveDA(4);

	  public final static PowerDistributionPanel	PDP = new PowerDistributionPanel();

	  public final static DriverStation				ds = DriverStation.getInstance();

	  public static NavX				navx;

	  // Wheel encoder is plugged into dio port 3 - orange=+5v blue=signal, dio port 4 black=gnd yellow=signal. 
	  public final static Encoder		encoder = new Encoder(3, 4, true, EncodingType.k4X);

	  // Encoder ribbon cable to dio ports: ribbon wire 2 = orange, 5 = yellow, 7 = blue, 10 = black
	  // not used.
	  
	  public final static Spark			ballMotor = new Spark(0);
	  public final static Talon			shooterMotor = new Talon(1);
	  public final static Talon			indexerMotor = new Talon(2);
	  public final static Talon			feederMotor = new Talon(3);
	  public final static WPI_TalonSRX	gearMotor = new WPI_TalonSRX(7);

	  // Shooter Wheel quad encoder is plugged into dio port 3 - orange=+5v blue=signal, dio port 4 - black=gnd yellow=signal. 
	  //public Encoder		shooterEncoder = new Encoder(3, 4, true, EncodingType.k4X);
		
	  // Touchless Encoder uses single channel on dio port 0.
	  public final static Counter			shooterEncoder = new Counter(0);

	  public final static AbsoluteEncoder	absEncoder = new AbsoluteEncoder(new AnalogInput(1), 192);
	  
	  // Create RobotDrive object for CAN Talon controllers.
	  
	  public static void InitializeCANTalonDrive()
	  {
		  Util.consoleLog();

		  LFCanTalon = new WPI_TalonSRX(1);
		  LRCanTalon = new WPI_TalonSRX(2);
		  RFCanTalon = new WPI_TalonSRX(3);
		  RRCanTalon = new WPI_TalonSRX(4);
		  LSlaveCanTalon = new WPI_TalonSRX(5);
		  RSlaveCanTalon = new WPI_TalonSRX(6);

	      // Initialize CAN Talons and write status to log so we can verify
	      // all the Talons are connected.
	      InitializeCANTalon(LFCanTalon);
	      InitializeCANTalon(LRCanTalon);
	      InitializeCANTalon(RFCanTalon);
	      InitializeCANTalon(RRCanTalon);
	      InitializeCANTalon(LSlaveCanTalon);
	      InitializeCANTalon(RSlaveCanTalon);
	      
	      // Configure CAN Talons with correct inversions.

	      LFCanTalon.setInverted(true);
		  LRCanTalon.setInverted(true);
		  
		  RFCanTalon.setInverted(true);
		  RRCanTalon.setInverted(true);
		  
		  LSlaveCanTalon.setInverted(false);
		  RSlaveCanTalon.setInverted(false);
	      
	      // Turn on brake mode for CAN Talons.
	      SetCANTalonBrakeMode(true);
	      
	      // Setup the SpeedControllerGroups for the left and right set of motors.
	      SpeedControllerGroup LeftGroup = new SpeedControllerGroup(LFCanTalon,LSlaveCanTalon,LRCanTalon);
		  SpeedControllerGroup RightGroup = new SpeedControllerGroup(RFCanTalon,RSlaveCanTalon,RRCanTalon);
		  
		  robotDrive = new DifferentialDrive(LeftGroup, RightGroup);
	  }

	  // Create RobotDrive object for PWM controllers.
	  
	//  private void InitializePWMTalonDrive()
	//  {
//		  Util.consoleLog();
	//
//		  LFPwmTalon = new Talon(3);
//		  LRPwmTalon = new Talon(4);
//		  RFPwmTalon = new Talon(5);
//		  RRPwmTalon = new Talon(6);
//		 
//		  robotDrive = new RobotDrive(LFPwmTalon, LRPwmTalon, RFPwmTalon, RRPwmTalon);
//		  
//		  Util.consoleLog("end");
	//  }
	  
	  // Initialize and Log status indication from CANTalon. If we see an exception
	  // or a talon has low voltage value, it did not get recognized by the RR on start up.
	  
	  public static void InitializeCANTalon(WPI_TalonSRX talon)
	  {
		  Util.consoleLog("talon init: %s   voltage=%.1f", talon.getDescription(), talon.getBusVoltage());

		  talon.clearStickyFaults(0);
		  //talon.enableControl();
		  //talon.changeControlMode(TalonControlMode.PercentVbus); //TODO Find PercentVbus
	  }
	  
	  // Set neutral behavior of CAN Talons. True = brake mode, false = coast mode.

	  public static void SetCANTalonBrakeMode(boolean brakeMode)
	  {
		  NeutralMode newMode;

		  Util.consoleLog("brakes on=%b", brakeMode);
		  
		  if (brakeMode) 
			  newMode = NeutralMode.Brake;
		  else 
			  newMode = NeutralMode.Coast;

		  LFCanTalon.setNeutralMode(newMode);
		  LRCanTalon.setNeutralMode(newMode);
		  RFCanTalon.setNeutralMode(newMode);
		  RRCanTalon.setNeutralMode(newMode);
		  LSlaveCanTalon.setNeutralMode(newMode);
		  RSlaveCanTalon.setNeutralMode(newMode);
	  }
	  
	  // Set CAN Talon voltage ramp rate. Rate is volts/sec and can be 2-12v.
	  // No 2018 equivalent.
	  
//	  public static void SetCANTalonRampRate(double rate)
//	  {
//		  Util.consoleLog("%f", rate);
//		  
//		  LFCanTalon.setVoltageRampRate(rate);
//		  LRCanTalon.setVoltageRampRate(rate);
//		  RFCanTalon.setVoltageRampRate(rate);
//		  RRCanTalon.setVoltageRampRate(rate);
//		  LSlaveCanTalon.setVoltageRampRate(rate);
//		  RSlaveCanTalon.setVoltageRampRate(rate);
//	  }
	  
	  // Return voltage and current draw for each CAN Talon.
	  
	  public static String GetCANTalonStatus()
	  {
		  return String.format("%.1f/%.1f  %.1f/%.1f  %.1f/%.1f  %.1f/%.1f  %.1f/%.1f  %.1f/%.1f", 
				  LFCanTalon.getMotorOutputVoltage(), LFCanTalon.getOutputCurrent(),
				  LRCanTalon.getMotorOutputVoltage(), LRCanTalon.getOutputCurrent(),
				  RFCanTalon.getMotorOutputVoltage(), RFCanTalon.getOutputCurrent(),
				  RRCanTalon.getMotorOutputVoltage(), RRCanTalon.getOutputCurrent(),
				  LSlaveCanTalon.getMotorOutputVoltage(), LSlaveCanTalon.getOutputCurrent(),
				  RSlaveCanTalon.getMotorOutputVoltage(), RSlaveCanTalon.getOutputCurrent());
	  }

}
