package tool.query;

import java.util.List;

public interface iQueryConditionGroup {
	public List<iQueryCondition> conditions();
	public void add(iQueryCondition condition);
	
	public String operator();
	public void operator(String operator);
}
