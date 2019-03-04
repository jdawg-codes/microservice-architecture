package tool.query;

import java.util.ArrayList;
import java.util.List;

public class QueryConditionGroup implements iQueryConditionGroup {
	private String operator = "AND"; //default grouping operator is AND
	private List<iQueryCondition> conditions = new ArrayList<iQueryCondition>();
		
	public QueryConditionGroup(String operator) {
		this.operator = operator;
	}
	
	public QueryConditionGroup() {
	
	}
	
	@Override
	public List<iQueryCondition> conditions() {
		return this.conditions;
	}

	@Override
	public void add(iQueryCondition condition) {
		this.conditions.add(condition);
	}

	@Override
	public String operator() {
		return this.operator;
	}

	@Override
	public void operator(String operator) {
		this.operator = operator;
	}

}
