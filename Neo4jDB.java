package Neo4jDB;

import org.neo4j.driver.*;

import static org.neo4j.driver.Values.parameters;

import java.util.logging.LogManager;


public class Neo4jDB{
    final private Driver driver;
    final private Session session;

    public Neo4jDB(String uri, String user, String password){
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        session = driver.session();
    }

    public void addlabel(String type) {
    	session.run("MERGE (l:Label {type:$t})",parameters("t", type));
    }
    
    public void addUser(String name) {
    	if(!userExist(name)) {
		session.run("CREATE (u:User {name:$n})",parameters("n", name));
		
		session.run("MATCH (s:Label {type:'Sports'}) "
				+ "MATCH (o:Label {type:'Digital Product'}) "
				+ "MATCH (d:Label {type:'Office Product'}) "
				+ "MATCH (u:User {name:$n})"
				+ "CREATE (u)-[:Rate{value:0}]->(s) "
				+ "CREATE (u)-[:Rate{value:0}]->(o) "
				+ "CREATE (u)-[:Rate{value:0}]->(d)",parameters("n", name));
    	}
    }
    
    public void increaseRateOne(String name, String type) {
    	Result result = session.run("MATCH (u:User {name:$n})"
    		    +"MATCH (u)-[r:Rate]->(s:Label{type:$t})"
    			+ "RETURN r.value",parameters("n", name,"t", type));
    	
    	if(result.hasNext()) {
    		Record record = result.next();
    		int newRate = record.get("r.value").asInt() + 1;
    		
    		session.run("MATCH (u:User {name:$n}) "
    				+ "MATCH (u)-[r:Rate]->(l:Label{type:$t})" 
    				+ "SET r.value = $v",parameters("n", name,"t", type,"v", newRate));
    	}
    }
    
    public boolean userExist(String name) {
    	Result result = session.run("MATCH (u:User {name:$n}) return u",parameters("n", name));
    	if(result.hasNext()) {
    		return true;
    	}else {
    		return false;
    	}
    }
    
    public String getPreferenceByRate(String name) {
    	Result result = session.run("MATCH (u:User {name:$n})"
    		    +"MATCH (u)-[sr:Rate]->(s:Label{type:'Sports'})"
    		    +"MATCH (u)-[or:Rate]->(o:Label{type:'Office Product'})"
    		    +"MATCH (u)-[dr:Rate]->(d:Label{type:'Digital Product'})"
    			+ "RETURN sr.value, or.value, dr.value",parameters("n", name));
    	
    	if(result.hasNext()) {
    		Record record = result.next();
    		int sr = record.get("sr.value").asInt();
    		int or = record.get("or.value").asInt();
    		int dr = record.get("dr.value").asInt();
    		
    		if(sr >= or) {
    			if(sr >= dr) {
        			return "Sports";
        		}else {
        			return "Digital Product";
        		}
    		}else {
    			if(or >= dr) {
    				return "Office Product";
        		}else {
        			return "Digital Product";
        		}
    		}
    	}
		return "";
    }
    
    public static void main(String... args){
    	LogManager.getLogManager().reset();
        Neo4jDB neo4jDB = new Neo4jDB("bolt://localhost:7687", "neo4j", "jmp");
        
        neo4jDB.addlabel("Sports");
        neo4jDB.addlabel("Office Product");
        neo4jDB.addlabel("Digital Product");
        
//        neo4jDB.addUser("root");
//        neo4jDB.addUser("admin");
//        neo4jDB.addUser("user");
//        
//        neo4jDB.increaseRateOne("user", "Sports");
//        neo4jDB.increaseRateOne("user", "Office Product");
//        neo4jDB.increaseRateOne("user", "Digital Product");
//
//        
//        String str = neo4jDB.getPreferenceByRate("user");
//        System.out.println(str);
        
        neo4jDB.session.close();
        neo4jDB.driver.close();


        System.out.println("End");
    }
}
