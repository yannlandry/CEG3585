import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;

// Data Link Layer Entity for Secondary Station
// Uses the HDLC protocol for communication over a multipoint link
// Assumptions
//    Normal Response Mode operation over multi-point link (simulated using PhysicalLayer class over Sockets)
//    Use 3-bit sequence numbers
// Not Supported:
//    FSC checking
//    Bit stuffing (frames are transmitted as strings)
//  Frames implemented:
//     Command Frames:
//        NRM:
//        DISC:
//     Response Frames:
//        UA:
//     Command/Response Frames:
//        I: maximum length of data field is 64 bytes.
//        RR:

public class SecondaryHDLCDataLink
{
	public static final MAX_WINDOW_SIZE = 8;

	// Private instance variables
	private PhysicalLayer physicalLayer; // for sending/receiving frames
	private int stationAdr; // Station address - not used for the primary station
	// Data for multiple connections in the case of the primary station
	// For the secondary station, used values at index 0

	/*
	private int vs; // looks like last message sent
	private int vr; // looks like last message received
	// how about decent variable names
	*/

	private int rhsWindow; // right hand side of window.
	private int windowSize; // transmit window size. reception window size is 1.
							// do we need that variable?
	private ArrayList<String> frameBuffer; // i guess the window plugs on here

	// Constructor
	public SecondaryHDLCDataLink(int adr)
	{
		physicalLayer = new PhysicalLayer();
		stationAdr = adr;
	    vs = 0;
	    vr = 0;
	    windowSize = 4; // why 4?
	    frameBuffer = new ArrayList<String>();
	    rhsWindow = vs+windowSize; // seq # < rhsWindow
	}

	public void close() throws IOException
	{
		physicalLayer.close();
	}

	/*----------------------------------------------------------
	 *  Connection Service
	 *-----------------------------------------------------------*/
	// This is a confirmed service, i.e. the return value reflects results from the confirmation

	public Result dlConnectIndication()
	{  // Receive NRM command frame
		Result.ResultCode cd = Result.ResultCode.SrvSucessful;
		int adr = 0;
		String retStr = null;
		// Wait for UA response frame
		String frame = getFrame(true);  // true - wait for frame
		adr = BitString.bitStringToInt(frame.substring(HdlcDefs.ADR_START,HdlcDefs.ADR_END));
		// Check if frame is U-frame
		String type = frame.substring(HdlcDefs.TYPE_START, HdlcDefs.TYPE_END);
		if(type.equals(HdlcDefs.U_FRAME) == false)
		{
			cd = Result.ResultCode.UnexpectedFrameReceived;
			retStr = type;
		}
		else
		{
			String uframe = frame.substring(HdlcDefs.M1_START, HdlcDefs.M1_END) +
			                frame.substring(HdlcDefs.M2_START, HdlcDefs.M2_END);
			if(uframe.equals(HdlcDefs.SNRM)==false)
			{
				cd = Result.ResultCode.UnexpectedUFrameReceived;
				retStr = uframe;
			}
			else System.out.println("Data Link Layer: received SNRM frame >"+BitString.displayFrame(frame)+"<");
		}
		return(new Result(cd, adr, retStr));
	}

	public Result dlConnectResponse()
	{
		Result.ResultCode cd = Result.ResultCode.SrvSucessful;
		// Check if room for additional connection
		String frame = HdlcDefs.FLAG+BitString.intToBitString(stationAdr,HdlcDefs.ADR_SIZE_BITS)+
		               HdlcDefs.U_FRAME+
		               HdlcDefs.UA_M1+HdlcDefs.P1+HdlcDefs.UA_M2+
		               HdlcDefs.FLAG;
		System.out.println("Data Link Layer: prepared UA frame >"+BitString.displayFrame(frame)+"<");
		physicalLayer.transmit(frame);
		vs=0;
		vr=0;
		return(new Result(cd, stationAdr, null));
	}

