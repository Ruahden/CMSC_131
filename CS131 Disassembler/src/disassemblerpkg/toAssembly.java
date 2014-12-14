/*
 * bugs:
 * 	di pa nahahandle char with commas in between
 * 	di pa nahahandle yung comments
 *  di pa nahahandle yung += and -= na dynamic. 
*/

package disassemblerpkg;

import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

public class ToAssembly {

	private static String convertedCode = new String();
	private static ArrayList<String> dataContent = new ArrayList<String>();
	private static ArrayList<String[]> dynamicData = new ArrayList<String[]>();
	
	/*
	 * Function:
	 * 	Converts input C code to assembly code
	 * Parameter:
	 * 	String originalCode - the C code to be converted
	 * Returns:
	 * 	none
	 * */
	ToAssembly(String originalCode) {
				
		ToAssembly.convertedCode = "";
		ToAssembly.dataContent.clear();
		ToAssembly.dynamicData.clear();
		
		String[] convertedCode = {"", "", "", "", ""};
		
		convertedCode[0] = ".model small \n";
		convertedCode[0] += ".stack 100h \n";
		convertedCode[0] += ".data \n";
				
		StringTokenizer tokenizer = new StringTokenizer(originalCode, "\n");
		while(tokenizer.hasMoreTokens()){
			String line = tokenizer.nextToken();
			
			if(line.contains("main()")){
				convertedCode[2] += ".code \n\n";
				
				convertedCode[2] += "print proc \n\n";
				convertedCode[2] += "\txor dx,dx \n";
				convertedCode[2] += "\tmov cx, 10000\n";
				convertedCode[2] += "\tcmp ax,0\n";
				convertedCode[2] += "\tjne zero\n";
				convertedCode[2] += "\tmov dl, '0'\n";
				convertedCode[2] += "\tmov ah, 02h\n";
				convertedCode[2] += "\tint 21h\n";
				convertedCode[2] += "\tjmp lastline\n";
				convertedCode[2] += "\tzero:\n";
				convertedCode[2] += "\tcmp cx, ax\n";
				convertedCode[2] += "\tjle printloop\n";
				convertedCode[2] += "\tmov bx, ax \n";
				convertedCode[2] += "\treduce:\n";
				convertedCode[2] += "\tmov ax, cx\n";
				convertedCode[2] += "\tmov cx, 10\n";
				convertedCode[2] += "\tdiv cx\n";
				convertedCode[2] += "\txor dx,dx\n";
				convertedCode[2] += "\tmov cx,ax\n";
				convertedCode[2] += "\tcmp cx, bx\n";
				convertedCode[2] += "\tjg reduce\n";
				convertedCode[2] += "\tmov ax,bx\n";
				convertedCode[2] += "\tprintloop:\n";
				convertedCode[2] += "\tdiv cx\n";
				convertedCode[2] += "\tmov bx, dx \n";
				convertedCode[2] += "\tadd ax, '0'\n";
				convertedCode[2] += "\tmov dl, al \n";
				convertedCode[2] += "\tmov ah, 02h\n";
				convertedCode[2] += "\tint 21h\n";
				convertedCode[2] += "\txor dx,dx \n";
				convertedCode[2] += "\tmov ax, cx \n";
				convertedCode[2] += "\tmov cx, 10\n";
				convertedCode[2] += "\tdiv cx \n";
				convertedCode[2] += "\tmov cx, ax\n";		
				convertedCode[2] += "\tmov ax,bx \n";	
				convertedCode[2] += "\tcmp cx, 0\n";
				convertedCode[2] += "\tjne printloop		\n";
				convertedCode[2] += "\tlastline:\n\n";
				convertedCode[2] += "ret\n";
				convertedCode[2] += "print endp\n\n";
				
				convertedCode[2] += "main proc \n\n";
				convertedCode[2] += "\t mov ax, @data \n";
				convertedCode[2] += "\t mov ds, ax \n\n";
				
				line = tokenizer.nextToken();
				String body = new String();
				while(!(line.contains("}") && !tokenizer.hasMoreTokens())){
					body += line + "\n";
					line = tokenizer.nextToken();
				}
				
				body = addCurlyBraces(body);
				
				convertedCode[3] = ToAssembly.bodyParser(body);
				
				convertedCode[4] += "\t mov ax, 4c00h \n";
				convertedCode[4] += "\t int 21h \n\n";
				convertedCode[4] += "main endp \n";
				convertedCode[4] += "end main";
			}
		}
		
		dataContent.add("\t temp dw 0 \n"); //used for many stuff
		
		for(int i = 0; i < dataContent.size(); i++){
			convertedCode[1] += dataContent.get(i);
		}
		
		for(String code :  convertedCode){
			ToAssembly.convertedCode += code;
		}
	}
	
