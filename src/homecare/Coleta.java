package homecare;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.ClientInfoStatus;
import java.util.ArrayList;
import java.util.Scanner;

public class Coleta {

	private ArrayList<Socket> myClients;
	private ArrayList<Socket> myServers;

	private int myPort;
	private ServerSocket server;

	private int counter = 1;

	public Coleta(int myPort) throws IOException {
		this.myPort = myPort;
		server = new ServerSocket(myPort);
		myClients = new ArrayList<Socket>();
		myServers = new ArrayList<Socket>();
	}

	public void getDadosHomeCare() {

	}

	public void runServer() {
		receiverUpdate();
		while (true) {
			try {
				waitForConnection();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				++counter;
			}
		}
	}

	private void waitForConnection() throws IOException {
		Socket connection = server.accept();
		System.out.println("Connection " + counter + " received from: "
				+ connection.getRemoteSocketAddress());

		this.myClients.add(connection);

		ClientConnection clientConnection = new ClientConnection();
		new Thread(clientConnection).start();
	}

	
	
	/*
	 * Suporte à vários clientes conectados ao servidor
	 */
	private class ClientConnection implements Runnable {		
		public void run() {
			try {
				broadCast();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Envia as informações do servidor para todos os clientes
	 */
	public void broadCast() throws IOException {
		for (Socket coleta : this.myClients) {
			PrintStream ps = new PrintStream(coleta.getOutputStream());
			ps.println("<cs: " + coleta.getRemoteSocketAddress() + " port: " + this.myPort +">");
		}
	}

	
	
	private void receiverUpdate(){
		Receiver receiverConnection = new Receiver();
		new Thread(receiverConnection).start();
	}
	
	private class Receiver implements Runnable {
		public void run() {
			try {
				receiverBroadcast();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Recebe informações do cliente conectado
	 */
	private void receiverBroadcast() throws IOException {
		for(Socket connection : this.myServers){
			Scanner s = new Scanner(connection.getInputStream());
			while (s.hasNextLine()) {
				System.out.println("Client: " + s.nextLine());
			}
			s.close();
		}
		System.out.println("receiver");
	}
	
	
	

	// myport serverport
	public static void main(String[] args) throws UnknownHostException,
			IOException {
		Coleta coleta = new Coleta(Integer.parseInt(args[0]));
		
		Socket client = new Socket("127.0.0.1", Integer.parseInt(args[1]));
		coleta.myServers.add(client);
		
		coleta.runServer();
	}

}