	/*----------------------------------------------------------
	 *  Disconnect service - non-confirmed service
	 *-----------------------------------------------------------*/

	public Result dlDisconnectIndication()
	{   // Disconnection to secondary.
		Result.ResultCode cd = Result.ResultCode.SrvSucessful;
		int adr = 0;
		String retStr = null;
		// Wait for DISC frame
		String frame = getFrame(true);  // true - wait for frame
		adr = BitString.bitStringToInt(frame.substring(HdlcDefs.ADR_START,HdlcDefs.ADR_END));
		// Check if frame is U-frame
		String type = frame.substring(HdlcDefs.TYPE_START, HdlcDefs.TYPE_END);
		if(type.equals(HdlcDefs.U_FRAME) == false)
		{
			cd = Result.ResultCode.UnexpectedFrameReceived;
			retStr = type;
		}
		else
		{
			String uframe = frame.substring(HdlcDefs.M1_START, HdlcDefs.M1_END) +
			                frame.substring(HdlcDefs.M2_START, HdlcDefs.M2_END);
			if(uframe.equals(HdlcDefs.DISC)==false)
			{
				cd = Result.ResultCode.UnexpectedUFrameReceived;
				retStr = uframe;
			}
			else System.out.println("Data Link Layer: received DISC frame >"+BitString.displayFrame(frame)+"<");
		}
		return(new Result(cd, adr, retStr));
	}

	/*----------------------------------------------------------
	 *  Data service - non-confirmed service
	 *-----------------------------------------------------------*/

	public Result dlDataRequest(String sdu)
	{
		Result.ResultCode cd = Result.ResultCode.SrvSucessful;

		// Wait for poll - need an RR with P bit - 1
		// wait wtf
		getRRFrame(true); // <- this?

		// Send the SDU
		// After each transmission, check for an ACK (RR)
		// Use a sliding window
		// Reception will be go back-N

		// Split the string into chunks you can send
		String [] dataArr = BitString.splitString(sdu, HdlcDefs.MAX_DATA_SIZE_BYTES);

		// Convert these chunks into proper bitstrings
		// make frames out of them right away, i guess
		for(int ix = 0; ix < dataArr.length; ix++) {
			String data = BitString.stringToBitString(dataArr[ix]);
			String frame = makeIFrame(data, ix % 8, dataArr.length - ix == 1);
			frameBuffer.add(frame);
			displayDataXchngState("Data Link Layer: prepared and buffered I frame >" + BitString.displayFrame(frame) + "<");
		}

		// set window and things
		// i don't even know what i'm doing
		rhsWindow = Math.min(MAX_WINDOW_SIZE, frameBuffer.size());
		vr = vs = 0;

		// Loop to transmit frames
		// i dunno wtf vr and vs mean but they aren't used anywhere so we'll use that
		// vs points to the next frame to send
		// vr points to the frame that was requested in the last RR
		// rhsWindow points to the first frame at the right of the window
		while(vr < frameBuffer.size()) {
			// if window not closed
			// send a frame
			if(vs < rhsWindow) {
				physicalLayer.transmit(frameBuffer[vs++]);
			}

			// check for acknowledgement of frame
			// poll only, otherwise keep sending for a while
			frame = getRRFrame(false);

			if(frame != null) {
				displayDataXchngState("received an RR frame (ack) >" + BitString.displayFrame(frame) + "<");
				// move vr to the position in the RR
				// move vs there as well?
				// what's up with the timeout?
				int nr = BitString.bitStringToInt(frame.substring(HdlcDefs.NR_START, HdlcDefs.NR_END));

				// if nr is equal to vr,
				// we've probably got the same RR twice,
				// meaning some frames have been lost
				// so bring vs back
				if(nr == vr) {
					vs = vr;
				}
				else {
					vr+= (nr - vr) % MAX_WINDOW_SIZE;
					rhsWindow = Math.min(vr + MAX_WINDOW_SIZE, frameBuffer.size());
				}
				// this is fucked up
			}
		}

		return(new Result(cd, 0, null));
	}

