/*
 * Yann Landry, 7603630
 * Jonathan Guillotte-Blouin, 7900293
 */

import java.applet.Applet;
import java.awt.Button;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.TextField;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;


public class ChatClient extends Applet implements Runnable, ActionListener, WindowListener {

	static final String STATUS_CONNECTED = "Connected to the chat server";
	static final String STATUS_DISCONNECTED = "Disconnected from the chat server";

	static final int DEFAULT_PORT = 4444;

	static ClientSocketManager connection = new ClientSocketManager();
	static boolean bConnected = false;
	static String sConnection = STATUS_DISCONNECTED;

	static String userName = null;
    static String messageBuffer = null;

    static TextField textField;
    static TextArea textArea;

	Thread thread;
	boolean threadStop;

	/*
	 * Initialize window
	 */
    public void init()
    {
		// write-a-message field
		textField = new TextField("", 50);
		this.add(textField);

		// message area
        textArea = new TextArea("No Messages", 15, 50);
		this.add(textArea);

		// connect button
		Button connectButton = new Button("Connect");
		connectButton.addActionListener(this);
		this.add(connectButton);

		// close button
        Button closeButton = new Button("Close");
		closeButton.addActionListener(this);
		this.add(closeButton);

		// send message button
        Button sendButton = new Button("Send Message");
		sendButton.addActionListener(this);
		this.add(sendButton);
    }

	/*
	 * Main event catcher
	 */
	public void actionPerformed(ActionEvent e) {
		// figure out what was clicked
		String command = e.getActionCommand();

		if(command.equals("Close")) {
			closeAction();
		}
		else if(command.equals("Connect")) {
			connectAction();
		}
		else if(command.equals("Send Message")) {
			sendMessageAction();
		}

		// update window
		this.repaint();
	}

	/*
	 * When you press the cutie little X on the window
	 */
	public void windowClosing(WindowEvent we) {
		closeAction();
	}

	public void windowClosed(WindowEvent we) {}
	public void windowDeactivated(WindowEvent we) {}
	public void windowActivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}

	/*
	 * Close action
	 */
	public void closeAction() {
		try {
			if(bConnected)
				connection.close();
		}
		catch (IOException e) {}

		System.exit(0);
	}

	/*
	 * Connect action
	 */
	public void connectAction() {
		String serverIP = "";

		try {
			int nPort = DEFAULT_PORT; // default

			// get information
			serverIP = JOptionPane.showInputDialog("Enter IP of chat server:");
			try {
				nPort = Integer.parseInt(JOptionPane.showInputDialog("Enter port number for the chat server (or nothing for default port):"));
			} catch (NumberFormatException e) {};
			userName = JOptionPane.showInputDialog("Enter user name for this session:");

			// connect to the socket
			connection.connect(serverIP, nPort);
			bConnected = true;
			connection.write(userName);

			// set screen messages
			sConnection = STATUS_CONNECTED;

			// define new thread
			thread = new Thread(this);
			thread.start();

		}

		catch (UnknownHostException e) {
			bConnected = false;
			sConnection = STATUS_DISCONNECTED;
			JOptionPane.showMessageDialog(null, "Don't know about host: " + serverIP);
		}

		catch (IOException e) {
			bConnected = false;
			sConnection = STATUS_DISCONNECTED;
			JOptionPane.showMessageDialog(null, "Server is not running!");
		}
	}

	/*
	 * Send message action
	 */
	public void sendMessageAction() {
		String message = textField.getText();

		if(message != null) {
			message = message.trim();

			if(message.length() > 0) {
				messageBuffer = textField.getText();
				textField.setText("");
			}
		}
	}

	/*
	 * Paints/updates the window according to current status
	 */
    public void paint(Graphics g)
    {
        Font font = new Font("Arial", Font.PLAIN, 12);
        Font fontb = new Font("Arial", Font.BOLD, 14);

        g.setFont(fontb);
	    g.drawString(sConnection, 60, 330);
    }

	/*
	 * where it all begins...
	 */
	public static void main(String args[]){
		String sTemp = null;

		// app
		ChatClient app = new ChatClient();
		app.init();
		app.start();

		// the window
		Frame frame = new Frame("Yann Landry | Jonathan GB - Client Chatting Program");
		frame.add("Center", app); // add app to window
		frame.addWindowListener(app);
		frame.setSize(450, 450);
		frame.setVisible(true);
	}

	/*
	 * stop thread
	 */
	public void stop() {
		threadStop = true;
	}

	/*
	 * thread method
	 */
	public void run() {
		threadStop = false;

		while(!threadStop && bConnected) { // only check server if connected
			checkServer(); // see below

			try {
				// always make your thread sleep a bit
				thread.sleep(10);
			}
			catch (InterruptedException e) {}
		}

		// check server
		sConnection = STATUS_DISCONNECTED;
		repaint();
	}

	/*
	 * read and send messages
	 */
	public static void checkServer(){
		String fromServer = null;
		String sTemp = null;
		boolean bLoop = true;

		try {
			// read from socket and do something
			// frame types: SEL, POL, ACK, NAC
			if((fromServer = connection.read()) != null) {

				// SELECT (new message?)
				if (fromServer.startsWith("SEL")) {
					fromServer = fromServer.substring(3, fromServer.length());
					sTemp = textArea.getText();

					// append message to textarea
					textArea.setText(sTemp + "\n" + fromServer);
				}

				// POLLING
				if (fromServer.startsWith("POL")) {
					// new message to be sent
					if (messageBuffer != null){
						connection.write("ACK" + userName + " says: " + messageBuffer);
						messageBuffer = null;
					}

					// no new message
					else {
						connection.write("NAC");
					}
				}

			}

			else {
				// received null -> server closed connection
				bConnected = false;
			}

		}
		catch (InterruptedIOException e) {}
		catch (IOException e) {}

	}

}
