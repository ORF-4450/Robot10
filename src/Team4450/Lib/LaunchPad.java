
package Team4450.Lib;

import java.util.EventObject;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;










//import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Joystick;

// This class handles the interface to the TI Launch Pad device. It monitors
// the state of the LP controls and raises events when control state changes.
// It currently only supports buttons.

public class LaunchPad
{
	private Object			caller;
	private	Joystick		joyStick;
	private 				Set<LaunchPadEventListener> listeners = new HashSet<LaunchPadEventListener>();
	private					Set<LaunchPadControl> controls = new HashSet<LaunchPadControl>();
	private Thread			monitorLaunchPadThread;

	/**
	 * Construct JoyStick class with all controls registered for monitoring.
	 * @param joystick The JoyStick object that maps to the LaunchPad
	 * @param caller 'this' in calling class.
	 */
	public LaunchPad(Joystick	joystick, Object caller)
	{
		LaunchPadControl	control;
		
		Util.consoleLog();

		try
		{
    		this.joyStick = joystick;
    		this.caller = caller;
    		
    		// Build full set of launch pad controls and register them
    		// for monitoring.
    		
    		control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_ONE);
    		controls.add(control);
    		
    		control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_TWO);
    		controls.add(control);
    		
    		//control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_THREE);
    		//controls.add(control);
    		
