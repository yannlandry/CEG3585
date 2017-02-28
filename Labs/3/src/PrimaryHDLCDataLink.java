import java.io.IOException;

// Data Link Layer Entity for Primary Station
// Uses the HDLC protocol for communication over a multipoint link
// Assumptions
//    Normal Response Mode operation over multi-point link (simulated using PhysicalLayer class over Sockets)
//    Use 3-bit sequence numbers
// Not Supported:
//    FSC checking
//    Bit stuffing (frames are transmitted as strings)
//  Flag = "01111110"
//  Frames implemented:
//     Command Frames: SNRM, DISC  
//     Response Frames: UA
//     Command/Response Frames: I, RR 

public class PrimaryHDLCDataLink 
{
	// Private instance variables
	private final int MAX = 5; // Maximum number of supported connections
	private PhysicalLayer physicalLayer; // for sending/receiving frames
	// Data for multiple connections in the case of the primary station
	// For the secondary station, used values at index 0
	private int [] adrs = new int [MAX];
	private int [] vs = new int [MAX];
	private int [] vr = new int [MAX];

	// Constructor
	public PrimaryHDLCDataLink()
	{
		physicalLayer = new PhysicalLayer();	
	}
	
	public void close() throws IOException
	{
		physicalLayer.close();
	}
	
	/*----------------------------------------------------------
	 *  Connection Service
	 *-----------------------------------------------------------*/
	//  This is a confirmed service, i.e. the return value reflects results from the confirmation
	
	// Method: dlConnecRequest
	//  Establishes connection to station at address adr
	//  Sends SNRM with Poll bit set to 1
	public Result dlConnectRequest(int adr)
	{
		Result.ResultCode cd = Result.ResultCode.SrvSucessful;
		int secondaryId = getFreeSecondaryId();
		// Check if room for additional connection
		if(secondaryId == -1)
			cd = Result.ResultCode.ReachedLimit;
		else
		{
			String frame = HdlcDefs.FLAG+BitString.intToBitString(adr,HdlcDefs.ADR_SIZE_BITS)+
			               HdlcDefs.U_FRAME+
			               HdlcDefs.SNRM_M1+HdlcDefs.P1+HdlcDefs.SNRM_M2+
			               HdlcDefs.FLAG;
			System.out.println("Data Link Layer: prepared SNRM frame >"+BitString.displayFrame(frame)+"<");
			physicalLayer.transmit(frame);
			adrs[secondaryId] = adr;
			vs[secondaryId]=0;
			vr[secondaryId]=0;
		}
		return(new Result(cd, adr, null));		
	}
	
	// Method: dlConnecConfirmation
	//  Confirms connection
	//  Receives UA with F bit set to 1, extract address
	public Result dlConnectConfirmation()
	{
		Result.ResultCode cd = Result.ResultCode.SrvSucessful;
		int adr = 0;
		String retStr = null;
		// Wait for UA response frame
		String frame = physicalLayer.receive();
		adr = BitString.bitStringToInt(frame.substring(HdlcDefs.ADR_START,HdlcDefs.ADR_END));
		// Check if frame is U-frame
		String type = frame.substring(HdlcDefs.TYPE_START, HdlcDefs.TYPE_END);
		if(type.equals(HdlcDefs.U_FRAME) == false)
		{
			cd = Result.ResultCode.UnexpectedFrameReceived;
			adr = BitString.bitStringToInt(frame.substring(HdlcDefs.ADR_START,HdlcDefs.ADR_END));
			retStr = type;
		}			
		else
		{
			String mBits = frame.substring(HdlcDefs.M1_START, HdlcDefs.M1_END) +
			                frame.substring(HdlcDefs.M2_START, HdlcDefs.M2_END);
			if(mBits.equals(HdlcDefs.UA)==false)
			{
				cd = Result.ResultCode.UnexpectedUFrameReceived;
				retStr = mBits;
			}
			else if(getSecondaryId(adr) == -1)
			{
				cd = Result.ResultCode.InvalidAddress;
			}
			else System.out.println("Data Link Layer: received UA frame >"+BitString.displayFrame(frame)+"<");

		}
		return(new Result(cd, adr, retStr));		
	}
	
