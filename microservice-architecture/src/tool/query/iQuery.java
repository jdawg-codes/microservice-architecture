package tool.query;

import java.util.List;
import java.util.Map;

public interface iQuery {
	//create query
	public iQuery createNew(String tableName);
	
	//read query
	public iQuery readFrom(String tableName);
	public iQuery returnFields(List<String> fields);
	public iQuery returnFields(Map<String,String> fields);
	public iQuery byGrouping(List<String> groups);
	public iQuery withGroupingConditions(iQueryConditionGroup conditions);
	public iQuery sortBy(Map<String,String> fields);
	public iQuery returnBatch(int batchNumber);
	public iQuery withBatchSize(int size);
	
	//update query
	public iQuery update(String tableName);
			
	public iQuery deleteFrom(String tableName);
	
	//shared by create and update queries
	public iQuery bySetting(Map<String,Object> fieldValuePairs);
	
	//shared by many type of queries
	public iQuery withConditions(iQueryConditionGroup conditions);
	
	//execute build
	public iQuery build();
	
	public String toString();

	
}
