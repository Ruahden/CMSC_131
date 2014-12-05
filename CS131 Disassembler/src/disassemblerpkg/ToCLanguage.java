package disassemblerpkg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.StringTokenizer;

public class ToCLanguage {

	private static String convertedCode = new String();
	
	/*
	 * The .data variables in the assembly code
	 * String[0] - variable name
	 * String[1] - variable value
	 * */
	private static ArrayList<String[]> dataContent = new ArrayList<String[]>();	
	private ArrayList<String[]> dynamicData = new ArrayList<String[]>();
	private static String[] registersArray = {"al","ah","ax","bl","bh","bx","cl","ch","cx","dl","dh","dx",""};
	private String[] registerValueArray = {"","","|","","","|","","","|","","","|",""};
	private boolean[] registerBoolArray = new boolean[12];
	
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
		
		Arrays.fill(registerBoolArray, false);
		
		StringTokenizer tokenizer = new StringTokenizer(originalCode, "\n");
		while(tokenizer.hasMoreTokens()){
			String line = tokenizer.nextToken();
			
			if(line.equals(".data")){
				line = tokenizer.nextToken();
				while(!line.equals(".stack 100h")){
					String[] data = new String[2];
					
					int valueStartIndex = line.lastIndexOf("d") + 3,
						valueEndIndex = line.length();
					String variable = "",
						value = line.substring(valueStartIndex, valueEndIndex).trim();
					
					// gets variable name
					int variableStartIndex = 0;
					String character = line.substring(variableStartIndex, variableStartIndex + 1);
						while(!character.equals(" ")){
							variable += character;
							variableStartIndex++;
							character = line.substring(variableStartIndex, variableStartIndex + 1);
						};
					
					// gets variable value and data type
					// string
					if (line.contains("\'") || line.contains("\"")) {
						line = line.replace('\"', '\'');
						int count = line.length() - line.replace("\'", "").length();
						if (count >= 2) {
							valueStartIndex = line.indexOf("\'") + 1;
							valueEndIndex = line.lastIndexOf("\'");
							value = line.substring(valueStartIndex, valueEndIndex);
							String concatenated = new String("");
							String[] splitValues = value.split("\',|\' ,|,\'|, \'");
							for (int i = 0; i < splitValues.length; i++) {
								String temp = splitValues[i].trim();
								if (temp.equals("\'$")) {
									temp = "";
								}
								try {
									int tempInt = Integer.parseInt(temp);
									if (tempInt == 10) {
										temp = "\\n";
									} 
									else {
										temp = Character.toString((char) tempInt);
									}
								} catch (NumberFormatException e) {
								}
								concatenated += temp;
							}
							if (concatenated.contains("$")) {
								concatenated = concatenated.replace("$", "");
							}
							if (concatenated.length() == 1) {
								int decValue = (int) concatenated.charAt(0);
								value = Integer.toString(decValue);
								variable = "int " + variable;
							} 
							else {
								value = "\"" + concatenated + "\"";
								variable = "char " + variable + "[]";
							}
						}
					}
					// uninitialized variable
					else if (value.equals("?")) {
						value = "?";
					}
					// integer (hexadecimal, will be converted to decimal)
					else if (value.toLowerCase().matches(".*\\d.*[h]")) {
						value = value.toLowerCase();
						value = value.substring(0, value.lastIndexOf("h"));
						int hexaValue = Integer.parseInt(value, 16);
						value = Integer.toString(hexaValue);
						variable = "int " + variable;
					}
					// integer (decimal)
					else if (line.matches(".*\\d.*")) {
						variable = "int " + variable;
					}

					data[0] = variable;
					data[1] = value;
					dataContent.add(data);
					dynamicData.add(data);
					
					line = tokenizer.nextToken();
				}
				
			}
			
			if(line.contains("mov ds, ax")){
				line = tokenizer.nextToken();
				String body = new String();
				while(!line.contains("mov ax, 4c00h")){
					body += line + "\n";
					line = tokenizer.nextToken();
				}
				convertedCode[3] = bodyParser(body);
			}
			
			String dataLine, entireDataCode = "";
			for (String[] dataArray : dataContent) {
				if (dataArray[1].equals("?")) {
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
		/*
		 * 0 = if
		 * 1 = else
		 * 2 = do while
		 * 3 = while
		 */
		Stack<Integer> underWhat = new Stack<Integer>();
		String[] tempArray = new String[2]; 
		String convertedBody = "\n",
			firstVar = new String(""), secondVar = new String(""),
			labelName = new String("");
		int indexOfLine = 0;
		
		StringTokenizer bodyToken = new StringTokenizer(body, "\n");
		while(bodyToken.hasMoreTokens()){
			
			String line = bodyToken.nextToken();
			indexOfLine = indexOfLine + line.length();
			String first, second, binaryValue;
			//  checks if line is a label
			if (line.contains(":") && !line.contains("\"")) {
				if (underWhat.isEmpty()) { //  loop if underWhat is empty
					labelName = line.substring(0, line.indexOf(":")).trim();
					int jmpIndex = body.indexOf("jmp", indexOfLine);
					if (jmpIndex != -1) {
						int newLine = body.indexOf("\n", jmpIndex);
						String jmpLabel = body.substring(jmpIndex + 4, newLine);
						if (jmpLabel.equals(labelName)) {
							
							int indexOfcmp = body.indexOf("cmp", indexOfLine);
							int comma = body.indexOf(',', indexOfcmp);
							newLine = body.indexOf("\n", indexOfcmp);
							firstVar = body.substring(indexOfcmp + 4, comma).trim();
							secondVar = body.substring(comma + 1, newLine).trim();
							
							int conditionalJumpIndex = body.indexOf('j',newLine+1);
							newLine = body.indexOf("\n", conditionalJumpIndex);
							int indexOfSpace = body.indexOf(" ", conditionalJumpIndex);
							String theJump = body.substring(conditionalJumpIndex, indexOfSpace);
							
							if (theJump.equals("je")) {
								theJump = " != ";
							} else if (theJump.equals("jne")) {
								theJump = " == ";
							} else if (theJump.equals("jle")) {
								theJump = " > ";
							} else if (theJump.equals("jge")) {
								theJump = " < ";
							} else if (theJump.equals("jl")) {
								theJump = " >= ";
							} else if (theJump.equals("jg")) {
								theJump = " <= ";
							}
							
							convertedBody += "\twhile (" + firstVar + theJump + secondVar + ") { \n";
							underWhat.push(3);
						} 
						else {
							convertedBody += "\tdo {\n";
							underWhat.push(2);
						}
					} 
					else {
						convertedBody += "\tdo {\n";
						underWhat.push(2);
					}
				} 
				else {
					int x = underWhat.pop();
					if (x == 0) {
						convertedBody += "\t}\n";
					} 
				else if (x == 1) {
						int r = body.indexOf(labelName + ":");
						int s = body.indexOf(":", r);
						s++;
						while (body.charAt(s) == ' ' || body.charAt(s) == '\n') {
							s++;
						}
						String isThisCompare = body.substring(s, s + 3);
						if (isThisCompare.equals("cmp")) {
							convertedBody += "\t} else \n";
						} else {
							convertedBody += "\t} else { \n";
						}
						underWhat.push(0);
					} else if (x == 3) {
						convertedBody += "\t} \n";
					}
				}
			}

			// checks if line is for jumping
			else if (line.contains("jmp")) {
				int x = underWhat.pop();
				if (x == 0) {
					underWhat.push(1);
				} 
				else if (x == 3) {
					underWhat.push(3);
				}
			}
			
			// checks if line is for comparing
			else if (line.contains("cmp")) {
				String values = line.substring(line.indexOf("cmp") + 3).trim();
				StringTokenizer valuesTokenizer = new StringTokenizer(values, ",");
				firstVar = valuesTokenizer.nextToken().trim();
				secondVar = valuesTokenizer.nextToken().trim();
				if (underWhat.isEmpty()) {
					underWhat.push(0); // ibigsabihin ng 0 eh, if jump siya.
				}
			}
			
			// checks if line is for conditional jumping
			else if (line.contains("jne") || line.contains("je")
				|| line.contains("jl") || line.contains("jle")
				|| line.contains("jg") || line.contains("jge")) {

				StringTokenizer whatJump = new StringTokenizer(line, " ");
				String theJump = whatJump.nextToken();
				int x = underWhat.pop();

				if (x == 0) {
					if (theJump.equals("je")) {
						theJump = " != ";
					} else if (theJump.equals("jne")) {
						theJump = " == ";
					} else if (theJump.equals("jle")) {
						theJump = " > ";
					} else if (theJump.equals("jge")) {
						theJump = " < ";
					} else if (theJump.equals("jl")) {
						theJump = " >= ";
					} else if (theJump.equals("jg")) {
						theJump = " <= ";
					}

					convertedBody += "\tif(" + firstVar + theJump + secondVar + "){ \n";
					underWhat.push(0); // means the code is under IF
				} 
				else if (x == 2) {
					if (theJump.equals("je")) {
						theJump = " == ";
					} else if (theJump.equals("jne")) {
						theJump = " != ";
					} else if (theJump.equals("jle")) {
						theJump = " <= ";
					} else if (theJump.equals("jge")) {
						theJump = " >= ";
					} else if (theJump.equals("jl")) {
						theJump = " < ";
					} else if (theJump.equals("jg")) {
						theJump = " > ";
					}
					convertedBody += "\t} while (" + firstVar + theJump + secondVar + ");\n";

				} 
				else if (x == 3) {
					if (theJump.equals("je")) {
						theJump = " != ";
					} else if (theJump.equals("jne")) {
						theJump = " == ";
					} else if (theJump.equals("jle")) {
						theJump = " > ";
					} else if (theJump.equals("jge")) {
						theJump = " < ";
					} else if (theJump.equals("jl")) {
						theJump = " >= ";
					} else if (theJump.equals("jg")) {
						theJump = " <= ";
					}
					underWhat.push(3);
				}
			}
			
			// checks if line is interrupt for printing
			else if (line.toLowerCase().contains("int 21h")) {
				String printf = new String(""),
					beforeAh, afterAh, withoutAh, fullTemp;
				if (registerValueArray[1].equals("2") && !registerValueArray[9].equals("")) {
					printf = "\tprintf(\"%c\", dl);\n";
				}
				else if (registerValueArray[1].equals("9") && !registerValueArray[12].equals("")) {
					printf = "\tprintf(\"%s\", " + registerValueArray[12] + ");\n";
				}
				// removes last occurrence of ah which is ah = 9 or ah = 2
				beforeAh = convertedBody.substring(0, convertedBody.lastIndexOf("ah = "));
				afterAh = convertedBody.substring(convertedBody.lastIndexOf("ah = "), convertedBody.length());
				withoutAh = afterAh.substring(afterAh.indexOf("\n"), afterAh.length());
				fullTemp = beforeAh + withoutAh + printf;
				
				convertedBody = fullTemp;
			}
			
			// checks if line contains lea for printing
			else if (line.matches("lea dx, .*")) {
				registerValueArray[12] = line.substring(line.indexOf(",") + 1, line.length()).trim();
			}
			
			// checks if line is for assigning variables/registers
			else if (line.matches("mov .*")) {
				first = line.substring(line.indexOf(" "), line.indexOf(",")).trim();
				second = line.substring(line.indexOf(",") + 1, line.length()).trim();
				String variable = new String("");
				// checks if first is a declared variable
				int ctr = -1;
				boolean isFirstVariable = false;
				for (String[] dataArray : dynamicData) {
					ctr++;
					// if first is an uninitialized variable, determines data type and first value
					if (first.equals(dataArray[0]) && dataArray[1].equals("?")) {
						String tempValue = second;
						for (int i = 0; i < registersArray.length; i++) {
							if (second.equals(registersArray[i])) {
								tempValue = registerValueArray[i];
								break;
							}
						}
						// string / character
						if (line.contains("\'") || line.contains("\"")) {
							line = line.replace('\"', '\'');
							int count = line.length() - line.replace("\'", "").length();
							if (count >= 2) {
								int valueStartIndex = line.indexOf("\'") + 1;
								int valueEndIndex = line.lastIndexOf("\'");
								tempValue = line.substring(valueStartIndex, valueEndIndex);
								String concatenated = new String("");
								String[] splitValues = tempValue.split("\',|\' ,|,\'|, \'");
								for (int i = 0; i < splitValues.length; i++) {
									String temp = splitValues[i].trim();
									if (temp.equals("\'$")) {
										temp = "";
									}
									try {
										int tempInt = Integer.parseInt(temp);
										if (tempInt == 10) {
											temp = "\\n";
										} 
										else {
											temp = Character.toString((char) tempInt);
										}
									} catch (NumberFormatException e) {
									}
									concatenated += temp;
								}
								if (concatenated.contains("$")) {
									concatenated = concatenated.replace("$", "");
								}
								if (concatenated.length() == 1) {
									int decValue = (int) concatenated.charAt(0);
									tempValue = Integer.toString(decValue);
									variable = "int " + first;
								} 
								else {
									tempValue = "\"" + concatenated + "\"";
									variable = "char " + first + "[]";
								}
							}
						}
						// hexadecimal
						else if (tempValue.toLowerCase().matches(".*\\d.*[h]")) {
							tempValue = tempValue.substring(0, tempValue.toLowerCase().lastIndexOf("h"));
							int hexaValue = Integer.parseInt(tempValue, 16);
							tempValue = Integer.toString(hexaValue);
							variable = "int " + first;
						}
						// decimal
						else if (tempValue.matches(".*\\d.*")) {
							variable = "int " + first;
						}
						
						tempArray = new String[2];
						tempArray[0] = variable;
						tempArray[1] = tempValue;
						dynamicData.set(ctr, tempArray);
						dataContent.get(ctr)[0] = tempArray[0];
						convertedBody += "\t" + first + " = " + tempValue + ";\n";
						isFirstVariable = true;
						break;
					}
					// if first is already initialized
					else if ((dataArray[0].contains(" ")
						&& (first.equals(dataArray[0].substring(dataArray[0].indexOf(" ") + 1, dataArray[0].length()))))) { 
						/*|| (dataArray[0].contains(" ")
						&& (first.equals(dataArray[0].substring(dataArray[0].indexOf(" ") + 1, dataArray[0].length() - 2))))){*/
						
						// if second is a register
						boolean isSecondRegister = false;
						for (int i = 0; i < registersArray.length; i++) {
							if (second.equals(registersArray[i])) {
								tempArray = new String[2];
								tempArray[0] = dataArray[0];
								tempArray[1] = registerValueArray[i];
								dynamicData.set(ctr, tempArray);
								convertedBody += "\t" + first + " = " + second + ";\n";
								isSecondRegister = true;
								break;
							}
						}
						// if second is not a register (i.e. second is a specific value)
						if (!isSecondRegister) {
							if (second.toLowerCase().matches(".*\\d.*[h]")) {
								second = second.substring(0, second.toLowerCase().lastIndexOf("h"));
								int hexaValue = Integer.parseInt(second, 16);
								second = Integer.toString(hexaValue);
							}
							// character (will be converted to int)
							else if (second.contains("\'") || second.contains("\"")) {
								int decValue = (int) second.charAt(1);
								second = Integer.toString(decValue);
							}
							tempArray = new String[2];
							tempArray[0] = dataArray[0];
							tempArray[1] = second;
							dynamicData.set(ctr, tempArray);
							convertedBody += "\t" + first + " = " + second + ";\n";
						}
						isFirstVariable = true;
						break;
					}
				}

				// checks if first is a register
				if (!isFirstVariable) {
					for (int i = 0; i < registersArray.length; i++) {
						if (first.equals(registersArray[i])) {
							boolean isSecondRegister = false;
							// if second is also a register
							for (int j = 0; j < registersArray.length; j++) {
								if (second.equals(registersArray[j])) {
									// determines data type of register
									if (!registerBoolArray[i]) {
										variable = registersArray[i];
										tempArray = new String[2];
										// character
										if (registerValueArray[j].contains("\"") || registerValueArray[j].contains("\'")) {
											tempArray[0] = "int " + variable;
											tempArray[1] = registerValueArray[j];
										} 
										// integer (decimal)
										else if (registerValueArray[j].matches(".*\\d.*")) {
											tempArray[0] = "int " + variable;
											tempArray[1] = "?";
										}
										dataContent.add(tempArray);
										registerBoolArray[i] = true;
									}
									int delimiterIndex;
									if (first.matches("[abcd]h")) {
										binaryValue = Integer.toBinaryString(Integer.parseInt(registerValueArray[j]));
										delimiterIndex = registerValueArray[i+1].indexOf("|");
										registerValueArray[i+1] = registerValueArray[i+1]
											.substring(delimiterIndex, registerValueArray[i+1].length());
										delimiterIndex = registerValueArray[i+1].indexOf("|");
										registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
											.insert(delimiterIndex, binaryValue).toString();
										registerValueArray[i] = registerValueArray[j];
									}
									else if (first.matches("[abcd]l")) {
										binaryValue = Integer.toBinaryString(Integer.parseInt(registerValueArray[j]));
										delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
										registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
										delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
										registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
											.insert(delimiterIndex, binaryValue).toString();
										registerValueArray[i] = registerValueArray[j];
									}
									else if (first.matches("[abcd]x")) {
										binaryValue = new StringBuilder(registerValueArray[j]).
											deleteCharAt(registerValueArray[j].indexOf("|")).toString();
										if (Integer.parseInt(binaryValue, 2) < 256) {
											registerValueArray[i] = "|" + binaryValue;
											registerValueArray[i-1] = new String("");
											registerValueArray[i-2] = Integer.toString(Integer.parseInt(binaryValue, 2));
										}
										else {
											String high = binaryValue.substring(0, binaryValue.length()-8);
											String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
											registerValueArray[i] = high + "|" + low;
											registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
											registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
										}
									}
									convertedBody += "\t" + first + " = " + second + ";\n";
									isSecondRegister = true;
									break;
								}
							}

							boolean isSecondVariable = false;
							if (!isSecondRegister) {
								for (String[] dataArray : dynamicData) {
									// if second is a declared variable
									if ((dataArray[0].contains(" ")
											&& (second.equals(dataArray[0].substring(dataArray[0].indexOf(" ") + 1, dataArray[0].length()))))) { 
											/*|| (dataArray[0].contains(" ")
											&& (second.equals(dataArray[0].substring(dataArray[0].indexOf(" ") + 1, dataArray[0].length() - 2))))){*/
										// determines data type of register
										if (!registerBoolArray[i]) {
											variable = registersArray[i];
											tempArray = new String[2];
											// character
											if (dataArray[1].contains("\"") || dataArray[1].contains("\'")) {
												tempArray[0] = "int " + variable;
												tempArray[1] = dataArray[1];
											} 
											// integer (decimal)
											else if (dataArray[1].matches(".*\\d.*")) {
												tempArray[0] = "int " + variable;
												tempArray[1] = "?";
											}
											dataContent.add(tempArray);
											registerBoolArray[i] = true;
										}
										binaryValue = Integer.toBinaryString(Integer.parseInt(dataArray[1]));
										int delimiterIndex;
										if (first.matches("[abcd]h")) {
											delimiterIndex = registerValueArray[i+1].indexOf("|");
											registerValueArray[i+1] = registerValueArray[i+1]
												.substring(delimiterIndex, registerValueArray[i+1].length());
											delimiterIndex = registerValueArray[i+1].indexOf("|");
											registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
												.insert(delimiterIndex, binaryValue).toString();
											registerValueArray[i] = dataArray[1];
										}
										else if (first.matches("[abcd]l")) {
											delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
											registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
											delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
											registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
												.insert(delimiterIndex, binaryValue).toString();
											registerValueArray[i] = dataArray[1];
										}
										else if (first.matches("[abcd]x")) {
											if (Integer.parseInt(dataArray[1]) < 256) {
												registerValueArray[i] = "|" + binaryValue;
												registerValueArray[i-1] = new String("");
												registerValueArray[i-2] = dataArray[1];
											}
											else {
												String high = binaryValue.substring(0, binaryValue.length()-8);
												String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
												registerValueArray[i] = high + "|" + low;
												registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
												registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
											}
										}
										convertedBody += "\t" + first + " = " + second + ";\n";
										isSecondVariable = true;
										break;
									}
								}
							}
							// if second is not a register and not a declared variable (i.e. second is a specific value)
							if (!isSecondVariable && !isSecondRegister) {
								// hexadecimal
								if (second.toLowerCase().matches(".*\\d.*[h]")) {
									second = second.substring(0, second.toLowerCase().lastIndexOf("h"));
									int hexaValue = Integer.parseInt(second, 16);
									second = Integer.toString(hexaValue);
								}
								// character (will be converted to int)
								else if (second.contains("\'") || second.contains("\"")) {
									int decValue = (int) second.charAt(1);
									second = Integer.toString(decValue);
								}
								// determines data type of register
								if (!registerBoolArray[i]) {
									variable = registersArray[i];
									if (!second.matches("offset .*")) {										
										tempArray = new String[2];
										// character
										if (second.contains("\"") || second.contains("\'")) {
											tempArray[0] = "int " + variable;
											tempArray[1] = second;
										}
										// integer (decimal)
										else if (second.matches(".*\\d.*")) {
											tempArray[0] = "int " + variable;
											tempArray[1] = "?";
										}
										dataContent.add(tempArray);
										registerBoolArray[i] = true;
									}
								}
								int delimiterIndex;
								if (first.matches("[abcd]h")) {
									binaryValue = Integer.toBinaryString(Integer.parseInt(second));
									delimiterIndex = registerValueArray[i+1].indexOf("|");
									registerValueArray[i+1] = registerValueArray[i+1]
										.substring(delimiterIndex, registerValueArray[i+1].length());
									delimiterIndex = registerValueArray[i+1].indexOf("|");
									registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
										.insert(delimiterIndex, binaryValue).toString();
									registerValueArray[i] = second;
									convertedBody += "\t" + first + " = " + second + ";\n";
								}
								else if (first.matches("[abcd]l")) {
									binaryValue = Integer.toBinaryString(Integer.parseInt(second));
									delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
									registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
									delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
									registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
										.insert(delimiterIndex, binaryValue).toString();
									registerValueArray[i] = second;
									convertedBody += "\t" + first + " = " + second + ";\n";
								}
								else if (first.matches("[abcd]x")) {
									if (second.matches("offset .*")) {
										registerValueArray[12] = second.substring(second.indexOf(" "), second.length()).trim();
									}
									else if (Integer.parseInt(second) < 256) {
										binaryValue = Integer.toBinaryString(Integer.parseInt(second));
										registerValueArray[i] = "|" + binaryValue;
										registerValueArray[i-1] = new String("");
										registerValueArray[i-2] = second;
										convertedBody += "\t" + first + " = " + second + ";\n";
									}
									else {
										binaryValue = Integer.toBinaryString(Integer.parseInt(second));
										String high = binaryValue.substring(0, binaryValue.length()-8);
										String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
										registerValueArray[i] = high + "|" + low;
										registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
										registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
										convertedBody += "\t" + first + " = " + second + ";\n";
									}
								}
							}
						}
					}
				}
			}
			
			// checks if line is for adding
			else if (line.matches("add .*") || line.matches("inc .*")) {
				if (line.matches("add .*")) {
					first = line.substring(line.indexOf(" "), line.indexOf(",")).trim();
					second = line.substring(line.indexOf(",") + 1, line.length()).trim();
				}
				else {
					first = line.substring(line.indexOf(" "), line.length()).trim();
					second = "1";
				}
				// checks if first is a declared variable
				int ctr = -1;
				boolean isFirstVariable = false;
				for (String[] dataArray : dynamicData) {
					ctr++;
					// if first is already initialized
					if (dataArray[0].contains(" ")
						&& (first.equals(dataArray[0].substring(dataArray[0].indexOf(" ") + 1,
						dataArray[0].length())))) {
						// if second is a register
						boolean isSecondRegister = false;
						for (int i = 0; i < registersArray.length; i++) {
							if (second.equals(registersArray[i])) {
								int firstValue = Integer.parseInt(dataArray[1]);
								int secondValue = Integer.parseInt(registerValueArray[i]);
								firstValue = firstValue + secondValue;
								tempArray = new String[2];
								tempArray[0] = dataArray[0];
								tempArray[1] = Integer.toString(firstValue);
								dynamicData.set(ctr, tempArray);
								convertedBody += "\t" + first + " = " + first + " + " + second + ";\n";
								isSecondRegister = true;
								break;
							}
						}
						// if second is not a register (i.e. second is a specific value)
						if (!isSecondRegister) {
							// hexadecimal
							if (second.toLowerCase().matches(".*\\d.*[h]")) {
								second = second.substring(0, second.toLowerCase().lastIndexOf("h"));
								int hexaValue = Integer.parseInt(second, 16);
								second = Integer.toString(hexaValue);
							}
							// character (will be converted to int)
							else if (second.contains("\'") || second.contains("\"")) {
								int decValue = (int) second.charAt(1);
								second = Integer.toString(decValue);
							}
							int firstValue = Integer.parseInt(dataArray[1]);
							int secondValue = Integer.parseInt(second);
							firstValue = firstValue + secondValue;
							tempArray = new String[2];
							tempArray[0] = dataArray[0];
							tempArray[1] = Integer.toString(firstValue);
							dynamicData.set(ctr, tempArray);
							convertedBody += "\t" + first + " = " + first + " + " + second + ";\n";
						}
						isFirstVariable = true;
						break;
					}
				}
				
				// checks if first is a register
				if (!isFirstVariable) {
					for (int i = 0; i < registersArray.length; i++) {
						if (first.equals(registersArray[i])) {
							boolean isSecondRegister = false;
							// if second is also a register
							for (int j = 0; j < registersArray.length; j++) {
								if (second.equals(registersArray[j])) {
									int firstValue, secondValue, delimiterIndex;
									if (first.matches("[abcd]h")) {
										firstValue = Integer.parseInt(registerValueArray[i]);
										secondValue = Integer.parseInt(registerValueArray[j]);
										firstValue = firstValue + secondValue;
										binaryValue = Integer.toBinaryString(firstValue);
										delimiterIndex = registerValueArray[i+1].indexOf("|");
										registerValueArray[i+1] = registerValueArray[i+1]
											.substring(delimiterIndex, registerValueArray[i+1].length());
										delimiterIndex = registerValueArray[i+1].indexOf("|");
										registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
											.insert(delimiterIndex, binaryValue).toString();
										registerValueArray[i] = Integer.toString(firstValue);
									}
									else if (first.matches("[abcd]l")) {
										firstValue = Integer.parseInt(registerValueArray[i]);
										secondValue = Integer.parseInt(registerValueArray[j]);
										firstValue = firstValue + secondValue;
										binaryValue = Integer.toBinaryString(firstValue);
										delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
										registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
										delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
										registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
											.insert(delimiterIndex, binaryValue).toString();
										registerValueArray[i] = Integer.toString(firstValue);
									}
									else if (first.matches("[abcd]x")) {
										firstValue = Integer.parseInt(new StringBuilder(registerValueArray[i]).
											deleteCharAt(registerValueArray[i].indexOf("|")).toString(), 2);
										secondValue = Integer.parseInt(new StringBuilder(registerValueArray[j]).
											deleteCharAt(registerValueArray[j].indexOf("|")).toString(), 2);
										firstValue = firstValue + secondValue;
										binaryValue = Integer.toBinaryString(firstValue);
										if (firstValue < 256) {
											registerValueArray[i] = "|" + binaryValue;
											registerValueArray[i-1] = new String("");
											registerValueArray[i-2] = Integer.toString(firstValue);
										}
										else {
											String high = binaryValue.substring(0, binaryValue.length()-8);
											String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
											registerValueArray[i] = high + "|" + low;
											registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
											registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
										}
									}
									convertedBody += "\t" + first + " = " + first + " + " + second + ";\n";
									isSecondRegister = true;
									break;
								}
							}
							
							boolean isSecondVariable = false;
							if (!isSecondRegister) {
								for (String[] dataArray : dynamicData) {
									// if second is a declared variable
									if (dataArray[0].contains(" ")
										&& (second.equals(dataArray[0].substring(dataArray[0].indexOf(" ") + 1,
										dataArray[0].length())))) {
										int firstValue, secondValue, delimiterIndex;
										if (first.matches("[abcd]h")) {
											firstValue = Integer.parseInt(registerValueArray[i]);
											secondValue = Integer.parseInt(dataArray[1]);
											firstValue = firstValue + secondValue;
											binaryValue = Integer.toBinaryString(firstValue);
											delimiterIndex = registerValueArray[i+1].indexOf("|");
											registerValueArray[i+1] = registerValueArray[i+1]
												.substring(delimiterIndex, registerValueArray[i+1].length());
											delimiterIndex = registerValueArray[i+1].indexOf("|");
											registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
												.insert(delimiterIndex, binaryValue).toString();
											registerValueArray[i] = Integer.toString(firstValue);
										}
										else if (first.matches("[abcd]l")) {
											firstValue = Integer.parseInt(registerValueArray[i]);
											secondValue = Integer.parseInt(dataArray[1]);
											firstValue = firstValue + secondValue;
											binaryValue = Integer.toBinaryString(firstValue);
											delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
											registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
											delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
											registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
												.insert(delimiterIndex, binaryValue).toString();
											registerValueArray[i] = Integer.toString(firstValue);
										}
										else if (first.matches("[abcd]x")) {
											firstValue = Integer.parseInt(new StringBuilder(registerValueArray[i]).
												deleteCharAt(registerValueArray[i].indexOf("|")).toString(), 2);
											secondValue = Integer.parseInt(dataArray[1]);
											firstValue = firstValue + secondValue;
											binaryValue = Integer.toBinaryString(firstValue);
											if (firstValue < 256) {
												registerValueArray[i] = "|" + binaryValue;
												registerValueArray[i-1] = new String("");
												registerValueArray[i-2] = Integer.toString(firstValue);
											}
											else {
												String high = binaryValue.substring(0, binaryValue.length()-8);
												String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
												registerValueArray[i] = high + "|" + low;
												registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
												registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
											}
										}
										convertedBody += "\t" + first + " = " + first + " + " + second + ";\n";
										isSecondVariable = true;
										break;
									}
								}
							}
							// if second is not a register and not a declared variable (i.e. second is a specific value)
							if (!isSecondVariable && !isSecondRegister) {
								if (second.toLowerCase().matches(".*\\d.*[h]")) {
									second = second.substring(0, second.toLowerCase().lastIndexOf("h"));
									int hexaValue = Integer.parseInt(second, 16);
									second = Integer.toString(hexaValue);
								}
								// character (will be converted to int)
								else if (second.contains("\'") || second.contains("\"")) {
									int decValue = (int) second.charAt(1);
									second = Integer.toString(decValue);
								}
								int firstValue, secondValue, delimiterIndex;
								if (first.matches("[abcd]h")) {
									firstValue = Integer.parseInt(registerValueArray[i]);
									secondValue = Integer.parseInt(second);
									firstValue = firstValue + secondValue;
									binaryValue = Integer.toBinaryString(firstValue);
									delimiterIndex = registerValueArray[i+1].indexOf("|");
									registerValueArray[i+1] = registerValueArray[i+1]
										.substring(delimiterIndex, registerValueArray[i+1].length());
									delimiterIndex = registerValueArray[i+1].indexOf("|");
									registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
										.insert(delimiterIndex, binaryValue).toString();
									registerValueArray[i] = Integer.toString(firstValue);
									convertedBody += "\t" + first + " = " + first + " + " + second + ";\n";
								}
								else if (first.matches("[abcd]l")) {
									firstValue = Integer.parseInt(registerValueArray[i]);
									secondValue = Integer.parseInt(second);
									firstValue = firstValue + secondValue;
									binaryValue = Integer.toBinaryString(firstValue);
									delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
									registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
									delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
									registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
										.insert(delimiterIndex, binaryValue).toString();
									registerValueArray[i] = Integer.toString(firstValue);
									convertedBody += "\t" + first + " = " + first + " + " + second + ";\n";
								}
								else if (first.matches("[abcd]x")) {
									firstValue = Integer.parseInt(new StringBuilder(registerValueArray[i]).
										deleteCharAt(registerValueArray[i].indexOf("|")).toString(), 2);
									secondValue = Integer.parseInt(second);
									firstValue = firstValue + secondValue;
									binaryValue = Integer.toBinaryString(firstValue);
									if (firstValue < 256) {
										registerValueArray[i] = "|" + binaryValue;
										registerValueArray[i-1] = new String("");
										registerValueArray[i-2] = Integer.toString(firstValue);
										convertedBody += "\t" + first + " = " + first + " + " + second + ";\n";
									}
									else {
										String high = binaryValue.substring(0, binaryValue.length()-8);
										String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
										registerValueArray[i] = high + "|" + low;
										registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
										registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
										convertedBody += "\t" + first + " = " + first + " + " + second + ";\n";
									}
								}
							}
						}
					}
				}
			}
			
			// checks if line is for subtracting
			else if (line.matches("sub .*") || line.matches("dec .*")) {
				if (line.matches("sub .*")) {
					first = line.substring(line.indexOf(" "), line.indexOf(",")).trim();
					second = line.substring(line.indexOf(",") + 1, line.length()).trim();
				}
				else {
					first = line.substring(line.indexOf(" "), line.length()).trim();
					second = "1";
				}
				// checks if first is a declared variable
				int ctr = -1;
				boolean isFirstVariable = false;
				for (String[] dataArray : dynamicData) {
					ctr++;
					// if first is already initialized
					if (dataArray[0].contains(" ")
						&& (first.equals(dataArray[0].substring(dataArray[0].indexOf(" ") + 1,
						dataArray[0].length())))) {
						// if second is a register
						boolean isSecondRegister = false;
						for (int i = 0; i < registersArray.length; i++) {
							if (second.equals(registersArray[i])) {
								int firstValue = Integer.parseInt(dataArray[1]);
								int secondValue = Integer.parseInt(registerValueArray[i]);
								firstValue = firstValue - secondValue;
								tempArray = new String[2];
								tempArray[0] = dataArray[0];
								tempArray[1] = Integer.toString(firstValue);
								dynamicData.set(ctr, tempArray);
								convertedBody += "\t" + first + " = " + first + " - " + second + ";\n";
								isSecondRegister = true;
								break;
							}
						}
						// if second is not a register (i.e. second is a specific value)
						if (!isSecondRegister) {
							// hexadecimal
							if (second.toLowerCase().matches(".*\\d.*[h]")) {
								second = second.substring(0, second.toLowerCase().lastIndexOf("h"));
								int hexaValue = Integer.parseInt(second, 16);
								second = Integer.toString(hexaValue);
							}
							// character (will be converted to int)
							else if (second.contains("\'") || second.contains("\"")) {
								int decValue = (int) second.charAt(1);
								second = Integer.toString(decValue);
							}
							int firstValue = Integer.parseInt(dataArray[1]);
							int secondValue = Integer.parseInt(second);
							firstValue = firstValue - secondValue;
							tempArray = new String[2];
							tempArray[0] = dataArray[0];
							tempArray[1] = Integer.toString(firstValue);
							dynamicData.set(ctr, tempArray);
							convertedBody += "\t" + first + " = " + first + " - " + second + ";\n";
						}
						isFirstVariable = true;
						break;
					}
				}
				
				// checks if first is a register
				if (!isFirstVariable) {
					for (int i = 0; i < registersArray.length; i++) {
						if (first.equals(registersArray[i])) {
							boolean isSecondRegister = false;
							// if second is also a register
							for (int j = 0; j < registersArray.length; j++) {
								if (second.equals(registersArray[j])) {
									int firstValue, secondValue, delimiterIndex;
									if (first.matches("[abcd]h")) {
										firstValue = Integer.parseInt(registerValueArray[i]);
										secondValue = Integer.parseInt(registerValueArray[j]);
										firstValue = firstValue - secondValue;
										binaryValue = Integer.toBinaryString(firstValue);
										delimiterIndex = registerValueArray[i+1].indexOf("|");
										registerValueArray[i+1] = registerValueArray[i+1]
											.substring(delimiterIndex, registerValueArray[i+1].length());
										delimiterIndex = registerValueArray[i+1].indexOf("|");
										registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
											.insert(delimiterIndex, binaryValue).toString();
										registerValueArray[i] = Integer.toString(firstValue);
									}
									else if (first.matches("[abcd]l")) {
										firstValue = Integer.parseInt(registerValueArray[i]);
										secondValue = Integer.parseInt(registerValueArray[j]);
										firstValue = firstValue - secondValue;
										binaryValue = Integer.toBinaryString(firstValue);
										delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
										registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
										delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
										registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
											.insert(delimiterIndex, binaryValue).toString();
										registerValueArray[i] = Integer.toString(firstValue);
									}
									else if (first.matches("[abcd]x")) {
										firstValue = Integer.parseInt(new StringBuilder(registerValueArray[i]).
											deleteCharAt(registerValueArray[i].indexOf("|")).toString(), 2);
										secondValue = Integer.parseInt(new StringBuilder(registerValueArray[j]).
											deleteCharAt(registerValueArray[j].indexOf("|")).toString(), 2);
										firstValue = firstValue + secondValue;
										binaryValue = Integer.toBinaryString(firstValue);
										if (firstValue < 256) {
											registerValueArray[i] = "|" + binaryValue;
											registerValueArray[i-1] = new String("");
											registerValueArray[i-2] = Integer.toString(firstValue);
										}
										else {
											String high = binaryValue.substring(0, binaryValue.length()-8);
											String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
											registerValueArray[i] = high + "|" + low;
											registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
											registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
										}
									}
									convertedBody += "\t" + first + " = " + first + " - " + second + ";\n";
									isSecondRegister = true;
									break;
								}
							}
							
							boolean isSecondVariable = false;
							if (!isSecondRegister) {
								for (String[] dataArray : dynamicData) {
									// if second is a declared variable
									if (dataArray[0].contains(" ")
										&& (second.equals(dataArray[0].substring(dataArray[0].indexOf(" ") + 1,
										dataArray[0].length())))) {
										int firstValue, secondValue, delimiterIndex;
										if (first.matches("[abcd]h")) {
											firstValue = Integer.parseInt(registerValueArray[i]);
											secondValue = Integer.parseInt(dataArray[1]);
											firstValue = firstValue - secondValue;
											binaryValue = Integer.toBinaryString(firstValue);
											delimiterIndex = registerValueArray[i+1].indexOf("|");
											registerValueArray[i+1] = registerValueArray[i+1]
												.substring(delimiterIndex, registerValueArray[i+1].length());
											delimiterIndex = registerValueArray[i+1].indexOf("|");
											registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
												.insert(delimiterIndex, binaryValue).toString();
											registerValueArray[i] = Integer.toString(firstValue);
										}
										else if (first.matches("[abcd]l")) {
											firstValue = Integer.parseInt(registerValueArray[i]);
											secondValue = Integer.parseInt(dataArray[1]);
											firstValue = firstValue - secondValue;
											binaryValue = Integer.toBinaryString(firstValue);
											delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
											registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
											delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
											registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
												.insert(delimiterIndex, binaryValue).toString();
											registerValueArray[i] = Integer.toString(firstValue);
										}
										else if (first.matches("[abcd]x")) {
											firstValue = Integer.parseInt(new StringBuilder(registerValueArray[i]).
												deleteCharAt(registerValueArray[i].indexOf("|")).toString(), 2);
											secondValue = Integer.parseInt(dataArray[1]);
											firstValue = firstValue - secondValue;
											binaryValue = Integer.toBinaryString(firstValue);
											if (firstValue < 256) {
												registerValueArray[i] = "|" + binaryValue;
												registerValueArray[i-1] = new String("");
												registerValueArray[i-2] = Integer.toString(firstValue);
											}
											else {
												String high = binaryValue.substring(0, binaryValue.length()-8);
												String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
												registerValueArray[i] = high + "|" + low;
												registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
												registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
											}
										}
										convertedBody += "\t" + first + " = " + first + " - " + second + ";\n";
										isSecondVariable = true;
										break;
									}
								}
							}
							// if second is not a register and not a declared variable (i.e. second is a specific value)
							if (!isSecondVariable && !isSecondRegister) {
								// hexadecimal
								if (second.toLowerCase().matches(".*\\d.*[h]")) {
									second = second.substring(0, second.toLowerCase().lastIndexOf("h"));
									int hexaValue = Integer.parseInt(second, 16);
									second = Integer.toString(hexaValue);
								}
								// character (will be converted to int)
								else if (second.contains("\'") || second.contains("\"")) {
									int decValue = (int) second.charAt(1);
									second = Integer.toString(decValue);
								}
								int firstValue, secondValue, delimiterIndex;
								if (first.matches("[abcd]h")) {
									firstValue = Integer.parseInt(registerValueArray[i]);
									secondValue = Integer.parseInt(second);
									firstValue = firstValue - secondValue;
									binaryValue = Integer.toBinaryString(firstValue);
									delimiterIndex = registerValueArray[i+1].indexOf("|");
									registerValueArray[i+1] = registerValueArray[i+1]
										.substring(delimiterIndex, registerValueArray[i+1].length());
									delimiterIndex = registerValueArray[i+1].indexOf("|");
									registerValueArray[i+1] = new StringBuilder(registerValueArray[i+1])
										.insert(delimiterIndex, binaryValue).toString();
									registerValueArray[i] = Integer.toString(firstValue);
									convertedBody += "\t" + first + " = " + first + " - " + second + ";\n";
								}
								else if (first.matches("[abcd]l")) {
									firstValue = Integer.parseInt(registerValueArray[i]);
									secondValue = Integer.parseInt(second);
									firstValue = firstValue - secondValue;
									binaryValue = Integer.toBinaryString(firstValue);
									delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
									registerValueArray[i+2] = registerValueArray[i+2].substring(0, delimiterIndex);
									delimiterIndex = registerValueArray[i+2].indexOf("|") + 1;
									registerValueArray[i+2] = new StringBuilder(registerValueArray[i+2])
										.insert(delimiterIndex, binaryValue).toString();
									registerValueArray[i] = Integer.toString(firstValue);
									convertedBody += "\t" + first + " = " + first + " - " + second + ";\n";
								}
								else if (first.matches("[abcd]x")) {
									firstValue = Integer.parseInt(new StringBuilder(registerValueArray[i]).
										deleteCharAt(registerValueArray[i].indexOf("|")).toString(), 2);
									secondValue = Integer.parseInt(second);
									firstValue = firstValue - secondValue;
									binaryValue = Integer.toBinaryString(firstValue);
									if (firstValue < 256) {
										registerValueArray[i] = "|" + binaryValue;
										registerValueArray[i-1] = new String("");
										registerValueArray[i-2] = Integer.toString(firstValue);
										convertedBody += "\t" + first + " = " + first + " - " + second + ";\n";
									}
									else {
										String high = binaryValue.substring(0, binaryValue.length()-8);
										String low = binaryValue.substring(binaryValue.length()-8, binaryValue.length());
										registerValueArray[i] = high + "|" + low;
										registerValueArray[i-1] = Integer.toString(Integer.parseInt(high, 10));
										registerValueArray[i-2] = Integer.toString(Integer.parseInt(low, 10));
										convertedBody += "\t" + first + " = " + first + " - " + second + ";\n";
									}
								}
							}
						}
					}
				}
			}
			
			// int gr=-1;for(String[] array:dynamicData) {
				// gr++;System.out.println(gr +" "+array[0] + " " + array[1]); }
			// int grr=-1;for(String[] array:dataContent) {
				// grr++;System.out.println(grr +" "+array[0] + " " + array[1]); }
			// for (int i = 0; i < registersArray.length; i++) {
				// System.out.println(registersArray[i] + " " + registerValueArray[i]); }
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
