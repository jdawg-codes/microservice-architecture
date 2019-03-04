package tool.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Query implements iQuery {
	protected String queryString;
	protected List<iQueryParameter> parameters = new ArrayList<iQueryParameter>();
	
	protected String tableName;
	protected List<String> returnFields;
	protected Map<String,String> returnFieldAliases;
	protected List<String> byGrouping;
	protected iQueryConditionGroup withGropuingConditions;
	protected Map<String,String> sortBy;
	protected Integer returnBatch;
	protected Integer withBatchSize;
	
	protected Map<String, Object> bySetting;
	
	protected iQueryConditionGroup withConditions;
		
	@Override
	public iQuery createNew(String tableName) {
		this.tableName = tableName;
		
		return this;
	}

	@Override
	public iQuery readFrom(String tableName) {
		this.tableName = tableName;
		
		return this;
	}
	
	@Override
	public iQuery update(String tableName) {
		this.tableName = tableName;
		
		return this;
	}

	@Override
	public iQuery deleteFrom(String tableName) {
		this.tableName = tableName;
		
		return this;
	}

	//fields to return from read
	@Override
	public iQuery returnFields(List<String> fields) {
		this.returnFields = fields;
		
		return this;
	}

	//fields to return from read with aliases
	@Override
	public iQuery returnFields(Map<String, String> fields) {
		this.returnFieldAliases = fields;
		
		return null;
	}

	//group results
	@Override
	public iQuery byGrouping(List<String> groups) {
		this.byGrouping = groups;
		
		return this;
	}

	//groping conditions
	@Override
	public iQuery withGroupingConditions(iQueryConditionGroup conditions) {
		this.withGropuingConditions = conditions;
		
		return this;
	}
	
	//sort read results
	@Override
	public iQuery sortBy(Map<String,String> fields) {
		this.sortBy = fields;
			
		return this;
	}
	
	//set which batch will be returned (e.g., 1st batch, 2nd batch,... nth batch)
	@Override
	public iQuery returnBatch(int batchNumber) {
		this.returnBatch = batchNumber;
		
		return this;
	}
	
	//limit number of read results in each batch
	@Override
	public iQuery withBatchSize(int size) {
		this.withBatchSize = size;
		
		return this;
	}

	//field value pairs for creation or update
	@Override
	public iQuery bySetting(Map<String, Object> fieldValuePairs) {
		this.bySetting = fieldValuePairs;
		
		return this;
	}

	//set conditions on query
	@Override
	public iQuery withConditions(iQueryConditionGroup conditions) {
		this.withConditions = conditions;
		
		return this;
	}
	
	public String toString() {
		return this.queryString;
	}
	
	@Override
	public List<iQueryParameter> parameters() {
		return this.parameters;
	}
}
