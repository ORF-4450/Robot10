// 2017 competition robot code.
// For Robot "tba" built for FRC game "First STEAMWorks".

package Team4450.Robot10;

import java.io.IOException;
import java.util.Properties;

import Team4450.Lib.*;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
//import edu.wpi.first.wpilibj.Talon;

import com.ctre.*;
import com.ctre.CANTalon.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file.
 */

public class Robot extends SampleRobot 
{
  static final String  	PROGRAM_NAME = "RAC10-02.02.17-01";

  // Motor CAN ID/PWM port assignments (1=left-front, 2=left-rear, 3=right-front, 4=right-rear)
  CANTalon				LFCanTalon, LRCanTalon, RFCanTalon, RRCanTalon, LSlaveCanTalon, RSlaveCanTalon;
  //Talon					LFPwmTalon, LRPwmTalon, RFPwmTalon, RRPwmTalon;
  RobotDrive      		robotDrive;
  
  final Joystick        utilityStick = new Joystick(2);	// 0 old ds configuration
  final Joystick        leftStick = new Joystick(0);	// 1
  final Joystick        rightStick = new Joystick(1);	// 2
  final Joystick		launchPad = new Joystick(3);
  
  final Compressor		compressor = new Compressor(0);	// Compressor class represents the PCM. There are 2.
  final Compressor		compressor1 = new Compressor(1);
  final AnalogGyro		gyro = new AnalogGyro(0);		// gyro must be plugged into analog port 0 or 1.
  
  public Properties		robotProperties;
  
  public boolean		isClone = false, isComp = false;
  
  PowerDistributionPanel PDP = new PowerDistributionPanel();

  DriverStation         ds = null;
    	
  DriverStation.Alliance	alliance;
  int                       location;
    
  Thread               	monitorBatteryThread, monitorDistanceThread, monitorCompressorThread;
  CameraFeed			cameraThread;
  
  NavX					navx;
    
  // Constructor.
  
  public Robot() throws IOException
  {	
	// Set up our custom logger.
	 
	try
	{
		Util.CustomLogger.setup();
    }
    catch (Throwable e) {Util.logException(e);}
      
    try
    {
    	Util.consoleLog(PROGRAM_NAME);

    	Util.consoleLog("RobotLib=%s", LibraryVersion.version);
    	
        ds = DriverStation.getInstance();
    }
    catch (Throwable e) {Util.logException(e);}
  }
    
  // Initialization, called at class start up.
  
  public void robotInit()
  {
   	try
    {
   		Util.consoleLog();

   		LCD.clearAll();
   		LCD.printLine(1, "Mode: RobotInit");
      
   		// Read properties file from RoboRio "disk".
      
   		robotProperties = Util.readProperties();
      
   		// Is this the competition or clone robot?
   		
		if (robotProperties.getProperty("RobotId").equals("comp"))
			isComp = true;
		else
			isClone = true;

   		SmartDashboard.putString("Program", PROGRAM_NAME);
   		
   		SmartDashboard.putBoolean("CompressorEnabled", Boolean.parseBoolean(robotProperties.getProperty("CompressorEnabledByDefault")));

   		// Initialize PID data entry fields on the DS to thier default values.
   		
   		SmartDashboard.putBoolean("PIDEnabled", false);
   		SmartDashboard.putNumber("PValue", 0);
   		SmartDashboard.putNumber("IValue", 0);
   		SmartDashboard.putNumber("DValue", 0);
   		SmartDashboard.putNumber("LowSetting", 0);
   		SmartDashboard.putNumber("HighSetting", 0);
   		
   		// Reset PDB & PCM sticky faults.
      
   		PDP.clearStickyFaults();
   		compressor.clearAllPCMStickyFaults();
   		compressor1.clearAllPCMStickyFaults();

   		// Configure motor controllers and RobotDrive.
   		
		InitializeCANTalonDrive();
		
        robotDrive.stopMotor();
        robotDrive.setSafetyEnabled(false);
        robotDrive.setExpiration(0.1);
        
        // Reverse motors so they all turn on the right direction to match "forward"
        // as we define it for the robot.

        robotDrive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        robotDrive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
    
        robotDrive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
        robotDrive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
     
        // calibrate the gyro if used. Takes several seconds.
        
//        gyro.initGyro();
//        gyro.setSensitivity(.007);	// Analog Devices model ADSR-S652.
//        gyro.calibrate();

   		// Start the battery, compressor and camera feed monitoring Tasks.

   		monitorBatteryThread = MonitorBattery.getInstance(ds);
   		monitorBatteryThread.start();

   		monitorCompressorThread = MonitorCompressor.getInstance();
   		monitorCompressorThread.start();

   		// Start camera server using our class for usb cameras.
      
   		cameraThread = CameraFeed.getInstance(); 
   		cameraThread.start();
   		
   		// Start thread to monitor distance sensor.
   		
   		//monitorDistanceThread = MonitorDistanceMBX.getInstance(this);
   		//monitorDistanceThread.start();
   		
   		// Create NavX object here so it has time to calibrate before we
   		// use it. Takes 10 seconds.
   		//navx = NavX.getInstance();
   		
   		Util.consoleLog("end");
    }
    catch (Throwable e) {Util.logException(e);}
  }
  
