
public class HdlcDefs 
{
	// Constant definitions to support the creation of an HCLC frame 
	// and extracting fields from an HDLC frame
	/*----------------------Encodings-------------------------------*/
	// Delimiter
	public static final String FLAG = "01111110";
	// Supervisory Bits
	public static final String RR_SS = "00";
	public static final String REJ_SS = "01";
	public static final String RNR_SS = "10";
	public static final String SREG_SS = "01";
	// Unnumbered Bits
	public static final String SNRM = "00001";
	public static final String SNRM_M1 = "00";   
	public static final String SNRM_M2 = "001";
	public static final String DISC = "00010";
	public static final String DISC_M1 = "00";   
	public static final String DISC_M2 = "010";
	public static final String UA = "00110";
	public static final String UA_M1 = "00";     
	public static final String UA_M2 = "110";
	// Poll Final Bits
	public static final String P1 = "1";
	public static final String P0 = "0";
	public static final String F1 = "1";
	public static final String F0 = "0";
	// Frame types
	public static final String I_FRAME = "0";
	public static final String S_FRAME = "10";
	public static final String U_FRAME = "11";
	
	/*-----------Indexes HDLC Fields----------------------*/
	// Indexes for use with the substring or charAt method to extract frame fields
	public static final int ADR_START = 8; public static final int ADR_END = 16;
	public static final int TYPE_START = 16; public static final int TYPE_END = 18;  // Can use charAt(TYPE_START) to test if Information frame
	public static final int S_START = 18; public static final int S_END = 20;
	public static final int NS_START = 17; public static final int NS_END = 20;
	public static final int NR_START = 21; public static final int NR_END = 24;
	public static final int PF_IX = 20;  // only one character - used charAt() method
	public static final int M1_START = 18; public static final int M1_END = 20;  
	public static final int M2_START = 21; public static final int M2_END = 24;  
	public static final int DATA_START = 24; // start of data field
	
	/*------------ Other Global Constants--------------*/
	public static final int MAX_DATA_SIZE_BYTES = 32;  // maximum number of BYTES in the data field.
	public static final int ADR_SIZE_BITS = 8;    // number of bits in the address
	public static final int SNUM_SIZE_BITS = 3;   // number of bits for the sequence number
	public static final int SNUM_SIZE_COUNT = 8;  // the number of sequence numbers = 2^SNUM_SIZE_BITS
	public static final int FLAG_SIZE_BITS = 8;   // number of bits in the flag
}
