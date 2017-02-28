

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientSocketManager 
{
    // References to objects for Socket connection and
	// Reading/writing to sockets
	private Socket mySocket = null;    // Reference to the Socket for managing a client socket
    private PrintWriter s_out = null;  // The PrintWriter used to write to the socket
    private BufferedReader s_in = null;  // The BufferedReader used to read from the socket
    // For maintaining the address components of the socket addresses.
    String destIP = null;
    String myIP = null;
    int destPort = -1;
    int myPort = -1;
    
    // Constructor
    public ClientSocketManager()
    {
    	mySocket = null; // need to call connect to setup the Socket.  	
    }
    
    // Connect socket
    // Create and connect the socket using the IP/Port values given in the arguments.
    // The instance varaiables destIP, destPort, myIP and myPort are also updated.
    public void connect(String dIp, int dport) throws IOException
    {
    	mySocket = new Socket(dIp, dport); // need to create new socket for new connection  	
    	destIP = dIp;
    	destPort = dport;
    	myIP = mySocket.getLocalAddress().toString();
    	myPort = mySocket.getLocalPort();   
    	// Setup reader and writer
    	s_out = new PrintWriter(mySocket.getOutputStream(), true);  // flag true for autoflush
    	s_in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
    }
    
    // Close the connection
    // Closes the socket connection.
    public void close() throws IOException
    {
    	mySocket.shutdownInput();
    	mySocket.shutdownOutput();
    	mySocket.close();
    	destIP = null;
    	destPort = -1;
    	myPort = -1;
    }

    // Poll socket to see if data available 
    // Do consult the documentation on the ready() method (BufferedInput) to 
    // understand the method.
    public boolean poll() throws IOException
    {
    	return(s_in.ready());
    }
    
    // Read a string from connection
    // If a SocketException occurs, assume the connection is closed.
    public String read() throws IOException
    {
    	String stream;
    	try {
        	stream = s_in.readLine(); // returns null if remote end closed connection
    	}
    	catch (SocketException e) { // assume connection is closed
    	   System.out.println("readClient: SocketException");
		   close(); // close the connection
		   stream = null;
	    }   	
    	return(stream);
    }
    
    // Write a String to the connection
    public void write(String stream) throws IOException
    {
    	s_out.println(stream); // need line feed so that readLine() sees the line - the line feed is not delivered.
    }

}
