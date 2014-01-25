import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Date;
import java.util.Random;

/**
 * Network Management System
 * This is the Network Element of the network management system.
 * This class is responsible for the simulation of loading, updating, sending and changing OID values.
 * @author Jones Sagabaen
 */
 
public class NESimulator {

    private HashMap<String, String> oidValues; // MIB values associated with OID key
    private ArrayList<String> writeOid;  // contains list of read-write OIDs
    private OidDataThreads oidData;  // Thread class for auto update of OID info
    private AlarmsThread alarm;  // Thread class for alarms/events
    private File sysLog;  // System Log file
    private int snmpGetTotal = 0;
    private int snmpGetNextTotal = 0;
    private int snmpSetTotal = 0;
    private long startTime;
    private String alarmMsg = "";
    
    /**
     * Constructor creates simulated Network Element
     */
    public NESimulator() {
        startTime = System.currentTimeMillis(); // current time when NE starts
        sysLog = new File("System_Log.txt");
        buildOidDataMap();
        oidData = new OidDataThreads();
        alarm = new AlarmsThread();
        oidData.start();  // start auto OID map thread
        alarm.start();  // start alarms thread
    }

    /**
     * Retrieve MIB data to included OIDs, Descriptions and tree location
     * (root, parent, child)
     * @return text file that contains MIB data
     */
    public FileReader getMibData() {
        FileReader downloadFile = null;

        try {
            downloadFile = new FileReader("mib_data.txt");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        return downloadFile;
    }

    /**
     * Returns the value associated with the SNMP OID number
     * @param oid SNMP OID number
     * @return value associated with the SNMP OID number;
     * null if OID does not exist
     */
    public String snmpGet(String oid) {
        snmpGetTotal++;  // counter for SNMP OID value

        // OID number not found
        if (!oidValues.containsKey(oid)) {
            writeSysLog("SNMP Get command called for OID " + oid +
                    " failed");
            
            return "Specified OID not found";
        }   

        writeSysLog("SNMP Get command called for OID " + oid);

        return oid + " = " + (String)oidValues.get(oid);
    }

    /**
     * Returns the next SNMP OID number in the sequence.
     * Example: if given .1.3.5.7.3 will return value associated with .1.3.5.7.4
     * or the next available value in the .1.3.5.7 sequence.
     * @param oid SNMP OID number to start search from
     * @return SNMP OID number value that is next in the sequence;
     * null if next OID does not exist
     */
    public String snmpGetNext(String oid) {
        snmpGetNextTotal++; // counter for SNMP OID value
        String nextOid = "";
        String data = "";
        int maxOidNbrs = 30;  // max number of OID numbers in any given sequence

        nextOid = increaseOid(oid);

        // searches for next OID in the OID sequence
        for (int i = 0; i < maxOidNbrs; i++) {
            
            data = (String)oidValues.get(nextOid);

            // OID key not in Map
            if (data == null) {
                nextOid = increaseOid(nextOid);
                if (nextOid.equals("none"))
                    i = maxOidNbrs;
                }
            else
                i = maxOidNbrs;
        }

        // No OID results were found
        if (nextOid.equals("none")) {
             writeSysLog("SNMP Get-Next command called for OID " + oid +
                    " failed");
            return "Error: No GetNext OID available.";
        }
            
        else {
            writeSysLog("SNMP Get-Next command called for OID " + oid +
                    ", returned OID " + nextOid);
            return nextOid + " = " + data;
        }
            
    }

    /**
     * With parent OID returns all children values of the parent
     * @param oid parent OID
     * @return all children values associated with the parent OID
     */
    public String snmpGetBulk(String oid) {
        int maxOidNbrs = 30; // max number of OID numbers in any given sequence
        String nextOid = oid.concat(".0"); // begain search at first OID segment
        String data = "";
        String result = "";

        // searches for next OID in the OID sequence
        for (int i = 0; i < maxOidNbrs; i++) {

            data = (String)oidValues.get(nextOid);

            // OID key not in Map
            if (data == null) {
                nextOid = increaseOid(nextOid);
                    if (nextOid.equals("none"))
                        i = maxOidNbrs;
                }
            else {
                result = result.concat(nextOid).concat(" = ").concat(data).
                        concat("\n");
                nextOid = increaseOid(nextOid);
                }
        }

        // No OID results were found
        if(result.equals("")) {
            writeSysLog("SNMP Get-Bulk command called for OID " + oid +
                    " failed");
            result = "GetBulk failed: there are no OID numbers below given OID";
        }
            
        writeSysLog("SNMP Get-Bulk command called for OID " + oid);

        return result;
    }

    /**
     * Allows a authorized user to set a new value into a read-write OID.
     * No check is made to ensure the new value is appropriate for the OID
     * @param oid The specified OID whose value is to be changed
     * @param newValue The new value for the OID
     * @return messages of success or failure
     */
    public String snmpSet(String oid, String newValue) {

        if (!oidValues.containsKey(oid)) {
            writeSysLog("SNMP Set command called for OID " + oid +
                " failed, OID not found");
            return "Specified OID not found";
        }

        if(!writeOid.contains(oid)) {
            writeSysLog("SNMP Set command called for OID " + oid +
                " failed, OID is read-only");
            return "Specified OID is read-only";
        }
        
        oidValues.put(oid, newValue);
        snmpSetTotal++;
        
        return "OID value set successful";
    }

    /**
     * Allows outside client to get alarm/event information.  Message nature
     * of alarm/event, network element name and date/time stamp.
     * @return alarm/event message
     */
    public String getAlarm() {

        if(!alarmMsg.equals(""))
            writeSysLog("External client pulled alarm/event message");

        return alarmMsg;
    }

    /**
     * Outside client informs the network element that the alarm/event message
     * was received by calling this method so the message can be cleared for
     * the next alarm/event.
     */
    public void clearAlarm() {

        writeSysLog("External client confirmed alarm/event message received");
        
        alarmMsg = "";
    }

    /**
     * Outside client can download System Log text file which tracks all internal
     * communication and activity for immediate and future reference.  System
     * Log file has the message along with a date/time stamp.
     * @return System Log text file
     */
    public FileReader getSystemLog() {
        FileReader downloadFile = null;

        try {
            downloadFile = new FileReader("System_Log.txt");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        writeSysLog("External client downloaded this System Log");

        return downloadFile;
    }

    /**
     * Appends new internal messages to the System Log text file to track all
     * activity.
     * @param str Message to append to the System Log text file
     */
    private void writeSysLog(String str) {

        Date dateNow = new Date();

        try {
            FileWriter fw = new FileWriter(sysLog, true);
            fw.write(DateFormat.getDateTimeInstance().format(dateNow) +
                    ": " + str + "\n\n");
            fw.close();
        } catch (IOException ex) {
            
        }
    }

    /**
     * Advances OID to its next value based on the last digit but no higher
     * than 30
     * @param oldOid the current OID
     * @return the next OID with its last digit advanced by one
     */
    private String increaseOid(String oldOid) {

        String dot = ".";
        String nextOid = "";
        oldOid = oldOid.replace('.', ':');

        String[] oidSegments = oldOid.split(":");

        int lastOidSegment =
                Integer.parseInt(oidSegments[oidSegments.length - 1]);
        lastOidSegment ++;

        if (lastOidSegment > 30)
            return "none";

        String newOidSegment = String.valueOf(lastOidSegment);

        oidSegments[oidSegments.length - 1] = newOidSegment;

        for (int i = 1; i < oidSegments.length; i++)
            nextOid = nextOid.concat(dot).concat(oidSegments[i]);

        return nextOid;
    }

    /**
     * set up the Key-Value relationships for the information of the MIB
     */
    private void buildOidDataMap() {
        oidValues = new HashMap<String, String>();
        writeOid = new ArrayList<String>();
        String[] temp;
        
        // try to read in a file.
		try {
			FileReader file = new FileReader("oid_data.txt");
			Scanner read = new Scanner(file);

			while(read.hasNextLine()) {

				temp = read.nextLine().split(":");

                // put read-wirte OIDs into list; othwise build OID maps
                if (temp[0].equals("rw"))
                    writeOid.add(temp[1]);
                else
                    oidValues.put(temp[0], temp[1]);
			}

			read.close();
            try {
                file.close();
            } catch (IOException ex) {
                
            }
		}
		catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
		}

        // makes entry into System Log that NE is up and running, giving NE name
        // and location
        writeSysLog(oidValues.get(".1.3.6.1.2.1.1.5") +
                " has begun operation at location " +
                oidValues.get(".1.3.6.1.2.1.1.6"));
    }

    /**
    * The time (in hundredth of a second) since the network management
    * portion of the system was last re-initialized.
    */
    private void sysUpTime() {

        String totalTime = "";
        long endTime = System.currentTimeMillis();
        long time1 = (endTime - startTime) / 1000;
        long time2 = (endTime - startTime) % 100;

        if (time2 <= 9)
            totalTime = time1 + "." + time2 + "0";
        else
            totalTime = time1 + "." + time2;

        oidValues.put(".1.3.6.1.2.1.1.3", totalTime);
    }

    /**
     * Counts number of datagrams received.  This method is used by the other
     * auto update methods to calculate their percentages from.
     * @return number of datagrams received
     */
    private String ipInReceives() {

        String totalDatagramNbr = "";
        long endTime = System.currentTimeMillis();

        int diffTime = (int)(endTime - startTime) / 1000;
        int totalDatagrams = diffTime * 527;    // receive 527 datagrams/second

        totalDatagramNbr = String.valueOf(totalDatagrams);

        oidValues.put(".1.3.6.1.2.1.4.3", totalDatagramNbr);

        return totalDatagramNbr;
    }

    /**
     * The number of input datagrams discarded due to
     * errors in their IP headers
     */
    private void ipHeaderErrors() {
        String headerErrors = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 2% of datagrams have header errors
        headerErrors = String.valueOf((int)(totalDatagrams * .02));
        oidValues.put(".1.3.6.1.2.1.4.4", headerErrors);
    }

    /**
     * The number of input IP datagrams for which no problems were encountered
     * to prevent their continued processing, but which were discarded
     */
    private void ipInDiscards() {

        String ipInputDiscards = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 1% of datagrams are dropped
        ipInputDiscards = String.valueOf((int)(totalDatagrams * .01));

        oidValues.put(".1.3.6.1.2.1.4.8", ipInputDiscards);
    }

    /**
     * The total number of input datagrams successfully delivered
     * to IP user-protocols
     */
    private void ipInDelivers() {

        String ipInputDeliveries = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 96% of datagrams are successful
        ipInputDeliveries = String.valueOf((int)(totalDatagrams * .97));

        oidValues.put(".1.3.6.1.2.1.4.9", ipInputDeliveries);
    }

    /**
     * The number of IP datagrams successfully reassembled
     */
    private void ipReasmOKs() {

        String ipOkReassemblies = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 94% of datagrams are successful
        ipOkReassemblies = String.valueOf((int)(totalDatagrams * .94));

        oidValues.put(".1.3.6.1.2.1.4.15", ipOkReassemblies);
    }

    /**
     * The total number of ICMP messages which the entity received
     */
    private void icmpInMsgs() {

        String icmpInMessages = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 98% of datagrams are successful
        icmpInMessages = String.valueOf((int)(totalDatagrams * .98));

        oidValues.put(".1.3.6.1.2.1.5.1", icmpInMessages);
    }

    /**
     * The number of ICMP messages which the entity received but determined as
     * having ICMP-specific errors
     */
    private void icmpInErrors() {

        String icmpInputErrors = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 7% of datagrams are successful
        icmpInputErrors = String.valueOf((int)(totalDatagrams * .07));

        oidValues.put(".1.3.6.1.2.1.5.2", icmpInputErrors);
    }

     /**
     * The number of ICMP Time Exceeded messages received
     */
    private void icmpInTimeExcds() {

        String icmpInTimeExceeds = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 6% of datagrams are successful
        icmpInTimeExceeds = String.valueOf((int)(totalDatagrams * .06));

        oidValues.put(".1.3.6.1.2.1.5.4", icmpInTimeExceeds);
    }

     /**
     * The number of ICMP Timestamp (request) messages received
     */
    private void icmpInTimestamps() {

        String icmpIncommingTimestamps = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 89% of datagrams are successful
        icmpIncommingTimestamps = String.valueOf((int)(totalDatagrams * .89));

        oidValues.put(".1.3.6.1.2.1.5.10", icmpIncommingTimestamps);
    }

    /**
     * The number of times TCP connections have made a direct transition to
     * the SYN-SENT state from the CLOSED state.
     */
    private void tcpActiveOpens() {

        String tcpConnections = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 39% of datagrams are handled by a single connection
        tcpConnections = String.valueOf((int)(totalDatagrams * .39));

        oidValues.put(".1.3.6.1.2.1.6.5", tcpConnections);
    }

    /**
     * The total number of UDP datagrams delivered to UDP users
     */
    private void udpInDatagrams() {

        String udpIncommingDatagrams = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 94% of datagrams are sent from this entity
        udpIncommingDatagrams = String.valueOf((int)(totalDatagrams * .94));

        oidValues.put(".1.3.6.1.2.1.7.1", udpIncommingDatagrams);
    }

    /**
     * The total number of UDP datagrams sent from this entity
     */
    private void udpOutDatagrams() {

        String udpDatagramNbr = "";
        int totalDatagrams = Integer.parseInt(ipInReceives());

        // 61% of datagrams are sent from this entity
        udpDatagramNbr = String.valueOf((int)(totalDatagrams * .61));

        oidValues.put(".1.3.6.1.2.1.7.4", udpDatagramNbr);
    }

    /**
     * The total number of SNMP Get-Request PDUs which have been accepted and
     * processed by the SNMP protocol entity
     */
    private void snmpInGetRequests() {

       String temp = String.valueOf(snmpGetTotal);

        oidValues.put(".1.3.6.1.2.1.11.15", temp);
    }

    /**
     * The total number of SNMP Get-Next PDUs which have been accepted and
     * processed by the SNMP protocol entity
     */
    private void snmpInGetNexts() {

       String temp = String.valueOf(snmpGetNextTotal);

        oidValues.put(".1.3.6.1.2.1.11.16", temp);
    }

    /**
     * The total number of MIB objects which have been altered successfully by
     * the SNMP protocol entity as the result of receiving valid SNMP
     * Set-Request PDUs
     */
    private void snmpInSetRequests() {

       String temp = String.valueOf(snmpSetTotal);

        oidValues.put(".1.3.6.1.2.1.11.16", temp);
    }

    /**
     * Sends an outgoing alarm
     * @param msg the text of the alarm/event message being sent
     * @return the nature of the alarm, name of network element sending the
     * alarm, date/time when alarm was sent
     */
    private void sendAlarm(String msg) {

        Date dateNow = new Date();

        alarmMsg = "Alarm: " + msg + ".\nOn Network Element " +
                oidValues.get(".1.3.6.1.2.1.1.5") + "\non " +
                DateFormat.getDateTimeInstance().format(dateNow);

        writeSysLog("Alarm/Event message generated: " + msg);
    }

    /**
     * Thread for OID items whose values automatically change over time
     */
    private class OidDataThreads extends Thread {
       
        public void run() {

            while (true) {

                sysUpTime();
                ipInReceives();
                ipHeaderErrors();
                ipInDiscards();
                ipInDelivers();
                ipReasmOKs();
                icmpInMsgs();
                icmpInErrors();
                icmpInTimeExcds();
                icmpInTimestamps();
                tcpActiveOpens();
                udpInDatagrams();
                snmpInGetNexts();
                snmpInSetRequests();
                udpOutDatagrams();
                snmpInGetRequests();

                try {
                    Thread.sleep(43);
                }
                catch (InterruptedException ie) {
                }
            }
        }
    }

    /**
     * Thread for Alerts that occur every so often
     */
    private class AlarmsThread extends Thread {

        Random rn = new Random();
        public void run() {
            int count = 0;

            while (count <= 7) {

             try {
                    // wait between 0 - 120 seconds
                    Thread.sleep(rn.nextInt(40000));
                }
                catch (InterruptedException ie) {
                }

                 if (count == 0)
                     sendAlarm("Cooling fan number 3 failure");
                 else if (count == 1)
                     sendAlarm("Port 2 down");
                 else if (count == 2)
                     sendAlarm("Excessive Ping requests");
                 else if (count == 3)
                     sendAlarm("Port 2 up");
                 else if (count == 4)
                     sendAlarm("Equipment temperature in Yellow zone");
                 else if (count == 5)
                     sendAlarm("Port 5 down");
                else if (count == 6)
                     sendAlarm("Port 5 up");
                 else
                     sendAlarm("Equipment temperature in Red zone");

                 count++;
             }
        }
    }
}