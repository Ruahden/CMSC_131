/*
 * MGA TANONG:
 * 	pano pag int? pag char? pano itretreat? 
 */

package disassemblerpkg;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ToAssembly {

	private static String convertedCode = new String();
	private static ArrayList<String> dataContent = new ArrayList<String>();
	
	/*
	 * Function:
	 * 	Converts input C code to assembly code
	 * Parameter:
	 * 	String originalCode - the C code to be converted
	 * Returns:
	 * 	none
	 * */
	ToAssembly(String originalCode) {
		
		/*
		 * convertedCode[0] - from start of assembly code to .data
		 * convertedCode[1] - the content of the data segment
		 * convertedCode[2] - from .code to the start of the body
		 * convertedCode[3] - the body
		 * convertedCode[4] - the end of body
		 * */
		String[] convertedCode = {"", "", "", "", ""};
		
		convertedCode[0] = ".model small \n";
		convertedCode[0] += ".stack 100h \n";
		convertedCode[0] += ".data \n";
				
		StringTokenizer tokenizer = new StringTokenizer(originalCode, "\n");
		while(tokenizer.hasMoreTokens()){
			String line = tokenizer.nextToken();
			
			if(line.contains("main(){")){
				convertedCode[2] += ".code \n\n";
				convertedCode[2] += "main proc \n\n";
				convertedCode[2] += "\t mov ax, @data \n";
				convertedCode[2] += "\t mov ds, ax \n\n";
				
				line = tokenizer.nextToken();
				String body = new String();
				while(!(line.contains("}") && !tokenizer.hasMoreTokens())){
					body += line + "\n";
					line = tokenizer.nextToken();
				}
							
				convertedCode[3] = ToAssembly.bodyParser(body);
				
				convertedCode[4] += "\t mov ax, 4c00h \n";
				convertedCode[4] += "\t int 21h \n\n";
				convertedCode[4] += "main endp \n";
				convertedCode[4] += "end main";
			}
		}
		
		dataContent.add("\t temp db 0 \n"); //used for many stuff
		
		for(int i = 0; i < dataContent.size(); i++){
			convertedCode[1] += dataContent.get(i);
		}
		
		for(String code :  convertedCode){
			ToAssembly.convertedCode += code;
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
		
		String line = "";
		String newBody = "";
		String command = "";
		String variableName = "";
		String value = "";
		String condition = "";
		String postActivity = "";
		
		boolean thereIsAnElse = false;
		boolean forLoop = false;
		boolean whileCurlyBrace = false;
		
		int firstParenthesis = 0;
		int secondParenthesis = 0;
		int equals = 0;
		int labelNumber = 0;
		int indexOfLine = 0;
		int stringNumber = 0;
		int doNumber = 0;
		int whileNumber = 0;
		int forNumber = 0;
				
		StringTokenizer tokenizer = new StringTokenizer(body, "\n");
		
		while(tokenizer.hasMoreTokens()){
			
			line = tokenizer.nextToken().trim();
			indexOfLine = indexOfLine + line.length();
			
			if(!line.equals("")){
				
				firstParenthesis = line.indexOf("(");
				
				if(firstParenthesis != -1){
					command = line.substring(0,firstParenthesis);
				}else{
					command = "";
				}
				
				if(line.contains("while") && line.contains("}")){
					
					int indexOfWhile = line.indexOf("while"); 
					firstParenthesis = line.indexOf("(", indexOfWhile + 1);
					secondParenthesis = line.indexOf(")", firstParenthesis);
					condition = line.substring(firstParenthesis+1, secondParenthesis);
					
					int lessThan = condition.indexOf("<");
					int lessThanOrEqualTo = condition.indexOf("<=");
					int greaterThan = condition.indexOf(">");
					int greaterThanOrEqualTo = condition.indexOf(">=");
					int notEqualTo = condition.indexOf("!=");
					equals = condition.indexOf("==");
					
					if(equals != -1){
						variableName = condition.substring(0,equals);
						value = condition.substring(equals+2);
					}else if(notEqualTo != -1){
						variableName = condition.substring(0,notEqualTo);
						value = condition.substring(notEqualTo+2);
					}else if(lessThanOrEqualTo != -1){
						variableName = condition.substring(0,lessThanOrEqualTo);
						value = condition.substring(lessThanOrEqualTo+2);
					}else if(greaterThanOrEqualTo != -1){
						variableName = condition.substring(0,greaterThanOrEqualTo);
						value = condition.substring(greaterThanOrEqualTo+2);
					}else if(lessThan != -1){
						variableName = condition.substring(0,lessThan);
						value = condition.substring(lessThan+1);
					}else if(greaterThan != -1){
						variableName = condition.substring(0,greaterThan);
						value = condition.substring(greaterThan+1);
					}

					variableName = variableName.trim();
					value = value.trim();
					
					newBody += "\t mov dl, "+value+" \n";
					newBody += "\t cmp "+variableName+", dl \n";
					
					if(equals != -1){
						newBody += "\t je do"+doNumber+" \n\n";						
					}else if(notEqualTo != -1){
						newBody += "\t jne do"+doNumber+" \n\n";						
					}else if(lessThanOrEqualTo != -1){
						newBody += "\t jle do"+doNumber+" \n\n";						
					}else if(greaterThanOrEqualTo != -1){
						newBody += "\t jge do"+doNumber+" \n\n";						
					}else if(lessThan != -1){
						newBody += "\t jl do"+doNumber+" \n\n";						
					}else if(greaterThan != -1){
						newBody += "\t jg do"+doNumber+" \n\n";
					}
				}
				
				else if(line.contains("}")){
					if(thereIsAnElse){
						labelNumber++;
						newBody += "\t jmp label"+labelNumber+" \n";
						labelNumber--;
						newBody += "\t label"+labelNumber+": \n\n";
						labelNumber++;
						thereIsAnElse = false;
					}
					else if(whileCurlyBrace){
						whileNumber--;
						newBody += "\t jmp while"+whileNumber+" \n";
						whileNumber++;
						newBody += "\t while"+whileNumber+": \n\n";
						whileCurlyBrace = false;
					}
					else if(forLoop){
						newBody += bodyParser(postActivity);
						forNumber--;
						newBody += "\t jmp for"+forNumber+" \n";
						forNumber++;
						newBody += "\t for"+forNumber+": \n\n";
						forLoop = false;
					}
					else{
						newBody += "\t label"+labelNumber+": \n\n";
					}
				}
				
				/* IF THE COMMAND IS PRINTF 
				 * make this more dynamic
				 * */	
				if(command.equals("printf")){
										
					int firstDoubleQuote = line.indexOf("\"");
					int secondDoubleQuote = line.indexOf("\"", firstDoubleQuote+1);
					String string = line.substring(firstDoubleQuote+1, secondDoubleQuote);
					
					stringNumber++;
					dataContent.add("\t string"+stringNumber+" db \""+string+"$\" \n");
					
					newBody += "\t lea dx, string"+stringNumber+" \n";
					newBody += "\t mov ah, 09h \n";
					newBody += "\t int 21h \n\n";
				}
				
				/* IF COMMAND CONTAINS CONDITIONAL STATEMENT 
				 * 
				 * POSSIBILITES:
				 * 	- if(age == 5)
				 * 	- if(name == "haha")
				 * 
				 * GUMAGANA NA PAG MAY IF-ELSE
				 * 
				 * */
				else if(command.equals("if")){
					
					firstParenthesis = line.indexOf("(");
					secondParenthesis = line.indexOf(")");
					condition = line.substring(firstParenthesis+1, secondParenthesis);
					
					int lessThan = condition.indexOf("<");
					int lessThanOrEqualTo = condition.indexOf("<=");
					int greaterThan = condition.indexOf(">");
					int greaterThanOrEqualTo = condition.indexOf(">=");
					int notEqualTo = condition.indexOf("!=");
					equals = condition.indexOf("==");
					
					if(equals != -1){
						variableName = condition.substring(0,equals);
						value = condition.substring(equals+2);
					}else if(notEqualTo != -1){
						variableName = condition.substring(0,notEqualTo);
						value = condition.substring(notEqualTo+2);
					}else if(lessThanOrEqualTo != -1){
						variableName = condition.substring(0,lessThanOrEqualTo);
						value = condition.substring(lessThanOrEqualTo+2);
					}else if(greaterThanOrEqualTo != -1){
						variableName = condition.substring(0,greaterThanOrEqualTo);
						value = condition.substring(greaterThanOrEqualTo+2);
					}else if(lessThan != -1){
						variableName = condition.substring(0,lessThan);
						value = condition.substring(lessThan+1);
					}else if(greaterThan != -1){
						variableName = condition.substring(0,greaterThan);
						value = condition.substring(greaterThan+1);
					}

					variableName = variableName.trim();
					value = value.trim();
					
					labelNumber++;
					newBody += "\t mov dl, "+value+" \n";
					newBody += "\t cmp " + variableName + ", dl \n";
					
					if(equals != -1){
						newBody += "\t jne label" + labelNumber + " \n\n";	
					}else if(notEqualTo != -1){
						newBody += "\t je label" + labelNumber + " \n\n";				
					}else if(lessThanOrEqualTo != -1){
						newBody += "\t jg label" + labelNumber + " \n\n";					
					}else if(greaterThanOrEqualTo != -1){
						newBody += "\t jl label" + labelNumber + " \n\n";				
					}else if(lessThan != -1){
						newBody += "\t jge label" + labelNumber + " \n\n";					
					}else if(greaterThan != -1){
						newBody += "\t jle label" + labelNumber + " \n\n";
					}
					
					//search for an else.
					int indexOfIf = body.indexOf("if", indexOfLine);
					int indexOfElse = body.indexOf("else", indexOfLine);
					
					// if indexOfElse > indexOfIf, it means that the else is not the else of this if statement.
					if((indexOfElse < indexOfIf) || (indexOfElse!=1 && indexOfIf == -1)){
						thereIsAnElse = true;
					}
				}
				
				/*
				 * IF ITERATION IS WHILE
				 */
				else if(command.equals("while")){
					firstParenthesis = line.indexOf("(");
					secondParenthesis = line.indexOf(")");
					condition = line.substring(firstParenthesis+1, secondParenthesis);
					
					int lessThan = condition.indexOf("<");
					int lessThanOrEqualTo = condition.indexOf("<=");
					int greaterThan = condition.indexOf(">");
					int greaterThanOrEqualTo = condition.indexOf(">=");
					int notEqualTo = condition.indexOf("!=");
					equals = condition.indexOf("==");
					
					if(equals != -1){
						variableName = condition.substring(0,equals);
						value = condition.substring(equals+2);
					}else if(notEqualTo != -1){
						variableName = condition.substring(0,notEqualTo);
						value = condition.substring(notEqualTo+2);
					}else if(lessThanOrEqualTo != -1){
						variableName = condition.substring(0,lessThanOrEqualTo);
						value = condition.substring(lessThanOrEqualTo+2);
					}else if(greaterThanOrEqualTo != -1){
						variableName = condition.substring(0,greaterThanOrEqualTo);
						value = condition.substring(greaterThanOrEqualTo+2);
					}else if(lessThan != -1){
						variableName = condition.substring(0,lessThan);
						value = condition.substring(lessThan+1);
					}else if(greaterThan != -1){
						variableName = condition.substring(0,greaterThan);
						value = condition.substring(greaterThan+1);
					}

					variableName = variableName.trim();
					value = value.trim();
					
					whileNumber++;
					newBody += "\t while"+whileNumber+": \n";
					newBody += "\t mov dl, "+value+" \n";
					newBody += "\t cmp "+variableName+", dl \n";
	
					whileNumber++;
					if(equals != -1){
						newBody += "\t jne while" + whileNumber + " \n\n";	
					}else if(notEqualTo != -1){
						newBody += "\t je while" + whileNumber + " \n\n";				
					}else if(lessThanOrEqualTo != -1){
						newBody += "\t jg while" + whileNumber + " \n\n";					
					}else if(greaterThanOrEqualTo != -1){
						newBody += "\t jl while" + whileNumber + " \n\n";				
					}else if(lessThan != -1){
						newBody += "\t jge while" + whileNumber + " \n\n";					
					}else if(greaterThan != -1){
						newBody += "\t jle while" + whileNumber + " \n\n";
					}
					
					whileCurlyBrace = true;
				}
				/* IF COMMAND CONTAINS do ITERATION*/
				else if(line.contains("do")){
					doNumber++;
					newBody += "\t do"+doNumber+": \n\n";
				}
				
				/* IF COMMAND CONTAINS for ITERATION*/
				else if(line.contains("for")){
					firstParenthesis = line.indexOf("(");
					secondParenthesis = line.indexOf(")");
					String insideOfFor = line.substring(firstParenthesis+1, secondParenthesis);
					StringTokenizer forTokenizer = new StringTokenizer(insideOfFor, ";");
					
					String initialization = forTokenizer.nextToken();
					condition = forTokenizer.nextToken();
					postActivity = forTokenizer.nextToken();
					
					//converting initialization
					if(!initialization.isEmpty()){
						equals = initialization.indexOf("=");
						//CASE: int age=5 or age=5. OR age = age + 5, or age += 5 or age = age + another
						if(equals != -1){
							
							//getting string to the left of equals.
							String leftside = initialization.substring(0, equals);
							StringTokenizer leftsideTokenizer = new StringTokenizer(leftside, " ");
							
							// CASE: age=5
							if(leftsideTokenizer.countTokens() == 1){
								variableName = leftsideTokenizer.nextToken();
								value = initialization.substring(equals+1);
								value = value.trim();
								
								// CASE: age = age + 5
								if(value.contains("+")){
									/*
									 * GET variable name, tapos mov 0 dun sa variable name
									 * 
									 * ASSUMING THAT THE VARIABLE IS DB
									 */
									StringTokenizer valueTokenizer = new StringTokenizer(value, "+");
									
									newBody += "\t mov temp, 0 \n\n ";
									
									while(valueTokenizer.hasMoreTokens()){
										newBody += "\t mov dl, "+valueTokenizer.nextToken().trim()+" \n ";
										newBody += "\t add temp, dl \n\n ";
									}
									newBody += "\t mov dl, temp \n ";
									newBody += "\t mov "+variableName+", dl \n\n ";
									
								}else if(value.contains("-")){
									StringTokenizer valueTokenizer = new StringTokenizer(value, "-");
									
									newBody += "\t mov temp, 0 \n\n ";
																		
									newBody += "\t mov dl, "+valueTokenizer.nextToken().trim()+" \n\n ";
									newBody += "\t mov temp, dl \n\n ";
									
									while(valueTokenizer.hasMoreTokens()){
										newBody += "\t mov dl, "+valueTokenizer.nextToken().trim()+" \n";
										newBody += "\t sub temp, dl \n\n ";
									}
									newBody += "\t mov dl, temp \n ";
									newBody += "\t mov "+variableName+", dl \n\n ";
									
								}
								else{
									newBody += "\t mov dl, "+value+"\n ";
									newBody += "\t mov " + variableName + ",dl \n\n ";
								}						
							}
						}
					}
					
					if(!condition.isEmpty()){
						int lessThan = condition.indexOf("<");
						int lessThanOrEqualTo = condition.indexOf("<=");
						int greaterThan = condition.indexOf(">");
						int greaterThanOrEqualTo = condition.indexOf(">=");
						int notEqualTo = condition.indexOf("!=");
						equals = condition.indexOf("==");
						
						if(equals != -1){
							variableName = condition.substring(0,equals);
							value = condition.substring(equals+2);
						}else if(notEqualTo != -1){
							variableName = condition.substring(0,notEqualTo);
							value = condition.substring(notEqualTo+2);
						}else if(lessThanOrEqualTo != -1){
							variableName = condition.substring(0,lessThanOrEqualTo);
							value = condition.substring(lessThanOrEqualTo+2);
						}else if(greaterThanOrEqualTo != -1){
							variableName = condition.substring(0,greaterThanOrEqualTo);
							value = condition.substring(greaterThanOrEqualTo+2);
						}else if(lessThan != -1){
							variableName = condition.substring(0,lessThan);
							value = condition.substring(lessThan+1);
						}else if(greaterThan != -1){
							variableName = condition.substring(0,greaterThan);
							value = condition.substring(greaterThan+1);
						}

						variableName = variableName.trim();
						value = value.trim();
						
						forNumber++;
						newBody += "\t for"+forNumber+": \n";
						newBody += "\t mov dl, "+value+" \n";
						newBody += "\t cmp "+variableName+", dl \n";
		
						forNumber++;
						if(equals != -1){
							newBody += "\t jne for" + forNumber + " \n\n";	
						}else if(notEqualTo != -1){
							newBody += "\t je for" + forNumber + " \n\n";				
						}else if(lessThanOrEqualTo != -1){
							newBody += "\t jg for" + forNumber + " \n\n";					
						}else if(greaterThanOrEqualTo != -1){
							newBody += "\t jl for" + forNumber + " \n\n";				
						}else if(lessThan != -1){
							newBody += "\t jge for" + forNumber + " \n\n";					
						}else if(greaterThan != -1){
							newBody += "\t jle for" + forNumber + " \n\n";
						}
						
						forLoop = true;
					}
				}
				
				/* IF USER USES ++ or --
				 */
				else if(line.contains("++") || line.contains("--")){
					variableName = line.replace(";", "");
					variableName = variableName.replace("++", "");
					variableName = variableName.trim();
					
					newBody += "\t inc "+variableName+" \n\n";
				}
				
				else if(line.contains("--")){
					variableName = line.replace(";", "");
					variableName = variableName.replace("--", "");
					variableName = variableName.trim();
					
					newBody += "\t dec "+variableName+" \n\n";
				}
				
				/* ASSUMES THAT IF NO COMMAND IS INPUT, IT IS A VARIABLE DECLARATION 
				 * make this more dynamic
				 * assume once lang nagdeclare ng variable
				 * 
				 * POSSIBILITES:
				 * 	- int age;
				 * 	- int age=5;
				 * 	- age=5;
				 * 	- } or { 
				 * 	- char, float?
				 * 
				 * QUERIES:
				 * 	- pano yung type?
				 **/				
				else if(command.equals("")){
					
					// counting tokens, excluding ";"
					if(line.lastIndexOf(";") != -1){
						
						String dataType = "";
						
						StringTokenizer lineTokenizer = new StringTokenizer(line.substring(0, line.lastIndexOf(";")), " ");
					
						// getting index of equals sign
						equals = line.indexOf("=");
						
						// CASE: int age;
						if(lineTokenizer.countTokens() == 2 && equals == -1){
							dataType = lineTokenizer.nextToken();
							variableName = lineTokenizer.nextToken();
							dataContent.add("\t " + variableName + " db 0 \n");
						}
						
						//CASE: int age=5 or age=5. OR age = age + 5, or age += 5 or age = age + another
						else if(equals != -1){
							
							//getting string to the left of equals.
							String leftside = line.substring(0, equals);
							StringTokenizer leftsideTokenizer = new StringTokenizer(leftside, " ");
							
							// CASE: int age=5
							if(leftsideTokenizer.countTokens() == 2){
								int firstSpaceBar = line.indexOf(" ");
								dataType = leftsideTokenizer.nextToken();
								variableName = leftsideTokenizer.nextToken();
								value = line.substring(equals+1, line.lastIndexOf(";"));
								value = value.trim();
								dataContent.add("\t "+variableName+" db " + value + "\n");
							}
							
							// CASE: age=5
							else if(leftsideTokenizer.countTokens() == 1){
								variableName = leftsideTokenizer.nextToken();
								value = line.substring(equals+1, line.lastIndexOf(";"));
								value = value.trim();
								
								// CASE: age = age + 5
								if(value.contains("+")){
									/*
									 * GET variable name, tapos mov 0 dun sa variable name
									 * 
									 * ASSUMING THAT THE VARIABLE IS DB
									 */
									StringTokenizer valueTokenizer = new StringTokenizer(value, "+");
									
									newBody += "\t mov temp, 0 \n\n ";
									
									while(valueTokenizer.hasMoreTokens()){
										newBody += "\t mov dl, "+valueTokenizer.nextToken().trim()+" \n ";
										newBody += "\t add temp, dl \n\n ";
									}
									newBody += "\t mov dl, temp \n ";
									newBody += "\t mov "+variableName+", dl \n\n ";
									
								}else if(value.contains("-")){
									StringTokenizer valueTokenizer = new StringTokenizer(value, "-");
									
									newBody += "\t mov temp, 0 \n\n ";
																		
									newBody += "\t mov dl, "+valueTokenizer.nextToken().trim()+" \n\n ";
									newBody += "\t mov temp, dl \n\n ";
									
									while(valueTokenizer.hasMoreTokens()){
										newBody += "\t mov dl, "+valueTokenizer.nextToken().trim()+" \n";
										newBody += "\t sub temp, dl \n\n ";
									}
									newBody += "\t mov dl, temp \n ";
									newBody += "\t mov "+variableName+", dl \n\n ";
									
								}
								else{
									newBody += "\t mov " + variableName + ", " + value + " \n\n ";
								}						
							}
						}					
					}
				}
			}
		}
		return newBody;
	}

	/*
	 * Function:
	 * 	Returns the converted assembly language
	 * Parameter:
	 * 	none
	 * Returns:
	 * 	String - the converted assembly code
	 * */
	String getCode() {
		return ToAssembly.convertedCode;
	}
}
