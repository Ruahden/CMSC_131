package disassemblerpkg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class ToCLanguage {

	private static String convertedCode = new String();
	
	/*
	 * The .data variables in the assembly code
	 * String[0] - variable name
	 * String[1] - variable value
	 * */
	private static ArrayList<String[]> dataContent = new ArrayList<String[]>();	
	
	private static String[] registersArray = {"al","ah","ax","bl","bh","bx","cl","ch","cx","dl","dh","dx"};
	private String[] registerValueArray = new String[12];
	
	/**
	 * Converts input c code to assembly code
	 * @param originalCode - the assembly code to be converted
	 * */
	ToCLanguage(String originalCode) {
		
		/*
		 * convertedCode[0] - the includes
		 * convertedCode[1] - the main proc [ main(){ ]
		 * convertedCode[2] - the data
		 * convertedCode[3] - the body 
		 * convertedCode[4] - the end main [ } ]
		 * */
		String[] convertedCode = {"#include<stdio.h>\n", "\nmain(){\n\n", "", "", "\n}"};
		
		StringTokenizer tokenizer = new StringTokenizer(originalCode, "\n");
		while(tokenizer.hasMoreTokens()){
			String line = tokenizer.nextToken();
			
			Arrays.fill(registerValueArray, "");
			
			if(line.equals(".data")){
				line = tokenizer.nextToken();
				while(!line.equals(".code")){
					
					String[] data = new String[2];
					String variable = new String();
					String value = new String();
					
					//gets variable name
					int startIndex = 0;
					String character = line.substring(startIndex, startIndex + 1);
						while(!character.equals(" ")){
							variable += character;
							startIndex++;
							character = line.substring(startIndex, startIndex + 1);
						};
					
					//gets variable value and data type
					int valueStartIndex, valueEndIndex;
					//string or character
					if (line.contains("\"")) {
						valueStartIndex = line.indexOf("\"") + 1;
						valueEndIndex = line.substring(0, line.indexOf("$") - 1).lastIndexOf("\"");
						value = "\"" + line.substring(valueStartIndex, valueEndIndex) + "\"";
						variable = "char " + variable + "[]";
					} else if (line.contains("\'")) {
						valueStartIndex = line.indexOf("\'") + 1;
						valueEndIndex = line.substring(0, line.indexOf("$") - 1).lastIndexOf("\'");
						value = "\'" + line.substring(valueStartIndex, valueEndIndex) + "\'";
						variable = "char " + variable + "[]";
					}
					//uninitialized variable
					else if (line.contains("?")) {
						value = "?";
					}
					//float
					else if (line.contains(".")) {
						valueStartIndex = line.lastIndexOf("d") + 3;
						valueEndIndex = line.length();
						value = line.substring(valueStartIndex, valueEndIndex);
						variable = "float " + variable;
					}
					//integer (hexadecimal)
					else if (line.contains("h") || line.contains("H")) {
						valueStartIndex = line.lastIndexOf("d") + 3;
						valueEndIndex = line.length();
						value = (line.substring(valueStartIndex, valueEndIndex)).toLowerCase();
						value = value.substring(0, value.lastIndexOf("h"));
						int hexaValue = Integer.parseInt(value, 16);
						value = Integer.toString(hexaValue);
						variable = "int " + variable;
					}
					//integer (decimal)
					else if (line.matches(".*\\d.*")) {
						valueStartIndex = line.lastIndexOf("d") + 3;
						valueEndIndex = line.length();
						value = line.substring(valueStartIndex, valueEndIndex);
						variable = "int " + variable;
					}

					data[0] = variable;
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
				convertedCode[3] = bodyParser(body);
			}
			
			String dataLine, entireDataCode = "";
			for (String[] dataArray : dataContent) {
				if (dataArray[1].charAt(0) == '?') {
					dataLine = "\t" + dataArray[0] + ";\n";
				}
				else {
					dataLine = "\t" + dataArray[0] + " = " + dataArray[1] + ";\n";
				}
				entireDataCode += dataLine;
			}
			convertedCode[2] = entireDataCode;
		}
		
		for(String code : convertedCode){
			ToCLanguage.convertedCode += code;
		}
	}
	
	/**
	 * Converts the body of the assembly code into C Language
	 * @param body - the body of the assembly code to be converted
	 * @return convertedBody - the converted assembly body code
	 * */
	private String bodyParser(String body){
		String convertedBody = "";
		
		StringTokenizer bodyToken = new StringTokenizer(body, "\n");
		while(bodyToken.hasMoreTokens()){
			String line = bodyToken.nextToken();
			String first, second, comment = "";
			int endIndex;
			
			//checks if line has comments
			if (line.contains(";")) {
				line = line.replaceFirst(";", "//");
				comment = line.substring(line.indexOf("//"), line.length());
			}
			
			//checks if line is interrupt for printing
			if (line.contains("int 21H".toLowerCase())) {
				
			}
			
			//checks if line for assigning variables/registers
			if (line.contains("mov ")) {
				if (line.contains("//")) {
					endIndex = line.indexOf("//");
				}
				else {
					endIndex = line.length();
				}
				first = line.substring(line.indexOf(" "), line.indexOf(",")).trim();
				second = line.substring(line.indexOf(",")+1, endIndex).trim();
				String variable = "";
				
				//checks if first is a declared variable
				for (String[] dataArray : dataContent) {
					//if first is an uninitialized variable, determines data type and first value
					if (first.equals(dataArray[0]) && dataArray[1].equals("?")) {
						if ((second.indexOf("\"") == 0 && second.lastIndexOf("\"") == second.length())
							|| (second.indexOf("\'") == 0 && second.lastIndexOf("\'") == second.length())) {
							variable = "char " + first + "[]";							
						}
						else if (second.contains(".")) {
							variable = "float " + first;
						}
						else if (line.contains("h") || line.contains("H") || line.matches(".*\\d.*")) {
							variable = "int " + first;
						}
						dataArray[0] = variable;
						dataArray[1] = "?" + second;
						convertedBody += "\t" + first + " = " + second + ";\n";
					}
					//if first is already declared
					else if (first.equals(dataArray[0])) {
						//if second is a register
						for (int i = 0; i < registersArray.length; i++) {
							
						}
						//if second is not a register
						
					}
				}
				
				//checks if first is a register
				for (int i = 0; i < registersArray.length; i++) {
					//if second is also a register
					if (first.equals(registersArray[i])) {
						this.registerValueArray[i] = second;
					}
					
					//if second is a declared variable
					//if second is not a register and not a declared variable
				}
			}
			
			//checks if line is for adding
			if (line.contains("add ")) {
				if (line.contains("//")) {
					endIndex = line.indexOf("//");
				}
				else {
					endIndex = line.length();
				}
				first = line.substring(line.indexOf(" "), line.indexOf(",")).trim();
				second = line.substring(line.indexOf(",")+1, endIndex).trim();
				convertedBody += "\t" + first + " = " + first + " + " + second + "; " + comment + "\n";
			}
			
			//checks if line is for subtracting
			if (line.contains("sub ")) {
				if (line.contains("//")) {
					endIndex = line.indexOf("//");
				}
				else {
					endIndex = line.length();
				}
				first = line.substring(line.indexOf(" "), line.indexOf(",")).trim();
				second = line.substring(line.indexOf(",")+1, endIndex).trim();
				convertedBody += "\t" + first + " = " + first + " - " + second + "; " + comment + "\n";
			}
			
			//checks if line is for printing a string
			/*if (line.contains("mov dx, offset") || line.contains("lea dx,")) {
				line = line.trim();
				String printfVar = line.substring(line.lastIndexOf(" "), line.length()).trim();
				String printfPercent = "";
				
				for (String[] dataArray : dataContent) {
					String variableName = dataArray[0].substring(dataArray[0].lastIndexOf(" ")+1, dataArray[0].length());
					if (variableName.equals(printfVar + "[]")) {
						printfPercent = "%s";
					}
				}
				
				convertedBody += "\tprintf(\"" + printfPercent + "\", " + printfVar + "); " + comment + "\n";
			}*/
			
			//checks if line is for printing a character
			
			//checks if line is for jumping (loop or if-else)
		}
		
		return convertedBody;
	}
	
	/**
	 * Returns the converted C Language code
	 * @return convertedCode - the complete converted C code
	 * */
	String getCode() {
		return ToCLanguage.convertedCode;
	}
	
}
