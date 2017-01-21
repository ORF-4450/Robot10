
package Team4450.Lib;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.util.logging.Level;

/**
 * Interface class to DS LCD panel.
 */

public class LCD
{
	private LCD	lcd;

	/**
	 * Get a reference to the global instance of LCD class.
	 * @return
	 */
	public LCD getInstance()
	{
	  	if (lcd == null) lcd = new LCD();
	    
	    return lcd;
	}

	private LCD()
	{
		Util.consoleLog();
		
		clearAll();
	}

	/**
	 * Release any resources held by LCD class.
	 */
	public void dispose()
	{
		Util.consoleLog();
	}

	@Deprecated
	/**
	 * Log message to the console as well as our log file.
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */
	public static void consoleLog(String message, Object... parms)
	{
		// logs to the console as well as our log file on RR disk.
		Util.logger.log(Level.INFO, String.format("robot: %s", String.format(message, parms)));
	}

	@Deprecated
	/**
	 * Log message to the console as well as our log file.
	 * @param message Message to display.
	 */
	public static  void consoleLogNoFormat(String message)
	{
		Util.logger.log(Level.INFO, String.format("robot: %s", message));
	}

	/**
	 * Print data to LCD line starting at column (no clear of line).
	 * @param line LCD line to print on (1-based).
	 * @param column Column in which to start printing (1-based).
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */

	public static void print(int line, int column, String message, Object... parms)
	{
		String	lcdLine = "";
		
		if (column < 1) column = 1;
		
		column--;	// in here, the column is zero based.
		
		switch (line)
		{
			case 1:
                lcdLine = "LCD_Line_1";
				break;

			case 2:
				lcdLine = "LCD_Line_2";
				break;

			case 3:
				lcdLine = "LCD_Line_3";
				break;

			case 4:
				lcdLine = "LCD_Line_4";
				break;

			case 5:
				lcdLine = "LCD_Line_5";
				break;

			case 6:
				lcdLine = "LCD_Line_6";
				break;

			case 7:
				lcdLine = "LCD_Line_7";
				break;

			case 8:
				lcdLine = "LCD_Line_8";
				break;

			case 9:
				lcdLine = "LCD_Line_9";
				break;

			case 10:
				lcdLine = "LCD_Line_10";
				break;
		}

		if (column > 1)
		{
			StringBuffer oldMessage = new StringBuffer(SmartDashboard.getString(lcdLine));
			String newMessage = String.format(message, parms);
			oldMessage.replace(column, newMessage.length() + column, newMessage);
			SmartDashboard.putString(lcdLine, oldMessage.toString());
		}
		else
			SmartDashboard.putString(lcdLine, String.format(message, parms));
	}

	/**
	 * Print data to LCD line (line cleared firat).
	 * @param line LCD line to print on (1-based).
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */

	public static void printLine(int line, String message, Object... parms)
	{
		clearLine(line);

		print(line, 1, message, parms);
	}

	/**
	 * Clear LCD line.
	 * @param line Line to clear (1-based).
	 */
	
	public static void clearLine(int line)
	{
		String blankLine = "                                          ";

		print(line, 1, blankLine);
	} 

	/**
	 * Clear all LCD lines.
	 */
	
	public static void clearAll()
	{
		String blankLine = "                                          ";
		
		for (int i = 1; i < 11; i++) {print(i, 1, blankLine);}
	} 
}
