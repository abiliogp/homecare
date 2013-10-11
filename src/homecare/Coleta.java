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

	private ArrayList<PrintStream> coletas;

	private ServerSocket server;
	private Socket client;
	
	private int counter = 1;

	public Coleta(int porta) throws IOException {
		server = new ServerSocket(porta);
		coletas = new ArrayList<PrintStream>();
		
	}

	public void getDadosHomeCare() {

	}

	public void runServer() {
		while (true) {
			try {
				waitForConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				++counter;
			}
		}
	}

	
	private void waitForConnection() throws IOException {
		Socket client = server.accept();
		System.out.println("Connection " + counter + " received from: "
				+ client.getInetAddress().getHostName());
		
		PrintStream ps = new PrintStream(client.getOutputStream());
	       this.coletas.add(ps);
		
		ClientConnection clientConnection = new ClientConnection(client.getInputStream());
		new Thread(clientConnection).start();		
	}

	/*
	 * Suporte à vários clientes conectados ao servidor
	 */
	private class ClientConnection implements Runnable {

		private InputStream input;

		public ClientConnection(InputStream input) {
			this.input = input;
		}

		public void run() {
			try {
				broadCast();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Scanner s = new Scanner(this.input);
			while (s.hasNextLine()) {
				//recebe informações dos outros clientes
				System.out.println(s);
			}
			s.close();
		}
	}

	/*
	 * Envia as informações do servidor para todos os clientes
	 */
	public void broadCast() throws IOException {
		 for (PrintStream coleta : this.coletas) {
		       //home.println(msg);
			 coleta.println("oi " +  this.server.getLocalPort());	
		 }
		 Scanner s = new Scanner(client.getInputStream());
	     while (s.hasNextLine()) {
	       System.out.println(s.nextLine());
	     }	
		
	}
	
	

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		Coleta coleta = new Coleta(Integer.parseInt(args[0]));
		coleta.client = new Socket("127.0.0.1", Integer.parseInt(args[1]));
		coleta.runServer();
		
		
	}
}
