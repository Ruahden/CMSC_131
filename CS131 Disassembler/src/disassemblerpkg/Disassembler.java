package disassemblerpkg;

import java.io.*;

public class Disassembler {
	
	/* String to store the read file */
	private static String originalCode = new String();
	
	/* String to store the converted file */
	private static String convertedCode = new String();
	
    	/*
    	 * Function:
    	 * 	readFile function only reads the input assembly or c file, the contents being stored in a string
    	 * Parameters:
    	 * 	String filename - the filename of the file to be read
    	 * Returns:
    	 * 	none
    	 * */
	private static void readFile(String filename) {
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			
		    	StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				originalCode += line + "\n";
				line = br.readLine();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("The file could not be found or could not be opened.");
		} catch (IOException e) {
			System.out.println("Error reading file.");
		}
	}
	
	/*
	 * Function:
	 * 	Converts input C code to assembly code
	 * Parameter:
	 * 	String cCode - the C code to be converted
	 * Returns:
	 * 	String convertedCode - the converted assembly code
	 * */
	private static String toAssemblyLanguage(String cCode){
		
		return convertedCode;
	}
	
	/*
	 * Function:
	 * 	Converts input c code to assembly code
	 * Parameter:
	 * 	String asmCode - the assembly code to be converted
	 * Returns:
	 * 	String convertedCode - the converted c code
	 * */
	private static String toCLanguage(String asmCode){
		
		return convertedCode;
	}
	
	public static void main(String[] args) {
		
		String filename = "helloworld.asm";
		/* To be put there the interface of input */
		
		
		Disassembler.readFile(filename);
		System.out.println(originalCode);
		
		String extension = filename.substring(filename.indexOf('.')+1).toLowerCase();
		if(extension.equals("c")){
			Disassembler.toAssemblyLanguage(originalCode);
		} else if(extension.equals("asm")){
			Disassembler.toCLanguage(originalCode);
		} else{
			System.out.println("Not an assembly code or a C Langage code!");
		}
		
		System.out.println(convertedCode);
		
	}
}
