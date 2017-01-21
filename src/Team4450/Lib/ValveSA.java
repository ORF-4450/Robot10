
package Team4450.Lib;

import edu.wpi.first.wpilibj.Solenoid;

/**
 * Wrapper class for Single Action pneumatic valve.
 * Single action opens against a spring. When you call open power is applied and
 * opens the valve against the spring and remains on. When close is called, power
 * is turned off and the spring closes the valve.
 */

public class ValveSA
{
	private final Solenoid valveOpenSide;

	/**
	 * @param port DIO port wired to valve. Assumes PCM with CAN Id 0.
	 */
	public ValveSA(int port)
	{
	  	Util.consoleLog("port=%d", port); 

		valveOpenSide = new Solenoid(port);
	}

	/**
	 * @param pcmCanId PCM CAN Id number.
	 * @param port DIO port wired to valve.
	 */
	public ValveSA(int pcmCanId, int port)
	{
	  	Util.consoleLog("port=%d", port); 

		valveOpenSide = new Solenoid(pcmCanId, port);
	}

	/**
	 * Release resources.
	 */
	public void dispose()
	{
		Util.consoleLog();
		
		Close();
		
		valveOpenSide.free();
	}

	/**
	 * Open the valve.
	 */
	public void Open()
	{
		Util.consoleLog();
    
		valveOpenSide.set(true);
	}

	/**
	 * Close the valve.
	 */
	public void Close()
	{
		Util.consoleLog();
    
		valveOpenSide.set(false);
	}
}