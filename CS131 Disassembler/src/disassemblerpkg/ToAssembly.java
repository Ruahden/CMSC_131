package disassemblerpkg;

import java.io.*;

public class ToAssembly {
	public static void main(String[] args) {
		try {
			BufferedReader inputStream = new BufferedReader(new FileReader("helloworld.c"));
			System.out.println("Contents of the .c file:\n");
			String line = null;
			for (int i = 0; i <=3; i++) {
				line = inputStream.readLine();
				System.out.println(line);
			}
			inputStream.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("The file could not be found or could not be opened.");
		}
		catch (IOException e) {
			System.out.println("Error reading file.");
		}
	}
}

