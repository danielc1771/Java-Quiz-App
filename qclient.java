import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


public class qclient {
	public static void main(String[] args) throws IOException {
		String serverName = args[0];
		int port = Integer.parseInt(args[1]);
		Scanner userInput = new Scanner(System.in);
		String userChoice = "";

		try {
			System.out.println("Connecting to " + serverName + " on port " + port);
			Socket client = new Socket(serverName, port);

			System.out.println("Just connected to " + client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);

			out.writeUTF("Hello from " + client.getLocalSocketAddress());
			out.flush();

			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);

			System.out.println("Server says " + in.readUTF());


			while(true) {
			
					System.out.print(">");
					userChoice = userInput.nextLine();
					out.writeUTF(userChoice);
					//PUT
					if(userChoice.equals("p")){
						String questionTag = userInput.nextLine();
						String temp1 = userInput.nextLine();
						String questionText = "";
						String temp2 = "";
						ArrayList<String> questionChoices = new ArrayList<String>();
						char correctAnswer;
		
						while(!temp1.equals(".")) {
							questionText += temp1;
							temp1 = userInput.nextLine();
						}
						temp1 = "";
						while(!(temp1.equals(".") && temp2.equals("."))) {
							if(temp1.equals(".")) {
								questionChoices.add(temp2);
								
							}
							temp2 = temp1;
							temp1 = userInput.nextLine();
						}
						correctAnswer = userInput.nextLine().charAt(0);

						out.writeUTF(questionTag);
						out.writeUTF(questionText);
						out.writeInt(questionChoices.size());
						for(int i = 0; i < questionChoices.size(); i++) {
							out.writeUTF(questionChoices.get(i));
						}
						out.writeChar(correctAnswer);
						System.out.println(in.readInt());
						
					} 
					//GET
					else if(userChoice.charAt(0) == 'g') {
						String questionNumber = "";
						for(int i = 2; i < userChoice.length(); i++) {
							questionNumber += userChoice.charAt(i);
						}
						out.writeInt(Integer.parseInt(questionNumber));
						System.out.println(in.readUTF());
					} 
					//CHECK
					else if(userChoice.charAt(0) == 'c') {
						String questionNumber = "";
						char userAnswer = userChoice.charAt(userChoice.length()-1);
						for(int i = 2; i < userChoice.length() -2; i++) {
							questionNumber += userChoice.charAt(i);
						}
						out.writeInt(Integer.parseInt(questionNumber));
						out.writeChar(userAnswer);
						System.out.println(in.readUTF());
					}
					//RANDOM
					else if(userChoice.equals("r")) {
						System.out.println(in.readUTF());
						char userAnswer = userInput.nextLine().charAt(0);
						out.writeChar(userAnswer);
						System.out.println(in.readUTF());
					}
					//DELETE
					else if(userChoice.charAt(0) == 'd') {
						String questionNumber = "";
						for(int i = 2; i < userChoice.length(); i++) {
							questionNumber += userChoice.charAt(i);
						}
						out.writeInt(Integer.parseInt(questionNumber));
						System.out.println(in.readUTF());
					}
					//HELP
					else if(userChoice.equals("h")) {
						System.out.println(in.readUTF());
					}
					//QUIT CLIENT
					else if(userChoice.equals("q")) {
						System.out.println("Closing connection....");
						client.close();
						System.out.println("Connection is closed.");
						userInput.close();
						in.close();
						out.close();
						break;
					} 	
					//KILL SERVER
					else if(userChoice.equals("k")) {
						System.out.println("Closing server....");
						client.close();
						System.out.println("Server is closed.");
						userInput.close();
						in.close();
						out.close();
						break;
					}
					 else {
						System.out.println(in.readUTF());
					}

				
			} 
		}
			catch (IOException e) {
			e.printStackTrace();
		}
	}
}