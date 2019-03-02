package gateway;

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
import tool.ErrorContainer;
import tool.iDependencyContainer;
import tool.query.QueryCondition;
import tool.query.QueryConditionGroup;
import tool.query.iQuery;
import tool.query.mysql.MySQLCreateQueryBuilder;
import tool.query.mysql.MySQLReadQueryBuilder;

public class MySQLGateway extends EntityGateway {
	private ErrorContainer<String> error;
	private iDependencyContainer dependencies;
	private Properties connectionDetails;
	private iRequest request;
	private iEntity entity;
	private Connection connection;
	
	private String queryString;
	
	public MySQLGateway(iDependencyContainer dependencies, Properties connectionDetails) {
		this.dependencies = dependencies;
		this.connectionDetails = connectionDetails;
		
		this.error = (ErrorContainer<String>) dependencies.get("error");
		this.request = (iRequest) dependencies.get("request");
	}
	
	public void entity(iEntity entity) {
		this.entity = entity;
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

	public Map<String,Object> insert() {
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
	
	public Collection<Map<String,Object>> select() {
		if(this.attributesExist((ArrayList<String>) this.request.get("fields"))) {
			
		}
		
		
		QueryConditionGroup conditionGroup = new QueryConditionGroup();
		
		Map<String,Object> conditions = (Map<String, Object>) this.request.get("conditions");
		
		if(conditions != null) {
			//conditionGroup.add(new QueryCondition());
		}
		
		iQuery query = MySQLReadQueryBuilder.query()
				.createNew(this.request.get("entity").toString())
				.returnFields((ArrayList<String>) this.request.get("fields"))
				.returnBatch(1)
				.withBatchSize(20)
				.build();
		
		System.out.println("query builder string = " + query.toString());
		
		this.queryString = "SELECT ";
		
		ArrayList<String> fieldNamesArray = new ArrayList<String>();
		ArrayList<Object> fieldValuesArray = new ArrayList<Object>();
		
		ArrayList<Integer> containsArray = new ArrayList<Integer>();  

		//build parameterized query string
		try {
			ArrayList<String> fields = (ArrayList<String>) this.request.get("fields");
			
			if(fields != null) {
				int iteration = 0;
				
				for(String field : fields) {
					if(this.entity.hasAttribute(field)) {
						if(iteration>0) {
							this.queryString += ", ";
						}
						
						this.queryString += "`" + field + "`";
						
						iteration++;
					} else {
						this.error.add("The requested entity does not have a field called: " + field);
					}
				}
			} else {
				this.queryString += " * ";
			}
			
			this.queryString += " FROM " + this.request.get("entity").toString();			
			
			if(conditions != null) {
				this.queryString += " WHERE ";
				
				conditions.forEach((k,v) -> {
					this.queryString += "`" + k + "`";
					
					if(((Map<String, Object>) v).get("operator").equals("equals")) {
						this.queryString += "=? ";
						
						fieldNamesArray.add(k.toString());
						fieldValuesArray.add(((Object) ((Map<String, Object>) v).get("value")));
					}
					else if(((Map<String, Object>) v).get("operator").equals("contains")) {
						this.queryString += " LIKE ? ";
						fieldNamesArray.add(k.toString());
						fieldValuesArray.add(((Object) ((Map<String, Object>) v).get("value")));
						
						containsArray.add(fieldNamesArray.size()-1, fieldNamesArray.size()-1);
					} 
				});
			}
			
			Integer limit = (Integer) this.request.get("limit");
			Integer offset = (Integer) this.request.get("offset");
			
			if(limit==null) {
				limit = 20;
				
				this.queryString += " LIMIT " + limit;
				
				if(offset!=null) {
					this.queryString += " OFFSET " + offset;
				}
			}
				
			System.out.println("query string: "+this.queryString); 
			//TODO remove after testing
		} catch(Exception e) {
			this.disconnect();
			this.error.add("Failed to build the requested query.");
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
					String parameterValue = fieldValuesArray.get(i).toString();
					
					try {
						if(containsArray.get(i)!=null) {
							parameterValue = "%" + parameterValue + "%";
							statement.setString(parameterIteration, parameterValue);
						}
					} catch(IndexOutOfBoundsException e) {
						statement.setString(parameterIteration, parameterValue);
					}					
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
			ResultSet results = statement.executeQuery();
			
			Collection<Map<String,Object>> resultArray = new ArrayList<Map<String,Object>>(); 
			
			while(results.next()) {
				
				Map<String,Object> resultMap = new HashMap<String, Object>();
			
				int columns = results.getMetaData().getColumnCount();
				
				for(int i=1; i<=columns; i++) {
					String columnName = results.getMetaData().getColumnName(i);
					Object value = results.getObject(i);
					
					resultMap.put(columnName, value);
				}
				
				resultArray.add(resultMap);
			}
			
			this.disconnect();				
			return resultArray;
		} catch (SQLException e) {
			this.error.add("Failed to execute the database request.");
		}
		
		this.disconnect();			
		return null;
	}
	
	public Map<String,Object> update() {
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
						queryString += ",";
					}
											
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
	
	public Map<String,Object> delete() {
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
