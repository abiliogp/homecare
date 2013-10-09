package homecare;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.ClientInfoStatus;
import java.util.ArrayList;

public class Coleta {

	private ArrayList<HomeCare> homeCare;

	private ServerSocket server;
	private Socket connection;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private int counter = 1;

	public void getDadosHomeCare() {

	}

	public void getRedundancia() {
		while (true) {
			try {
				server = new ServerSocket(12345, 100);
				waitForConnection();
				getStreams();
				processConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				closeConnection();
				++counter;
			}
		}
	}

	private void waitForConnection() throws IOException {
		connection = server.accept();
		System.out.println("Connection " + counter + " received from: "
				+ connection.getInetAddress().getHostName());
	}

	private void getStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();

		input = new ObjectInputStream(connection.getInputStream());
	}

	private void processConnection() {
		String message = null;
		sendData();
		do {
			try {
				message = (String) input.readObject();
				System.out.println("Client: " + message);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!message.equals("client>>end"));
	}

	private void sendData() {
		try {
			output.writeObject("Server: "
					+ server.getInetAddress().getHostName());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeConnection() {
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		Coleta coleta = new Coleta();
		coleta.getRedundancia();
		Socket client = new Socket(InetAddress.getByName("127.0.0.1"), 12345);
		
	}
}
