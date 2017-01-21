
package Team4450.Lib;

import java.util.EventObject;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import edu.wpi.first.wpilibj.Joystick;

/**
* This class handles the interface to the GamePad buttons. It monitors
* the state of the GP buttons and raises events when button state changes.
*/

public class GamePad
{
	private final Object	caller;
	private final Joystick	joyStick;
	private 				Set<GamePadEventListener> listeners = new HashSet<GamePadEventListener>();
	private					Set<GamePadButton> buttons = new HashSet<GamePadButton>();
	private Thread			monitorGamePadThread;
	private String			gamePadName = "";
	
	private static GamePadButton	povButton;
	
	public  double			deadZone = 0.1;
	
	/**
	 * Constructor which adds all GamePad buttons to be monitored.
	 * @param joyStick JoyStick object representing the GamePad.
	 * @param name Identifying name for the pad object.
	 * @param caller calling class instance (use 'this').
	 */
	
	public GamePad(Joystick joyStick, String name, Object caller)
	{
		GamePadButton	button;
		
		Util.consoleLog("%s (all buttons)", name);

		this.joyStick = joyStick;
		gamePadName = name;
		this.caller = caller;
		
		// Add all the gamepad buttons to be monitored.
		
		button = new GamePadButton(GamePadButtonIDs.A);
		buttons.add(button);
		
		button = new GamePadButton(GamePadButtonIDs.B);
		buttons.add(button);
		
		button = new GamePadButton(GamePadButtonIDs.X);
		buttons.add(button);
		
		button = new GamePadButton(GamePadButtonIDs.Y);
		buttons.add(button);
		
		button = new GamePadButton(GamePadButtonIDs.LEFT_BUMPER);
		buttons.add(button);

		button = new GamePadButton(GamePadButtonIDs.RIGHT_BUMPER);
		buttons.add(button);
		
		button = new GamePadButton(GamePadButtonIDs.BACK);
		buttons.add(button);
		
		button = new GamePadButton(GamePadButtonIDs.START);
		buttons.add(button);
		
		button = new GamePadButton(GamePadButtonIDs.POV);
		buttons.add(button);

		Start();
	}
	
	/**
	 * Constructor which adds single button to be monitored.
	 * @param joyStick JoyStick object representing the GamePad.
	 * @param name Identifying name for the pad object.
	 * @param button Enum value identifying button to add.
	 * @param caller Calling class instance (use 'this').
	 */
	
	GamePad(Joystick joyStick, String name, GamePadButtonIDs	button, Object caller)
	{
		Util.consoleLog("%s (single button)", name);

		this.joyStick = joyStick;
		gamePadName = name;
		this.caller = caller;
		
		AddButton(button);
	}
	
	/**
	 * Add additonal GamePadButton button to be monitored.
	 * @param button id value identifying button to add.
	 * @return New button added or existing button.
	 */

	public GamePadButton AddButton(GamePadButtonIDs button)
	{
		Util.consoleLog("%s (%s)", gamePadName, button.name());
	
		GamePadButton gpButton = FindButton(button);
		
		if (gpButton == null)
		{
			gpButton = new GamePadButton(button);
			buttons.add(gpButton);
		}

		return gpButton;
	}
	
	/**
	 * Find GamePadButton button by id in the list of registered buttons.
	 * @param buttonID Enum value identifying button to find.
	 * @return Button or null if not found.
	 */
	
	public GamePadButton FindButton(GamePadButtonIDs button)
	{
		Util.consoleLog("%s (%s)", gamePadName, button.name());

        for (GamePadButton gpButton: buttons) 
        	if (gpButton.id.value == button.value) return gpButton;

        return null;
	}
	
	/**
	 *  Call to start GamePad button monitoring once all buttons are added.
	 */
	
	public void Start()
	{
		Util.consoleLog(gamePadName);

		monitorGamePadThread = new MonitorGamePad();
		monitorGamePadThread.start();
	}

