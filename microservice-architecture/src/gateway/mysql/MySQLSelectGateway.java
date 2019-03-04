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

public class MySQLSelectGateway extends EntityGateway {
	private MySQLGateway gateway;
	
	public MySQLSelectGateway(MySQLGateway gateway) {
		this.gateway = gateway;
	}
	
	@Override
	public void connect() {
		this.gateway.connect();		
	}

	@Override
	public void disconnect() {
		this.gateway.disconnect();
	}

	@Override
	public Map<String,Object> execute() {
		iQuery query;		
		PreparedStatement statement;
		
		query = this.buildQuery();
		
		if(query == null) {
			return null; }
 		
		statement = this.parameterizeQueryStatement(query);
		
		if(statement == null) {
			return null; }
		
		return this.getQueryResults(statement);
	}
	
	private iQuery buildQuery() {
		iQuery query;
		Map<String,Object> userConditions = (Map<String, Object>) this.gateway.request().get("conditions");

		try {
			//collect all fields sent by user in request to validate their existence in the entity
			List<String> attributesFromFields = (ArrayList<String>) this.gateway.request().get("fields");
			List<String> attributesFromConditions = new ArrayList<String>(userConditions.keySet());			

			//TODO remove duplicates to reduce validation time
			List<String> allAttributes = new ArrayList<String>();			
			
			if(attributesFromFields!=null) {
				allAttributes.addAll(attributesFromFields); }
			
			if(attributesFromConditions!=null) {
				allAttributes.addAll(attributesFromConditions); }

			//validate that fields in request actually exist in entity to prevent injection attacks
			if(!this.gateway.hasAttributes(allAttributes)) {
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
						method = this.gateway.entity().getClass().getMethod(userCondition);
					} catch (NullPointerException | NoSuchMethodException | SecurityException e) {
						this.gateway.disconnect();
						this.gateway.error().add("The request contained an entity attribute that does not exist.");
						return null;}
					
					iQueryCondition condition = new QueryCondition(
							(String) userCondition,
							(String) userConditionObject.get("operator"),
							(Object) userConditionObject.get("value"),
							(String) method.getReturnType().toString());
										
					conditionGroup.add(condition); } }
			
			query = MySQLReadQueryBuilder.query()
					.createNew(this.gateway.request().get("entity").toString())
					.returnFields((ArrayList<String>) this.gateway.request().get("fields"))
					.withConditions(conditionGroup)
					.returnBatch(1)
					.withBatchSize(20)
					.build();
			
			System.out.println(query.toString());
			
			return query;
		} catch(Exception e) {
			this.gateway.disconnect();
			this.gateway.error().add("Failed to build the requested query.");
			return null; }		
	}
	
	private PreparedStatement parameterizeQueryStatement(iQuery query) {
		//Parameterize query
		PreparedStatement statement = null;
		
		try {
			statement = this.gateway.connection().prepareStatement(query.toString());
			
			for(int i=0; i<query.parameters().size(); i++) {
				iQueryParameter parameter = query.parameters().get(i);
				
				if(parameter.dataType().equals("String")) {
					statement.setString(i+1, (String) parameter.value()); }
				else if(parameter.dataType().equals("Integer")) {
					statement.setInt(i+1, (int) parameter.value()); }
				else {
					this.gateway.disconnect();
					this.gateway.error().add("The data type for attribute " + parameter.name() + " is not currently supported.");
					return null; } }
			
			return statement; 
		} catch (SQLException e) {
			this.gateway.error().add("Experienced a database error. Please try again.");
			this.gateway.disconnect();
			return null; }
	}
	
	private Map<String, Object> getQueryResults(PreparedStatement statement) {
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
			
			this.gateway.disconnect();				
			
			Map<String,Object> resultsMap = new HashMap<String,Object>();			
			resultsMap.put("data", resultArray);
			
			return resultsMap;
		} catch (SQLException e) {
			this.gateway.disconnect();
			this.gateway.error().add("Failed to execute the entity request."); 
			return null; }
	}
}
