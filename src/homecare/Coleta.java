package homecare;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
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
import java.util.Date;
import java.util.Scanner;
import java.util.TreeMap;
import java.sql.Timestamp;
import java.util.Date;

public class Coleta {

	private HomeCare myHomeCare;
	private String pacienteCpf;
	private ArrayList<Socket> myClients;
	private ArrayList<Socket> myServers;

	private int defaultPort = 12345;
	private String myIp;

	private ServerSocket server;

	private int counter = 1;
	private Thread senderThread, receiverThread;

	private TreeMap<String, ArrayList<Dado>> trieDatas;

	public static Coleta me;

	private enum Msg {
		cser, csmy, temp, pres, card, ip;
	}

	public Coleta(String myIp) throws IOException {
		this.myHomeCare = new HomeCare();
		this.pacienteCpf = this.myHomeCare.getCpf();
		this.myIp = myIp;
		server = new ServerSocket(defaultPort);
		myClients = new ArrayList<Socket>();
		myServers = new ArrayList<Socket>();
		trieDatas = new TreeMap<String, ArrayList<Dado>>();
	}

	public void getDadosHomeCare() {

	}

	public ArrayList<Socket> getClientList() {
		return myClients;
	}

	public TreeMap<String, ArrayList<Dado>> getTrieDatas() {
		return trieDatas;
	}

	public void runServer() {
		receiverUpdate();
		senderUpdate();
		while (true) {
			try {
				waitForConnection();
				receiverUpdate();
				senderUpdate();
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
		System.out.println(this.myClients.size());

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
	private void broadCast() throws IOException {
		for (Socket coleta : this.myClients) {
			PrintStream ps = new PrintStream(coleta.getOutputStream());
			ps.println("cpf<" + this.pacienteCpf + ">st:ip<" + this.myIp + ">");
		}
	}

	/*
	 * envia ip e porta para clientes
	 */
	private void clientsUpdate() throws IOException {
		for (Socket coleta : this.myClients) {
			for (Socket client : this.myClients) {
				PrintStream ps = new PrintStream(client.getOutputStream());
				ps.println("cpf<" + this.pacienteCpf + ">st:ip<" + this.myIp
						+ ">");
			}
		}
	}

	/*
	 * Recebe informações dos servidores
	 */
	private void receiverUpdate() {
		Receiver receiverConnection = new Receiver();
		receiverThread = new Thread(receiverConnection, "receiver");
		receiverThread.start();
	}

	private class Receiver implements Runnable {
		public void run() {
			Thread myThread = Thread.currentThread();
			while (myThread == receiverThread) {
				try {
					receiverBroadcast();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * Recebe informações do cliente conectado
	 */
	private void receiverBroadcast() throws IOException {
		String str, cpf, ip, st, data, time;
		for (Socket connection : this.myServers) {
			Scanner s = new Scanner(connection.getInputStream());
			while (s.hasNextLine()) {
				str = s.nextLine();
				cpf = manipulate(str, "cpf", "<", ">");
				st = manipulate(str, "st", ":", "<");
				System.out.println(str);
				switch (Msg.valueOf(st)) {
				case cser:
					break;
				case csmy:
					ip = manipulate(str, "ip", "<", ">");
					updateClientList(cpf, ip);
					break;
				default:
					data = manipulate(str, st, "<", ">");
					time = manipulate(str, "time", "<", ">");
					saveData(cpf, st, data, time);
					break;
				}
			}
			s.close();
		}
	}

	/*
	 * faz conexão com novo cliente abrir conexao com o novo cliente
	 */
	private void updateClientList(String cpf, String ip)
			throws UnknownHostException, IOException {
		// if (!ip.equals(this.myIp)) {
		System.err.println("new client " + ip);
		Socket client = new Socket(ip, this.defaultPort);
		this.myServers.add(client);
		// }
	}

	/*
	 * salvar dados
	 */
	private void saveData(String cpf, String st, String data, String time) {
		ArrayList<Dado> dataColeta;
		if (!trieDatas.containsKey(cpf)) {
			dataColeta = new ArrayList<Dado>();
			dataColeta.add(new Dado(st, data, time));
			trieDatas.put(cpf, dataColeta);
		} else {
			dataColeta = trieDatas.get(cpf);
			dataColeta.add(new Dado(st, data, time));
			if (dataColeta.size() > 1000) {
				System.out.println("Save to file " + dataColeta.size());
				saveToFile();
				dataColeta.clear();
			}
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
					e.printStackTrace();
				}
			}
		}
	}

	private void senderDatas() throws IOException {
		int i = 0;
		for (Socket coleta : this.myClients) {
			PrintStream ps = new PrintStream(coleta.getOutputStream());
			ps.println("cpf<" + this.pacienteCpf + ">st:temp<" + 10 + ">"
					+ "time<"
					+ (new Timestamp(new Date().getTime())).toString() + ">");
			ps.println("cpf<" + this.pacienteCpf + ">st:pres<" + 11 + ">"
					+ "time<"
					+ (new Timestamp(new Date().getTime())).toString() + ">");
			ps.println("cpf<" + this.pacienteCpf + ">st:card<" + i++ + ">"
					+ "time<"
					+ (new Timestamp(new Date().getTime())).toString() + ">");
		}
	}

	private void saveToFile() {
		try {
			FileOutputStream fileOutput = new FileOutputStream("homecare.log",
					true);
			ObjectOutputStream objectOutput = new ObjectOutputStream(
					new BufferedOutputStream(fileOutput));
			objectOutput.writeObject(trieDatas);
			objectOutput.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	public TreeMap<String, ArrayList<Dado>> readFile() {
		TreeMap<String, ArrayList<Dado>> map = null;
		try {
			FileInputStream fileInput = new FileInputStream("homecare.log");
			ObjectInputStream objectInput = new ObjectInputStream(
					new BufferedInputStream(fileInput));
			map = (TreeMap<String, ArrayList<Dado>>) objectInput.readObject();
			objectInput.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return map;
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

		Coleta coleta = new Coleta("127.0.0.1");

		// tomar por padrao a mesma porta dae só se preocupa com o IP

		Socket client = new Socket("127.0.0.1", 12345);
		coleta.myServers.add(client);

		me = coleta;

		coleta.runServer();
	}

}
