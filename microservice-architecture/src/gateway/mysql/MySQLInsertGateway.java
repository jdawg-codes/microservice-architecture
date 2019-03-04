package gateway.mysql;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import boundary.iRequest;
import entity.iEntity;
import gateway.EntityGateway;
import tool.ErrorContainer;
import tool.iDependencyContainer;
import tool.query.QueryCondition;
import tool.query.QueryConditionGroup;
import tool.query.iQuery;
import tool.query.iQueryCondition;
import tool.query.iQueryParameter;
import tool.query.mysql.MySQLReadQueryBuilder;

public class MySQLInsertGateway extends EntityGateway {
	private Properties connectionDetails;
	private iRequest request;
	private Connection connection;
	private String queryString;
	
	public MySQLInsertGateway(iDependencyContainer dependencies, Properties connectionDetails) {
		this.dependencies = dependencies;
		this.connectionDetails = connectionDetails;
		
		this.error = (ErrorContainer<String>) dependencies.get("error");
		this.request = (iRequest) dependencies.get("request");
	}
	
	@Override
	public void connect() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			this.error.add("Incorrect gateway driver.");
		}
				
		try {
			String host = this.connectionDetails.getProperty("host");
			String database = this.connectionDetails.getProperty("database");
			String user = this.connectionDetails.getProperty("user");
			String password = this.connectionDetails.getProperty("password");
			
			String connectionString = host + "/" + database;
			
			this.connection = DriverManager.getConnection(connectionString,user,password);
		} catch (SQLException e) {
			this.error.add("Unable to connect to the requested gateway. Please try again later.");
		}		
	}

	@Override
	public void disconnect() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			this.error.add("Failed to close the gateway connection.");
		}
	}

	@Override
	public Map<String,Object> execute() {
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

	private Map<String,Object> insert() {
		this.queryString = "";
		
		ArrayList<String> fieldNamesArray = new ArrayList<String>();
		ArrayList<Object> fieldValuesArray = new ArrayList<Object>();
		
		//build parameterized query string
		try {
			this.queryString = "INSERT INTO " + this.request.get("entity").toString();
			
			String fieldsString = " (";
			String valuesString = " VALUES(";			
			
			Map<String, Object> attributes = (HashMap<String, Object>) this.request.get("attributes");				
			List<String> fieldNames = this.entity.attributeNames();

			int iteration = 0;
			
			for(String fieldName: fieldNames) {						
				if(fieldName.equals("id")) {
					if(iteration>0) {
						fieldsString += ",";
						valuesString += ",";
					}
					
					fieldNamesArray.add(fieldName);
					fieldValuesArray.add(this.entity.id());
					
					fieldsString += "`" + fieldName + "`";
					valuesString += "?";
					
					iteration++;
				} else if(attributes.get(fieldName)!=null) {	
					if(iteration>0) {
						fieldsString += ",";
						valuesString += ",";
					}
											
					fieldNamesArray.add(fieldName);
					fieldValuesArray.add(attributes.get(fieldName));
								
					fieldsString += "`" + fieldName + "`";
					valuesString += "?";
				
					iteration++;
				} 
			}
			
			fieldsString += ")";
			valuesString += ")";
			
			this.queryString += fieldsString + valuesString;
			
			System.out.println("query string: "+this.queryString); 
			//TODO remove after testing
		} catch(Exception e) {
			this.disconnect();
			this.error.add("Failed to create the requested entity.");
			return null;
		}
		
		//Parameterize query
		PreparedStatement statement = null;
		
		try {
			statement = this.connection.prepareStatement(this.queryString);
						
			int parameterIteration = 1;
						
			for(int i=0; i<fieldNamesArray.size(); i++) {
				String fieldName = fieldNamesArray.get(i);
				
				Method method;
				
				try {
					method = this.entity.getClass().getMethod(fieldName);
				} catch (NullPointerException | NoSuchMethodException | SecurityException e) {
					this.disconnect();
					this.error.add("The request contained an entity attribute that does not exist.");
					return null;
				}
				
				String fieldType = method.getReturnType().toString(); 
				
				if(fieldType.equals("class java.lang.String")) {
					//System.out.println("String is: "+fieldValuesArray.get(i).toString());
					statement.setString(parameterIteration, fieldValuesArray.get(i).toString());
				} 
				else if(fieldType.equals("int")) {
					//System.out.println("Integer is: " + (int) fieldValuesArray.get(i));
					statement.setInt(parameterIteration, (int) fieldValuesArray.get(i)); } 
				else {
					this.disconnect();
					this.error.add("The data type for attribute " + fieldName + " is not currently supported.");
					return null;
				}
				//TODO add other data types					
			
				parameterIteration++;
			}
		} catch (SQLException e) {
			this.error.add("Experienced a database error. Please try again.");
			this.disconnect();
			return null;
		}
			
		//execute query
		try {
			if(statement.executeUpdate()>0) {
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("result", "success");
				
				this.disconnect();				
				return resultMap;
			}
		} catch (SQLException e) {
			this.error.add("Failed to execute the database request.");
		}
		
		this.disconnect();			
		return null;		
	}
	
	private Collection<Map<String,Object>> select() {
		iQuery query;		
		Map<String,Object> userConditions = (Map<String, Object>) this.request.get("conditions");

		try {
			//collect all fields sent by user in request to validate their existence in the entity
			List<String> attributesFromFields = (ArrayList<String>) this.request.get("fields");
			List<String> attributesFromConditions = new ArrayList<String>(userConditions.keySet());			

			//TODO remove duplicates to reduce validation time
			List<String> allAttributes = new ArrayList<String>();			
			
			if(attributesFromFields!=null) {
				allAttributes.addAll(attributesFromFields); }
			
			if(attributesFromConditions!=null) {
				allAttributes.addAll(attributesFromConditions); }

			//validate that fields in request actually exist in entity to prevent injection attacks
			if(!this.hasAttributes(allAttributes)) {
				return null; }
	
			QueryConditionGroup conditionGroup = new QueryConditionGroup();		

			//convert user request conditions to QueryConditions and add to conditionGroup
			if(userConditions != null) {
				for(String userCondition : userConditions.keySet()) {
					Map<String,Object> userConditionObject = 
							((Map<String,Object>) userConditions.get(userCondition));
					
					//get data type of entity attribute
					Method method;
					
					try {
						method = this.entity.getClass().getMethod(userCondition);
					} catch (NullPointerException | NoSuchMethodException | SecurityException e) {
						this.disconnect();
						this.error.add("The request contained an entity attribute that does not exist.");
						return null;}
					
					iQueryCondition condition = new QueryCondition(
							(String) userCondition,
							(String) userConditionObject.get("operator"),
							(Object) userConditionObject.get("value"),
							(String) method.getReturnType().toString());
										
					conditionGroup.add(condition); } }
			
			query = MySQLReadQueryBuilder.query()
					.createNew(this.request.get("entity").toString())
					.returnFields((ArrayList<String>) this.request.get("fields"))
					.withConditions(conditionGroup)
					.returnBatch(1)
					.withBatchSize(20)
					.build();
			
				
		} catch(Exception e) {
			this.disconnect();
			this.error.add("Failed to build the requested query.");
			return null; }
		
		System.out.println(query.toString());
		
		//Parameterize query
		PreparedStatement statement = null;
		
		try {
			statement = this.connection.prepareStatement(query.toString());
			
			for(int i=0; i<query.parameters().size(); i++) {
				iQueryParameter parameter = query.parameters().get(i);
				
				if(parameter.dataType().equals("String")) {
					statement.setString(i+1, (String) parameter.value()); }
				else if(parameter.dataType().equals("Integer")) {
					statement.setInt(i+1, (int) parameter.value()); }
				else {
					this.disconnect();
					this.error.add("The data type for attribute " + parameter.name() + " is not currently supported.");
					return null; } }
		} catch (SQLException e) {
			this.error.add("Experienced a database error. Please try again.");
			this.disconnect();
			return null; }
		
		//execute query
		try {
			ResultSet results = statement.executeQuery();
			
			Collection<Map<String,Object>> resultArray = new ArrayList<Map<String,Object>>(); 
			
			while(results.next()) {
				
				Map<String,Object> resultMap = new HashMap<String, Object>();
			
				int columns = results.getMetaData().getColumnCount();
				
				for(int i=1; i<=columns; i++) {
					String columnName = results.getMetaData().getColumnName(i);
					Object value = results.getObject(i);
					
					resultMap.put(columnName, value); }
				
				resultArray.add(resultMap); }
			
			this.disconnect();				
			return resultArray;
		} catch (SQLException e) {
			this.error.add("Failed to execute the entity request."); }
		
		this.disconnect();			
		return null;
	}
	
	private Map<String,Object> update() {
		String queryString = "UPDATE " + this.request.get("entity").toString() + " SET ";
		
		ArrayList<String> fieldNamesArray = new ArrayList<String>();
		ArrayList<Object> fieldValuesArray = new ArrayList<Object>();

		//build parameterized query string
		try {
			Map<String, Object> attributes = (HashMap<String, Object>) this.request.get("attributes");				
			List<String> fieldNames = this.entity.attributeNames();

			int iteration = 0;
			
			for(String fieldName: fieldNames) {						
				if(attributes.get(fieldName)!=null && !fieldName.equals("id")) {	
					if(iteration>0) {
						queryString += ","; }
											
					fieldNamesArray.add(fieldName);
					fieldValuesArray.add(attributes.get(fieldName));
								
					queryString += "`" + fieldName + "` = ";
					queryString += "?";
				
					iteration++;
				} 
			}
			
			System.out.println("query string: "+queryString); 
			//TODO remove after testing
		} catch(Exception e) {
			this.disconnect();
			this.error.add("Failed to create the requested entity.");
			return null;
		}
		
		//Parameterize query
		PreparedStatement statement = null;
		
		try {
			statement = this.connection.prepareStatement(queryString);
						
			int parameterIteration = 1;
						
			for(int i=0; i<fieldNamesArray.size(); i++) {
				String fieldName = fieldNamesArray.get(i);
				
				Method method;
				
				try {
					method = this.entity.getClass().getMethod(fieldName);
				} catch (NullPointerException | NoSuchMethodException | SecurityException e) {
					this.disconnect();
					this.error.add("The request contained an entity attribute that does not exist.");
					return null;
				}
					
				String fieldType = method.getReturnType().toString(); 
						
				if(fieldType.equals("class java.lang.String")) {
					//System.out.println("String is: "+fieldValuesArray.get(i).toString());
					statement.setString(parameterIteration, fieldValuesArray.get(i).toString());
				} 
				else if(fieldType.equals("int")) {
					//System.out.println("Integer is: " + (int) fieldValuesArray.get(i));
					statement.setInt(parameterIteration, (int) fieldValuesArray.get(i)); } 
				else {
					this.disconnect();
					this.error.add("The data type for attribute " + fieldName + " is not currently supported.");
					return null;
				}
				//TODO add other data types					
		
				parameterIteration++;
			}
		} catch (SQLException e) {
			this.error.add("Experienced a database error. Please try again.");
			this.disconnect();
			return null;
		}
			
		//execute query
		try {
			if(statement.executeUpdate()>0) {
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("result", "success");
				
				this.disconnect();				
				return resultMap;
			}
		} catch (SQLException e) {
			this.error.add("Failed to execute the database request.");
		}
		
		this.disconnect();			
		return null;
	}
	
	private Map<String,Object> delete() {
		String queryString = "DELETE FROM " + this.request.get("entity").toString();
		
		ArrayList<String> fieldNamesArray = new ArrayList<String>();
		ArrayList<Object> fieldValuesArray = new ArrayList<Object>();

		//build parameterized query string
		try {			
			List<String> fieldNames = this.entity.attributeNames();

			int iteration = 0;
			
			for(String fieldName: fieldNames) {						
				
			}
			
			System.out.println("query string: "+queryString); 
			//TODO remove after testing
		} catch(Exception e) {
			this.disconnect();
			this.error.add("Failed to create the requested entity.");
			return null;
		}
		
		//Parameterize query
		PreparedStatement statement = null;
		
		try {
			statement = this.connection.prepareStatement(queryString);
						
			int parameterIteration = 1;
						
			for(int i=0; i<fieldNamesArray.size(); i++) {
				String fieldName = fieldNamesArray.get(i);
				
				Method method;
				
				try {
					method = this.entity.getClass().getMethod(fieldName);
				} catch (NullPointerException | NoSuchMethodException | SecurityException e) {
					this.disconnect();
					this.error.add("The request contained an entity attribute that does not exist.");
					return null;
				}
					
				String fieldType = method.getReturnType().toString(); 
						
				if(fieldType.equals("class java.lang.String")) {
					//System.out.println("String is: "+fieldValuesArray.get(i).toString());
					statement.setString(parameterIteration, fieldValuesArray.get(i).toString());
				} 
				else if(fieldType.equals("int")) {
					//System.out.println("Integer is: " + (int) fieldValuesArray.get(i));
					statement.setInt(parameterIteration, (int) fieldValuesArray.get(i)); } 
				else {
					this.disconnect();
					this.error.add("The data type for attribute " + fieldName + " is not currently supported.");
					return null;
				}
				//TODO add other data types					
		
				parameterIteration++;
			}
		} catch (SQLException e) {
			this.error.add("Experienced a database error. Please try again.");
			this.disconnect();
			return null;
		}
			
		//execute query
		try {
			if(statement.executeUpdate()>0) {
				Map<String,Object> resultMap = new HashMap<String, Object>();
				resultMap.put("result", "success");
				
				this.disconnect();				
				return resultMap;
			}
		} catch (SQLException e) {
			this.error.add("Failed to execute the database request.");
		}
		
		this.disconnect();			
		return null;
	}
}
