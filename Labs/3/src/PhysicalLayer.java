import java.io.IOException;


public class PhysicalLayer
{
	static ClientSocketManager medium = new ClientSocketManager();
	
	// Constructor - connect to local ip address using server port
	public PhysicalLayer()
	{
		try {
			medium.connect("0", PhysicalLayerServer.PL_PORT); // connects to local IP
		} catch (IOException e) {
			System.out.println("Physical layer: Could not connect to Physical Layer Server");
			e.printStackTrace();
		} 		
	}
	
	public void close() throws IOException
	{
		medium.close();
	}
	
	public void transmit(String frame)
	{
		try {
			medium.write(frame);
			System.out.println("Physical layer: transmitted frame >"+BitString.displayFrame(frame)+"<");
		} catch (IOException e) {
			System.out.println("Physical layer: IO Exception on transmitting frame");
			e.printStackTrace();
		}
	}
    
	// returns a null if no frame available
	// at the physical layer
	public String pollReceive()
	{
		String frame = null;
		
		try {
			if(medium.poll()) frame = medium.read();
			if(frame != null) System.out.println("Physical layer: received frame >"+BitString.displayFrame(frame)+"<");
		} catch (IOException e) {
			System.out.println("Physical layer: IO Exception on receiving frame");
			e.printStackTrace();
		}
		return(frame);
	}
	
	public String receive()
	{
		String frame = null;
		try {
			frame = medium.read();
			System.out.println("Physical layer: received frame >"+BitString.displayFrame(frame)+"<");
		} catch (IOException e) {
			System.out.println("Physical layer: IO Exception on receiving frame");
			e.printStackTrace();
		}
		return(frame);
	}
	


}
