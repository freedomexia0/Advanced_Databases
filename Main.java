package Main;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import MongoDB.MongoDB;
import WebSocket.WebsocketServer;

public class Main {
	public static void main(String[] args) throws Exception {
		WebsocketServer server = new WebsocketServer( 1234 );
		server.start();
		System.out.println( "WebSocket Server on port: " + server.getPort() );

	}
}
