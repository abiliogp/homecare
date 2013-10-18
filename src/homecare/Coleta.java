package homecare;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
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
import java.util.TreeMap;

public class Coleta {

	private ArrayList<Socket> myClients;
	private ArrayList<Socket> myServers;

	private int myPort;
	private ServerSocket server;

	private int counter = 1;
	private Thread senderThread;

	private TreeMap<String, ArrayList<Dado>> datas;

	private enum Msg {
		cser, csmy, temp, pres, card;
	}

	public Coleta(int myPort) throws IOException {
		this.myPort = myPort;
		server = new ServerSocket(myPort);
		myClients = new ArrayList<Socket>();
		myServers = new ArrayList<Socket>();
		datas = new TreeMap<String, ArrayList<Dado>>();
	}

	public void getDadosHomeCare() {

	}

	public void runServer() {
		receiverUpdate();
		senderUpdate();
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
				clientsUpdate();
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
			ps.println("ip<" + coleta.getInetAddress().getHostAddress()
					+ ">st:csmy<" + this.myPort + ">");
		}
	}

	/*
	 * envia ip e porta para clientes
	 */
	private void clientsUpdate() throws IOException {
		for (Socket coleta : this.myClients) {
			for (Socket client : this.myClients) {
				PrintStream ps = new PrintStream(client.getOutputStream());
				ps.println("ip<" + coleta.getInetAddress().getHostAddress()
						+ ">st:csmy<>");
			}
		}
	}

	/*
	 * Recebe informações dos servidores
	 */
	private void receiverUpdate() {
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
		String str, ip, st, data;
		for (Socket connection : this.myServers) {
			Scanner s = new Scanner(connection.getInputStream());
			while (s.hasNextLine()) {
				str = s.nextLine();
				ip = manipulate(str, "ip", "<", ">");
				st = manipulate(str, "st", ":", "<");
				switch (Msg.valueOf(st)) {
				case cser:
					break;
				case csmy:
					updateClientList(ip);
					break;
				default:
					data = manipulate(str, st, "<", ">");
					saveData(ip, st, data);
					break;
				}
			}
			s.close();
		}
	}

	/*
	 * faz conexão com novo cliente abrir conexao com o novo cliente
	 */
	private void updateClientList(String ip) {

	}

	/*
	 * salvar dados
	 */
	private void saveData(String ip, String st, String data) {
		ArrayList<Dado> dataColeta;
		if (!datas.containsKey(ip)) {
			dataColeta = new ArrayList<Dado>();
			dataColeta.add(new Dado(st, data));
			datas.put(ip, dataColeta);
		} else {
			dataColeta = datas.get(ip);
			dataColeta.add(new Dado(st, data));
			System.out.println(dataColeta.size());
			if(dataColeta.size() > 1000){
				System.out.println("Save to file " + dataColeta.size());
				saveToFile();
				dataColeta.clear();
			}
		}

	}

	public void saveToFile() {
		try {
			FileOutputStream fileOutput = new FileOutputStream(
					"homecare.log",true);
			ObjectOutputStream objectOutput = new ObjectOutputStream(
					new BufferedOutputStream(fileOutput));
			objectOutput.writeObject(datas);
			objectOutput.close();
		} catch (IOException e )  {
			System.out.println(e.toString());
		}
	}

	/*
	 * Envia as informações coletas
	 */
	private void senderUpdate() {
		Sender sender = new Sender();
		senderThread = new Thread(sender, "sender");
		senderThread.start();
	}

	private class Sender implements Runnable {
		public void run() {
			Thread myThread = Thread.currentThread();
			while (myThread == senderThread) {
				try {
					senderDatas();
					Thread.sleep(1000);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void senderDatas() throws IOException {
		int i = 0;
		for (Socket coleta : this.myClients) {
			PrintStream ps = new PrintStream(coleta.getOutputStream());
			ps.println("ip<" + coleta.getInetAddress().getHostAddress()
					+ ">st:temp<" + 10 + ">");
			ps.println("ip<" + coleta.getInetAddress().getHostAddress()
					+ ">st:pres<" + 10 + ">");
			ps.println("ip<" + coleta.getInetAddress().getHostAddress()
					+ ">st:card<" + i++ + ">");
		}
	}

	private String manipulate(String str, String key, String first, String last) {
		int n = str.indexOf(key);
		if (n == -1) {
			return "";
		}
		int x = str.indexOf(first, n);
		int x1 = str.indexOf(last, x + 1);
		return str.substring(x + 1, x1);
	}

	// myport serverport
	public static void main(String[] args) throws UnknownHostException,
			IOException {
		Coleta coleta = new Coleta(Integer.parseInt(args[0]));

		// tomar por padrao a mesma porta dae só se preocupa com o IP
		Socket client = new Socket("127.0.0.1", Integer.parseInt(args[1]));
		coleta.myServers.add(client);

		coleta.runServer();
	}

}