	/*
	 * Function:
	 * 	gets the type of the specified variableName
	 * Parameter:
	 * 	ArrayList<String[]> dc - the array that contains the variable (dynamic data or data content)
	 *  String variableName -  the variable name of the variable whose type is to be returned.
	 * Returns:
	 * 	String - 'db' or 'dw', the type of the variable.
	 * */
	private static String typeGetter(ArrayList<String> dc, String variableName){
		String type = "";
		for(int i=0; i<dc.size(); i++){
			if(dc.get(i).contains(variableName)){
				StringTokenizer tokenizer = new StringTokenizer(dc.get(i).trim());
				tokenizer.nextToken(); // this is the variable name
				type = tokenizer.nextToken();
				break;
			}
		}
		return type;
	}
	
	
	/*
	 * Function:
	 * 	gets the next command from index x
	 * Parameter:
	 * 	x - the index where the function will start searching from
	 * Returns:
	 * 	String - the next command from the given index x.
	 * */
	private static String getNextCommand(String body, int x){
		String nextCommand = "";
		while(body.charAt(x) != '}'){
			x++;
		}
		x++;
		while(body.charAt(x) == ' ' || body.charAt(x) == '\n'){
			x++;
		}
		
		nextCommand = body.substring(x, x+4);
		
		return nextCommand;
	}
	
	/*
	 * Function:
	 * 	adds curly braces to body
	 * Parameter:
	 * 	body - the string where the curly braces will be added
	 * Returns:
	 * 	String - body with curly braces
	 * */
	private static String addCurlyBraces(String body){
		int x = 0, y=0;
		String[] commands = {"if", "else","for","while"};
		
		for(int i=0; i<commands.length; i++){
			
			for(x = body.indexOf(commands[i], 0);x!=-1;x = body.indexOf(commands[i], x)){	
				
				if(i!=1){
					while(body.charAt(x) != ')'){
						x++;
					}
					x++;
				}else{ 
					x += 4;
					while(body.charAt(x) == ' ' || body.charAt(x) == '\n'){
						x++;
					}
				}
				y = x;
				while(body.charAt(x) == ' ' || body.charAt(x) == '\n'){
					x++;
				}
				
				if(i!=4 && body.charAt(x)!=';'){				
					if(body.charAt(x) != '{' && body.charAt(x)!= 'i'){
						body = body.substring(0, y) + "{" + body.substring(y);
						int z = body.indexOf(";", y);
						body = body.substring(0, z+1) + "\n } \n" + body.substring(z+1);
					}
				}
			}
		}
		return body;
	}
	
	/*
	 * Function:
	 * 	parses Conditional Statements
	 * Parameter:
	 * 	line - line of code that contains condition
	 * 	number - the number added to the label
	 * 	whatType - specifies if its a if, dowhile, while or for condition
	 * Returns:
	 * 	String - the code equivalent to assembly
	 * */
	