  // Called when robot is disabled.
  
  public void disabled()
  {
	  try
	  {
		  Util.consoleLog();

		  LCD.printLine(1, "Mode: Disabled");

		  // Reset driver station LEDs.

		  SmartDashboard.putBoolean("Disabled", true);
		  SmartDashboard.putBoolean("Auto Mode", false);
		  SmartDashboard.putBoolean("Teleop Mode", false);
		  SmartDashboard.putBoolean("PTO", false);
		  SmartDashboard.putBoolean("FMS", ds.isFMSAttached());
		  SmartDashboard.putBoolean("AutoTarget", false);
		  SmartDashboard.putBoolean("TargetLocked", false);
		  
		  Util.consoleLog("end");
	  }
	  catch (Throwable e) {Util.logException(e);}
  }
  
  // Called at the start of Autonomous period.
  
  public void autonomous() 
  {
      try
      {
    	  Util.consoleLog();

    	  LCD.clearAll();
    	  LCD.printLine(1, "Mode: Autonomous");
            
    	  SmartDashboard.putBoolean("Disabled", false);
    	  SmartDashboard.putBoolean("Auto Mode", true);
        
    	  // Make available the alliance (red/blue) and staring position as
    	  // set on the driver station or FMS.
        
    	  alliance = ds.getAlliance();
    	  location = ds.getLocation();

    	  // This code turns off the automatic compressor management if requested by DS.
    	  compressor.setClosedLoopControl(SmartDashboard.getBoolean("CompressorEnabled", true));

    	  PDP.clearStickyFaults();
    	  compressor.clearAllPCMStickyFaults();
    	  compressor1.clearAllPCMStickyFaults();
             
    	  // Start autonomous process contained in the Autonomous class.
        
    	  Autonomous autonomous = new Autonomous(this);
        
    	  autonomous.execute();
        
    	  autonomous.dispose();
    	  
    	  SmartDashboard.putBoolean("Auto Mode", false);
    	  Util.consoleLog("end");
      }
      catch (Throwable e) {Util.logException(e);}
  }

  // Called at the start of the teleop period.
  
  public void operatorControl() 
  {
      try
      {
    	  Util.consoleLog();

    	  LCD.clearAll();
      	  LCD.printLine(1, "Mode: Teleop");
            
      	  SmartDashboard.putBoolean("Disabled", false);
      	  SmartDashboard.putBoolean("Teleop Mode", true);
        
      	  alliance = ds.getAlliance();
      	  location = ds.getLocation();
        
          Util.consoleLog("Alliance=%s, Location=%d, FMS=%b", alliance.name(), location, ds.isFMSAttached());

          PDP.clearStickyFaults();
          compressor.clearAllPCMStickyFaults();
       	  compressor1.clearAllPCMStickyFaults();

          // This code turns off the automatic compressor management if requested by DS.
          compressor.setClosedLoopControl(SmartDashboard.getBoolean("CompressorEnabled", true));
        
          // Start operator control process contained in the Teleop class.
        
          Teleop teleOp = new Teleop(this);
       
          teleOp.OperatorControl();
        
          teleOp.dispose();
        	
          Util.consoleLog("end");
       }
       catch (Throwable e) {Util.logException(e);} 
  }
    
  public void test() 
  {
  }

  // Start usb camera server for single camera.
  
  public void StartUSBCameraServer(String cameraName, int device)
  {
	  Util.consoleLog("%s:%d", cameraName, device);

      CameraServer.getInstance().startAutomaticCapture(cameraName, device);
  }

  // Create RobotDrive object for CAN Talon controllers.
  
