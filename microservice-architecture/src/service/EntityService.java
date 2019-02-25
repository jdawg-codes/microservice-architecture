package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import boundary.iInputBoundary;
import boundary.iOutputBoundary;
import boundary.iRequest;
import boundary.iRequestElement;
import boundary.iResponse;
import entity.EntityFactory;
import entity.iEntity;
import gateway.iEntityGateway;
import gateway.iGateway;
import tool.ErrorContainer;
import tool.iDependencyContainer;

public class EntityService implements iInputBoundary {
	private iDependencyContainer dependencies;
	
	private ErrorContainer<String> error;
	private iRequest request;
	private iEntityGateway gateway;
	private iResponse response;
	private iOutputBoundary presenter;
	private iEntity entity;
	
	public EntityService(iDependencyContainer dependencies) {
		this.dependencies = dependencies;

		this.error = (ErrorContainer<String>) this.dependencies.get("error");
		this.request = (iRequest) this.dependencies.get("request");
		this.gateway = (iEntityGateway) this.dependencies.get("gateway");
		this.response = (iResponse) this.dependencies.get("response");
		this.presenter = (iOutputBoundary) this.dependencies.get("output-boundary");
	}
	
	public iOutputBoundary execute() {
		String methodName = null;
	
		try {
			methodName = (String) this.request.get("method");
		} catch(Exception e) {			
			this.error.add("Please ensure your entity request possesses a 'method' attribute with accepted values (e.g., post, get, put, delete).");
			return this.presenter;
		}

		if(methodName.equals("post")) {
			return this.create();
		} else if(methodName.equals("get")) {
			return this.read();
		} else if(methodName.equals("put")) {
			return this.update();
		} else if(methodName.equals("delete")) {
			return this.delete();
		} else {
			this.error.add("The requested method is not supported. Currently supported methods include: post, get, put, and delete");
			return null;
		}
	}
	
	private iOutputBoundary create() {
		//create new object of requested entity
		iEntity entity = this.newEntity();
		
		if(entity == null) {
			return this.presenter; }
		
		if(!this.validateAttributes()) {
			return this.presenter; }
		
		if(!this.connectToGateway()) {
			return this.presenter; }
		
		//execute gateway command
		try {
			String entityName = this.request.get("entity").toString();
			
			Map<String, Object> result = this.gateway.execute();
			
			if(result != null) {
				this.response.put("entity", entityName);
				this.response.put("status", 201);
				this.response.put("message", "Created");				
				this.response.put("id", entity.id());
			} else {
				throw new Exception();
			}
		} catch(Exception e) {
			this.gateway.disconnect();
			this.error.add("Failed to perform the requested command.");
			return this.presenter;
		}
		
		return this.presenter;		
	}

	private iOutputBoundary read() {
		//create new object of requested entity
		iEntity entity = this.newEntity();
				
		if(entity == null) {
			return this.presenter; }
		
		//ensure that the requested fields are valid to avoid injection attacks
		try {
			ArrayList<String> fields = (ArrayList<String>) this.request.get("fields");
			
			if(fields != null) {
				for(String field : fields) {
					if(!this.entity.hasAttribute(field)) {
						this.error.add("The requested field, " + field + ", does not exist on the entity.");
						return this.presenter;
					}
				}
			}			
		} catch(Exception e) {
			this.error.add("Failed to perform the requested command.");
			return this.presenter;
		}
		
				
		if(!this.connectToGateway()) {
			return this.presenter; }
		
		//execute gateway command
		try {
			String entityName = this.request.get("entity").toString();
			
			Map<String, Object> result = this.gateway.execute();
			
			if(result != null) {
				this.response.put("entity", entityName);
				this.response.put("status", 200);
				this.response.put("message", "OK");				
				this.response.put("results", result);
			} else {
				throw new Exception();
			}
		} catch(Exception e) {
			this.gateway.disconnect();
			this.error.add("Failed to perform the requested command.");
			return this.presenter;
		}
		
		return this.presenter;
	}
	
	private iOutputBoundary update() {
		//create new object of requested entity
		iEntity entity = this.newEntity();
		
		if(entity == null) {
			return this.presenter; }
		
		if(!this.validateAttributes()) {
			return this.presenter; }
				
		if(!this.connectToGateway()) {
			return this.presenter; }
				
		//execute gateway command
		try {
			String entityName = this.request.get("entity").toString();
			
			Map<String, Object> result = this.gateway.execute();
			
			if(result != null) {
				this.response.put("entity", entityName);
				this.response.put("status", 200);
				this.response.put("message", "OK");				
			} else {
				throw new Exception();
			}
		} catch(Exception e) {
			this.gateway.disconnect();
			this.error.add("Failed to perform the requested command.");
			return this.presenter;
		}
				
		return this.presenter;
	}
	
	private iOutputBoundary delete() {
		//create new object of requested entity
		iEntity entity = this.newEntity();
		
		if(entity == null) {
			return this.presenter; }
				
		if(!this.connectToGateway()) {
			return this.presenter; }
				
		//execute gateway command
		try {
			String entityName = this.request.get("entity").toString();
			
			Map<String, Object> result = this.gateway.execute();
			
			if(result != null) {
				this.response.put("entity", entityName);
				this.response.put("status", 200);
				this.response.put("message", "Successfully deleted the entity");				
			} else {
				throw new Exception();
			}
		} catch(Exception e) {
			this.gateway.disconnect();
			this.error.add("Failed to perform the requested command.");
			return this.presenter;
		}
						
		return this.presenter;
	}
	
	private iEntity newEntity() {
		EntityFactory factory = new EntityFactory();
		iEntity entity = null;		
		
		String entityName = this.request.get("entity").toString();

		//create requested entity object
		try {
			entity = factory.newEntity(entityName);
			
			this.entity = entity;
			
			entity.dependencies(this.dependencies);
			this.dependencies.put("entity", entity);
			
			return entity;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			this.error.add("The entity, " + entityName + ", does not exist or is inaccessible. Please check the entity name and try again.");
			return null;
		}
	}
	
	private boolean validateAttributes() {
		//validate the entity attributes exist
		Map<String, Object> attributes = (HashMap<String, Object>) this.request.get("attributes");
		
		if(attributes==null) {
			this.error.add("To create or update an entity, you must supply attribute-value pairs.");
			return false;
		}
		
		//validate the user values against expected values
		try {			
			Set<String> attributeNames = attributes.keySet();
		
			for(String attributeName : attributeNames) {
				if(!this.entity.validateAttribute(attributeName, attributes)) {
					this.error.add("Failed to validate the value of " + attributeName);
					return false;
				}
			}
		} catch(Exception e) {
			this.error.add("Data validation failed. Please try again later.");
			return false;
		}
		
		return true;
	}
	
	private boolean connectToGateway() {
		//connect to entity gateway
		try {
			this.gateway.entity(entity);
			this.gateway.connect();
			return true;
		} catch(Exception e) {
			this.gateway.disconnect();
			this.error.add("Failed to make a connection to the data gateway. Please try again later");
			return false;
		}
	}
}
