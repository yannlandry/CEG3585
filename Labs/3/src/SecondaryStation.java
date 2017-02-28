import java.io.*;

public class SecondaryStation 
{	
	public static void main(String[] args) throws InterruptedException, IOException 
	{
		String eol = System.getProperty("line.separator");
        String message = "Message for testing data transfer. "+eol+
                         "The HDLC protocol is designed to support communication over "+eol+
                         "a physical link between physically connected stations."+eol+
                         "In this lab a primary station is connected to two secondary stations."+eol+
                         "This message will be sent by each Secondary station to the Primary."+eol+
                         "Many HDLC I frames shall be used to send this message";
		Result res;  // results from service 
		// Get address from command line
		if(args.length != 1)
		{
			System.out.println("Usage: java SecondaryStation <Station Address");
			return;
		}
		
		int address = Integer.parseInt(args[0]);
		
		// Setup connection with Primary Station
		SecondaryHDLCDataLink dl = new SecondaryHDLCDataLink(address);
		
		// Connect to primary
    	System.out.println("--------------------Connection to Primary-------------------");
		res = dl.dlConnectIndication();
		if(res.getResult() != Result.ResultCode.SrvSucessful)
		{
			System.out.println("Secondary Station ("+address+"): did not get connect indication"+res.getResult());
			System.out.println(res);
			return; // end program
		}
		System.out.println("Secondary Station ("+address+"): Received conenct indication");
		System.out.println("Secondary Station ("+address+"): Issuing connect confirmation");
		res = dl.dlConnectResponse();
		if(res.getResult() != Result.ResultCode.SrvSucessful)
		{
			System.out.println("Secondary Station ("+address+"): could send connect response");
			System.out.println(res);
			return; // end program
		}
    	System.out.println("------------------------------------------------------------");
    	
    	
    	System.out.println("--------------------Send Message To Primary-------------------");
		System.out.println("Secondary Station ("+address+"): Issuing data request");
    	res = dl.dlDataRequest("Station "+address+" to Primary: "+message);
		if(res.getResult() != Result.ResultCode.SrvSucessful)
		{
			System.out.println("Secondary Station ("+address+"): could not send message");
			System.out.println(res);
			return; // end program
		}
    	System.out.println("------------------------------------------------------------");
    	
    	System.out.println("--------------------Disconnection-------------------");
    	res = dl.dlDisconnectIndication();
		System.out.println("Secondary Station ("+address+"): Received disconnect indication");
		if(res.getResult() != Result.ResultCode.SrvSucessful)
		{
			System.out.println("Secondary Station ("+address+"): could not disconnect");
			System.out.println(res);
			return; // end program
		}
    	System.out.println("------------------------------------------------------------");
		
		Thread.sleep(5000);
		dl.close();
	}

}// end of class SecondaryStation
