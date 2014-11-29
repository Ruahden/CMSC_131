package disassemblerpkg;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
	
	private static String originalCode = new String();
	
	/*
    	 * Function:
    	 * 	Reads the content of the input file
    	 * Parameters:
    	 * 	String filename - the name of the file to be read
    	 * Returns:
    	 * 	none
    	 * */
	ReadFile(String filename) {
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			
			String line = br.readLine();
			while (line != null) {
				ReadFile.originalCode += line + "\n";
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
    	 * 	Returns the read file contents
    	 * Parameters:
    	 * 	none
    	 * Returns:
    	 * 	String - the read contents
    	 * */
	String getCode(){
		return ReadFile.originalCode;
	}
	
}
