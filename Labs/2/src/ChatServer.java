

import java.io.IOException;

public class ChatServer {

    private static final int DEFAULT_PORT = 4444;

    private static String[] messages = new String[ServerSocketManager.MAXCLIENTS];
    private static int messageCount = 0;

    private static ServerSocketManager manager = null;

    public static void main(String[] args) throws IOException {
        // get port number
        int port = getPort(args);

        // Create the server socket manager
        manager = new ServerSocketManager(port);

        // start loop
        while(loop());
    }

    private static int getPort(String[] args) {
        if(args.length > 0 && args[0] != null) {
            try {
                int port = Integer.parseInt(args[0]);
                if(port > 0) {
                    return port;
                }
            }
            catch(NumberFormatException e) {}
        }

        return DEFAULT_PORT;
    }

    private static boolean loop() throws IOException {

        acceptNewConnections();

        pollForMessages();

        checkResponses();

        distributeMessages();

        manager.closeConnections();

        return true;
        // true to loop again
        // i guess there used to be a stopping mechanism
        // in that case to stop just return false
    }

    private static void acceptNewConnections() throws IOException {
        int newClientId = manager.listenOnSocket(); // times out

        if(newClientId != -1) {
            // expect name to be received
            messages[messageCount++] = manager.readClient(newClientId) + " joined";
        }
    }

    private static void pollForMessages() throws IOException {
        for(int id = 0; id < ServerSocketManager.MAXCLIENTS; ++id) {
            if(!manager.isClosed(id)) {
                manager.writeClient(id, "POL");
            }
        }
    }

    private static void checkResponses() throws IOException {
        for(int id = manager.pollClients(); id != -1; id = manager.pollClients()) {

            String message = manager.readClient(id);

            if(message != null) { // received a string
                if(message.startsWith("ACK")) {
                    messages[messageCount++] = message.substring(3);
                }

                else if(message.startsWith("NAC")) /*no msg*/;

                else {
                    System.out.println("Unknown message ("+id+"): >"+message+"<");
                }
            }
        }
    }

    private static void distributeMessages() throws IOException {
        for(int msg = 0; msg < messageCount; ++msg) {
            System.out.println(messages[msg]);
            String message = "SEL" + messages[msg];

            for(int id = 0; id < ServerSocketManager.MAXCLIENTS; ++id) {
                if(!manager.isClosed(id)) {
                    manager.writeClient(id, message);
                }
            }
        }

        messageCount = 0; // reset index to fill array again
    }
}