	/*------------------------------------------------------------------------
	 * Helper Methods
	 *------------------------------------------------------------------------*/

	private String makeIFrame(String bitString, int frameNumber, boolean isFinal) {
		return
			// flag
			HdlcDefs.FLAG
			// station address
			+ BitString.intToBitString(stationAdr, 8)
			// control
			+ HdlcDefs.I_FRAME
			+ BitString.intToBitString(frameNumber, 3)
			+ (isFinal ? HdlcDefs.F1 : HdlcDefs.F0) // not sure about this one
			+ "000" // not used
			// contents
			+ bitString
			// skip FCS
			+ HdlcDefs.FLAG;
	}

	// Determines the number of frames acknowledged from the
	// acknowledge number nr.
	// Parameters
	// nr - received ack number - indicates next expected
	//      sequence number (nr can equal lhs - window is closed)
	// rhs - right hand side of window - seq number to the
	//       right of the last valid number that can be used
	// sz - size of the window
	private int checkNr(int nr, int rhs, int sz)
	{
		int lhs = (rhs - sz) % HdlcDefs.SNUM_SIZE_COUNT;

		if (lfs < rhs && nr >= lfs) {
			return nr - lfs;
		}
		if (lfs > rhs && (nr <= rhs || nr >= lfs)) {
			return (nr - lfs) % HdlcDefs.SNUM_SIZE_COUNT;
		}

		return 0;
	}

	// Helper method to get an RR-frame
	// If wait is true then wait until a frame
	// arrives (call getframe(true).
	// If false, return null if no frame
	// is available from the physical layer (call getframe(false)
	// or frame received is not an RR frame.
	private String getRRFrame(boolean wait)
	{
		String frame;

		do {
			frame = getFrame(wait);

			// bonne trame?
			if (frame != null) {
				String type = frame.substring(15, 17);

				if (type.equals(HdlcDefs.S_FRAME)) {
					String sframe = frame.substring(17, 19);

					// si pas "RR", on
					if (!sframe.equals(HdlcDefs.RR_SS)) {
						frame = null;
					}
				}
				// sinon, frame = null
			}
		} while(wait && frame == null);

		return frame;
	}

	// For displaying the status of variables used
	// in exchanging data between stations.
	private void displayDataXchngState(String msg)
	{
		int lhs; // left hand side of the window
		//compute lhs
		if( (rhsWindow-windowSize) >= 0) lhs = rhsWindow - windowSize;
		else lhs = rhsWindow - windowSize + HdlcDefs.SNUM_SIZE_COUNT;

		System.out.println("Data Link Layer: Station "+stationAdr+": "+msg);
		System.out.println("    v(s) = "+vs+", v(r) = "+vr+
				           ", Window: lhs="+lhs+" rhs="+rhsWindow+
				           ", Number frames buffered = "+frameBuffer.size());
	}

	// Waits for reception of frame
	// If wait is true, then wait for a frame to arrive,
	// otherwise just poll physical layer for a frame.
	// Returns null if no frame is received.
	private String getFrame(boolean wait)
	{
		// Only frames with this stations address is processed - others are ignored
		String frame = null;
		do
		{
			if(wait) frame = physicalLayer.receive(); // block on receive.
			else frame = physicalLayer.pollReceive();  // get frame from physical layer
			if(frame != null)
			{
				int adr = BitString.bitStringToInt(frame.substring(HdlcDefs.ADR_START, HdlcDefs.ADR_END));
				if(adr != stationAdr) frame = null;  // ignore strings for other destinations
			}
		} while(frame == null && wait);
		//if(frame != null) System.out.println("Data Link Layer: Received frame >"+BitString.displayFrame(frame)+"<");
		return(frame);
	}

}
