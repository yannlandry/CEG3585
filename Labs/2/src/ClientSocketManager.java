/*
 * Yann Landry, 7603630
 * Jonathan Guillotte-Blouin, 7900293
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientSocketManager {

    // References to objects for Socket connection and
	// Reading/writing to sockets
	private Socket socket = null;    // Reference to the Socket for managing a client socket
    private PrintWriter writer = null;  // The PrintWriter used to write to the socket
    private BufferedReader reader = null;  // The BufferedReader used to read from the socket
    // For maintaining the address components of the socket addresses.
    String localIP = null;
    int localPort = -1;

    String destIP = null;
    int destPort = -1;

    // Constructor
    public ClientSocketManager() {
    	socket = null; // need to call connect to setup the Socket.
    }

    // Connect socket
    // Create and connect the socket using the IP/Port values given in the arguments.
    // The instance varaiables destIP, destPort, localIP and localPort are also updated.
    public void connect(String dIP, int dPort) throws IOException {
		this.destIP = dIP;
		this.destPort = dPort;

		socket = new Socket(destIP, destPort);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream());
    }

    // Close the connection
    // Closes the socket connection.
    public void close() throws IOException {
		socket.close();
		socket = null;
    }

    // Read a string from connection
    // If a SocketException occurs, assume the connection is closed.
    public String read() throws IOException {
		try {
			String line = reader.readLine();
			return line;
		}
		catch(SocketException e) {
			return null;
		}
    }

    // Write a String to the connection
    public void write(String stream) throws IOException {
		writer.println(stream);
		writer.flush();
    }

}