  private void InitializeCANTalonDrive()
  {
	  Util.consoleLog();

	  LFCanTalon = new CANTalon(1);
	  LRCanTalon = new CANTalon(2);
	  RFCanTalon = new CANTalon(3);
	  RRCanTalon = new CANTalon(4);
	  LSlaveCanTalon = new CANTalon(5);
	  RSlaveCanTalon = new CANTalon(6);
	  
	  robotDrive = new RobotDrive(LFCanTalon, LRCanTalon, RFCanTalon, RRCanTalon);

      // Initialize CAN Talons and write status to log so we can verify
      // all the talons are connected.
      InitializeCANTalon(LFCanTalon);
      InitializeCANTalon(LRCanTalon);
      InitializeCANTalon(RFCanTalon);
      InitializeCANTalon(RRCanTalon);
      InitializeCANTalon(LSlaveCanTalon);
      InitializeCANTalon(RSlaveCanTalon);
      
      // Configure slave CAN Talons to follow the front L & R Talons.
      LSlaveCanTalon.changeControlMode(TalonControlMode.Follower);
      LSlaveCanTalon.set(LFCanTalon.getDeviceID());

      RSlaveCanTalon.changeControlMode(TalonControlMode.Follower);
      RSlaveCanTalon.set(RFCanTalon.getDeviceID());
      
      // Turn on brake mode for CAN Talons.
      SetCANTalonBrakeMode(true);
  }

  // Create RobotDrive object for PWM controllers.
  
//  private void InitializePWMTalonDrive()
//  {
//	  Util.consoleLog();
//
//	  LFPwmTalon = new Talon(3);
//	  LRPwmTalon = new Talon(4);
//	  RFPwmTalon = new Talon(5);
//	  RRPwmTalon = new Talon(6);
//	 
//	  robotDrive = new RobotDrive(LFPwmTalon, LRPwmTalon, RFPwmTalon, RRPwmTalon);
//	  
//	  Util.consoleLog("end");
//  }
  
  // Initialize and Log status indication from CANTalon. If we see an exception
  // or a talon has low voltage value, it did not get recognized by the RR on start up.
  
  public void InitializeCANTalon(CANTalon talon)
  {
	  Util.consoleLog("talon init: %s   voltage=%.1f", talon.getDescription(), talon.getBusVoltage());

	  talon.clearStickyFaults();
	  talon.enableControl();
	  talon.changeControlMode(TalonControlMode.PercentVbus);
  }
  
  // Set neutral behavior of CAN Talons. True = brake mode, false = coast mode.

  public void SetCANTalonBrakeMode(boolean brakeMode)
  {
	  Util.consoleLog("brakes on=%b", brakeMode);
	  
	  LFCanTalon.enableBrakeMode(brakeMode);
	  LRCanTalon.enableBrakeMode(brakeMode);
	  RFCanTalon.enableBrakeMode(brakeMode);
	  RRCanTalon.enableBrakeMode(brakeMode);
	  LSlaveCanTalon.enableBrakeMode(brakeMode);
	  RSlaveCanTalon.enableBrakeMode(brakeMode);
  }
  
  // Set CAN Talon voltage ramp rate. Rate is volts/sec and can be 2-12v.
  
  public void SetCANTalonRampRate(double rate)
  {
	  Util.consoleLog("%f", rate);
	  
	  LFCanTalon.setVoltageRampRate(rate);
	  LRCanTalon.setVoltageRampRate(rate);
	  RFCanTalon.setVoltageRampRate(rate);
	  RRCanTalon.setVoltageRampRate(rate);
	  LSlaveCanTalon.setVoltageRampRate(rate);
	  RSlaveCanTalon.setVoltageRampRate(rate);
  }
  
  // Return voltage and current draw for each CAN Talon.
  
  public String GetCANTalonStatus()
  {
	  return String.format("%.1f/%.1f  %.1f/%.1f  %.1f/%.1f  %.1f/%.1f  %.1f/%.1f  %.1f/%.1f", 
			  LFCanTalon.getOutputVoltage(), LFCanTalon.getOutputCurrent(),
			  LRCanTalon.getOutputVoltage(), LRCanTalon.getOutputCurrent(),
			  RFCanTalon.getOutputVoltage(), RFCanTalon.getOutputCurrent(),
			  RRCanTalon.getOutputVoltage(), RRCanTalon.getOutputCurrent(),
			  LSlaveCanTalon.getOutputVoltage(), LSlaveCanTalon.getOutputCurrent(),
			  RSlaveCanTalon.getOutputVoltage(), RSlaveCanTalon.getOutputCurrent());
  }
}