	/**
	 * Call to stop monitoring GamePad buttons.
	 */
	public void Stop()
	{
		Util.consoleLog(gamePadName);

		if (monitorGamePadThread != null) monitorGamePadThread.interrupt();
		
		monitorGamePadThread = null;
	}
	
	/**
	 * Release GamePad resources.
	 */
	public void dispose()
	{
		Util.consoleLog(gamePadName);
		
		if (monitorGamePadThread != null) monitorGamePadThread.interrupt();
	}

	/**
	 * Get left joystick X value.
	 * @return X axis deflection value.
	 */
	public double GetLeftX()
	{
		double x;
		
		x = joyStick.getRawAxis(0);
		if (Math.abs(x) < deadZone) x = 0.0;
		return x;
	}
	
	/**
	 * Get left joystick Y value.
	 * @return Y axis deflection value.
	 */
	public double GetLeftY()
	{
		double y;
		
		y = joyStick.getRawAxis(1);
		if (Math.abs(y) < deadZone) y = 0.0;
		return y;
	}

	/**
	 * Get right joystick X value.
	 * @return X axis deflection value.
	 */
	public double GetRightX()
	{
		double x;
		
		x = joyStick.getRawAxis(4);
		if (Math.abs(x) < deadZone) x = 0.0;
		return x;
	}
	
	/**
	 * Get rightt joystick Y value.
	 * @return Y axis deflection value.
	 */
	public double GetRightY()
	{
		double y;
		
		y = joyStick.getRawAxis(5);
		if (Math.abs(y) < deadZone) y = 0.0;
		return y;
	}

	/**
	 * Get right trigger deflection value.
	 * @return Trigger deflection value.
	 */
	public double GetRightTrigger()
	{
		double x;
		
		x = joyStick.getRawAxis(2);
		if (Math.abs(x) < deadZone) x = 0.0;
		return x;
	}
	
	/**
	 * Get left trigger deflection value.
	 * @return Trigger deflection value.
	 */
	public double GetLeftTrigger()
	{
		double y;
		
		y = joyStick.getRawAxis(3);
		if (Math.abs(y) < deadZone) y = 0.0;
		return y;
	}

	/**
	 * Get the actual instantaneous POV angle value directly from the GamePad.
	 * @return Angle in degrees.
	 */
	public int GetPOVAngle()
	{
		return joyStick.getPOV();
	}
	
	/**
	 * Get the last set POV angle. Only works if POV button is being monitored.
	 * @return Last POV angle in degrees or -1 if not set or button not defined.
	 */
	public int GetLastPOVangle()
	{
		if (povButton == null) povButton = FindButton(GamePadButtonIDs.POV);
		
		if (povButton != null) return povButton.lastPOVAngle;

		return -1;
	}

	// GamePad Button Monitor thread.
	
	private class MonitorGamePad extends Thread
	{
		boolean previousState;
		int		previousPOVAngle;
		
		MonitorGamePad()
		{
			this.setName("Monitor " + gamePadName);
	    }
		
	    public void run()
	    {
	    	Util.consoleLog(gamePadName);
	    	
	    	try
	    	{
    	    	while (!isInterrupted())
    	    	{
    	    		// Loop through the set of Game Pad buttons and read the value of each
    	    		// saving the button state and raising events for change in button state.
    	    		
    	            for (GamePadButton button: buttons) 
    	            {
    	            	if (button.id == GamePadButtonIDs.POV)
    	            	{
    	            		previousPOVAngle = button.povAngle;
    	            	
    	            		button.povAngle = joyStick.getPOV();
    	            	
     	            		if (button.povAngle != previousPOVAngle) notifyButtonDown(button);
     	            		
     	            		if (button.povAngle != -1) button.lastPOVAngle = button.povAngle;
    	            	}
    	            	else
    	            	{
              	    		if (joyStick.getRawButton(button.id.value))
              				{
              					previousState = button.currentState;
              					button.currentState = true;
              					
              					//if (button.id.equals(GamePadButtons.TOP_MIDDLE)) LCD.consoleLog("button=%s-%s true", joyStickName, button.id.name());
              						
              					if (!previousState)
              					{
              						button.latchedState = !button.latchedState;
              						
              						notifyButtonDown(button);
              					}
              				}
              				else
              				{
              					previousState = button.currentState;
              					button.currentState = false;
              					
              					//if (button.equals(GamePadButtons.TOP_MIDDLE)) LCD.consoleLog("button=%s-%s false", joyStickName, button.id.name());
              					
              					if (previousState) notifyButtonUp(button);
              				}
    	            	}
    	            }
    	            
    	            sleep(50);
    	    	}
	    	}
	    	catch (InterruptedException e) {}
	    	catch (Throwable e) {Util.logException(e);}
	    }
	}	// end of MonitorJoystick thread class.

