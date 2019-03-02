package tool.query.mysql;

import tool.query.Query;
import tool.query.iQuery;

public class MySQLCreateQuery extends Query {
	@Override
	public iQuery build() {
		this.queryString += "INSERT INTO " + this.tableName;
				
		this.buildValuePairStatement();		
		
		return this;
	}
	
	private void buildValuePairStatement() {
		
	}
}
