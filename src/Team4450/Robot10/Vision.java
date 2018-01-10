package Team4450.Robot10;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import Team4450.Lib.CameraFeed;
import Team4450.Lib.Util;

public class Vision
{
	private static Vision	vision;
	private Robot			robot;
	private int				pegOffset = 0, pegDistance = 0;
	private PegPipeline		pegPipeline = new PegPipeline();
	private	int				imageCenter = CameraFeed.imageWidth / 2;
	private	static int		imageCount = 0;


	public Rect				targetRectangle1 = null, targetRectangle2 = null;

	// This is a "singleton" class. One global instance.
	
	private Vision(Robot robot)
	{
		Util.consoleLog();
		
		this.robot = robot;
	}
	
	public static Vision getInstance(Robot robot)
	{
		Util.consoleLog();
		
		if (vision == null) vision = new Vision(robot);
		
		return vision;
	}
	
	/**
	 * Seek the offset of the peg from center.
	 * @return True if offset found, false if not.
	 */
	
	boolean SeekPegOffset()
	{
		int			centerX1 = 0, centerX2 = 0, pegX;
		Mat			currentImage;
		
		Util.consoleLog();

		if (imageCount > 100) imageCount = 0;
		
		currentImage = robot.cameraThread.getCurrentImage();
		
		// Images overwrite any existing image with the same number.
		
		Imgcodecs.imwrite(String.format("/home/lvuser/image%d.jpg", imageCount), currentImage);

		pegPipeline.process(currentImage);
		
		targetRectangle1 = targetRectangle2 = null;
		
		if (pegPipeline.filterContoursOutput().size() > 1)
		{
			targetRectangle1 = Imgproc.boundingRect(pegPipeline.filterContoursOutput().get(0));
			targetRectangle2 = Imgproc.boundingRect(pegPipeline.filterContoursOutput().get(1));
		}
		else
			Util.consoleLog("no targets found  image=%d", imageCount);
		
		if (targetRectangle1 != null && targetRectangle2 != null)
		{
			centerX1 = targetRectangle1.x + targetRectangle1.width / 2;
			centerX2 = targetRectangle2.x + targetRectangle2.width / 2;

			Util.consoleLog("x1=%d y1=%d c=%d h=%d w=%d cnt=%d", targetRectangle1.x, targetRectangle1.y, centerX1, targetRectangle1.height,
			         targetRectangle1.width, pegPipeline.filterContoursOutput().size());

			Util.consoleLog("x2=%d y2=%d c=%d h=%d w=%d cnt=%d", targetRectangle2.x, targetRectangle2.y, centerX2, targetRectangle2.height,
			         targetRectangle2.width, pegPipeline.filterContoursOutput().size());
			
			// Figure out which target is the "left" one so the math is correct.
			
			if (centerX1 < centerX2)
			{
				pegX = ((centerX2 - centerX1) / 2) + centerX1;
				
				pegDistance = centerX2 - centerX1;
			}
			else
			{
				pegX = ((centerX1 - centerX2) / 2) + centerX2;
				
				pegDistance = centerX1 - centerX2;
			}
			
			// Figure out the pegX locations offset from center of image which is where
			// the robot is pointing.
			
			pegOffset = imageCenter - pegX;
			
			Util.consoleLog("cX1=%d  cX2=%d  pegX=%d  pegOffset=%d  dist=%d  image=%d", centerX1, centerX2, pegX, pegOffset, pegDistance, imageCount);

			imageCount++;
			
			return true;
		}

		imageCount++;
		
		return false;
	}
	
	/**
	 * Return last peg offset from center of camera image.
	 * @return Peg offset from center in pixels, + is left of center meaning robot is veering right.
	 * - is right of center meaning robot is veering left..
	 */
	
	public int getPegOffset()
	{
		return pegOffset;
	}
	
	/**
	 * Return measurement of distance from targets. 280 appears to be value to
	 * stop at when approaching the peg.
	 * @return Distance between peg targets measured in pixels
	 */
	
	public int getDistance()
	{
		return pegDistance;
	}
}
