package Team4450.Robot10;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import Team4450.Lib.CameraFeed;
import Team4450.Lib.Util;

public class Vision
{
	private static Vision	vision;
	private Robot			robot;
	private int				pegOffset = 0;
	private PegPipeline		pegPipeline = new PegPipeline();
	private	int				imageCenter = CameraFeed.imageWidth / 2;

	public Rect				targetRectangle1 = null, targetRectangle2 = null;

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
	public boolean SeekPegOffset()
	{
		int	centerX1 = 0, centerX2 = 0, pegX;
		
		Util.consoleLog();

		pegPipeline.process(robot.cameraThread.getCurrentImage());
		
		if (pegPipeline.filterContoursOutput().size() > 1)
		{
			targetRectangle1 = Imgproc.boundingRect(pegPipeline.filterContoursOutput().get(0));
			targetRectangle2 = Imgproc.boundingRect(pegPipeline.filterContoursOutput().get(1));
		}
		
		if (targetRectangle1 != null)
			Util.consoleLog("x1=%d y1=%d c=%d h=%d w=%d cnt=%d", targetRectangle1.x, targetRectangle1.y, centerX1, targetRectangle1.height,
			         targetRectangle1.width, pegPipeline.filterContoursOutput().size());

		if (targetRectangle2 != null)
			Util.consoleLog("x2=%d y2=%d c=%d h=%d w=%d cnt=%d", targetRectangle2.x, targetRectangle2.y, centerX2, targetRectangle2.height,
			         targetRectangle2.width, pegPipeline.filterContoursOutput().size());
			
		if (targetRectangle1 != null && targetRectangle2 != null)
		{
			centerX1 = targetRectangle1.x + targetRectangle1.width / 2;
			centerX2 = targetRectangle2.x + targetRectangle2.width / 2;

			if (centerX1 < centerX2)
				pegX = ((centerX2 - centerX1) / 2) + centerX1;
			else
				pegX = ((centerX1 - centerX2) / 2) + centerX2;
				
			pegOffset = imageCenter - pegX;
			
			Util.consoleLog("cX1=%d  cX2=%d  pegX=%d  pegOffset=%d", centerX1, centerX2, pegX, pegOffset);

			return true;
		}

		return false;
	}
	
	/**
	 * Return last peg offset from center of camera image.
	 * @return Peg offset from  center, - is left of center + is right of center.
	 */
	public int getPegOffset()
	{
		return pegOffset;
	}
}
