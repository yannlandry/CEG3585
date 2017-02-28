
public class TestBitString {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String tstMsg = "This is a test message to check the bit string metnods";
		
		String bitString = BitString.stringToBitString(tstMsg);
		
		System.out.println(tstMsg+"<");
		System.out.println(bitString);
		System.out.println(BitString.bitStringToString(bitString)+"<");

	}

}
