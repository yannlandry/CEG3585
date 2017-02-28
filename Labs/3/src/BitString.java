
public class BitString 
{
	/*------- Methods to convert to/from bit strings -----*/
	
	// BitString to String
    public static  String bitStringToString(String bitString)
    {
    	String str = ""; // Empty String
    	// Extract 8 bits at a time
    	int ix = 0;
    	int numChars = bitString.length()/8;  // Assume multiple of 8
    	for(int i = 0 ; i<numChars; i++)
    	{
    		String bitCharString = bitString.substring(ix,ix+8);
    		str = str+bitStringToChar(bitCharString);
    		ix += 8; // skip 8 bits
    	}
     	return(str);
    }

    
    // BitString to int
    public static int bitStringToInt(String bitString)
    {
    	int intVal = 0;
    	// Initilaise mask according to number of bits
    	int mask = (int) Math.pow(2,bitString.length()-1); // set high-order bit to 1
    	for(int i = 0; i<bitString.length(); i++)
    	{
    		if(bitString.charAt(i) == '1') 
			    intVal = intVal | mask;   // set the bit
     		mask = mask>>1;  // shift 1 by one position.
    	}
    	return (intVal);
    }
    
	// 8 bit BitString to Char
    public static char bitStringToChar(String bitString)
    {
    	char ch = '\0';  // null char - all zeros
       	int mask = 128; // set bit 0 to 1;
    	for(int i = 0; i<8; i++)
    	{
    		if(bitString.charAt(i) == '1') 
    			ch = (char) ((int) ch | mask);   // set the bit
    		mask = mask>>1;  // shift 1 by one position.
    	}
    	return ch;
    }	
	
	
	// String to BitString
    public static  String stringToBitString(String str)
    {
    	String bitString = ""; // Empty String
    	
    	for(int i = 0 ; i<str.length(); i++) // to process all characters
    	   bitString = bitString+charToBitString(str.charAt(i));   	
    	return(bitString);
    }

    
    // Char to BitString
    public static  String charToBitString(char ch)
    {
    	String bitAdr = "";
    	int mask = 1; // set bit 0 to 1;
    	for(int i = 0; i<8; i++)
    	{
    		if((ch & mask) == 0) // bit in address is sero
    		   bitAdr = "0"+bitAdr;
    		else
    		   bitAdr = "1"+bitAdr;
    		mask = mask<<1;  // shift 1 by one position.
    	}
    	return (bitAdr);
    }
    
	// int to BitString
    // Its up to the calling method to ensure numBits is 
    // large enough to represent the value. Can be used
    // to convert address and sequence numbers to BitString
    public static String intToBitString(int intVal, int numBits)
    {
    	String bitString = "";
    	int mask = 1; // set bit 0 to 1;
    	for(int i = 0; i<numBits; i++)
    	{
    		if((intVal & mask) == 0) // bit in address is sero
    			bitString = "0"+bitString;
    		else
    			bitString = "1"+bitString;
    		mask = mask<<1;  // shift 1 by one position.
   	    }
    	return bitString;
    }

	/*------------------------ Other methods ----------------------------*/

    // Splits the string into and array of strings
    // Each string is at most size chars long
    public static String[] splitString(String str, int size)
    {
    	// Variable declarations
    	int numSubStrings; // number of substrings
    	int ix, arIx;  // index to index into str String and subString array.
    	String [] subStrArray; // reference to substring array.
    	int last; // last index in the substring
    	// Set up an array with enough entries to references substrings
    	if(str.length() % size == 0) numSubStrings = str.length()/size;
    	else numSubStrings = str.length()/size +1;
    	subStrArray = new String [numSubStrings];
    	// Fill in the array
    	for(ix = 0, arIx = 0 ; ix < str.length() ; ix +=size, arIx++)
    	{
    		if(ix+size <= str.length()) last = ix+size;
    		else last = str.length();
    		subStrArray[arIx] = str.substring(ix,last);
    	}
    	return(subStrArray);
    }
    
    // For displaying a frame (bitString)
	public static String displayFrame(String frame)
	{
		String display;
		int ix, last; // for indexing and substring
		// insert space every 8 bits
		if(frame.length() > 8) 
		{
			 display = frame.substring(0,8);
			 ix = 8;
		}
		else 
		{
			display = frame;
			ix = frame.length();
		}
		String eightBits;
		int end = frame.length();
		for(; ix < end ; ix +=8)
		{
			// If frame contains more than 7 bytes (i.e. information frame
			// only want to display first 4 bytes and last 2 bytes
			// Adjust ix to point to the last two bytes after the first 
			// 4 bytes have been displayed
			if(frame.length() > 7 && ix == 4*8) 
			{
				ix = frame.length()-2*8;
				display = display+ " ... "; // shows undisplayed data
			}
			// set the index lf the last bit to extra 8 bits
			if(ix+8 <= frame.length()) last = ix+8;
			else last = frame.length(); // in case not multiple of 8
			eightBits = frame.substring(ix,last);
			display = display+" "+eightBits;
		}
		return(display);
	}
}