	/**
	 * Get the current state of the button.
	 * @param button Enum identifying button.
	 * @return True if pressed.
	 */
	public boolean GetCurrentState(GamePadButtonIDs button)
	{
      for (GamePadButton gpButton: buttons) 
      	if (gpButton.id.equals(button)) return gpButton.currentState;
      
      return false;
	}
	
	/**
	 * Get the latched state of the button. This is toggled by each button press.
	 * @param button Enum identifying button.
	 * @return True or false as set by last button press.
	 */
	public boolean GetLatchedState(GamePadButtonIDs button)
	{
      for (GamePadButton gpButton: buttons) 
      	if (gpButton.id.equals(button)) return gpButton.latchedState;
      
      return false;
	}
	
  // Event Handling classes.
	
  /**
   *  Event description class returned to event handlers.
   */
  public class GamePadEvent extends EventObject 
  {
	  private static final long serialVersionUID = 1L;

	  public GamePadButton	button;
	
	  public GamePadEvent(Object source, GamePadButton button) 
	  {
		  super(source);
		  this.button = button;
	  }
  }
  
  /**
   *  Interface defintion for event listener. Actual listener implements
   *  the actions associated with button up and down events.
   */
  public interface GamePadEventListener extends EventListener 
  {
      public void ButtonDown(GamePadEvent GamePadEvent);
      
      public void ButtonUp(GamePadEvent GamePadEvent);
  }
  
  /**
   * Add (register) an event listener.
   * @param listener function to register
   */
  public void addGamePadEventListener(GamePadEventListener listener) 
  {
      this.listeners.add(listener);
  }
   
  /**
   * Remove an event listener registration.
   * @param listener function to unregister
   */
  public void removeGamePadEventListener(GamePadEventListener listener) 
  {
      this.listeners.remove(listener);
  }  
  
  // Notify all registered handlers of button up event.
  
  private void notifyButtonUp(GamePadButton button) 
  {
      for (GamePadEventListener GamePadEventListener: listeners) 
      {
          GamePadEventListener.ButtonUp(new GamePadEvent(caller, button));
      }
  }
  
  // Notify all registered handlers of button down event.
  
  private void notifyButtonDown(GamePadButton button) 
  {
      for (GamePadEventListener GamePadEventListener: listeners) 
      {
          GamePadEventListener.ButtonDown(new GamePadEvent(caller, button));
      }
  }

  /**
  *  Button object which contains button id and current and latched state and POV value of the button
  *  when contained in an event and if you directly request button state.
  */
  public class GamePadButton
  {
	  public GamePadButtonIDs	id;
      public boolean		currentState, latchedState;
      public int			povAngle = -1, lastPOVAngle;
  
      public GamePadButton(GamePadButtonIDs buttonID)
      {
    	  id = buttonID;
      }
  }
  
  /**
   *  Gamepad button id enumeration. 
   */
  public enum GamePadButtonIDs
  {
      A (1),
      B (2),
      X (3),
      Y (4),
      LEFT_BUMPER (5),
      RIGHT_BUMPER (6),
      BACK (7),
      START (8),
      POV (9);
      
      private int value;
      
      private GamePadButtonIDs(int value) 
      {
    	  this.value = value;
      }
  }
}

