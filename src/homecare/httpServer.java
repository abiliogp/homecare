package homecare;

import java.util.ArrayList;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/*
 * http://brunovernay.blogspot.com.br/2009/05/suns-jvm-has-http-server-embedded.html
 * http://docs.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html
 */

public class httpServer {

	private ArrayList<HomeCare> homeCares;

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
	 * Responde o cliente com as informa��es dispon�veis
	 */
	static class HandlerHttp implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
	
	public void listaInfo(){
		
	}
	
}
