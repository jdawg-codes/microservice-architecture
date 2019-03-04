package tool.query;

public interface iQueryParameter {
	public String name();
	public void parameterName(String name);
	
	public Object value();
	public void value(Object value);
	
	public String dataType();
	public void dataType(String dataType);
}
