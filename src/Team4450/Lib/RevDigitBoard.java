/*
 * Based on code from team 1493. 
 */

package Team4450.Lib;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.AnalogInput;

public class RevDigitBoard
{
	// RevDigitBoard is a Singleton class. One instance only as there is only one RevDigitBoard per robot.
	private static RevDigitBoard	instance;
	
	// I2C address of the digit board is 0x70
	private I2C i2c = new I2C(Port.kMXP, 0x70);
	
	// Buttons A and B are keyed to dgital inputs 19 and 20	
	private DigitalInput buttonA = new DigitalInput(19);
	private DigitalInput buttonB = new DigitalInput(20);
	
	// The potentiometer is keyed to AI 3	
	private AnalogInput	 pot = new AnalogInput(3);
	
	// this is the array of all characters - this is not the most efficient way to store the data - but it works for now
	byte[][] charreg = new byte[37][2]; //charreg is short for character registry
	
	// Private constructor prevents instantiation.
	private RevDigitBoard()
	{
	}

	/**
	 * Returns the common instance of RevDigitBoard.
	 * @return Instance of RevDigitBoard.
	 */
	public static RevDigitBoard getInstance()
	{
		Util.consoleLog();
		
		// Create common instance if needed.
		
		if (instance == null) instance = new RevDigitBoard();

		// Load the character registry.
		instance.charreg[0][0] = (byte)0b00000110; instance.charreg[0][1] = (byte)0b00000000; //1
		instance.charreg[1][0] = (byte)0b11011011; instance.charreg[1][1] = (byte)0b00000000; //2
		instance.charreg[2][0] = (byte)0b11001111; instance.charreg[2][1] = (byte)0b00000000; //3
		instance.charreg[3][0] = (byte)0b11100110; instance.charreg[3][1] = (byte)0b00000000; //4
		instance.charreg[4][0] = (byte)0b11101101; instance.charreg[4][1] = (byte)0b00000000; //5
		instance.charreg[5][0] = (byte)0b11111101; instance.charreg[5][1] = (byte)0b00000000; //6
		instance.charreg[6][0] = (byte)0b00000111; instance.charreg[6][1] = (byte)0b00000000; //7
		instance.charreg[7][0] = (byte)0b11111111; instance.charreg[7][1] = (byte)0b00000000; //8
		instance.charreg[8][0] = (byte)0b11101111; instance.charreg[8][1] = (byte)0b00000000; //9
		instance.charreg[9][0] = (byte)0b00111111; instance.charreg[9][1] = (byte)0b00000000; //0
		instance.charreg[10][0] = (byte)0b11110111; instance.charreg[10][1] = (byte)0b00000000; //A
		instance.charreg[11][0] = (byte)0b10001111; instance.charreg[11][1] = (byte)0b00010010; //B
		instance.charreg[12][0] = (byte)0b00111001; instance.charreg[12][1] = (byte)0b00000000; //C
		instance.charreg[13][0] = (byte)0b00001111; instance.charreg[13][1] = (byte)0b00010010; //D
		instance.charreg[14][0] = (byte)0b11111001; instance.charreg[14][1] = (byte)0b00000000; //E
		instance.charreg[15][0] = (byte)0b11110001; instance.charreg[15][1] = (byte)0b00000000; //F
		instance.charreg[16][0] = (byte)0b10111101; instance.charreg[16][1] = (byte)0b00000000; //G
		instance.charreg[17][0] = (byte)0b11110110; instance.charreg[17][1] = (byte)0b00000000; //H
		instance.charreg[18][0] = (byte)0b00001001; instance.charreg[18][1] = (byte)0b00010010; //I
		instance.charreg[19][0] = (byte)0b00011110; instance.charreg[19][1] = (byte)0b00000000; //J
		instance.charreg[20][0] = (byte)0b01110000; instance.charreg[20][1] = (byte)0b00001100; //K
		instance.charreg[21][0] = (byte)0b00111000; instance.charreg[21][1] = (byte)0b00000000; //L
		instance.charreg[22][0] = (byte)0b00110110; instance.charreg[22][1] = (byte)0b00000101; //M
		instance.charreg[23][0] = (byte)0b00110110; instance.charreg[23][1] = (byte)0b00001001; //N
		instance.charreg[24][0] = (byte)0b00111111; instance.charreg[24][1] = (byte)0b00000000; //O
		instance.charreg[25][0] = (byte)0b11110011; instance.charreg[25][1] = (byte)0b00000000; //P
		instance.charreg[26][0] = (byte)0b00111111; instance.charreg[26][1] = (byte)0b00001000; //Q
		instance.charreg[27][0] = (byte)0b11110011; instance.charreg[27][1] = (byte)0b00001000; //R
		instance.charreg[28][0] = (byte)0b10001101; instance.charreg[28][1] = (byte)0b00000001; //S
		instance.charreg[29][0] = (byte)0b00000001; instance.charreg[29][1] = (byte)0b00010010; //T
		instance.charreg[30][0] = (byte)0b00111110; instance.charreg[30][1] = (byte)0b00000000; //U
		instance.charreg[31][0] = (byte)0b00110000; instance.charreg[31][1] = (byte)0b00100100; //V
		instance.charreg[32][0] = (byte)0b00110110; instance.charreg[32][1] = (byte)0b00101000; //W
		instance.charreg[33][0] = (byte)0b00000000; instance.charreg[33][1] = (byte)0b00101101; //X
		instance.charreg[34][0] = (byte)0b00000000; instance.charreg[34][1] = (byte)0b00010101; //Y
		instance.charreg[35][0] = (byte)0b00001001; instance.charreg[35][1] = (byte)0b00100100; //Z
		instance.charreg[36][0] = (byte)0b00000000; instance.charreg[36][1] = (byte)0b00000000; //Space

		// set up the board - turn on, set blinking and brightness   
	    byte[] osc = new byte[1];
	    byte[] blink = new byte[1];
	    byte[] bright = new byte[1];
	    
	    osc[0] = (byte)0x21;
	    blink[0] = (byte)0x81;
	    bright[0] = (byte)0xEF;

	    instance.i2c.writeBulk(osc);
		Timer.delay(.01);
		
		instance.i2c.writeBulk(bright);
		Timer.delay(.01);
		
		instance.i2c.writeBulk(blink);
		Timer.delay(.01);
		
		instance.display("");
		
		return instance;
    }

