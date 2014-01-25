import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 * Network Management System
 * This is the client software of the network management system.
 * This class is responsible for sending commands and parameters for the server to respond to.
 * @author Jones Sagabaen
 */

 public class NMSClient
{
	private static boolean DEMO_MODE = false;
	
	private static boolean loggedIn = false;
	
	private static boolean admin = false;

	private static MIBClient gui;
	
	private static NMSServer nms_server;
	
	private static Socket server;
	
	private static PrintWriter out;
	
	private static BufferedReader in;

    private static Map<String,String> accounts = new HashMap<String,String>();
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
//		accounts.put("admin", "password");
//		accounts.put("user", "password");
		Scanner scan = new Scanner(System.in);
		String userInput = "", pw = "";
		System.out.println("Press the Enter key to continue...");
		userInput = scan.nextLine();
		if(userInput.equalsIgnoreCase("Enable Demo Mode"))
		{
			//System.out.println("Local network bypassed.");
			DEMO_MODE = true;
		}

		server = null;
		out = null;
        in = null;
		
		nms_server = new NMSServer();
		if(DEMO_MODE)
		{
			nms_server = new NMSServer();
			gui = new MIBClient();
			gui.runMibGui();
		}
		
		else
		{	
	        try 
	        {
	            server = new Socket("localhost", 80);
	            out = new PrintWriter(server.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(server.getInputStream()));
	        }
			catch (UnknownHostException e) 
			{
				System.err.println("Can't connect to server.");
				System.exit(1);
			}
			catch (IOException e) 
			{
	            System.err.println("Couldn't get I/O from the server.");
	            System.exit(1);
	        }
			
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			
			nms_server = null;
			gui = new MIBClient();
			gui.runMibGui();
			//Send commands through out.println(userInput)
			//Get response from in.readLine()
/*			while ((userInput = stdIn.readLine()) != null) 
			{
			    out.println(userInput);
			    System.out.println("echo: " + in.readLine());
			}
*/
			out.close();
			in.close();
			stdIn.close();
			server.close();
		}
		
	}
	
	private static String processCommand(String input) throws IOException
	{
		if(DEMO_MODE)
			return nms_server.processInput(input);	
		out.println(input);
		return in.readLine();
	}

	//When a certain button is pressed, use its listener to call the processCommand() method with a String
	//command as its parameter.  The return String will be the response it gets from the NMSServer.
	private static class MIBClient extends JPanel{

	    
		public MIBClient() {
	    	
	        super(new GridLayout(1, 0));
	        
	        //Create tree that allows one selection at a time.
	        tree = new JTree(mib);
	        
	        if (playWithLineStyle) {
	            System.out.println("line style = " + lineStyle);
	            tree.putClientProperty("JTree.lineStyle", lineStyle);
	        }

	        //Create the scroll pane and add the tree to it.
	        treeView = new JScrollPane(tree);

	        //Create the HTML viewing pane for testing purposes.
	        htmlPane = new JEditorPane();
	        htmlPane.setText("Please Login");
	        htmlPane.setEditable(false);
	        htmlView = new JScrollPane(htmlPane);

	        //Add the scroll panes to a split pane.
	        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	        splitPane.setTopComponent(treeView);
	        splitPane.setBottomComponent(htmlView);

	        Dimension minimumSize = new Dimension(600, 550);
	        htmlView.setMinimumSize(minimumSize);
	        treeView.setMinimumSize(minimumSize);
	        splitPane.setDividerLocation(400);
	        splitPane.setPreferredSize(new Dimension(600, 550));

	        //Add the split pane to this panel.
	        j = new JPanel();
	        j.add(splitPane);
	        add(j);
	    }
	    
	        public void runMibGui() {
	            //Schedule a job for the event dispatch thread:
	            //creating and showing this application's GUI.
	            javax.swing.SwingUtilities.invokeLater(new Runnable() {

	                public void run() {
	                    createAndShowGUI();
	                }
	            });
	        }
	        
	        /**
	         * Create the GUI and show it.  For thread safety,
	         * this method should be invoked from the
	         * event dispatch thread.
	         */
	        private void createAndShowGUI() {
	            if (useSystemLookAndFeel) {
	                try {
	                    UIManager.setLookAndFeel(
	                            UIManager.getSystemLookAndFeelClassName());
	                } catch (Exception e) {
	                    System.err.println("Couldn't use system look and feel.");
	                }
	            }

	    		final JMenuBar bar = new JMenuBar();
	    		
	    		//Menu bar with File and Operations
	    		JMenu file = new JMenu("File");
	    		//JMenu edit = new JMenu("Edit");
	    		JMenu oper = new JMenu("SNMP Operations");
	    		JMenu oper2 = new JMenu("RMON Operations");
	    		JMenu quit = new JMenu("Quit");
	    		
	    		//The drop-down options for File
	    		JMenuItem login = new JMenuItem("Login");
	    		load = new JMenuItem("Connect");
	    		JMenuItem save = new JMenuItem("Save MIB");
	    		JMenuItem saveAs = new JMenuItem("Save MIB As");
	    		JMenuItem pref = new JMenuItem("MIB Prefrences");
	    		JMenuItem exit = new JMenuItem("Exit");
	    		
	    		//The drop-down options for Operations
	    		get = new JMenuItem("SNMP Get");
	    		getNext = new JMenuItem("SNMP Get Next");
	    		getBulk = new JMenuItem("SNMP Get Bulk");
	    		set = new JMenuItem("SNMP Set");
	    		
	    		//The drop-down options for Operations
	    		rmonEnable = new JMenuItem("Enable RMON");
	    		rmonShow = new JMenuItem("RMON Show");
	    		rmonAlarms = new JMenuItem("RMON Alarm");
	    		rmonEvents = new JMenuItem("RMON Events");
	    		rmonQueueSize = new JMenuItem("RMON Set Queue Size");
	    		
	    		//Adding the menu options to the top bar
	    		bar.add(file);
	    		//bar.add(edit);
	    		bar.add(oper);
	    		bar.add(oper2);
	    		bar.add(quit);
	    		
	    		//Adding the drop-down options to the File
	    		file.add(login);
	    		file.add(load);
	    		//file.add(save);
	    		//file.add(saveAs);

	    		//edit.add(pref);
	    		quit.add(exit);
	    		
	    		//Adding the drop-down options to the Operations
	    		oper.add(get);
	    		oper.add(getBulk);
	    		oper.add(getNext);
	    		oper.add(set);
	    		
	    		//Adding the drop-down options to the Operations
	    		oper2.add(rmonEnable);
	    		oper2.add(rmonShow);
	    		oper2.add(rmonAlarms);
	    		oper2.add(rmonEvents);
	    		oper2.add(rmonQueueSize);
	    		
	    		load.setEnabled(false);
	    		set.setEnabled(false);
	    		getNext.setEnabled(false);
	    		getBulk.setEnabled(false);
	    		get.setEnabled(false);
	    		rmonEnable.setEnabled(false);
	    		rmonShow.setEnabled(false);
	    		rmonAlarms.setEnabled(false);
	    		rmonEvents.setEnabled(false);
	    		rmonQueueSize.setEnabled(false);
	    		
	    		//The basic frame of the window
	    		final JFrame frame = new JFrame("MIB Browser");
	    		frame.setSize(600, 550);
	    		frame.setResizable(false);
	    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    		frame.add(bar,BorderLayout.NORTH);

	    		login.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				String user = (String)JOptionPane.showInputDialog(window, 
	    						"Please enter username", "Load",
	    						JOptionPane.QUESTION_MESSAGE, null, null, 
	    						"guest");
	    				
	    				if(user.equalsIgnoreCase("admin"))
	    				{
		    				String password = (String)JOptionPane.showInputDialog(window, 
		    						"Please enter password", "Load",
		    						JOptionPane.QUESTION_MESSAGE, null, null, 
		    						"");	
		    				if(password.equals("password"))
		    				{
		    					htmlPane.setText("Login successful");
		    					admin = true;
		    					loggedIn = true;
		    					load.setEnabled(true);
		    				}
	    				}
	    				else if(user.equalsIgnoreCase("guest"))
	    				{
	    					htmlPane.setText("Logged in as guest");
	    					admin = false;
	    					loggedIn = true;
	    					load.setEnabled(true);
	    				}
	    			}
	    		});
	    		
	    		//When load is selected a pop-up will ask for the input File
	    		//I may change this to be a file browser
	    		load.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				if(loggedIn && load.isEnabled())
	    				{
	    					htmlPane.setText("Connected to the server");
		    				load();
		    				tree = new JTree(mib);

		    		        tree.getSelectionModel().setSelectionMode(
		    		                TreeSelectionModel.SINGLE_TREE_SELECTION);

		    		      //Listen for when the selection changes.
		    		        tree.addTreeSelectionListener(new TreeSelectionListener() {
		    		        public void valueChanged(TreeSelectionEvent e) {
		    		            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		    		                    tree.getLastSelectedPathComponent();

		    		            if (node == null) {
		    		                return;
		    		            }

		    		            Object nodeInfo = node.getUserObject();
		    		            // node must be a leaf and not allowed to have children
		    		            if (node.isLeaf() && !node.getAllowsChildren()) {
		    		                String[] strArray = nodeInfo.toString().split(" "); //gets name and oid
		    		                currentOidNbr = strArray[1];
		    		                htmlPane.setText(myMap.get(currentOidNbr).toString());
		    		            }
		    		        }
		    		        });
		    				treeView = new JScrollPane(tree);
		    				splitPane.setTopComponent(treeView);
		    				splitPane.setBottomComponent(htmlView);
		    				splitPane.setDividerLocation(400);
		    				j.add(splitPane);
		    				if(admin)
		    					set.setEnabled(true);
		    	    		getNext.setEnabled(true);
		    	    		getBulk.setEnabled(true);
		    	    		get.setEnabled(true);
		    	    		rmonEnable.setEnabled(true);
	    				}
	    				else
	    					htmlPane.setText("Unable to connect to the server.\nMust authenticate user first.");
	    			}
	    		});
	    		
	    		//Saves the MIB connection settings
	    		save.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				save("save");
	    			}
	    		});
	    		
	    		//Saves the MIB connection settings
	    		saveAs.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				save("SaveAs");
	    			}
	    		});
	    		
	    		//Brings a pop-up with the preferences of the MIB connection
	    		pref.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				pref();
	    			}
	    		});
	    		
	    		//Exits the MIB Browser and asks if you want to save the settings
	    		exit.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				try {
	    					exit();
	    				} 
	    				catch (SocketException e) {
	    					e.printStackTrace();
	    				}
	    			}
	    		});
	    		
	    		get.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				get();
	    			}
	    		});
	    		
	    		set.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				if(set.isEnabled()){
		    				String response = (String)JOptionPane.showInputDialog(window, 
		    						"What do you want to change OID: "+getOID()+" to?", "Set",
		    						JOptionPane.QUESTION_MESSAGE, null, null, 
		    						" "); 
		    				set(response);
	    				}
	    			}
	    		});
	    	
	    		
	    		getNext.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				getNext();
	    			}
	    		});
	    		
	    		getBulk.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				getBulk();
	    			}
	    		});
	    		
	    		rmonEnable.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				RMON();
	    			}
	    		});
	    		
	    		rmonShow.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				RMONShow();
	    			}
	    		});
	    		
	    		rmonAlarms.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				RMONAlarm();
	    			}
	    		});
	    		
	    		rmonEvents.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				RMONEvent();
	    			}
	    		});
	    		
	    		rmonQueueSize.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				if(rmonEnable.isEnabled() && rmonQueueSize.isEnabled()) {
		    				String response = (String)JOptionPane.showInputDialog(window, 
		    						"What do you want to set the queue size to be?", "Set Queue Size",
		    						JOptionPane.QUESTION_MESSAGE, null, null, 
		    						""); 
		    				RMONQueueSize(response);
		    			}
					}
	    		});
	    		
	            //Create and set up the window.
	            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	            //Add content to the window.
	            frame.add(new MIBClient());

	            //Display the window.
	            frame.setVisible(true);
	        }
	        
	        /**
	         * Processes the ROID (Root)
	         * @param name, where name is the name of the OID
	         */
	        public static void ROID(String name) {
		        mib = new DefaultMutableTreeNode(name);
		        parents = new ArrayList<DefaultMutableTreeNode>();
	        }
	        
	        /**
	         * Processes the POID (Parent)
	         * @param name, where name is the name of the OID
	         */
	        public static void POID(String name) {
	            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
	            parents.add(newNode);
	            mib.add(newNode);
	        }
	        
	        /**
	         * Processes the COID (Child)
	         * @param name, where name is the name of the OID
	         */
	        public static void COID(String name) {
	        	String[] contents = name.split(":");
	        	DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(contents[0] +" "+contents[1],false);
	        	for(int i = 0; i < parents.size(); i++) {
	        		DefaultMutableTreeNode parent = parents.get(i);
	        		String[] oid = parent.toString().split(":");
	        		if(contents[1].contains(oid[1].substring(0, oid[1].length()-1))) {
	        			mib.remove(parent);
	        			parent.add(newNode);
	        			mib.add(parent);
	        		}
	        	}
	        }
	        
	        //For now we have manual loading
	    	public static void load() {
	    			try{
	    				/*
	    				String response = (String)JOptionPane.showInputDialog(window, 
	    						"Please input the name of the file", "Load",
	    						JOptionPane.QUESTION_MESSAGE, null, null, 
	    						"File.ser"); 
	    				
	    				
	    				*/
	    				FileInputStream in = new FileInputStream("mib_data.txt");
	    				Scanner read = new Scanner(in);
	    				
	    				myMap = new HashMap<String,String>();
	    				
	    				while(read.hasNextLine()) {
	    					String j = read.nextLine();
	    					String[] myStrings = j.split(":");
	    					if(myStrings[0].equalsIgnoreCase("ROID"))
	    						ROID(myStrings[1] +":"+ myStrings[2]);
	    					if(myStrings[0].equalsIgnoreCase("POID"))
	    						POID(myStrings[1] +":"+ myStrings[2]);
	    					if(myStrings[0].equalsIgnoreCase("COID"))
	    						COID(myStrings[1] +":"+ myStrings[2]);
	    					myMap.put(myStrings[2], myStrings[3]);
	    				}
	    				
	    				read.close();
	    				in.close();
	    			}
	    			catch(Exception e) {
	    				if(e.getClass().equals(NullPointerException.class)) {System.out.println(e.toString());}
	    				else{
	    					JOptionPane.showMessageDialog(dialogFrame, "File not found",
	    							"Error", JOptionPane.ERROR_MESSAGE);
	    				}
	    			}
	    	}
	    	
	    	public static void save(String saveAs) {
	    		if(writeTo == null || saveAs.equals("SaveAs")) {
	    			try {
	    				String response = (String)JOptionPane.showInputDialog(window, 
	    						"Please input the name of the file you want to save as", 
	    						"Save As", JOptionPane.QUESTION_MESSAGE, null, null,
	    						"File.ser");
	    					
	    				writeTo = response;
	    				FileOutputStream fos = new FileOutputStream(writeTo);
	    				ObjectOutputStream oos = new ObjectOutputStream(fos);
	    				//oos.writeObject(con); //SAVES
	    				oos.close();
	    				fos.close();
	    			}
	    			catch(Exception e) {
	    				if(e.getClass().equals(NullPointerException.class)) {}
	    				else {
	    					JOptionPane.showMessageDialog(dialogFrame,
	    						e.toString(), 
	    						"Error", 
	    						JOptionPane.ERROR_MESSAGE);
	    				}
	    			}
	    		}
	    		else {
	    			try {
	    				FileOutputStream fos = new FileOutputStream(writeTo);
	    				ObjectOutputStream oos = new ObjectOutputStream(fos);
	    				//oos.writeObject(con); //SAVES
	    				oos.close();
	    				fos.close();
	    			}
	    			catch(Exception e){
	    				if(e.getClass().equals(NullPointerException.class)){}
	    				else{
	    					JOptionPane.showMessageDialog(dialogFrame,
	    						e.toString(), 
	    						"Error", 
	    						JOptionPane.ERROR_MESSAGE);
	    				}
	    			}					
	    		}
	    	}
	    	
	    	public static void pref() {
	    		final JFrame prefFrame = new JFrame("MIB Preferences");
	    		prefFrame.setSize(350, 200);
	    		prefFrame.setResizable(false);
	    		
	    		JPanel panel = new JPanel(new SpringLayout());
	    		
	    		JLabel address = new JLabel("Address: ", JLabel.TRAILING);
	    		panel.add(address);
	    		final JTextField textAddress = new JTextField();
	    		//textAddress.setText(con.getAddress().toString());
	    		address.setLabelFor(textAddress);
	    		panel.add(textAddress);
	    		
	    		JLabel port = new JLabel("Port: ", JLabel.TRAILING);
	    		panel.add(port);
	    		final JTextField textPort = new JTextField();
	    		//textPort.setText(String.valueOf(con.getPort()));
	    		port.setLabelFor(textPort);
	    		panel.add(textPort);
	    		
	    		JLabel rComm = new JLabel("Read Community: ", JLabel.TRAILING);
	    		panel.add(rComm);
	    		final JTextField textRCom = new JTextField();
	    		//textRCom.setText(con.getRCom());
	    		rComm.setLabelFor(textRCom);
	    		panel.add(textRCom);
	    		
	    		JLabel wComm = new JLabel("Write Community: ", JLabel.TRAILING);
	    		panel.add(wComm);
	    		final JTextField textWCom = new JTextField();
	    		//textWCom.setText(con.getWCom());
	    		wComm.setLabelFor(textWCom);
	    		panel.add(textWCom);
	    		
	    		JLabel Object = new JLabel("Object ID: ", JLabel.TRAILING);
	    		panel.add(Object);
	    		final JTextField textObject = new JTextField();
	    		//textObject.setText(con.getObject());
	    		Object.setLabelFor(textObject);
	    		panel.add(textObject);
	    		
	    		JButton ok = new JButton("OK");
	    		JButton cancel = new JButton("CANCEL");
	    		
	    		ok.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
//	    				try {
//	    					//con.setAddress(textAddress.getText());
//	    				} 
//	    				
//	    				catch (UnknownHostException e) {
//	   					e.printStackTrace();
//	    				}
	    				//con.setPort(textPort.getText());
	    				//con.setRCom(textRCom.getText());
	    				//con.setWCom(textWCom.getText());
	    				//con.setObject(textObject.getText());
	    				prefFrame.dispose();
	    			}
	    		});
	    		
	    		cancel.addMouseListener(new MouseAdapter() {
	    			public void mousePressed(MouseEvent event) {
	    				prefFrame.dispose();
	    			}
	    		});
	    		
	    		panel.add(ok);
	    		panel.add(cancel);

	    		prefFrame.add(panel);
	    		prefFrame.setVisible(true);
	    	}
	    	
	    	public static void exit() throws SocketException {
	    		Object[] options = {"Save and exit",
	                    "Don't save and exit",
	                    "Cancel"};
	    		int n = JOptionPane.showOptionDialog(dialogFrame,
	    				"Are you sure you want to exit?",
	    				"Exit?",
	    				JOptionPane.YES_NO_CANCEL_OPTION,
	    				JOptionPane.QUESTION_MESSAGE,
	    				null,
	    				options,
	    				options[2]);
	    		//Save and exit
	    		if(n == 0){
	    			save("save");
	    			//con.closeConnection();
	    			System.exit(1);
	    		}
	    		//Don't save and exit
	    		else if(n == 1){
	    			//con.closeConnection();
	    			System.exit(1);
	    		}
	    		//Cancel
	    		else{}
	    	}
	    	
	        /**
	         * Gets the current selected object in the tree
	         * @return objectID
	         */
	    	public static String getOID() {
	    	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
	    	    Object nodeInfo = new Object();
	    	    try {
	    	    	nodeInfo = node.getUserObject();
	    	    }
	    	    catch (Exception e) {
					JOptionPane.showMessageDialog(dialogFrame,
    						"You need to select a Object to use this option", 
    						"Error", 
    						JOptionPane.ERROR_MESSAGE);
	    	    }
	    	    String[] oid;
	    	    if(nodeInfo.toString().contains(":"))
		    	    oid = nodeInfo.toString().split(":");
	    	    else
	    	    	oid = nodeInfo.toString().split(" ");
	    	    return oid[1].substring(1,oid[1].length()-1);
	    	}
	    	
	        /**
	         * Sends the get request to the server
	         */
	    	public static void get() {
	    	    try {
					htmlPane.setText(processCommand("get:"+getOID()));
				}
	    	    catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	
	        /**
	         * Sends the getNext request to the server
	         */
	    	public static void getNext() {
	    		try {
	    			htmlPane.setText(processCommand("getNext:"+getOID()));
				}
	    		catch (IOException e) {
					e.printStackTrace();
				}
	    		
	    	}
	    	
	        /**
	         * Sends the getBulk request to the server
	         */
	    	public static void getBulk() {
	    		try {
	    			htmlPane.setText(processCommand("getBulk:"+getOID()));
	    		}
	    		catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	
	        /**
	         * Sends the set request to the server
	         * @param set, the string given by the user
	         */
	    	public static void set(String set) {
	    		try {
					htmlPane.setText(processCommand("set:"+getOID()+":"+set));
				}
	    		catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	
	    	//starts the RMON, only gather stats when enabled
	    	//make a button that is indented when rmon is on
	    	public static void RMON() {
	    		try {
	    			if(rmonEnable.getText().equals("Enable RMON")) {
	    				rmonEnable.setText("Disable RMON");
	    	    		rmonShow.setEnabled(true);
	    	    		rmonAlarms.setEnabled(true);
	    	    		rmonEvents.setEnabled(true);
	    	    		if(admin)
	    	    			rmonQueueSize.setEnabled(true);
	    				htmlPane.setText(processCommand("RMON_ON"));
	    			}
	    			else {
	    				rmonEnable.setText("Enable RMON");
	    	    		rmonShow.setEnabled(false);
	    	    		rmonAlarms.setEnabled(false);
	    	    		rmonEvents.setEnabled(false);
	    	    		rmonQueueSize.setEnabled(false);
	    				htmlPane.setText(processCommand("RMON_OFF"));
	    				
	    			}
				}
	    		catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	
	    	public static void RMONEvent() {
	    		if(rmonEvents.isEnabled()) {
		    		try {
		    			htmlPane.setText(processCommand("RMON_EVENTS"));
					}
		    		catch (IOException e) {
						e.printStackTrace();
					}	
	    		}
	    	}
	    	
	    	public static void RMONAlarm() {
	    		if(rmonEvents.isEnabled()) {
		    		try {
		    			htmlPane.setText(processCommand("RMON_ALARMS"));
					}
		    		catch (IOException e) {
						e.printStackTrace();
					}
	    		}
	    	}
	    	
	    	
	    	public static void RMONShow() {
	    		if(rmonEvents.isEnabled()) {
		    		try {
						htmlPane.setText(processCommand("RMON_SHOW"));
					}
		    		catch (IOException e) {
						e.printStackTrace();
					}
	    		}
	    	}

	    	//queue size in terms of packets
			public static void RMONQueueSize(String size) {
		    	try {
					htmlPane.setText(processCommand("RMON_QUEUE_SIZE:"+size));
				}
		    	catch (IOException e) {
					e.printStackTrace();
				}
			}
	        
	        /**
	         * Inner class to construct and display MIB Tree GUI
	         */
	        public static ArrayList<DefaultMutableTreeNode> parents;
	        public static DefaultMutableTreeNode mib;
	    	public static String writeTo; 
	    	public static JFrame window;
	    	public static JFrame dialogFrame;
	        private static JEditorPane htmlPane;
	        private static JTree tree;
	        private boolean playWithLineStyle = false;
	        private String lineStyle = "Horizontal";
	        private boolean useSystemLookAndFeel = true;
	        private String currentOidNbr;
	        private static JScrollPane treeView;
	        private static JSplitPane splitPane;
	        private static JScrollPane htmlView;
	        private static JPanel j;
	        private static JMenuItem rmonEnable;
    		private static JMenuItem rmonShow;
    		private static JMenuItem rmonAlarms;
    		private static JMenuItem rmonEvents;
    		private static JMenuItem rmonQueueSize;
    		private static JMenuItem load;
    		private static JMenuItem set;
    		private static JMenuItem get;
    		private static JMenuItem getNext;
    		private static JMenuItem getBulk;
    		private static HashMap<String, String> myMap;
		}
}
