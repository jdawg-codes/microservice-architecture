package tool.query;

import java.util.List;

public interface iQueryCondition {
	public String field();
	public void field(String field);
	
	public String operator();
	public void operator(String operator);
	
	public Object value();
	public void value(Object value);
	
	public List<Object> values();
	public void values(List<Object> values);
	
	public String dataType();
	public void dataType(String dataType);
}
