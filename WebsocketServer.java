package WebSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import MongoDB.MongoDB;
import Neo4jDB.Neo4jDB;
import redis.clients.jedis.Jedis;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.gridfs.*;

import MongoDB.MongoDB;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.logging.LogManager;

import javax.imageio.ImageIO;

import org.bson.Document;
import org.bson.types.Binary;

public class WebsocketServer extends WebSocketServer {
	private final MongoDB mgDB = new MongoDB("mongodb://localhost:27017");
	private final Neo4jDB neo4jDB = new Neo4jDB("bolt://localhost:7687", "neo4j", "jmp");
	private final Jedis jedisDB = new Jedis("localhost");
	
	static public List<WebSocket> clients;
	
	public WebsocketServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		System.out.println( conn + " connected!" );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println( conn + " disconnected!" );
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		JSONObject obj = new JSONObject(message);
		processMessage(conn, obj.get("para0").toString(), obj.get("para1").toString(), obj.get("para2").toString(), obj.get("para3").toString());
	}
	
	public void processMessage(WebSocket conn, String para0, String para1, String para2, String para3) {
		switch(para0) {
		case "signup":
			if(para1.length() == 0 || para2.length() == 0) {
				sendMessage(conn, "alert","Invalid username or password!","","");
			}else if(mgDB.hasUser(para1)) {
				sendMessage(conn, "alert","User exist!","","");
			}else {
				mgDB.insertUser(para1, para2);
				neo4jDB.addUser(para1);
				sendMessage(conn, "alert","Sign up succeed!","","");
			}
			break;
		case "signin":
			if(para1.length() == 0 || para2.length() == 0) {
				sendMessage(conn, "alert","Invalid username or password!","","");
			}else if(mgDB.matchPassword(para1, para2)) {
				sendMessage(conn, "alert","User "+para1+" signed in!","","");
				sendMessage(conn, "signin","","","");
				
				sendMessage(conn, "userinfo",para1,"","");
				
				ArrayList<String> allitem = mgDB.getAllAvailableItems();

				for(int i =0;i<allitem.size();++i) {
					String pair = allitem.get(i);
					sendMessage(conn, "additem","item_container",pair,"");
				}
				
				sendMessage(conn, "cleantransaction","","","");
				String str = mgDB.getAllTransactions(para1);

				sendMessage(conn, "alltransactions",str,"","");
				
			}else {
				sendMessage(conn, "alert","Sign in failed!","","");
			}
			
			break;
		case "getiteminfo":
			String info = mgDB.getItemInfo(para1);
			sendMessage(conn, "getiteminfo",info,"","");
			
			String label = mgDB.getItemLabel(para1);
			neo4jDB.increaseRateOne(para2, label);
			
			String preference = neo4jDB.getPreferenceByRate(para2);
			
			String suggestedItemId = mgDB.getRandomItemIdByLabel(preference);
			String suggestinfo = mgDB.getItemInfo(suggestedItemId);
			sendMessage(conn, "getsuggesttiteminfo",suggestedItemId+";"+suggestinfo,"","");
			
			
			break;
		case "buyitem":
			mgDB.buyItem(para1,para2,para3);
			sendMessage(conn, "cleantransaction","","","");
			String str = mgDB.getAllTransactions(para1);

			sendMessage(conn, "alltransactions",str,"","");

			break;
		}
	}
	
	public void sendMessage(WebSocket conn, String para0, String para1, String para2, String para3) {
		JSONObject obj = new JSONObject();
		obj.put("para0",para0);
		obj.put("para1",para1);
		obj.put("para2",para2);
		obj.put("para3",para3);
		
		conn.send(obj.toString());
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {	
	}

}