	/*----------------------------------------------------------
	 *  Disconnect service - non-confirmed service
	 *-----------------------------------------------------------*/	
	// Data disconnection service - non-confirmed service
	public Result dlDisconnectRequest(int adr)
	{   // Disconnection by Primary.
		Result.ResultCode cd = Result.ResultCode.SrvSucessful;
		int secondaryId = this.getSecondaryId(adr); // find the id
		
		if(secondaryId == -1)
		{
			cd = Result.ResultCode.InvalidAddress;
		}
		else
		{
		    // Send DISC frame
			String frame = HdlcDefs.FLAG+BitString.intToBitString(adr,HdlcDefs.ADR_SIZE_BITS)+
			               HdlcDefs.U_FRAME+
			               HdlcDefs.DISC_M1+HdlcDefs.P0+HdlcDefs.DISC_M2+
			               HdlcDefs.FLAG;
			System.out.println("Data Link Layer: prepared DISC frame >"+BitString.displayFrame(frame)+"<");
			physicalLayer.transmit(frame);
			adrs[secondaryId] = 0;
		}
		return(new Result(cd, adr, null));		
	}
		

	/*----------------------------------------------------------
	 *  Data service - non-confirmed service
	 *-----------------------------------------------------------*/	
	// Returns null if no data received
	public Result dlDataIndication(int adr)
	{   
		// Some vriable declarations;
		int id;  // identifier of the station (for indexing into adrs, vs, vr
		int ns; // ns found in information frame
		Result.ResultCode cd = Result.ResultCode.SrvSucessful;
		String frame;  // For referencing frames
		String sdu = ""; // for building return string
		String data; // for getting data field from information frame		

		// The primary polls secondary at address to
		// get secondary to send data.  
		id = this.getSecondaryId(adr);
		if(id == -1) 
		{
			cd = Result.ResultCode.InvalidAddress;
			return(new Result(cd,adr,null));
		}
		// address is valid
		// Send the poll (an RR with the P bit set
		frame = HdlcDefs.FLAG+BitString.intToBitString(adr,HdlcDefs.ADR_SIZE_BITS)+
                       HdlcDefs.S_FRAME+HdlcDefs.RR_SS+HdlcDefs.P1+
                       BitString.intToBitString(vr[adr], HdlcDefs.SNUM_SIZE_BITS)+
                       HdlcDefs.FLAG;
		System.out.println("Data Link Layer: sending RR frame (poll) >"+BitString.displayFrame(frame)+"<");
		physicalLayer.transmit(frame);
		
		// Collect the data in the received frames - ack each frame (go back N)
		boolean flag = true;
		while(flag)
		{
			frame = physicalLayer.receive();
			if(frame.charAt(HdlcDefs.TYPE_START) == '0') // Ignore other frames
			{
				ns = BitString.bitStringToInt(frame.substring(HdlcDefs.NS_START,HdlcDefs.NS_END));
				if(ns == vr[id])  // Is it the expected frame
				{
					data =  frame.substring(HdlcDefs.DATA_START, frame.length()-HdlcDefs.FLAG_SIZE_BITS);
					sdu = sdu+BitString.bitStringToString(data);
					vr[id] = (vr[id]+1)%HdlcDefs.SNUM_SIZE_COUNT;  // increment next expected seq num
					if(frame.charAt(HdlcDefs.PF_IX) == '1') flag = false; // last frame of data - stop 
					System.out.println("Data Link Layer: received I frame >"+BitString.displayFrame(frame)+"<");
				}				
				// Send acknowledgement even if frame not expected
				frame = HdlcDefs.FLAG+BitString.intToBitString(adr,HdlcDefs.ADR_SIZE_BITS)+
                        HdlcDefs.S_FRAME+HdlcDefs.RR_SS+HdlcDefs.P0+
                        BitString.intToBitString(vr[id], HdlcDefs.SNUM_SIZE_BITS)+
                        HdlcDefs.FLAG;
				System.out.println("Data Link Layer: prepared RR frame(ack) >"+BitString.displayFrame(frame)+"<");
	            physicalLayer.transmit(frame);
			}
		}		
		return(new Result(cd, adr, sdu));		
	}
		
	
	/*------------------------------------------------------------------------
	 * Helper Methods
	 *------------------------------------------------------------------------*/
	
    // Finds the index in the adrs array that is free (=0).
    private int getFreeSecondaryId()
    {
    	int id = -1;
    	for(int ix=0 ; ix < MAX && id == -1; ix++)
    	{
    		if(adrs[ix] == 0) id = ix;
    	}
    	return(id);   	
    }
    
    // Finds the index in the adrs array for a given address.
    private int getSecondaryId(int adr)
    {
    	int id = -1;
    	for(int ix=0 ; ix < MAX && id == -1; ix++)
    	{
    		if(adrs[ix] == adr) id = ix;
    	}
    	return(id);   	
    }  
 
}