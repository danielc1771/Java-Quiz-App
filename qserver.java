import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class qserver extends Thread {

	public static void main(String[] args) throws IOException {

		int port = Integer.parseInt(args[0]);
		ServerSocket serverSocket = new ServerSocket(port);

		while(true) {
   			Socket server =  null;
   			try {
   				System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
   				server = serverSocket.accept();

   				System.out.println("Just connected to " + server.getRemoteSocketAddress());
   				DataInputStream in = new DataInputStream(server.getInputStream());   	

   				System.out.println(in.readUTF());
   				DataOutputStream out = new DataOutputStream(server.getOutputStream());
   				out.writeUTF("Thank you for connecting to "+ server.getLocalSocketAddress());
   				out.flush();

   				Thread t = new ClientHandler(server, in, out);
   				t.start();

   			} catch (Exception e) {
   				server.close();
   				e.printStackTrace();
   			}
   		}
	}
}

class ClientHandler extends Thread {

	final Socket server;
	final DataInputStream in;
	final DataOutputStream out;
	
	public ClientHandler(Socket server, DataInputStream in, DataOutputStream out) {
		this.server = server;
		this.in = in;
		this.out = out;
	}

	public void run() {
		ArrayList<Question> questionBank = new ArrayList<Question>();
		try {
			questionBank = deserializeQuestions("questions.txt");
		
		} catch (EOFException e) {
			
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
		String received;
		String deliver;
		while(server.isConnected()) {
			try {
				received = in.readUTF();
				//PUT
				if(received.equals("p")) {
					//questionBank.add(createQuestion(numOfQuestions));
					Question newQuestion = createQuestion("meta.txt");
					questionBank.add(newQuestion);
					serializeQuestions(questionBank, "questions.txt");
					out.writeInt(newQuestion.questionNumber);
				}
				//GET
				else if(received.charAt(0) == 'g') {
					int questionNumber = in.readInt();
					Question returnedQuestion = getQuestion(questionBank, questionNumber);
					if(returnedQuestion != null) {
						out.writeUTF(returnedQuestion.toString());
					} else {
						out.writeUTF("Error: question " + questionNumber + " not found.");
					}
				} 
				//CHECK
				else if(received.charAt(0) == 'c') {
					int questionNumber = in.readInt();
					char userAnswer = in.readChar();
					boolean correct = checkQuestion(questionBank, questionNumber, userAnswer);
					if(correct) {
						out.writeUTF("Correct");
					} else {
						out.writeUTF("Incorrect");
					}
				}
				//RANDOM
				else if(received.equals("r")) {
					Random rand = new Random();
					int randNumber = rand.nextInt(questionBank.size());
					out.writeUTF(questionBank.get(randNumber).askUserString());
					char userAnswer = in.readChar();
					int questionNumber = questionBank.get(randNumber).questionNumber; 
					boolean correct = checkQuestion(questionBank, questionNumber, userAnswer);
					if(correct) {
						out.writeUTF("Correct");
					} else {
						out.writeUTF("Incorrect");
					}
				}
				//DELETE 
				else if(received.charAt(0) == 'd') {
					int questionNumber = in.readInt();
					boolean removed = deleteQuestion(questionBank, questionNumber); 
					if(removed) {
						out.writeUTF("Deleted question " + questionNumber);
					} else {
						out.writeUTF("Error: Unable to delete question");
					}
				} 
				//HELP
				else if(received.equals("h")) {
					out.writeUTF(sendInstructions());
				}
				//QUIT CLIENT
				else if(received.equals("q")) {
					try {
						System.out.println("Connection to " + this.server + " closed.");
						this.server.close();
						this.in.close();
						this.out.close();
						break;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				//KILL SERVER
				else if(received.equals("k")) {
					try {
						System.out.println("Closing server...");
						this.server.close();
						this.in.close();
						this.out.close();
						System.exit(0);	
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					out.writeUTF("Invalid Option");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String sendInstructions() {
		String put = "p: Put a question in the bank. Provide question tag, question text, question choices, and correct answer. Server assigns question number.\n";
		String delete = "d <n>: Delete question with question number n.\n";
		String get = "g <n>: Get question with question number n.\n";
		String rand = "r: Get a random question.\n";
		String check = "c <n> <x>: Check answer x to question n\n";
		String kill = "k: Terminate the server\n";
		String quit = "q: Terminate the client";

		return put + delete + get + rand + check + kill + quit;
	}

	public Question createQuestion(String filename) {
		try {
					String questionTag = in.readUTF();
					String questionText = in.readUTF();
					int numOfChoices = in.readInt();
					String[] questionChoices = new String[numOfChoices];
					for(int i = 0; i < numOfChoices; i++) {
						questionChoices[i] = in.readUTF();
					}
					char correctAnswer = in.readChar();
					int questionNumber = 0;

					try(BufferedReader bufferedReader = new BufferedReader(new FileReader("meta.txt"))) {  
					    String number = bufferedReader.readLine();
						questionNumber = Integer.parseInt(number) + 1;
					} catch (FileNotFoundException e) {
					
					} catch (IOException e) {
					
					}

					try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))) {  
					    bufferedWriter.write(Integer.toString(questionNumber));
					} catch (IOException e) {
					 
					}

					Question newQuestion = new Question(questionTag, questionText, questionChoices, correctAnswer, questionNumber);
					return newQuestion;

			}	catch (IOException e) {
				e.printStackTrace();
			}
			return null;						
	}

	public Question getQuestion(ArrayList<Question> questionBank, int questionNumber) {
		for(int i = 0; i < questionBank.size(); i++) {
			if(questionBank.get(i).questionNumber == questionNumber) {
				return questionBank.get(i);
			}
		}
		return null;
	}

	public boolean deleteQuestion(ArrayList<Question> questionBank, int questionNumber) {
		for(int i = 0; i < questionBank.size(); i++) {
			if(questionBank.get(i).questionNumber == questionNumber) {
				questionBank.remove(i);
				try {
					serializeQuestions(questionBank, "questions.txt");
				} catch(IOException e) {
					e.printStackTrace();
				}
				
				return true;
			}
		}
		return false;
	}
	public boolean checkQuestion(ArrayList<Question> questionBank, int questionNumber,  char userAnswer) {
		Question question = getQuestion(questionBank, questionNumber);
		if(question.correctAnswer == userAnswer) {
			return true;
		}
		return false;
	}

	public static void serializeQuestions(Object question, String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);   
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(question);
		oos.close();
	}

	public static ArrayList<Question> deserializeQuestions(String filename) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(filename);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);
		ArrayList<Question> questions = new ArrayList<Question>();
		try{

			questions = (ArrayList<Question>) ois.readObject();

		} catch (EOFException e) {

		}
		ois.close();
		return questions;
	}
}

class Question implements Serializable {
	private static final long serialVersionUID = 69;
	String questionTag;
	String questionText;
	String[] questionChoices;
	char correctAnswer; 
	int questionNumber;

	Question (String questionTag, String questionText, String[] questionChoices, char correctAnswer, int questionNumber) {
		this.questionTag = questionTag;
		this.questionText = questionText;
		this.questionChoices = questionChoices;
		this.correctAnswer = correctAnswer;
		this.questionNumber = questionNumber;
	}

	public String toString() {
		String str = questionTag + "\n" + questionText + "\n.\n";
		for(int i = 0; i < questionChoices.length; i++) {
			str += questionChoices[i] + "\n.\n";
		}
		str += ".\n" + correctAnswer + "\n" + Integer.toString(questionNumber);
		return str;
	}

		public String askUserString() {
		String str = Integer.toString(questionNumber) + "\n" + questionText +"\n";
		for(int i = 0; i < questionChoices.length; i++) {
			str += questionChoices[i] + "\n";
		}
		return str;
	}


}