	private static String parseCondition(String line, int number, String whatType){
		
		String variableName = new String();
		String value = new String();
		String newBody = new String();
		String condition = "";
		
		if(whatType.equals("dowhile")){
			int indexOfWhile = line.indexOf("while"); 
			int firstParenthesis = line.indexOf("(", indexOfWhile + 1);
			int secondParenthesis = line.indexOf(")", firstParenthesis);
			condition = line.substring(firstParenthesis+1, secondParenthesis);
		}else{		
			int firstParenthesis = line.indexOf("(");
			int secondParenthesis = line.indexOf(")");
			condition = line.substring(firstParenthesis+1, secondParenthesis);
		}
		
		int lessThan = condition.indexOf("<");
		int lessThanOrEqualTo = condition.indexOf("<=");
		int greaterThan = condition.indexOf(">");
		int greaterThanOrEqualTo = condition.indexOf(">=");
		int notEqualTo = condition.indexOf("!=");
		int equals = condition.indexOf("==");
		
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
		
		if(whatType.equals("while") || whatType.equals("for")){
			number++;
			if(whatType.equals("while"))
				newBody += "\t while"+number+": \n";
			else
				newBody += "\t for"+number+": \n";
		}
		
		String type = typeGetter(dataContent, variableName);
		
		if(type.equals("db")){
			newBody += "\t mov dl, "+value+" \n";
			newBody += "\t cmp "+variableName+", dl \n";
		}else if(type.equals("dw")){
			newBody += "\t mov dx, "+value+" \n";
			newBody += "\t cmp "+variableName+", dx \n";
		}
		
		String labelName = "";
		String jump = "";
		
		if(whatType.equals("while") || whatType.equals("for")){
			number++;
		}
		
		if(whatType.equals("dowhile")){
			labelName = "do";
			if(equals != -1){
				jump = "je ";						
			}else if(notEqualTo != -1){
				jump = "jne ";					
			}else if(lessThanOrEqualTo != -1){
				jump = "jle ";						
			}else if(greaterThanOrEqualTo != -1){
				jump = "jge ";					
			}else if(lessThan != -1){
				jump = "jl ";				
			}else if(greaterThan != -1){
				jump = "jg ";
			}
		}else{
			if(whatType.equals("if")){
				labelName = "label";
			}else if(whatType.equals("while")){
				labelName = "while";
			}else{
				labelName = "for";
			}
			if(equals != -1){
				jump = "jne ";						
			}else if(notEqualTo != -1){
				jump = "je ";					
			}else if(lessThanOrEqualTo != -1){
				jump = "jg ";						
			}else if(greaterThanOrEqualTo != -1){
				jump = "jl ";					
			}else if(lessThan != -1){
				jump = "jge ";				
			}else if(greaterThan != -1){
				jump = "jle ";
			}
		}
		
		newBody += "\t "+jump+labelName+number+" \n\n";			
		return newBody;
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
		
		Stack<Integer> curlyBrace = new Stack<Integer>();
		/*
		 * Stack convention
		 * 	if = 0
		 * 	else = 1
		 *  while = 2
		 *  dowhile = 3
		 *  for = 4
		 */
		
		int firstParenthesis = 0;
		int secondParenthesis = 0;
		int equals = 0;
		int labelNumber = 0;
		int elseNumber = 0;
		int indexOfLine = 0;
		int stringNumber = 0;
		int doNumber = 0;
		int whileNumber = 0;
		int forNumber = 0;
		int linelength = 0;
				
		StringTokenizer tokenizer = new StringTokenizer(body, "\n");
		
		while(tokenizer.hasMoreTokens()){
			line = tokenizer.nextToken();
			linelength = line.length();
			line = line.trim();

			if(!line.equals("")){

				
				firstParenthesis = line.indexOf("(");
				
				if(firstParenthesis != -1){
					command = line.substring(0,firstParenthesis);
					command = command.trim();
				}else{
					command = "";
				}
				
				if(line.contains("while") && line.contains(";")){
					curlyBrace.pop();					
					newBody += parseCondition(line, doNumber, "dowhile");					
				}
				
				else if(line.contains("else")){										
					newBody += "\t jmp else"+elseNumber+" \n";
					newBody += "\t label"+labelNumber+": \n\n";
				}
				
				else if(line.contains("}")){
					String nextCommand = getNextCommand(body, indexOfLine);
					if(!nextCommand.equals("else")){
						int	curlyBraceEquivalent = curlyBrace.pop();
							
						if(curlyBraceEquivalent == 2){
							whileNumber--;
							newBody += "\t jmp while"+whileNumber+" \n";
							whileNumber++;
							newBody += "\t while"+whileNumber+": \n\n";
						}
						else if(curlyBraceEquivalent == 4){
							newBody += bodyParser(postActivity+";");
							forNumber--;
							newBody += "\t jmp for"+forNumber+" \n";
							forNumber++;
							newBody += "\t for"+forNumber+": \n\n";
						}
						else if(curlyBraceEquivalent == 0){
							newBody += "\t label"+labelNumber+": \n\n";
						}
						else if(curlyBraceEquivalent == 1){
							newBody += "\t else"+elseNumber+": \n\n";
						}
					}
				}
				
				
				
				/* IF THE COMMAND IS PRINTF 
				 * make this more dynamic
				 * 
				 * handle %s, %d, %c case
				 * */	
				if(command.equals("printf")){
					boolean hasNewLine = false;
					int index = 0;			
					int firstDoubleQuote = line.indexOf("\"");
					int secondDoubleQuote = line.indexOf("\"", firstDoubleQuote+1);
					String string = line.substring(firstDoubleQuote+1, secondDoubleQuote);
					String variable = line.substring(secondDoubleQuote+1, line.indexOf(")"));
					variable = variable.substring(variable.indexOf(',')+1).trim();
					
					if(string.contains("%s") || string.contains("%c") || string.contains("%d")){						
						if(string.contains("%s")){
							index = string.indexOf("%s");
						}else if(string.contains("%c")){
							index = string.indexOf("%c");
						}else if(string.contains("%d")){
							index = string.indexOf("%d");
						}
						
						String firstHalf = string.substring(0, index);
						String secondHalf = string.substring(index+2);
						
						if(!firstHalf.trim().isEmpty()){
							stringNumber++;
							dataContent.add("\t string"+stringNumber+" db \""+firstHalf+"$\" \n");
							
							newBody += "\t lea dx, string"+stringNumber+" \n";
							newBody += "\t mov ah, 09h \n";
							newBody += "\t int 21h \n\n";
						}
						
						if(string.contains("%s")){
							newBody += "\t lea dx, "+variable+" \n";
							newBody += "\t mov ah, 09h \n";
							newBody += "\t int 21h \n\n";														
						}else if(string.contains("%c")){							
							newBody += "\t mov dl, "+variable+" \n";
							newBody += "\t mov ah, 02h \n";
							newBody += "\t int 21h \n\n";														
						}else if(string.contains("%d")){
							//assuming single digit
							newBody += "\t mov ax, "+variable+" \n";
							newBody += "\t call print \n";							
						}
						
						if(!secondHalf.trim().isEmpty()){
							if(secondHalf.contains("\\n")){
								secondHalf = secondHalf.substring(0,secondHalf.indexOf("\\n"));
								hasNewLine = true;
							}
							if(!secondHalf.trim().isEmpty()){
								stringNumber++;
								dataContent.add("\t string"+stringNumber+" db \""+secondHalf+"$\" \n");
								
								newBody += "\t lea dx, string"+stringNumber+" \n";
								newBody += "\t mov ah, 09h \n";
								newBody += "\t int 21h \n\n";
							}
							if(hasNewLine){
								newBody += "\t mov dl, 10 \n";
								newBody += "\t mov ah, 02h \n";
								newBody += "\t int 21h \n\n";
							}
						}
					}else{					
						if(string.contains("\\n")){
							string = string.substring(0,string.indexOf("\\n"));
							hasNewLine = true;
						}
						
						stringNumber++;
						dataContent.add("\t string"+stringNumber+" db \""+string+"$\" \n");
						
						newBody += "\t lea dx, string"+stringNumber+" \n";
						newBody += "\t mov ah, 09h \n";
						newBody += "\t int 21h \n\n";
						
						if(hasNewLine){
							newBody += "\t mov dl, 10 \n";
							newBody += "\t mov ah, 02h \n";
							newBody += "\t int 21h \n\n";
						}
					}
				}
				
				else if(command.contains("if")){
					
					labelNumber++;
					newBody += parseCondition(line, labelNumber, "if");
					Boolean initialize = false;
					
					if(!curlyBrace.isEmpty()){
						if(curlyBrace.peek() != 1){
							initialize = true;
						}
					}else{
						initialize = true;
					}
					
					if(initialize){						
						//looking for an else.
						int indexOfIf = body.indexOf("if", indexOfLine+2);
						int indexOfElse = body.indexOf("else", indexOfLine+2);						
						if((indexOfElse < indexOfIf) || (indexOfElse!=1 && indexOfIf == -1)){
							curlyBrace.push(1);
							elseNumber++;
						}else{
							curlyBrace.push(0);
						}
					}
				}
				
				else if(command.equals("while")){
					newBody += parseCondition(line, whileNumber, "while");
					whileNumber += 2;
					curlyBrace.push(2);
				}
				
				else if(line.contains("do")){
					doNumber++;
					newBody += "\t do"+doNumber+": \n\n";
					curlyBrace.push(3);
				}
				
				else if(line.contains("for")){
					firstParenthesis = line.indexOf("(");
					secondParenthesis = line.indexOf(")");
					String insideOfFor = line.substring(firstParenthesis+1, secondParenthesis);
					StringTokenizer forTokenizer = new StringTokenizer(insideOfFor, ";");
					
					String initialization = forTokenizer.nextToken();
					newBody += bodyParser(initialization+";");
					condition = forTokenizer.nextToken();
					postActivity = forTokenizer.nextToken();
					
					if(!condition.isEmpty()){
						newBody += parseCondition("while("+condition+"){", forNumber, "for");
						forNumber+=2;
						curlyBrace.push(4);
					}
				}
				
				else if(line.contains("++")){
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
				
				//means variable declaration, initialization or operation.
				else if(command.equals("")){
					
					// counting tokens, excluding ";"
					/*
					 * Handle cases: int a,b; int a=5,b;
					 */
					
					if(line.lastIndexOf(";") != -1){
						
						String dataType = "";
						int numberOfTokens = 0;
						
						StringTokenizer lineTokenizer = new StringTokenizer(line.substring(0, line.lastIndexOf(";")), " ");
						numberOfTokens = lineTokenizer.countTokens();
						dataType = lineTokenizer.nextToken();
						dataType = dataType.trim();
						
						if((dataType.contains("int") && line.contains(",")) 
								|| (dataType.contains("char") && line.matches(".+['\"].+['\"],.+"))){
							
							if(dataType.equals("int")){
								StringTokenizer commaTokenizer = new StringTokenizer(line.substring(line.indexOf(" ")+1), ",");
								while(commaTokenizer.hasMoreTokens()){
									String declaration = commaTokenizer.nextToken();
									if(declaration.lastIndexOf(";") == -1){
										declaration += ";";
									}
									newBody += bodyParser("int "+declaration);
								}
							}else{ //this means that dataType is equal to 'char'
								StringTokenizer commaTokenizer = new StringTokenizer(line.substring(line.indexOf(" ")+1), ",");
								
								while(commaTokenizer.hasMoreTokens()){
									String declaration = commaTokenizer.nextToken();
									if(declaration.lastIndexOf(";") == -1){
										declaration += ";";
									}
									newBody += bodyParser("char "+declaration);
								}
							}
						}else{

						equals = line.indexOf("=");
						
						// CASE: int age, char age[];
						if(numberOfTokens == 2 && equals == -1){
							boolean isItAnArray = false;
							String sindex = "";
							int index = 0;
							variableName = lineTokenizer.nextToken();
							variableName = variableName.trim();
							if(variableName.contains("[")){
								sindex = variableName.substring(variableName.indexOf("[")+1, variableName.indexOf("]"));
								sindex = sindex.trim();
								if(!sindex.equals("")){
									index = Integer.parseInt(sindex);
								}
								variableName = variableName.substring(0,variableName.indexOf("["));
								variableName = variableName.trim();
								isItAnArray = true;
							}
							String[] temp = new String[3];
							temp[0] = dataType;
							temp[1] = variableName;
							if(dataType.contains("int")){
								temp[2] = "0";
								dataContent.add("\t " + variableName + " dw ? \n");
							}else if(dataType.contains("char")){
								temp[2] = "";
								while(index>0){
									temp[2] += "?";
									index--;
								}
								if(isItAnArray){
									temp[2] += ",'$'";
								}else{
									temp[2] += "?";
								}
								dataContent.add("\t " + variableName + " db "+temp[2]+" \n");
							}
							dynamicData.add(temp);
						}
						
						//CASE: int age=5 or age=5. OR age = age + 5, or age += 5 or age = age + another 
						else if(equals != -1){
							
							//getting string to the left of equals.
							String leftside = line.substring(0, equals);
							StringTokenizer leftsideTokenizer = new StringTokenizer(leftside, " ");
														
							// CASE: int age=5, char[] age = "age";
							if(leftsideTokenizer.countTokens() == 2){
								
								dataType = leftsideTokenizer.nextToken();
								dataType = dataType.trim();
								variableName = leftsideTokenizer.nextToken();
								variableName = variableName.trim();
								if(variableName.contains("[")){
									variableName = variableName.substring(0,variableName.indexOf("["));
									variableName = variableName.trim();
								}
								
								value = line.substring(equals+1, line.lastIndexOf(";"));
								value = value.trim();
								
								
									if (value.contains("+")) {
										StringTokenizer valueTokenizer = new StringTokenizer(value, "+");
										newBody += "\t mov temp, 0 \n\n ";
										while (valueTokenizer.hasMoreTokens()) {
											newBody += "\t mov dx, " + valueTokenizer.nextToken().trim()+ " \n ";
											newBody += "\t add temp, dx \n\n ";
										}
										newBody += "\t mov dx, temp \n ";
										newBody += "\t mov " + variableName+ ", dx \n\n ";
										dataContent.add("\t "+variableName+" dw 0 \n");
									} else if (value.contains("-")) {
										StringTokenizer valueTokenizer = new StringTokenizer(value, "-");
										newBody += "\t mov temp, 0 \n\n ";
										newBody += "\t mov dx, "+ valueTokenizer.nextToken().trim() + " \n\n ";
										newBody += "\t mov temp, dx \n\n ";
										while (valueTokenizer.hasMoreTokens()) {
											newBody += "\t mov dx, "+ valueTokenizer.nextToken().trim()	+ " \n";
											newBody += "\t sub temp, dx \n\n ";
										}
										newBody += "\t mov dx, temp \n ";
										newBody += "\t mov " + variableName+ ", dx \n\n ";
										dataContent.add("\t "+variableName+" dw 0 \n");
									} else {
										if(dataType.contains("char")){
											int firstQuoteIndex = value.indexOf("'");
											int secondQuoteIndex = value.indexOf("'", firstQuoteIndex+1);
											if(firstQuoteIndex == -1){
												firstQuoteIndex = value.indexOf("\"");
												secondQuoteIndex = value.indexOf("\"", firstQuoteIndex+1);
											}
											value = value.substring(0, secondQuoteIndex) + "$" + value.substring(secondQuoteIndex);
										}
										String[] temp = {dataType, variableName, value};
										dynamicData.add(temp);
										if(dataType.contains("char")){
											dataContent.add("\t "+variableName+" db " + value + "\n");
										}else if(dataType.contains("int")){
											dataContent.add("\t "+variableName+" dw " + value + "\n");
										}
								}
							}
							

							else if(line.contains(",")){
								StringTokenizer commaTokenizer = new StringTokenizer(line, ",");
								while(commaTokenizer.hasMoreTokens()){
									String temp = commaTokenizer.nextToken();
									if(!temp.contains(";")){
										temp += ";";
									}else{
										temp = temp.substring(0, temp.indexOf(";")+1);
									}
									newBody += bodyParser(temp);
								}
							}
							
							else if(line.contains("+=") || line.contains("-=")){
								String variable = "";
								value = line.trim().substring(line.indexOf("=")+1, line.indexOf(";")).trim();
								newBody += "\t mov temp, 0 \n\n ";
								
								if(line.contains("+=")){									
									variable = line.trim().substring(0, line.indexOf("+")).trim();
																		
									newBody += "\t mov dx, "+variable+" \n ";
									newBody += "\t add temp, dx \n\n ";
																	
									newBody += "\t mov dx, "+value+" \n ";
									newBody += "\t add temp, dx \n\n ";
								}
								else{
									variable = line.trim().substring(0, line.indexOf("-")).trim();
									
									newBody += "\t mov dx, "+variable+" \n ";
									newBody += "\t mov temp, dx \n\n ";
																	
									newBody += "\t mov dx, "+value+" \n ";
									newBody += "\t sub temp, dx \n\n ";
								}
								
								newBody += "\t mov dx, temp \n ";
								newBody += "\t mov "+variable+", dx \n\n ";
							}
							
							// CASE: age=5, char[0] = 'e' - handle later
							else if(leftsideTokenizer.countTokens() == 1){
								variableName = leftsideTokenizer.nextToken();
								value = line.substring(equals+1, line.lastIndexOf(";"));
								value = value.trim();
								
								// CASE: age = age + 5
								if(value.contains("+")){							
									StringTokenizer valueTokenizer = new StringTokenizer(value, "+");
									
									newBody += "\t mov temp, 0 \n\n ";
									
									while(valueTokenizer.hasMoreTokens()){
										newBody += "\t mov dx, "+valueTokenizer.nextToken().trim()+" \n ";
										newBody += "\t add temp, dx \n\n ";
									}
									newBody += "\t mov dx, temp \n ";
									newBody += "\t mov "+variableName+", dx \n\n ";
									
								}else if(value.contains("-")){
									StringTokenizer valueTokenizer = new StringTokenizer(value, "-");
									
									newBody += "\t mov temp, 0 \n\n ";
																		
									newBody += "\t mov dx, "+valueTokenizer.nextToken().trim()+" \n\n ";
									newBody += "\t mov temp, dx \n\n ";
									
									while(valueTokenizer.hasMoreTokens()){
										newBody += "\t mov dx, "+valueTokenizer.nextToken().trim()+" \n";
										newBody += "\t sub temp, dx \n\n ";
									}
									newBody += "\t mov dx, temp \n ";
									newBody += "\t mov "+variableName+", dx \n\n ";
									
								}
								else{
									
									String type = typeGetter(dataContent, variableName);						
									if(type.equals("db")){
										newBody += "\t mov dl, "+value+" \n ";
										newBody += "\t mov " + variableName + ", dl \n\n ";
									}else if(type.equals("dw")){
										newBody += "\t mov dx, "+value+" \n ";
										newBody += "\t mov " + variableName + ", dx \n\n ";
									}	
									
									
								}						
							}
							
						}
					}
					}
				}
			}
			indexOfLine = indexOfLine + linelength + 1; //adding '\n'
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

//int index = 0;
// CASE: var[0] = 'a' - not handled , var = 'b' , age = 5;
/*if(variableName.contains("[")){
	String sindex = variableName.substring(variableName.indexOf("[")+1, variableName.indexOf("]"));
	sindex = sindex.trim();
	index = Integer.parseInt(sindex);
	variableName = variableName.substring(0,variableName.indexOf("["));
	variableName = variableName.trim();
}*/
							
/*if(value.contains("\"") || value.contains("'")){
	//ibigsabihin string
	
	int firstQuoteIndex = value.indexOf("'");
	int secondQuoteIndex = value.indexOf("'", firstQuoteIndex+1);
	if(firstQuoteIndex == -1){
		firstQuoteIndex = value.indexOf("\"");
		secondQuoteIndex = value.indexOf("\"", firstQuoteIndex+1);
	}
	value = value.substring(1, secondQuoteIndex);
	
	//look for variable name in arraylist, tapos baguhin yung value sa dynamicData
	/*for(int i=0; i<dynamicData.size(); i++){
		if(dynamicData.get(i)[1].trim().equals(variableName.trim())){
			originalValue = dynamicData.get(i)[2].trim();
			originalValue = originalValue.substring(0, index+1) +value+ originalValue.substring(index+2);
			String[] temp = {dynamicData.get(i)[0].trim(), 
					dynamicData.get(i)[1].trim(), originalValue};
			dynamicData.set(i, temp);
		}
	}
	
	//baguhin sa dataContent
	for(int i=0; i<dataContent.size(); i++){
		if(dataContent.get(i).contains(variableName)){
			dataContent.set(i, "\t "+variableName+" db " + originalValue + "\n");
		}
	}
}*/

/*for(int i=0;i<dynamicData.size(); i++){
if(dynamicData.get(i)[1].trim().equals(variableName.trim())){
	String temp[] = new String[3];
	temp[0] = dynamicData.get(i)[0];
	temp[1] = dynamicData.get(i)[1];
	temp[2] = Integer.toString(Integer.parseInt(dynamicData.get(i)[2]) + 1);
	dynamicData.set(i, temp);
	break;
}
}*/

/*
 * convertedCode[0] - from start of assembly code to .data
 * convertedCode[1] - the content of the data segment
 * convertedCode[2] - from .code to the start of the body
 * convertedCode[3] - the body
 * convertedCode[4] - the end of body
 *  
 **/

/*for(int i=0;i<dynamicData.size(); i++){
	if(dynamicData.get(i)[1].trim().equals(variableName.trim())){
		String temp[] = new String[3];
		temp[0] = dynamicData.get(i)[0];
		temp[1] = dynamicData.get(i)[1];
		temp[2] = Integer.toString(Integer.parseInt(dynamicData.get(i)[2]) - 1);
		dynamicData.set(i, temp);
		break;
	}
}*/
/*
				if(line.contains("//")||line.contains("/*")){
					if(line.contains("//")){
						newBody += "\t ;"+line.substring(line.indexOf("//")+2)+"\n";
					}else{
						newBody += "\t ;"+line.substring(line.indexOf("/*")+2)+"\n";
					}
				}
*/
			
