package tool.query.mysql;

import tool.query.iQuery;

public final class MySQLReadQueryBuilder {
	public static iQuery query() {
		return new MySQLReadQuery();
	}
}
