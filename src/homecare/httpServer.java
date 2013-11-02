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

	/*
	 * Inicializa o servidor http
	 */
	public httpServer() throws IOException {
		try {
			HttpServer server;
			loadConfigs();
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
	private static String getIP() {
		String ip = "127.0.0.1";
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			ip = in.readLine();
			in.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return ip;
	}
	
	/*
	 * Carrega informações de configuração
	 */
	private void loadConfigs() {
		try {
			FileInputStream fileInput = new FileInputStream("homecare.web");
			BufferedReader bufferInput = new BufferedReader(new InputStreamReader(fileInput));
			host = bufferInput.readLine();
			user_pass = bufferInput.readLine();
			port = Integer.parseInt(bufferInput.readLine());
			fileInput.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	/*
	 * HTTP GET request
	 */
	private void atualizaNoIP() throws Exception {

		URL obj = new URL("http://dynupdate.no-ip.com/nic/update?hostname="+host+"&myip="+getIP());
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", "Homecare homecare_no_ip");
		con.setRequestProperty("Authorization",
				"Basic "+user_pass);// cmFwaGFlbHRzMzpzbm91Zmx5MzEy
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

	}

	/*
	 * Responde o cliente com as informações disponíveis
	 */
	static class HandlerHttp implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			ArrayList<Socket> clients = null;
			TreeMap<String,CopyOnWriteArrayList<Dado>> trieDatas = null;
			try {
				clients = Coleta.me.getClientList();
				trieDatas = Coleta.me.getTrieDatas();
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
				header += "<title>Página teste</title>\n";
				header += "</head>\n";
				header += "<body>\n";
				header += "<table>\n";
				os.write(header.getBytes());
				if( clients != null ) {
					for (Socket socket : clients) {
						String ip = socket.getInetAddress().getHostAddress();
						String body = "<tr>";
						body += "<tr><td>Paciente</td><td>Nome</td></tr>";// nome do
																			// paciente
						body += "<tr><td>IP</td><td>" + ip + "</td></tr>";// ip do
																			// paciente
						if (trieDatas.containsKey(ip)) {
							CopyOnWriteArrayList<Dado> dataColeta = trieDatas.get(ip);
							for (Dado dado : dataColeta) {
								body += "<tr><td>Dado</td><td>" + dado.getValor()
										+ " " + dado.getUnidadeMedida()
										+ "</td></tr>";// nome do paciente
							}
						}
						body += "</tr><tr></tr>";
						os.write(body.getBytes());
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