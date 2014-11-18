package disassemblerpkg;

public class toCLanguage {

	private static String convertedCode = new String();
	
	/*
	 * Function:
	 * 	Converts input c code to assembly code
	 * Parameter:
	 * 	String originalCode - the assembly code to be converted
	 * Returns:
	 * 	none
	 * */
	toCLanguage(String originalCode) {
		
		String convertedCode = new String();
		
		/* THE CONVERSION BEGINS */
		
		
		/* THE CONVERSION ENDS */
		
		toCLanguage.convertedCode = convertedCode;
	}
	
	/*
	 * Function:
	 * 	Returns the converted C Language code
	 * Parameter:
	 * 	none
	 * Returns:
	 * 	String convertedCode - the converted c code
	 * */
	String getCode() {
		return toCLanguage.convertedCode;
	}
}
