import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.io.*;

/**
 * Network Management System
 * This is the server side of the network management system.
 * This class is responsible for receiving the commands sent by the client and respond accordingly.
 * @author Jones Sagabaen
 */

 public class NMSServer 
{
	private static NESimulator ne;

    public static boolean RMON_ON, RMON_ALARM, DIE;
    
    private static AlarmMonitor monitor;
    
    public String alarm;
    
	public NMSServer()
	{
		alarm = "";
		DIE = false;
		ne = new NESimulator();
		monitor = new AlarmMonitor();
		RMON_ON = false;
		RMON_ALARM = false;
	}
	
    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException 
    {	
        ServerSocket serverSocket = null;
        try 
        {
            serverSocket = new ServerSocket(80);
        } 
        catch (IOException e) 
        {
            System.err.println("Could not listen on port.");
            System.exit(1);
        }
        
        Socket clientSocket = null;
        try 
        {
            clientSocket = serverSocket.accept();
        } 
        catch (IOException e) 
        {
            System.err.println("Accept failed.");
            System.exit(1);
        }
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String inputLine, outputLine;
        
        while ((inputLine = in.readLine()) != null) 
        {
            outputLine = processInput(inputLine);
            out.println(outputLine);
            if (outputLine.equalsIgnoreCase("exit"))
               break;
       }
       out.close();
       in.close();
       clientSocket.close();
       serverSocket.close();        
    }

	
    @SuppressWarnings("deprecation")
	public static String processInput(String input)
    {
    	String[] splitInput;
    	splitInput = input.split(":");
    	
    	if(splitInput[0].equals("get"))
    		return ne.snmpGet(splitInput[1]);
    	else if(splitInput[0].equals("getNext"))
    		return ne.snmpGetNext(splitInput[1]);
    	else if(splitInput[0].equals("getBulk"))
    		return ne.snmpGetBulk(splitInput[1]);
    	else if(splitInput[0].equals("set"))
    		return ne.snmpSet(splitInput[1], splitInput[2]);
    
    	else if(input.equals("RMON_ON"))
    	{
    		RMON_ON = true;
    		return "RMON Enabled";
    	}
    	else if(input.equals("RMON_OFF"))
    	{
    		RMON_ON = false;
    		return "RMON Disabled";
    	}
    	else if(input.equals("RMON_SHOW"))
    	{
    		if(RMON_ON)
    			return "RMON Enabled";
    		else
    			return "RMON Disabled";
    	}
    	else if(input.equals("RMON_EVENTS"))
    	{
    		if(RMON_ON)
    		{
    			ne.clearAlarm();
    			return "Alarms cleared";
    		}
    		else
    			return "RMON is currently disabled";
    		
   		}
    	else if(input.equals("RMON_ALARMS"))
    	{
    		String message = "";
   
    		if(RMON_ON)
    		{
    			RMON_ALARM = !RMON_ALARM;
    			if(RMON_ALARM)
    			{
    				DIE = false;
    				if(!monitor.isAlive())
    					monitor.start();
    				message = "Alarm Monitoring On";
    			}
    			else
    			{
    				DIE = true;
    				message = "Alarm Monitoring Off";
    			}
    		}
    		else
    			return "RMON is currently disabled";
    		
    		return message;
    	}
    	else if(splitInput[0].equals("RMON_QUEUE_SIZE"))
    	{
    		if(RMON_ON)
    		{
    			return "RMON Queue set to: " + splitInput[1];
    		}
    		else
    			return "RMON is currently disabled";
    	}
    	return null;
    }
    
    /**
     * Thread for Alerts that occur every so often
     */
    private class AlarmMonitor extends Thread 
    {
        public void run() {
            //String alarm = "";
            
            while (true) {
            	if(!DIE)
            		alarm = ne.getAlarm();
            	if(!alarm.equals("") && !DIE){
            		System.out.println(alarm);
            		ne.clearAlarm();
            	}
            	alarm = "";
            }
        }
    } 
}
