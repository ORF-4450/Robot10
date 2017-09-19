/**
 * 2017 competition robot code.
 *
 * For Robot "Enterprise" built for FRC game "First STEAMWorks".
 * 
 * This version has all physical devices defined in a new static class
 * called Devices. This puts all the devices and thier port assignments
 * in one place. This means devices get created as they are accessed and
 * continue to exist until the code is stopped.
*/

package Team4450.Robot10;

import java.io.IOException;
import java.util.Properties;

import Team4450.Lib.*;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file.
 */

public class Robot extends SampleRobot 
{
  static final String  	PROGRAM_NAME = "RAC10D-09.14.17-01";

  public Properties		robotProperties;
  
  public boolean		isClone = false, isComp = false;
    	
  DriverStation.Alliance	alliance;
  int                       location;
    
  Thread               	monitorBatteryThread, monitorPDPThread;
  //MonitorDistanceMBX	monitorDistanceThread;
  MonitorCompressor		monitorCompressorThread;
  MonitorDistance		monitorDistanceThread;
  CameraFeed			cameraThread;
      
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
   		
//   		SmartDashboard.putBoolean("PIDEnabled", false);
//   		SmartDashboard.putNumber("PValue", 0);
//   		SmartDashboard.putNumber("IValue", 0);
//   		SmartDashboard.putNumber("DValue", 0);
//   		SmartDashboard.putNumber("LowSetting", 0);
//   		SmartDashboard.putNumber("HighSetting", 0);

   		// Initialize PID data entry fields on the DS to thier default values.
   		// We create an instance of Shooter class to get the default PID values set
   		// according to which robot is running this code.
   		
   		Shooter shooter = new Shooter(this);
   		
   		SmartDashboard.putBoolean("PIDEnabled", true);
   		SmartDashboard.putNumber("PValue", shooter.PVALUE);
   		SmartDashboard.putNumber("IValue", shooter.IVALUE);
   		SmartDashboard.putNumber("DValue", shooter.DVALUE);
   		SmartDashboard.putNumber("LowSetting", shooter.SHOOTER_LOW_RPM);
   		SmartDashboard.putNumber("HighSetting", shooter.SHOOTER_HIGH_RPM);
   		
   		shooter.dispose();
   		
   		// Reset PDB & PCM sticky faults.
      
   		Devices.PDP.clearStickyFaults();
   		Devices.compressor.clearAllPCMStickyFaults();
   		Devices.compressor1.clearAllPCMStickyFaults();
   		
   		// Seat unused valve.
   		Devices.unusedValve.SetA();

   		// Configure motor controllers and RobotDrive.
   		
   		Devices.InitializeCANTalonDrive();
		
   		Devices.robotDrive.stopMotor();
   		Devices.robotDrive.setSafetyEnabled(false);
   		Devices.robotDrive.setExpiration(0.1);
        
        // Reverse motors so they all turn on the right direction to match "forward"
        // as we define it for the robot.

   		Devices.robotDrive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
   		Devices.robotDrive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
    
   		Devices.robotDrive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
   		Devices.robotDrive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
     
        // calibrate the gyro if used. Takes several seconds.
        
//        Devices.gyro.initGyro();
//        Devices.gyro.setSensitivity(.007);	// Analog Devices model ADSR-S652.
//        Devices.gyro.calibrate();
   		
   		// Create NavX object here so it has time to calibrate before we
   		// use it. Takes 10 seconds. Must appear before CamerFeed is created.
   		
   		Devices.navx = NavX.getInstance(NavX.PortType.SPI);
   		
   		Devices.navx.dumpValuesToNetworkTables();

   		// Start the battery, compressor, PDP and camera feed monitoring Tasks.

   		monitorBatteryThread = MonitorBattery.getInstance(Devices.ds);
   		monitorBatteryThread.start();

   		monitorCompressorThread = MonitorCompressor.getInstance(Devices.pressureSensor);
   		monitorCompressorThread.setDelay(1.0);
   		monitorCompressorThread.SetLowPressureAlarm(50);
   		monitorCompressorThread.start();
   		
   		//monitorPDPThread = MonitorPDP.getInstance(ds, PDP);
   		//monitorPDPThread.start();

   		// Start camera server using our class for usb cameras.
      
       	cameraThread = CameraFeed.getInstance(); 
       	cameraThread.start();
   		
   		// Start thread to monitor distance sensor.
   		
   		//monitorDistanceThread = MonitorDistanceMBX.getInstance(this);
   		monitorDistanceThread = MonitorDistance.getInstance(this, Devices.distanceSensor);
   		monitorDistanceThread.start();
   		
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
		  SmartDashboard.putBoolean("FMS", Devices.ds.isFMSAttached());
		  SmartDashboard.putBoolean("AutoTarget", false);
		  SmartDashboard.putBoolean("TargetLocked", false);
		  SmartDashboard.putBoolean("BallPickupMotor", false);
		  SmartDashboard.putBoolean("ShooterMotor", false);
		  SmartDashboard.putBoolean("GearPickupMotor", false);
		  SmartDashboard.putBoolean("GearPickupDown", false);
		  SmartDashboard.putBoolean("Low", false);
		  SmartDashboard.putBoolean("High", false);
		  SmartDashboard.putBoolean("Neutral", false);
		  SmartDashboard.putBoolean("Feeder", false);
		  SmartDashboard.putBoolean("Overload", false);
		  SmartDashboard.putNumber("AirPressure", 0);
		  
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
        
    	  alliance = Devices.ds.getAlliance();
    	  location = Devices.ds.getLocation();

    	  // This code turns off the automatic compressor management if requested by DS.
    	  Devices.compressor.setClosedLoopControl(SmartDashboard.getBoolean("CompressorEnabled", true));

    	  // Reset persistent fault flags in control system modules.
    	  Devices.PDP.clearStickyFaults();
    	  Devices.compressor.clearAllPCMStickyFaults();
    	  Devices.compressor1.clearAllPCMStickyFaults();
             
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
        
      	  alliance = Devices.ds.getAlliance();
      	  location = Devices.ds.getLocation();
        
          Util.consoleLog("Alliance=%s, Location=%d, FMS=%b", alliance.name(), location, Devices.ds.isFMSAttached());

    	  // Reset persistent fault flags in control system modules.
          Devices.PDP.clearStickyFaults();
          Devices.compressor.clearAllPCMStickyFaults();
          Devices.compressor1.clearAllPCMStickyFaults();

          // This code turns off the automatic compressor management if requested by DS.
          Devices.compressor.setClosedLoopControl(SmartDashboard.getBoolean("CompressorEnabled", true));
        
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
}
