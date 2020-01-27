package MongoDB;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongoDB implements AutoCloseable {
	private final MongoClient mongoClient;

	public MongoDB(String uri) {
		LogManager.getLogManager().reset();
		mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
	}
	
	@Override
	public void close() throws Exception {
		mongoClient.close();
		System.out.println("Mongo Closed!");
	}
	
	public boolean hasUser(String username) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("UserCL");
		
		BasicDBObject allQuery = new BasicDBObject();
		allQuery.put("username", username);
		
		MongoCursor<Document> cursor = mongoCollection.find(allQuery).iterator();
//		cursor.close();
		return cursor.hasNext(); 
	}
	
	public void insertUser(String username, String password) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("UserCL");
		
		Document doc = new Document("username", username)
				.append("password", password);
		
		mongoCollection.insertOne(doc);
	}
	
	public boolean matchPassword(String username, String password) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("UserCL");
		
		BasicDBObject allQuery = new BasicDBObject();
		allQuery.put("username", username);
		allQuery.put("password", password);
		
		MongoCursor<Document> cursor = mongoCollection.find(allQuery).iterator();
//		cursor.close();
		return cursor.hasNext(); 
	}
	
	public ArrayList<String> getAllAvailableItems() {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("ItemCL");

		MongoCursor<Document> cursor = mongoCollection.find().iterator();
		
		ArrayList<String> al = new ArrayList<String>();
		
	    while (cursor.hasNext()) {
	    	Document element = cursor.next();
	    	String itemid = element.getObjectId("_id").toString();
	    	String itemname = element.getString("name");
	    	al.add(itemid+";"+itemname);
	    }
	    cursor.close();
//	    System.out.println(al);
		return al;
	}
	
	public String getRandomItemIdByLabel(String label) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("ItemCL");
		
		BasicDBObject allQuery = new BasicDBObject();
		allQuery.put("label", label);
		
		MongoCursor<Document> cursor = mongoCollection.find(allQuery).iterator();

		int index = (int)(Math.random()*100)%3;
		int count = 0;
		while (cursor.hasNext()) {
	    	Document element = cursor.next();
	    	String itemid = element.getObjectId("_id").toString();
	    	if(count == index) {
	    		return itemid;
	    	}
	    	++count;
    	}
		return "";
	}

	public String getItemInfo(String id) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("ItemCL");

		BasicDBObject allQuery = new BasicDBObject();
		allQuery.put("_id",  new ObjectId(id));

		Document doc = mongoCollection.find(allQuery).first();
		
    	String itemname = doc.getString("name");
    	String price = doc.getString("price");
    	String image = doc.getString("image");
    	String res = itemname+";"+price+";"+image;
    	
//    	String res = image;

		return res;
	}
	
	public String getItemLabel(String id) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("ItemCL");

		BasicDBObject allQuery = new BasicDBObject();
		allQuery.put("_id",  new ObjectId(id));

		Document doc = mongoCollection.find(allQuery).first();
		
    	String res = doc.getString("label");
		return res;
	}
	
	public void buyItem(String userName, String itemId, String Num) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> ItemCL = mongoDatabase.getCollection("ItemCL");
		MongoCollection<Document> UserCL = mongoDatabase.getCollection("UserCL");
		
		Document doc = ItemCL.find(new BasicDBObject("_id", new ObjectId(itemId))).first();

		Document item = new Document("id", itemId).append("name", doc.getString("name")).append("num", Num);
		
		BasicDBObject updateQuery = new BasicDBObject("username", userName);
		BasicDBObject updateCommand = new BasicDBObject("$push", new BasicDBObject("transactions", item));
		UserCL.updateOne(updateQuery, updateCommand);
	}
	
	public String getAllTransactions(String username) {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("JavaDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("UserCL");
		
		BasicDBObject findFilter = new BasicDBObject();
		findFilter.put("username", username);
		
		List<Document> arr = (List<Document>) mongoCollection.find(findFilter).first().get("transactions");
		
		String str = "";
		for (Document element : arr) {
			str += element.get("name")+","+element.get("num")+";";
		}
		str = str.substring(0,str.length()-1);
		return str;
	}
	
	public String getImageFromMongoDB() {
		MongoDatabase mongoDatabase = mongoClient.getDatabase("BinaryDB");
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("IMG");
     
		Document doc = mongoCollection.find().first();

		return doc.getString("base64String");
	}


}