	public void dispose()
	{
		Util.consoleLog();

		if (pot != null) pot.free();
		if (buttonA != null) buttonA.free();
		if (buttonB != null) buttonB.free();
		
		instance = null;
	}
	
	/**
	 * Gets push status of the A button.
	 * @return True if button is pressed.
	 */
	public boolean getButtonA()
	{
		return buttonA.get();
	}

	/**
	 * Gets push status of the B button.
	 * @return True if button is pressed.
	 */
	public boolean getButtonB()
	{
		return buttonB.get();
	}

	/**
	 * Gets current value of the potentiometer.
	 * @return Integer value of pot.
	 */
	public int getPotValue()
	{
		return pot.getValue();
	}

	/**
	 * Get the AnalogInput object representing the potentiometer.
	 * @return AnalogInput instance.
	 */
	public AnalogInput getPot()
	{
		return pot;
	}

	/**
	 * Displays test pattern of all defined characters.
	 */
    public void displayTestPattern()
	{
    	byte[] byte1 = new byte[10];

    	Util.consoleLog();
    	
    	// first reset the transmit array to zeros.
    	for(int c = 0; c < 10; c++)
    	{
    		byte1[c] = (byte)(0b00000000) & 0xFF;
    	}

    	// put single character data in the array and write to display.    	
    	for(int c = 0; c < 9; c++)
    	{
    		byte1[0] = (byte)(0b0000111100001111);
    		byte1[2] = charreg[4*c+3][0];
    		byte1[3] = charreg[4*c+3][1];
    		byte1[4] = charreg[4*c+2][0];
    		byte1[5] = charreg[4*c+2][1];
    		byte1[6] = charreg[4*c+1][0];
    		byte1[7] = charreg[4*c+1][1];
    		byte1[8] = charreg[4*c][0];
    		byte1[9] = charreg[4*c][1];
    	
    		// send the array to the board
    		i2c.writeBulk(byte1);
    		
    		Timer.delay(3);
    	}
    }

    /**
     * Display a message in the character LEDs.
     * @param message 4 character message, 0-9 A-Z.
     */
    public void display(String message)
    {
    	int		len, idx = 8;
    	byte[] 	byte1 = new byte[10];

    	message = message.toUpperCase();
    	
    	Util.consoleLog(message);
    	
    	// first reset the transmit array to zeros.
    	for(int c = 0; c < 10; c++)
    	{
    		byte1[c] = (byte)(0b00000000) & 0xFF;
    	}

    	// Set memory address command to location to store characters on revboard.
    	// Note: this sets max address depending on board automatically wrapping
    	// around to address of 1st character.
    	byte1[0] = (byte)(0b0000111100001111);

    	len = message.length();
    	
    	if (len > 4) len = 4;

    	// Process each character in the message. Use char value to index into
    	// charreg array and set the 2 bytes for each char in output array.
    	for(int c = 0; c < len; c++)
    	{
    		char ch = message.charAt(c);
    		int chidx = ch;
    		
    		if (chidx >= 65 && chidx <= 90) 		// Upper case letters.
    			chidx -= 55;
    		else if (chidx == 48)					// zero.
    			chidx = 9;
    		else if (chidx == 32)					// space.
    			chidx = 36;		
    		else if (chidx >= 49 && chidx <= 57)	// 1-9.
    			chidx -= 49;
    		
    		Util.consoleLog("c=%d i=%d chidx=%d char=%s", c, idx, chidx, ch);
    		
    		byte1[idx] = charreg[chidx][0];
    		byte1[idx+1] = charreg[chidx][1];
    		idx -= 2;
    	}
    	
    	// send the array to the board
		i2c.writeBulk(byte1);
    }
    
    /**
     * Enable/Disable character LED blink.
     * @param blink True to blink.
     */
    public void blink(boolean blink)
    {
	    byte[] buffer = new byte[1];

	    Util.consoleLog("%b", blink);

    	if (blink)
    	    buffer[0] = (byte)0x83;
    	else
    	    buffer[0] = (byte)0x81;

    	i2c.writeBulk(buffer);
    	
		Timer.delay(.01);
    }
}
