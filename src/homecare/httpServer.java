package homecare;

import java.util.ArrayList;
import java.util.TreeMap;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/*
 * http://brunovernay.blogspot.com.br/2009/05/suns-jvm-has-http-server-embedded.html
 * http://docs.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html
 */

public class httpServer {

	/*
	 * Inicializa o servidor http
	 */
	public httpServer() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/", new HandlerHttp());
        server.setExecutor(null); // creates a default executor
        server.start();
	}
	
	/*
	 * Responde o cliente com as informações disponíveis
	 */
	static class HandlerHttp implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
        	ArrayList<Socket> clients = Coleta.me.getClientList();
        	TreeMap<String, ArrayList<Dado>> trieDatas = Coleta.me.getTrieDatas();
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
            for(Socket socket : clients) {
            	String ip = socket.getInetAddress().getHostAddress();
        		String body = "<tr>";
        		body += "<tr><td>Paciente</td><td>Nome</td></tr>";//nome do paciente
        		body += "<tr><td>IP</td><td>"+ip+"</td></tr>";//ip do paciente
            	if( trieDatas.containsKey(ip) ) {
            		ArrayList<Dado> dataColeta = trieDatas.get(ip);
            		for( Dado dado : dataColeta ) {
                		body += "<tr><td>Dado</td><td>"+dado.getDados()+" "+dado.getUnidadeMedida()+"</td></tr>";//nome do paciente
            		}
            	}
            	body += "</tr><tr></tr>";
                os.write(body.getBytes());
            }
            String footer = "</table>\n";
            footer += "</body>\n";
            footer += "</html>";
            os.write(footer.getBytes());
            os.close();
        }
    }
	
	public void listaInfo(){
		
	}

	public static void main(String[] args) throws IOException {
		httpServer server = new httpServer();
	}
}