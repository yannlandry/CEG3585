import java.io.IOException;


/* For simulating a multi-point physical layer using sockets
 * Assumptions:
 *   1) only one node may communicate at a time (half-duplex)
 *   2) a "frame" received from one node is re-transmitted to all other nodes
 *   3) The port 4444 shall be used as the server port.
 */
public class PhysicalLayerServer 
{
	public static final int PL_PORT = 4444;
    public static void main(String[] args) throws IOException 
    {        
        //get port number from the command line
    	System.out.println("Physical Layer Server starting on port "+PL_PORT);
        
        // Create the server socket manager
        ServerSocketManager ssm= new ServerSocketManager(PL_PORT);
        //
        String frame; // for receiving and transmitting frame 
        // Main Loop       
        boolean bListening = true;
        while(bListening)
        {
        	// Accepted Connections
        	int newClientId = ssm.listenOnSocket();  // Times out
        	if(newClientId != -1) 
        		System.out.println("Physical Layer Server: connection from Physical Layer Client  "+newClientId);
        	
        	// Frame received received
        	for(int rcv_id = ssm.pollClients(); rcv_id != -1; rcv_id = ssm.pollClients())
        	{
        		frame = ssm.readClient(rcv_id);
        		if(frame != null) // received a frame
        		{   // Transmit to all nodes (clients)
            		System.out.println("Physical Layer Server: received frame from client  "+rcv_id+": >"+frame+"<, sending to other clients.");
            		for(int tr_id = 0 ; tr_id < ServerSocketManager.MAXCLIENTS; tr_id++)
                    {
                	   if(ssm.isClosed(tr_id) == false && tr_id != rcv_id)
                	   {
                	       ssm.writeClient(tr_id, frame);
                	   }
                	}       		
        		}
        	}        	        	
        	// Check for closed connections
        	ssm.closeConnections();        	
        }        
    }// end main 

}
