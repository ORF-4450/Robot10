
package Team4450.Lib;

import java.util.ArrayList;

import org.opencv.core.Mat;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.UsbCameraInfo;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Timer;

/**
 * USB camera feed task. Runs as a thread separate from Robot class.
 * Manages one or more usb cameras feeding their images to the 
 * CameraServer class to send to the DS.
 * We create one or more usb of our camera objects and start them capturing
 * images. We then loop on a thread getting the current image from
 * the currently selected camera and pass the image to the camera
 * server which passes the image to the driver station.
 */

public class CameraFeed extends Thread
{
	private UsbCamera			currentCamera;
	private int					currentCameraIndex;
	private ArrayList			<UsbCamera>cameras = new ArrayList<UsbCamera>();
	private Mat 				image = new Mat();
	private static CameraFeed	cameraFeed;
	private boolean				initialized;
	private MjpegServer			mjpegServer;
	private CvSink				imageSource;
	private CvSource			imageOutputStream;
	private boolean				changingCamera;
	
	// Camera settings - Static
	public static final int 	imageWidth = 320; //640;
	public static final int 	imageHeight = 240; //480;
	//public static final double 	fovH = 48.0;
	//public static final double 	fovV = 32.0;
	public static final	double	frameRate = 20;		// frames per second
	public static final int		whitebalance = 4700;	// Color temperature in K, -1 is auto
	public static final int		brightness = 50;		// 0 - 100, -1 is "do not set"
	public static final int		exposure = 50;		// 0 - 100, -1 is "auto"

	// Create single instance of this class and return that single instance to any callers.
	
	/**
	 * Get a reference to global CameraFeed object.
	 * @return Reference to global CameraFeed object.
	 */
	  
	public static CameraFeed getInstance() 
	{
		Util.consoleLog();
		
		if (cameraFeed == null) cameraFeed = new CameraFeed();
	    
	    return cameraFeed;
	}

	// Private constructor means callers must use getInstance.
	// This is the singleton class model.
	
	private CameraFeed()
	{
		UsbCameraInfo	cameraInfo, cameraList[];
		UsbCamera		camera;

		try
		{
    		Util.consoleLog();
    
    		this.setName("CameraFeed");

            // Create Mjpeg stream server.
            
            mjpegServer = CameraServer.getInstance().addServer("4450-mjpegServer", 1180);

            // Create image source.
            
            imageSource = new CvSink("4450-CvSink");
            
            // Create output image stream.
            
            imageOutputStream = new CvSource("4450-CvSource", VideoMode.PixelFormat.kMJPEG, imageWidth, imageHeight, (int) frameRate);
            
            mjpegServer.setSource(imageOutputStream);
            
            // Create cameras by getting the list of cameras detected by the RoboRio and
            // creating camera objects and storing them in an arraylist so we can switch
            // between them.
    		
    		cameraList = UsbCamera.enumerateUsbCameras();
    		
    		for(int i = 0; i < cameraList.length; ++i) 
    		{
    			cameraInfo = cameraList[i];
    			
    			Util.consoleLog("dev=%d name=%s path=%s", cameraInfo.dev, cameraInfo.name, cameraInfo.path);
    			
    			camera = new UsbCamera("cam" + cameraInfo.dev, cameraInfo.dev);
    			
    			updateCameraSettings(camera);
    			
    			cameras.add(camera);
    		}

            initialized = true;
            
            // Set starting camera.

            ChangeCamera();
		}
		catch (Throwable e) {Util.logException(e);}
	}
	
	/**
	 * Update camera with current settings fields values.
	 */
	public void updateCameraSettings(UsbCamera camera) 
	{
		Util.consoleLog();

		camera.setResolution(imageWidth, imageHeight);
		camera.setFPS((int) frameRate);
		camera.setExposureManual(exposure);
		camera.setWhiteBalanceManual(whitebalance);
		camera.setBrightness(brightness);
	}

	// Run thread to read and feed camera images. Called by Thread.start().
	
	public void run()
	{
		Util.consoleLog();
		
		if (!initialized) return;
		
		try
		{
			Util.consoleLog();

			while (!isInterrupted())
			{
				if (!changingCamera) UpdateCameraImage();
		
				Timer.delay(1 / frameRate);
			}
		}
		catch (Throwable e) {Util.logException(e);}
	}
	
	/**
	 * Get last image read from camera.
	 * @return Mat Last image from camera.
	 */
	public Mat getCurrentImage()
	{
		Util.consoleLog();
		
	    synchronized (this) 
	    {
	    	return image.clone();
	    }
	}
	
	/**
	 * Stop image feed, ie close cameras stop feed thread, release the
	 * singleton cameraFeed object.
	 */
	public void EndFeed()
	{
		if (!initialized) return;

		try
		{
    		Util.consoleLog();

    		Thread.currentThread().interrupt();
    		
    		for(int i = 0; i < cameras.size(); ++i) 
    		{
    			currentCamera = cameras.get(i);
    			currentCamera.free();
    		}
    		
    		currentCamera = null;

    		mjpegServer = null;
	
    		cameraFeed = null;
		}
		catch (Throwable e)	{Util.logException(e);}
	}
	
	/**
	 * Change the camera to get images from the other camera. 
	 */
	public void ChangeCamera()
    {
		Util.consoleLog();
		
		if (!initialized) return;
		
		changingCamera = true;
		
		if (currentCamera == null)
			currentCamera = cameras.get(currentCameraIndex);
		else
		{
			currentCameraIndex++;
			
			if (currentCameraIndex == cameras.size()) currentCameraIndex = 0;
			
			currentCamera =  cameras.get(currentCameraIndex);
		}
		
		Util.consoleLog("current=(%d) %s", currentCameraIndex, currentCamera.getName());
		
	    synchronized (this) 
	    {
	    	imageSource.setSource(currentCamera);
	    }
	    
	    changingCamera = false;
	    
	    Util.consoleLog("end");
    }
    
	// Get an image from current camera and give it to the server.
    
	private void UpdateCameraImage()
    {
		if (currentCamera != null)
		{	
		    synchronized (this) 
		    {
		    	imageSource.grabFrame(image);
		    }
		    
		    imageOutputStream.putFrame(image);
		}
    }
}
