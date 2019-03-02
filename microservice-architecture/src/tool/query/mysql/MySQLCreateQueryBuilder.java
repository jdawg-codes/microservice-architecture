package tool.query.mysql;

import tool.query.iQuery;

public final class MySQLCreateQueryBuilder {
	public static iQuery query() {
		return new MySQLCreateQuery();
	}
}
