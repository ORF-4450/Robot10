package Team4450.Lib;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * This class manages Grip run in a separate process. 
 * Starts/Stops that process and obtains Grip contour reports from 
 * that process via network tables. 
 */

public final class Grip
{
	public static final	 int			IMAGE_WIDTH = 320;
	public static final  int			IMAGE_HEIGHT = 240;
	
	private static 		 Process		gripProcess = null;
	private static final NetworkTable	gripTable = NetworkTable.getTable("GRIP/myContoursReport");
	
	// Private constructor means this class cannot be instantiated. All access is static.
	
	private Grip()
	{
		
	}
	
	public static class ContoursReport
	{
		public double	area[];
		public double	centerX[];
		public double	centerY[];
		public double	width[];
		public double	height[];
		public double	solidity[];
		
		public String toString()
		{
			String value = "";
		
			Util.consoleLog("area.length=%d", area.length);
			
			for (int i = 0; i < area.length; i++)
				value += String.format("[%d] a=%.0f x=%.0f y=%.0f w=%.0f h=%.0f s=%.4f\n", i, area[i], centerX[i], centerY[i], width[i], height[i], solidity[i]);
			
			return value;
		}
		
		public int contourCount()
		{
			return area.length;
		}
		
		public Contour getContour(int index)
		{
			Util.consoleLog("%d(%d)", index, area.length);
			
			if (area.length == 0 || index < 0 || index >= area.length) return null;
			
			Contour contour = new Contour();
			
			try
			{
    			contour.area = (int) area[index];
    			contour.centerX = (int) centerX[index];
    			contour.centerY = (int) centerY[index];
    			contour.width = (int) width[index];
    			contour.height = (int) height[index];
    			contour.solidity = solidity[index];
			}
			catch (Throwable e) {return null;};
			
			return contour; 
		}
	}
	
	public static class Contour
	{
		public int		area;
		public int		centerX;
		public int		centerY;
		public int		width;
		public int		height;
		public double	solidity;
		
		public String toString()
		{
			return String.format("a=%d x=%d y=%d w=%d h=%d s=%.4f\n", area, centerX, centerY, width, height, solidity);
		}
	}
	
	/**
	 * Launch Grip process and start it capturing images.
	 */
	public static void startGrip()
	{
		ProcessBuilder pb;
		
		Util.consoleLog();
	  
		try
		{
			pb = new ProcessBuilder("/home/lvuser/grip");
			//pb.redirectErrorStream(true);
			//pb.redirectOutput(new File("/home/lvuser/grip.log.txt"));
			
			gripProcess = pb.start();
		}
		catch (Throwable e) {e.printStackTrace(Util.logPrintStream);}
	}
  
	/**
	 * Stop Grip process.
	 */
	public static void stopGrip()
	{
		Util.consoleLog();
	  
		if (gripProcess != null) gripProcess.destroyForcibly();
	  
		gripProcess = null;
	}
	
	/**
	 * Suspend/resume Grip image capturing.
	 * @param suspend True to suspend, false to resume.
	 */
	public static void suspendGrip(boolean suspend)
	{
		Util.consoleLog("%b", suspend);
		
		gripTable.putBoolean("run", !suspend);
	}
 
	/**
	 * Gets current contours report from the data Grip posts to the network tables. 
	 * Note that this data is about .75 seconds old, that is represents what the 
	 * camera saw .75 seconds ago, not what the camera sees at the time this method
	 * is called.
	 * @return A ContoursReport object containing the list of contours (objects)
	 * found in the camera image. Grip should only return contours matching the target
	 * criteria.
	 */
	public static ContoursReport getContoursReport()
	{
		ContoursReport report = new ContoursReport();
		
		Util.consoleLog();
		
		report.area = gripTable.getNumberArray("area", new double[0]);
		report.centerX = gripTable.getNumberArray("centerX", new double[0]);
		report.centerY = gripTable.getNumberArray("centerY", new double[0]);
		report.width = gripTable.getNumberArray("width", new double[0]);
		report.height = gripTable.getNumberArray("height", new double[0]);
		report.solidity = gripTable.getNumberArray("solidity", new double[0]);
		
		return report;
	}
}
