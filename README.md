JavaNetworkSimulatorApp
=======================

A Network Management System simulator application that primarily contains a client and a server.

Execution of this application requires two PCs to communication with one another across the same network. 
The NMSServer.java class will act as the server side of the system.  The simulation logic (NESimulator.java) will 
also be handled on this end of the system.  The NMSClient.java class will act as the client software application 
that requests to communicate with the server.  

After launching the client software, it must first establish a connection with the server application 
through a handshake in order for data packets to be sent to and from one another.  This is accomplished through 
socket programming.  The client software then has a set of commands available to request for the server to 
respond to.  So the responsibility of the server application is to process the command recieved by the client 
and respond accordingly.  The main function of the simulator class is to run a process over time that will perform 
operations which include processing OID numbers and randomly generating alerts.
