package tool.query.mysql;

import tool.query.Query;
import tool.query.QueryParameter;
import tool.query.iQuery;
import tool.query.iQueryCondition;

public class MySQLReadQuery extends Query {
	private int fieldIterator = 0;
	private int sortIterator = 0;
	
	@Override
	public iQuery build() {
		this.queryString = "SELECT ";
		
		//add return fields to query
		this.buildFieldList();
		
		//add table to query string
		this.queryString += " FROM " + this.tableName;
		
		//add condition statements to query
		this.buildConditions();
		
		//add groupings and grouping conditions to query
		this.buildGrouping();
		
		//add sort to query
		this.buildSort();
		
		//add batching to query
		this.buildBatch();
		
		return this;
	}
	
	private void buildFieldList() {
		//add field list to query string
		if(this.returnFields != null) {
			this.fieldIterator = 0;
			
			//add each field to query string
			this.returnFields.forEach((v) -> {
				if(this.fieldIterator > 0) {
					this.queryString += " , "; }
				
				this.queryString += this.tableName + ".`" + v + "` ";
				
				this.fieldIterator++;
			});	} 
		else if(this.returnFieldAliases != null) {
			this.fieldIterator = 0;
			
			//add each field to query string with an alias
			this.returnFieldAliases.forEach((k,v) -> {
				if(this.fieldIterator > 0) {
					this.queryString += " , "; }
				
				this.queryString += this.tableName + ".`" + k + "` AS " + v;
				
				this.fieldIterator++; }); } 
		else {
			this.queryString += " * "; }
	}
	
	private void buildConditions() {
		if(this.withConditions != null) {
			this.queryString += " WHERE ";
			
			int conditionCount = 0;
			
			for(iQueryCondition condition : this.withConditions.conditions()) {
				if(conditionCount > 0) {
					this.queryString += " " + this.withConditions.operator() + " ";
				}
				
				this.queryString += this.tableName + ".`" + condition.field() + "` ";
				
				if(condition.operator().equals("equals")) {
					this.queryString += " = "; } 
				else if(condition.operator().equals("contains")) {
					this.queryString += " LIKE ";					
					condition.value("%" + (String) condition.value() + "%"); } 
				else if (condition.operator().equals("gt")) {
					this.queryString += " > "; } 
				else if (condition.operator().equals("gte")) {
					this.queryString += " >= "; } 
				else if (condition.operator().equals("lt")) {
					this.queryString += " < "; } 
				else if (condition.operator().equals("lte")) {
					this.queryString += " <= "; } 
				else {
					this.queryString += " = "; }
				
				this.queryString += " ? ";
				
				this.parameters.add(
						new QueryParameter(
								condition.field(),
								condition.value(),
								condition.dataType()));
				
				conditionCount++;
			}
		}
	}
	
	private void buildGrouping() {
		if(this.byGrouping != null) { }
	}
	
	private void buildSort() {
		if(this.sortBy != null) {
			this.queryString += " ORDER BY ";
			
			this.sortIterator = 0;
			
			this.sortBy.forEach((k,v) -> {
				if(this.sortIterator > 0) {
					this.queryString += " , "; }
				
				this.queryString += k;
				
				if(v != null) {
					this.queryString += v; } });
		}
	}
	
	private void buildBatch() {
		if(this.withBatchSize != null) {
			this.queryString += " LIMIT " + this.withBatchSize.intValue();
			
			if(this.returnBatch != null) {
				int offset = this.withBatchSize.intValue() * (this.returnBatch.intValue() - 1);
				
				this.queryString += " OFFSET " + offset; } }
	}
}
