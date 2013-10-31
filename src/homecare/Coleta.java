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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.sql.Timestamp;
import java.util.Date;

public class Coleta implements Runnable{

	private HomeCare myHomeCare;
	private String pacienteCpf;
	private ArrayList<Socket> myClients;
	ArrayList<Socket> myServers;

	private int defaultPort = 12344;
	private String myIp;

	private ServerSocket server;

	private int counter = 1;
	private Thread senderThread, receiverThread;

	private static TreeMap<String, ArrayList<Dado>> trieDatas;
	private TreeMap<String, String> trieIpCpf;

	public static Coleta me;

	private enum Msg {
		cser, csmy, temp, press, presd, card, ip, end;
	}

	public Coleta(String myIp) throws IOException {
		this.myHomeCare = new HomeCare();
		this.pacienteCpf = this.myHomeCare.getCpf();
		this.myIp = myIp;
		server = new ServerSocket(defaultPort);
		myClients = new ArrayList<Socket>();
		myServers = new ArrayList<Socket>();
		trieDatas = new TreeMap<String, ArrayList<Dado>>();
		trieIpCpf = new TreeMap<String, String>();
		this.trieDatas.put(pacienteCpf, new ArrayList<Dado>());
		
	}

	public void getDadosHomeCare() {

	}

	public ArrayList<Socket> getClientList() {
		return myClients;
	}

	public TreeMap<String, ArrayList<Dado>> getTrieDatas() {
		return trieDatas;
	}

	public static List<Dado> getLastDatasOfCpf(String cpf){
		int size = trieDatas.get(cpf).size();
		List<Dado> subList = new ArrayList();
		subList = trieDatas.get(cpf).subList(size-80, size);
		return subList ;
	}
	
	public void run() {
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
		String ip = connection.getInetAddress().getHostAddress();
		System.out.println("Connection " + counter + " received from: " + ip);

		if (!this.trieIpCpf.containsKey(ip)) {

			this.myClients.add(connection);
			System.out.println(this.myClients.size());

			Socket client = new Socket(ip, this.defaultPort);
			this.myServers.add(client);
			this.trieIpCpf.put(ip, "");

			ClientConnection clientConnection = new ClientConnection();
			new Thread(clientConnection).start();
		}

	}

	/*
	 * Suporte à vários clientes conectados ao servidor
	 */
	public class ClientConnection implements Runnable {
		public void run() {
			try {
				broadCast();
				// clientsUpdate();
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
		String str, cpf, ip, st, time;
		double data;
		Scanner s = null;
		boolean endSt = false;
		Collection<Socket> forIteration = new HashSet<Socket>(this.myServers);
		int i = 0;
		for (Socket connection : forIteration) {
			System.out.println("receiver " + i);
			i++;
			if (connection != null) {
				s = new Scanner(connection.getInputStream());
				if (!s.hasNext()) {
					System.out.println("parou "
							+ connection.getInetAddress().getHostAddress());
					this.trieIpCpf.remove(connection.getInetAddress()
							.getHostAddress());
					this.myServers.remove(connection);
					break;
				}
			}

			while (s.hasNextLine()) {
				str = s.nextLine();
				cpf = manipulate(str, "cpf", "<", ">");
				st = manipulate(str, "st", ":", "<");
				System.out.println(str);
				if (st == null || Msg.valueOf(st) == null) {
					break;
				}
				switch (Msg.valueOf(st)) {
				case end:
					endSt = true;
					break;
				case ip:
					ip = manipulate(str, "ip", "<", ">");
					System.out.println("new ip " + ip);
					updateClientList(cpf, ip);
					break;
				default:
					data = Double.parseDouble(manipulate(str, st, "<", ">"));
					time = manipulate(str, "time", "<", ">");
					saveData(cpf, st, data, time);
					break;
				}
				if (endSt) {
					break;
				}
			}

			s = null;
			connection = null;

		}
	}

	/*
	 * faz conexão com novo cliente abrir conexao com o novo cliente
	 */
	private void updateClientList(String cpf, String ip)
			throws UnknownHostException, IOException {
		if (this.trieIpCpf.get(ip) != null) {
			if (this.trieIpCpf.get(ip).equals(""))
				this.trieIpCpf.put(ip, cpf);
			System.out.println("update cpf");
		}
		if (!ip.equals(this.myIp) && !this.trieIpCpf.containsKey(ip)) {
			Socket client = new Socket(ip, this.defaultPort);
			this.myServers.add(client);
			this.trieIpCpf.put(ip, cpf);
			System.err.println("my servers size " + this.myServers.size());
		}

	}

	/*
	 * salvar dados
	 */
	private void saveData(String cpf, String st, double data, String time) {
		ArrayList<Dado> dataColeta;
		if (!trieDatas.containsKey(cpf)) {
			dataColeta = new ArrayList<Dado>();
			dataColeta.add(new Dado(data, st ,time));
			trieDatas.put(cpf, dataColeta);
		} else {
			dataColeta = trieDatas.get(cpf);
			dataColeta.add(new Dado(data, st ,time));
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

	/*
	 * envia dados para os clientes e salva os próprios dados
	 */
	private void senderDatas() throws IOException {
		ArrayList<Dado> cpfDatas;
		ArrayList<Dado> myDatas = this.myHomeCare.getDados();
		cpfDatas = this.trieDatas.get(this.pacienteCpf);
		cpfDatas.addAll(myDatas);
		System.err.println("size trie " + cpfDatas.size());
		for (Socket coleta : this.myClients) {
			PrintStream ps = new PrintStream(coleta.getOutputStream());
			for (int i = 0; i < myDatas.size(); i++) {
				ps.println("cpf<" + this.pacienteCpf + ">st:"
						+ myDatas.get(i).getTipo() + "<"
						+ myDatas.get(i).getValor() + ">" 
						+ "time<" + myDatas.get(i).getTime() + ">");
			}
			ps.println("cpf<" + this.pacienteCpf + ">st:end<");
		}
	}

	private void saveToFile() {
		try {
			String time = new Timestamp(new Date().getTime()).toString();
			FileOutputStream fileOutput = new FileOutputStream("homecare"
					+ time + ".log");
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

		//coleta.runServer();
	}

}
