import java.io.*;

public class PrimaryStation {	
	
    public static void main(String[] args) throws IOException, InterruptedException 
    {
    	Result res;  // for referencing result from data link layer
   	    // Setup Data Link Layer
    	PrimaryHDLCDataLink dl = new PrimaryHDLCDataLink();
    	
    	// Connect to 2 stations
    	if(connectStation(1,dl) == false) return; // stop application on error.
    	if(connectStation(2,dl) == false) return; // stop application on error.
    	
    	// Get message from each station
    	System.out.println("--------------------Get Message from Station 2-------------------");
    	res = dl.dlDataIndication(2); // start with station 2
    	if(res.getResult() == Result.ResultCode.SrvSucessful)
    	{
    		System.out.println("Primary Station: Received message from Station 2 >"+res.getSdu()+"<");
    	}
       	System.out.println("----------------------------------------------------------------");
        System.out.println("--------------------Get Message from Station 1-------------------");
    	res = dl.dlDataIndication(1); // then station 1
    	if(res.getResult() == Result.ResultCode.SrvSucessful)
    	{
    		System.out.println("Primary Station: Received message from Station 2 >"+res.getSdu()+"<");
    	}
       	System.out.println("----------------------------------------------------------------");
       	
    	System.out.println("------------------- Disconnect Station 1-------------------");
		System.out.println("Primary Station: Requesting dicsonnect from station 1");
    	res = dl.dlDisconnectRequest(1);
    	if(res.getResult() != Result.ResultCode.SrvSucessful)
    	{
    		System.out.println("Primary Station: Could not disconnect station 1");
    		System.out.println(res);
    	}
       	System.out.println("----------------------------------------------------------------");
       	
     	System.out.println("------------------- Disconnect Station 2-------------------");
		System.out.println("Primary Station: Requesting dicsonnect from station 2");
		     	res = dl.dlDisconnectRequest(2);
    	if(res.getResult() != Result.ResultCode.SrvSucessful)
    	{
    		System.out.println("Primary Station: Could not disconnect station 2");
    		System.out.println(res);
    	}
       	System.out.println("----------------------------------------------------------------");
    	
		Thread.sleep(5000);
    	dl.close();
   }
    
    public static boolean connectStation(int adr, PrimaryHDLCDataLink dl)
    {
    	Result res;  // for referencing result from data link layer
    	boolean retVal = true;  // return value
    	System.out.println("--------------------Connection to Station "+adr+"-------------------");
		System.out.println("Primary Station: Requesting connection to station "+adr);
    	res = dl.dlConnectRequest(adr);
    	if(res.getResult() != Result.ResultCode.SrvSucessful)
    	{
    		System.out.println("Primary Station: Could not initiate conection with station "+adr);
    		System.out.println(res);
    	    retVal = false;  // End program
    	}
    	else
    	{
        	res = dl.dlConnectConfirmation(); // Response from Station 1
        	if(res.getResult() != Result.ResultCode.SrvSucessful)
        	{
        		System.out.println("Primary Station: Could not get confirmation of conection with station )"+adr);
    			System.out.println(res);
        	    retVal = false;  // End program
        	}
        	else
        		System.out.println("Primary Station: Received connect confirmation from station " + adr);

    	}
    	System.out.println("----------------------------------------------------------------");
    	return(retVal);
    }
    
}// end of class PrimaryStation
