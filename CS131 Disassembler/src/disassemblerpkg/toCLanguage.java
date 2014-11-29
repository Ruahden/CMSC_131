package disassemblerpkg;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ToCLanguage {

	private static String convertedCode = new String();
	
	/*
	 * The .data variables in the assembly code
	 * String[0] - variable name
	 * String[1] - variable value
	 * */
	private static ArrayList<String[]> dataContent = new ArrayList<String[]>();
	
	/*
	 * Function:
	 * 	Converts input c code to assembly code
	 * Parameter:
	 * 	String originalCode - the assembly code to be converted
	 * Returns:
	 * 	none
	 * */
	ToCLanguage(String originalCode) {
		
		/*
		 * convertedCode[0] - the includes
		 * convertedCode[1] - the main proc [ main(){ ]
		 * convertedCode[2] - the body 
		 * convertedCode[3] - the end main [ } ]
		 * */
		String[] convertedCode = {"#include<stdio.h> \n", "main(){ \n\n", "", "\n}"};
		
		StringTokenizer tokenizer = new StringTokenizer(originalCode, "\n");
		while(tokenizer.hasMoreTokens()){
			String line = tokenizer.nextToken();
			
			if(line.equals(".data")){
				line = tokenizer.nextToken();
				while(!line.equals(".code")){
					
					String[] data = new String[2];
						String variable = new String();
						String value = new String();
					
					int startIndex = 0;
					String character = line.substring(startIndex, startIndex + 1);
						while(!character.equals(" ")){
							variable += character;
							startIndex++;
							character = line.substring(startIndex, startIndex + 1);
						};
					data[0] = variable;
						
						startIndex += 5;
						if(line.contains("\"")){
							int valueStartIndex = line.indexOf("\"")+1;
							int valueEndIndex = line.substring(0, line.indexOf("$")-1).lastIndexOf("\"");
							value = "\"" + line.substring(valueStartIndex, valueEndIndex) + "\"";
						} else{
							int valueStartIndex = line.indexOf("\'")+1;
							int valueEndIndex = line.substring(0, line.indexOf("$")-1).lastIndexOf("\'");
							value = "\'" + line.substring(valueStartIndex, valueEndIndex) + "\'";
						}
					data[1] = value;
					
					dataContent.add(data);

					line = tokenizer.nextToken();
				}
			}
			
			if(line.equals("mov ds, ax")){
				line = tokenizer.nextToken();
				String body = new String();
				while(!line.equals("mov ax, 4c00h")){
					body += line + "\n";
					line = tokenizer.nextToken();
				}
				convertedCode[2] = ToCLanguage.bodyParser(body);
			}
		}
		
		for(String code :  convertedCode){
			ToCLanguage.convertedCode += code;
		}
	}
	
	/*
	 * Function:
	 * 	converts the body of the assembly code into C Language
	 * Parameter:
	 * 	String body - the assembly code to be converted
	 * Returns:
	 * 	String - the converted assembly body code
	 * */
	private static String bodyParser(String body){
		
		
		
		return body;
	}
	
	
	/*
	 * Function:
	 * 	Returns the converted C Language code
	 * Parameter:
	 * 	none
	 * Returns:
	 * 	String - the converted c code
	 * */
	String getCode() {
		return ToCLanguage.convertedCode;
	}
}
