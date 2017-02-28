

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ServerSocketManager 
{
	public static final int MAXCLIENTS = 10;  // Maximum number of clients - static variable and public
    private ServerSocket serverSocket = null;  // Socket for listening to incoming connections
    private Socket[] clients = new Socket[MAXCLIENTS];  // Used to maintain up to 10 clients (i.e. Socket objects)
    private PrintWriter[] s_out = new PrintWriter[MAXCLIENTS]; // For writing to sockets
    private BufferedReader[] s_in = new BufferedReader[MAXCLIENTS]; // For reading from sockets
    private int clientCount = 0;
    
    // Constructor
    // Setup the ServerSocket object
    // Need to configure a timout of 1000 ms so that listening on the ServerSocket is not blocked
    // See the API documentation in the Java API docummentation for ServerSocket
    public ServerSocketManager(int portNumber) throws IOException
    {
        // Create Socket for listening
           serverSocket = new ServerSocket(portNumber);            
        // set timeout on the socket so the program does not
        // hang up
        serverSocket.setSoTimeout(1000);
        // Note: all entries in clients array are null - no connection.
    }
    
    // To listen on the socket.  
    // Calls accept() to check for connections, times out after 1 second (see Constructor).
    // If a call from a client is accepted (reference to Socket object returned by accept())
    // then:
    //   1) Create a BufferReader object to read from the Socket
    //   2) Create a PrintWriter object to write to the Socket
    //   3) Find a client id (to serve as an index into the three arrays) - the method 
    //      getFreeClientID() has been provided for this.
    //   4) Add the Socket, BufferedReader, and PrintWriter to the appropriate
    //      arrays (clients, s_out, s_in
    public int listenOnSocket() throws IOException
    {
    	// Reference variables for clients
    	Socket newClient;
    	PrintWriter out;
    	BufferedReader in;
    	// client identifier (index into arrays)
    	int clientid;
    	// Have we reached the limit
    	if(clientCount == MAXCLIENTS)
    	{
    		System.out.println("Maximum clients reached");
    		return(-1);
    	}
    	//System.out.println("looking for new clients");
		// trying to listen to the socket to accept
		// clients
		// if there is nobody to connect, exception will be
		// thrown - set by setSoTimeout()
    	try {
		   newClient = serverSocket.accept();  
    	}
    	catch (SocketTimeoutException e) {
    	   newClient = null;  //times out
    	}
		// connection got accepted
		if (newClient != null)
		{
			System.out.println("Connection from " +
			                   newClient.getInetAddress() + " accepted.");
			newClient.setSoTimeout(0);  // no timeout - use polling with ready() method in buffered reader.
			System.out.println("Accepted client");
			out = new PrintWriter(newClient.getOutputStream(),true); // flag true for autoflush
			in = new BufferedReader(new InputStreamReader(newClient.getInputStream()));
			// Find client id
			clientid = getFreeClientId();
			// Setup arrays for client
			clients[clientid] = newClient;
			s_out[clientid] = out;
			s_in[clientid] = in;
			clientCount++;  // increment number of connected clients
		}
		else
		{
			//System.out.println("serverSocket.accepted() returned null");
			clientid = -1;
		}		
		return(clientid);
    }
    
    // Read string from socket
    // Returns null if no input or client not connected.
    // Note that pollClients() below can be used to determine
    // which clients have input waiting.
    // Notes:
    //    1) Do check that the clients' entry is not null
    //    2) If a SocketException occurs, assume that the connection is closed
    //       and free up the entries in the arrays, and decrement clientCount.
    public String readClient(int clientid) throws IOException
    {
    	String stream = null; // input string
    	
    	try {
        	if(clients[clientid] != null)  // check that client connected
        	{
        		stream = s_in[clientid].readLine(); 
        	}          
    	}
    	catch (SocketException e) { // assume connection is closed
   		    clients[clientid] = null;
    		s_out[clientid] = null;
    		s_in[clientid] = null;
    		clientCount--;
    	}
    	return(stream);    	
    }
    
    // Poll all connected sockets 
    // Returns a client id of a connection with received data, and -1 if no data exists for any data
    // This method is provided - do consult the documentation on the ready() method (BufferedInput) to 
    // understand the method.
    public int pollClients() throws IOException
    {
    	int clientid=-1;
    	
    	// Check only clients not null
    	for(int ix = 0 ; ix < MAXCLIENTS && clientid == -1; ix++)
    	{
    		if(clients[ix] != null) // connection exists
    		{
    			if(s_in[ix].ready() == true) clientid = ix;
    		}
    	}    	
    	return(clientid);
    }
        
    // Write string to socket
    // Write to the Socket using the ReadWriter object
    public void writeClient(int clientid, String stream) throws IOException
    {    	
    	if(clients[clientid] != null)  // check that client connected
    	{
    		s_out[clientid].println(stream);  /// need line feed so that readLine() sees the line - the line feed is not delivered.
    	}
    }
    
    // Returns true if connection closed
    public boolean isClosed(int clientid)
    {
    	boolean retval = true;
    	if(clients[clientid] != null && clients[clientid].isClosed() != true)
    		retval = false;
    	return(retval);
    }
    
    // Checks for closed connections - cleans up sockets
    // Provided.
    public void closeConnections() throws IOException
    {
    	for(int ix=0; ix < MAXCLIENTS; ix++)
    	{
    		if(clients[ix] != null && clients[ix].isClosed())  // Does not work - ToDo - need to fix
    		{
    			clients[ix] = null;
        		s_out[ix] = null;
        		s_in[ix] = null;
   			    clientCount--;
    		}
    	}
    }
    
    // Finds the index in the clients array that is null
    // provided.
    private int getFreeClientId()
    {
    	int id = -1;
    	for(int ix=0 ; ix < MAXCLIENTS && id == -1; ix++)
    	{
    		if(clients[ix] == null) id = ix;
    	}
    	return(id);   	
    }
    
}