    		control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_FOUR);
    		controls.add(control);
    		
    		control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_FIVE);
    		controls.add(control);
    		
    		control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_SIX);
    		controls.add(control);
    		
    		//control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_SEVEN);
    		//controls.add(control);
    		
    		control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_EIGHT);
    		controls.add(control);
    		
    		control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_NINE);
    		controls.add(control);
    		
    		//control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_TEN);
    		//controls.add(control);
    		
    		control = new LaunchPadControl(LaunchPadControlIDs.BUTTON_ELEVEN);
    		controls.add(control);

    		Start();
		}
		catch (Exception  e) {Util.logException(e);}
	}
	
	/**
	 * Construct JoyStick class with one control registered for monitoring.
	 * @param joystick The JoyStick object that maps to the LaunchPad
	 * @param controlID Launch pad control id identifying control to register.
	 * @param caller 'this' in calling class.
	 */
	public LaunchPad(Joystick joystick, LaunchPadControlIDs controlID, Object caller)
	{
		Util.consoleLog(controlID.name());

		try
		{
    		this.joyStick = joystick;
    		this.caller = caller;
    		
    		if (controlID != null) AddControl(controlID);
    	}
    	catch (Exception  e) {e.printStackTrace(Util.logPrintStream);}
	}
	
	// Add additional control to be monitored.
	
	/**
	 * Register additional control.
	 * @param controlID LaunchPad control id of control to register.
	 * @return New control added or existing control.
	 */
	public LaunchPadControl AddControl(LaunchPadControlIDs controlID)
	{
		Util.consoleLog(controlID.name());

//		try
//		{
//			if (!controls.contains(control)) controls.add(control);
//		}
//		catch (Exception  e) {e.printStackTrace(Util.logPrintStream);}

		LaunchPadControl control = FindButton(controlID);
		
		if (control == null)
		{
			control = new LaunchPadControl(controlID);
			controls.add(control);
		}

		return control;
	}
	
	/**
	 * Get a reference to a registered LaunchPad control.
	 * @param controlID Id of control to find.
	 * @return Reference to the control object or null if not found.
	 */
	public LaunchPadControl FindButton(LaunchPadControlIDs controlID)
	{
		Util.consoleLog(controlID.name());

        for (LaunchPadControl control: controls) 
        	if (control.id.value == controlID.value) return control;

        return null;
	}

	/**
	 *  Call to start LaunchPad control monitoring once all controls are added.
	 */
	public void Start()
	{
		Util.consoleLog();
		
		monitorLaunchPadThread = new MonitorLaunchPad();
		monitorLaunchPadThread.start();
	}
	
	/**
	 * Stop montioring the Launchpad controls.
	 */
	public void Stop()
	{
		Util.consoleLog();
		
		if (monitorLaunchPadThread != null) monitorLaunchPadThread.interrupt();
		
		monitorLaunchPadThread = null;
	}

	/**
	 * Release LaunchPad resources.
	 */
	public void dispose()
	{
		Util.consoleLog();
		
		if (monitorLaunchPadThread != null) monitorLaunchPadThread.interrupt();
	}

	// Launch Pad Monitor thread.
	
	private class MonitorLaunchPad extends Thread
	{
    	boolean	previousState;
    	
		MonitorLaunchPad()
		{
			Util.consoleLog();
			this.setName("MonitorLaunchPad");
	    }
	    
	    public void run()
	    {
	    	Util.consoleLog();
	    	
	    	try
	    	{
    	    	while (!isInterrupted())
    	    	{
    	    		// Loop through the set of Launch Pad controls and read the value of each
    	    		// saving the control state and raising events for change in control state.
    	    		
    	            for (LaunchPadControl control: controls) 
    	            {
    	            	if (control.controlType.equals(LaunchPad.LaunchPadControlTypes.BUTTON))
    	            	{
    	            		// Checking not because the buttons on DS are wired backwards.
        	            	if (!joyStick.getRawButton(control.id.value)) //(control.joyStickButton.value))
            				{
            					previousState = control.currentState;
            					control.currentState = true;
            					
            					if (!previousState)
            					{
            						control.latchedState = !control.latchedState;
            						
            						notifyButtonDown(control);
            					}
            				}
            				else
            				{
            					previousState = control.currentState;
            					control.currentState = false;
            					
            					if (previousState) notifyButtonUp(control);
            				}
    	            	}
    	            	
    	            	if (control.controlType.equals(LaunchPad.LaunchPadControlTypes.SWITCH))
    	            	{
        					previousState = control.currentState;
        					
        					control.currentState = joyStick.getRawButton(control.id.value); //(control.joyStickButton.value);
        					
        					control.latchedState = control.currentState;
        							
        					if (control.currentState != previousState) notifySwitchChange(control);
    	            	}
    	            }
    	            
    	            sleep(50);
    	    	}
	    	}
	    	catch (InterruptedException e) {}
	    	catch (Throwable e) {Util.logException(e);}
	    }
	}
	
	/**
	 * Get the current state of a registered control.
	 * @param requestedControl Control id to check.
	 * @return If button, True if pressed, false if not. If rocker switch,
	 * true or false depending on rocker position.
	 */
	public boolean GetCurrentState(LaunchPadControlIDs requestedControl)
	{
        for (LaunchPadControl control: controls) 
        	if (control.equals(requestedControl)) return control.currentState;
        
        return false;
	}
	
	/**
	 * For buttons, gets the latched state of a registered control. When buttons
	 * are pressed, the latch state is toggled and retained. Latched is in effect
	 * a presistent button press. Press and it latches, press again and it unlatches.
	 * @param requestedControl Control id to check.
	 * @return True if button latched, false if not.
	 */
	public boolean GetLatchedState(LaunchPadControlIDs requestedControl)
	{
        for (LaunchPadControl control: controls) 
        	if (control.id.equals(requestedControl)) return control.latchedState;
        
        return false;
	}
	
	// Event Handling classes.
	
	/**
	 *  Event description class returned to event handlers.
	 */
    public class LaunchPadEvent extends EventObject 
    {
		private static final long serialVersionUID = 1L;

		public LaunchPadControl	control;
		
		public LaunchPadEvent(Object source, LaunchPadControl control) 
		{
            super(source);
            this.control = control;
        }
    }
    
    /**
     *  Interface defintion for event listener. Actual listener implements
     *  the actions associated with button up and down or switch change events.
     */
    public interface LaunchPadEventListener extends EventListener 
    {
        public void ButtonDown(LaunchPadEvent launchPadEvent);
        
        public void ButtonUp(LaunchPadEvent launchPadEvent);
        
        public void SwitchChange(LaunchPadEvent launchPadEvent);
    }
    
    /**
     * Register a LaunchPadEventListener object to receive events.
     * @param listener LaunchPadEventListener object to receive events.
     */
    public void addLaunchPadEventListener(LaunchPadEventListener listener) 
    {
        this.listeners.add(listener);
    }
     
    /**
     * Remove the specifed LaunchPadEventListener object from event notification.
     * @param listener LaunchPadEventListener object to remove.
     */
    public void removeLaunchPadEventListener(LaunchPadEventListener listener) 
    {
        this.listeners.remove(listener);
    }  
    
    private void notifyButtonUp(LaunchPadControl control) 
    {
        for (LaunchPadEventListener launchPadEventListener: listeners) 
        {
            launchPadEventListener.ButtonUp(new LaunchPadEvent(this, control));
        }
    }
    
    private void notifyButtonDown(LaunchPadControl control) 
    {
        for (LaunchPadEventListener launchPadEventListener: listeners) 
        {
            launchPadEventListener.ButtonDown(new LaunchPadEvent(caller, control));
        }
    }
    
    private void notifySwitchChange(LaunchPadControl control) 
    {
        for (LaunchPadEventListener launchPadEventListener: listeners) 
        {
            launchPadEventListener.SwitchChange(new LaunchPadEvent(caller, control));
        }
    }
    
