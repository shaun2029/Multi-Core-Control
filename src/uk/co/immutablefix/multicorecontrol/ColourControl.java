// Copyright 2013 Shaun Simpson shauns2029@gmail.com

package uk.co.immutablefix.multicorecontrol;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

public class ColourControl {
    private String output;
    
    public int[] getColours() throws Exception {
    	String[] strColours = null;
		output = "";
		String settingsPath = "";
			
		if (RootTools.exists("/sys/class/misc/colorcontrol/multiplier")) {
			settingsPath = "/sys/class/misc/colorcontrol/multiplier";
		} else {
			throw new Exception("Failed to find colour settings, unsupported kernel.");
		}
			
		CommandCapture command = new CommandCapture(0, 
				"chmod 777 " + settingsPath);

		Command command2 = new Command(0, "cat " + settingsPath)
		{
		        @Override
		        public void output(int id, String line)
		        {
		        	output += line + " ";
		        }
				@Override
				public void commandCompleted(int arg0, int arg1) {}
				@Override
				public void commandOutput(int arg0, String arg1) {}
				@Override
				public void commandTerminated(int arg0, String arg1) {}
		};
		
		RootTools.getShell(true).add(command);
		commandWait(command);
		RootTools.getShell(true).add(command2);
		commandWait(command2);

		// Parse colour values;
		output = output.replaceAll("[:]", " ");
		strColours = output.split("[ ]+");
		
		if (strColours.length != 3) {
			throw new Exception("Failed to read colours, unsupported kernel.");
		}
		
		int[] colours = new int[strColours.length];
		
		for (int i=0; i<colours.length; i++){
			colours[i] = Integer.parseInt(strColours[i]);
		}
		
		return colours;
	}
	
	
	// Colours max 1000.
	public void setColours(int[] colours) throws Exception {
		String settingsPath;

		// Check number of parameters
		if (colours.length != 3){
			throw new Exception("Failed to set colours, incorrect number of parameters.");
		}

		// Check value of parameters
		for(int i=0; i<colours.length; i++)
		{
			if ((colours[i] < 0) || (colours[i] > 2000000000)){
				throw new Exception("Failed to set colours, incorrect parameters.");
			}
		}
		
		if (RootTools.exists("/sys/class/misc/colorcontrol/multiplier")) {
			settingsPath = "/sys/class/misc/colorcontrol/multiplier";
/*
			// Scale settings.
			for(int i=0; i<colours.length; i++)
			{
				colours[i] = colours[i]/1000 * 2000000000;
			}
*/			
		} else {
			throw new Exception("Failed to find colour settings, unsupported kernel.");
		}

		Shell shell = RootTools.getShell(true);
		
		CommandCapture command = new CommandCapture(0, "chmod 777 " + settingsPath);
		shell.add(command);
		commandWait(command);

		try {
			StringBuilder c = new StringBuilder();
			
			for(int i=0; i<colours.length; i++)
			{
				c.append(colours[i] + " ");
			}
			
			CommandCapture command2 = new CommandCapture(0, "echo " + c.toString() + " > " + settingsPath);

			shell.add(command2);
			commandWait(command2);
		} catch (IOException e) {
			throw new Exception("Error setting CPU voltages, IO exception.");
		} catch (TimeoutException e) {
			throw new Exception("Error setting CPU voltages, timeout.");
		} catch (RootDeniedException e) {
			throw new Exception("Error setting CPU voltages, failed to get root permissions.");
		} catch (Exception e) {
			throw new Exception("Error setting CPU voltages, general  exception.");
		}
	}

	public void setColours(String strColours) throws Exception {
		String[] c = strColours.split("[ ]+");
		int[] colours = new int[c.length];
		
		for (int i=0; i<colours.length; i++){
			colours[i] = Integer.parseInt(c[i]);
		}
			
		setColours(colours);
	}
	
	protected void commandWait(Command cmd) throws Exception {
        while (!cmd.isFinished()) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
}