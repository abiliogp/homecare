package homecare;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/*
 * http://brunovernay.blogspot.com.br/2009/05/suns-jvm-has-http-server-embedded.html
 * http://docs.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html
 */

public class httpServer {

	private String host;
	private int port;
	private String user_pass;

	private enum Msg {
		temp, press, presd, card;
	}

	/*
	 * Inicializa o servidor http
	 */
	public httpServer() {
		loadConfigs();
	}

	/*
	 * Inicializa o servidor http
	 */
	public void initServer() throws IOException {
		try {
			HttpServer server;
			atualizaNoIP();
			server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/", new HandlerHttp());
			server.setExecutor(null); // creates a default executor
			server.start();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/*
	 * Get IP
	 */
	public static String getIP() {
		String ip = "127.0.0.1";
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					whatismyip.openStream()));
			ip = in.readLine();
			in.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return ip;
	}

	/*
	 * Carrega informaÃ§Ãµes de configuraÃ§Ã£o
	 */
	private void loadConfigs() {
		try {
			FileInputStream fileInput = new FileInputStream("homecare.web");
			BufferedReader bufferInput = new BufferedReader(
					new InputStreamReader(fileInput));
			host = bufferInput.readLine();
			user_pass = bufferInput.readLine();
			port = Integer.parseInt(bufferInput.readLine());
			fileInput.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	private class webServerVerify implements Runnable {

		public void run() {
			try {
				Thread.sleep(180000);
				if (InetAddress.getByName("homecare.sytes.net")
						.getHostAddress()
						.equals(httpServer.getIP())) {
					Coleta.me.webServer.initServer();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * HTTP GET request
	 */
	public void atualizaNoIP() throws Exception {
		Thread thread;
		webServerVerify webserververify = new webServerVerify();
		URL obj = new URL("http://dynupdate.no-ip.com/nic/update?hostname="
				+ host + "&myip=" + getIP());
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", "Homecare homecare_no_ip");
		con.setRequestProperty("Authorization", "Basic " + user_pass);// cmFwaGFlbHRzMzpzbm91Zmx5MzEy
																		// =
																		// base64encode("user:password")

		int responseCode = con.getResponseCode();
		System.out.println(responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());
		thread = new Thread(webserververify);
		thread.start();
	}

	/*
	 * Responde o cliente com as informaÃ§Ãµes disponÃ­veis
	 */
	static class HandlerHttp implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			ArrayList<Socket> clients = null;
			CopyOnWriteArrayList<Dado> dados = new CopyOnWriteArrayList<Dado>();
			try {
				clients = Coleta.me.getClientList();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			try {
				t.sendResponseHeaders(200, 0);
				OutputStream os = t.getResponseBody();
				String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
				header += "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
				header += "<head>\n";
				header += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n";
				header += "<title>HomeCare - UFPel</title>\n";
				header += "</head>\n";
				header += "<body>\n";
				header += "<table>\n";
				os.write(header.getBytes());
				if (clients != null) {
					for (Socket socket : clients) {
						String ip = socket.getInetAddress().getHostAddress();
						String cpf = Coleta.me.getCpfFromIp(ip);
						if (cpf != null) {
							dados = (CopyOnWriteArrayList<Dado>) Coleta
									.getLastDatasOfCpf(cpf);
							String body = "<tr>";
							body += "<tr><td>Paciente</td><td>Nome</td></tr>";// nome
																				// do
																				// paciente
							body += "<tr><td>CPF</td><td>" + cpf + "</td></tr>";// cpf
																				// do
																				// paciente
							if (dados != null) {
								String tipo;
								for (Dado dado : dados) {
									tipo = dado.getTipo();
									switch (Msg.valueOf(tipo)) {
									case temp:
										body += "<tr><td>Temperature</td><td>"
												+ dado.getValor()
												+ " ºC</td></tr>";
										break;
									case press:
										body += "<tr><td>Pressure Systolic</td><td>"
												+ dado.getValor()
												+ " mmHg</td></tr>";
										break;
									case presd:
										body += "<tr><td>Pressure Diastolic</td><td>"
												+ dado.getValor()
												+ " mmHg</td></tr>";
										break;
									case card:
										body += "<tr><td>Pulse</td><td>"
												+ dado.getValor()
												+ "</td></tr>";
										break;
									}
								}
							}
							body += "</tr><tr></tr>";
							os.write(body.getBytes());
						}
					}
				}
				String footer = "</table>\n";
				footer += "</body>\n";
				footer += "</html>";
				os.write(footer.getBytes());
				os.close();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	public void listaInfo() {

	}

	public static void main(String[] args) throws IOException {
		httpServer server = new httpServer();
	}
}