//    private void notifyButtonUp(int control) 
//    {
//        for (LaunchPadEventListener launchPadEventListener: listeners) 
//        {
//            launchPadEventListener.ButtonUp(new LaunchPadEvent(this, control));
//        }
//    

    /**
     * LaunchPad control type enumeration. 
     */
    public enum LaunchPadControlTypes
    {
    	BUTTON,
    	SWITCH
    };
    
    /**
    *  Control object which contains the id, type and current and latched state values of the control
    *  when contained in an event and if you directly request button state.
    */
    public class LaunchPadControl
    {
    	public  LaunchPadControlIDs		id;
    	public 	LaunchPadControlTypes	controlType = LaunchPadControlTypes.BUTTON;
        public	boolean					currentState, latchedState;
    	
        public LaunchPadControl(LaunchPadControlIDs controlID)
        {
      	  id = controlID;
        }
    }
    
    // Driver Station Launch Pad to Booster to Joystick mapping.
    // -- MSP --  Booster  --  JS button Id -- Name
    //    Left
    //    P1.6      A4              1          Trigger
    //    P3.2      A5              2          Top Back
    //    P2.7      B8              3          Top Center
    //    P4.2      A10             4          Top Left
    //    P4.1      A11             5          Top Right
    //    P3.6      A3              6          Left Front
    //    P3.5      none            7          Left Rear
    //
    //    Right
    //    P2.2      A2              8          Back Left
    //    P7.4      B1              9          Back Right
    //    P1.5      A6             10          Right Rear
    //    P1.4      B2             11          Right Front

    /**
     * LaunchPad control id enumeration.
     */
    public enum LaunchPadControlIDs
    {
        BUTTON_ONE (1),
        BUTTON_TWO (2),
        BUTTON_THREE (3),
        BUTTON_FOUR (4),
        BUTTON_FIVE (5),
        BUTTON_SIX (6),
        //BUTTON_SEVEN (7),
        BUTTON_EIGHT (8),
        BUTTON_NINE (9),
        BUTTON_TEN (10),
        BUTTON_ELEVEN (11),
        BUTTON_GREEN(1),
        BUTTON_BLUE(2),
        BUTTON_RED_RIGHT(3),
        BUTTON_BLACK(6),
        BUTTON_RED(8),
        BUTTON_BLUE_RIGHT(10),
        BUTTON_YELLOW(11),
        ROCKER_LEFT_FRONT(4),
        ROCKER_LEFT_BACK(5),
        ROCKER_RIGHT(9);
        
        private int value;

        private LaunchPadControlIDs(int value) 
        {
        	this.value = value;
        }
    };
}