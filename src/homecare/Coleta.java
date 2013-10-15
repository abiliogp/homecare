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

	private ArrayList<Socket> coletas;

	private ServerSocket server;
	private Socket client;

	private int counter = 1;

	public Coleta(int porta) throws IOException {
		server = new ServerSocket(porta);
		coletas = new ArrayList<Socket>();

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
		Socket connection = server.accept();
		System.out.println("Connection " + counter + " received from: "
				+ connection.getInetAddress().getHostName());

		// PrintStream ps = new PrintStream(connection.getOutputStream());
		this.coletas.add(connection);

		ClientConnection clientConnection = new ClientConnection(
				connection.getInputStream());
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
				receiver();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Envia as informações do servidor para todos os clientes
	 */
	public void broadCast() throws IOException {
		for (Socket coleta : this.coletas) {
			PrintStream ps = new PrintStream(coleta.getOutputStream());
			ps.println(coleta.getRemoteSocketAddress());
		}

	}

	/*
	 * Recebe informações do cliente conectado
	 */
	private void receiver() throws IOException {
		Scanner s = new Scanner(client.getInputStream());
		while (s.hasNextLine()) {
			System.out.println("Client: " + s.nextLine());
		}
		s.close();
	}

	// myport serverport
	public static void main(String[] args) throws UnknownHostException,
			IOException {
		Coleta coleta = new Coleta(Integer.parseInt(args[0]));
		coleta.client = new Socket("127.0.0.1", Integer.parseInt(args[1]));
		coleta.runServer();
	}

}
