package tool.query;

import java.util.List;

public interface iQueryCondition {
	public String field();
	public iQueryCondition field(String field);
	
	public String operator();
	public iQueryCondition operator(String operator);
	
	public Object value();
	public iQueryCondition value(Object value);
	
	public List<Object> values();
	public iQueryCondition values(List<Object> values);
	
	public String dataType();
	public iQueryCondition dataType(String dataType);
}
