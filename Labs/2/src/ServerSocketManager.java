

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ServerSocketManager {
	public static final int MAXCLIENTS = 10;  // Maximum number of clients - static variable and public

	private ServerSocket serverSocket = null;  // Socket for listening to incoming connections

	private Socket[] clients = new Socket[MAXCLIENTS];  // Used to maintain up to 10 clients (i.e. Socket objects)
    private PrintWriter[] writers = new PrintWriter[MAXCLIENTS]; // For writing to sockets
    private BufferedReader[] readers = new BufferedReader[MAXCLIENTS]; // For reading from sockets

	private int clientCount = 0;

    // Constructor
    // Setup the ServerSocket object
    // Need to configure a timout of 1000 ms so that listening on the ServerSocket is not blocked
    // See the API documentation in the Java API docummentation for ServerSocket
    public ServerSocketManager(int portNumber) throws IOException {
		serverSocket = new ServerSocket(portNumber);
		serverSocket.setSoTimeout(1000); // timeout 1000 ms
    }

    // To listen on the socket.
    // Calls accept() to check for connections, times out after 1 second (see Constructor).
    // If a call from a client is accepted (reference to Socket object returned by accept())
    // then:
    //   1) Create a BufferReader object to read from the Socket
    //   2) Create a PrintWriter object to write to the Socket
    //   3) Find a client id (to serve as an index into the three arrays) - the method
    //      getFreeClientId() has been provided for this.
    //   4) Add the Socket, BufferedReader, and PrintWriter to the appropriate
    //      arrays (clients, writer, reader
    public int listenOnSocket() throws IOException {
		Socket socket = null;

		// get new socket
		try {
			socket = serverSocket.accept();
		}
		catch(SocketTimeoutException e) {
			// if nothing
			return -1;
		}

		// now we have a socket so create readers and writers
		PrintWriter writer = new PrintWriter(socket.getOutputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		// get free client id, duh
		int cid = getFreeClientId();
		clients[cid] = socket;
		writers[cid] = writer;
		readers[cid] = reader;

		++clientCount;

		return cid;
    }

    // Read string from socket
    // Returns null if no input or client not connected.
    // Note that pollClients() below can be used to determine
    // which clients have input waiting.
    // Notes:
    //    1) Do check that the clients' entry is not null
    //    2) If a SocketException occurs, assume that the connection is closed
    //       and free up the entries in the arrays, and decrement clientCount.
    public String readClient(int cid) throws IOException {

		if(clients[cid] == null) {
			return null;
		}

		try {
			String line = readers[cid].readLine();

			if(line == null || line.length() == 0) {
				return null;
			}
			else {
				return line;
			}
		}

		// client is dead (gone)
		catch(SocketException e) {
			clients[cid].close();
			wipeClient(cid);

			return null;
		}
    }

    // Poll all connected sockets
    // Returns a client id of a connection with received data, and -1 if no data exists for any data
    // This method is provided - do consult the documentation on the ready() method (BufferedInput) to
    // understand the method.
    public int pollClients() throws IOException {

		// find a reader that's ready
    	for(int id = 0; id < MAXCLIENTS; ++id) {
    		if(clients[id] != null && readers[id].ready()) {
				return id;
    		}
    	}

		return -1;
    }

    // Write string to socket
    // Write to the Socket using the ReadWriter object
    // Do check that the clientid is valid
    public void writeClient(int cid, String stream) throws IOException {
		if(clients[cid] != null) {
			writers[cid].println(stream);
			writers[cid].flush();
		}
    }

    // Returns true if connection closed
    public boolean isClosed(int clientid) {
    	boolean retval = true;
    	if(clients[clientid] != null && clients[clientid].isClosed() != true)
    		retval = false;
    	return(retval);
    }

    // Checks for closed connections - cleans up sockets
    // Provided.
    public void closeConnections() throws IOException {
    	for(int id = 0; id < MAXCLIENTS; ++id) {
    		if(clients[id] != null && clients[id].isClosed()) {
    			wipeClient(id);
    		}
		}
    }

	private void wipeClient(int cid) {
		clients[cid] = null;
		writers[cid] = null;
		readers[cid] = null;

		--clientCount;
	}

    // Finds the index in the clients array that is null
    // provided.
    private int getFreeClientId() {
    	int id = -1;

    	for(int ix=0 ; ix < MAXCLIENTS && id == -1; ix++) {
    		if(clients[ix] == null) id = ix;
    	}

    	return(id);
    }

}
