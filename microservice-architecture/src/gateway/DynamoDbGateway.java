package gateway;

import java.util.HashMap;
import java.util.Map;
import entity.iEntity;
import tool.ErrorContainer;
import tool.iDependencyContainer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.GetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

import boundary.iRequest;

public class DynamoDbGateway implements iEntityGateway {
	private AmazonDynamoDB connection;
	private iDependencyContainer dependencies;
	private iRequest request;
	private iEntity entity;
	
	private ErrorContainer<String> error;
	
	public DynamoDbGateway(iDependencyContainer dependencies) {
		this.dependencies = dependencies;
		
		this.error = (ErrorContainer<String>) dependencies.get("error");
		this.request = (iRequest) dependencies.get("request");
	}
	
	@Override
	public void entity(iEntity entity) {
		this.entity = entity;
	}
	
	@Override
	public void connect() {
		try {
			this.connection = AmazonDynamoDBClientBuilder.standard().build();
		} catch(Exception e) {
			this.error.add("Unable to connect to the requested gateway. Please try again later.");
		}		
	}

	@Override
	public void disconnect() {
		try {
			this.connection.shutdown();
		} catch (Exception e) {
			this.error.add("Failed to close the gateway connection.");
		}		
	}

	@Override
	public Map<String, Object> execute() {
		String methodName = null;
		
		try {
			methodName = (String) this.request.get("method");
		} catch(Exception e) {			
			this.error.add("Please ensure your request possesses a 'method' attribute with accepted values (e.g., post, get, put, delete).");
			return null;
		}
		
		if(methodName.equals("post")) {
			return this.insert();
		} else if(methodName.equals("get")) {
			Map<String,Object> resultsMap = new HashMap<String,Object>();			
			resultsMap.put("data", this.select());
						
			return resultsMap;
		} else if(methodName.equals("put")) {
			return this.update();
		} else if(methodName.equals("delete")) {
			return this.delete();
		} else {
			this.error.add("The requested method is not supported. Currently supported methods include: post, get, put, and delete");
			return null;
		}
	}

	private Object select() {
		DynamoDB database = new DynamoDB(this.connection);
		
		Table table = database.getTable(this.request.get("entity").toString());
		return null;
	}

	private Map<String, Object> update() {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<String, Object> insert() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Map<String, Object> delete() {
		
		return null;
	}

	

}
