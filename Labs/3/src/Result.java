
// Class for creating rsults objects
public class Result 
{
	public enum ResultCode { SrvSucessful,   // Service completed - no errors
		                     SrvNotAllowed,  // Service not allowed for context
		                     ReachedLimit,   // For primary - cannot support additional secondary stations
		                     UnexpectedFrameReceived, // Unexpected Frame Type (I, S, U) received
		                     UnexpectedUFrameReceived, // Unexpected U-Frame (SNRM, UA, DISC) received
		                     InvalidAddress  // For primary - address of frame invalid (no connection exists)
		                    };
    private ResultCode result;
    private int address;
    private String sdu; // Service data unit
    
    // Constructor - sets all values of the results
    public Result(ResultCode res, int adr, String sdu)
    {
    	result = res;
    	address = adr;
    	this.sdu = sdu;
    }
    // Getters to get results
    public ResultCode getResult() { return result; }
    public int getAddress() { return address; }
    public String getSdu() { return sdu; }
    
    public String toString()
    {
    	return("Result: code is "+result+", address is "+address+", data is "+sdu);
    }
}
