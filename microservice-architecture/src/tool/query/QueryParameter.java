package tool.query;

public class QueryParameter implements iQueryParameter {
	private String name;
	private Object value;
	private String dataType;
	
	public QueryParameter(String name, Object value, String dataType) {
		this.name = name;
		this.value = value;
		this.dataType = dataType;
	}
	
	public QueryParameter() {}
	
	@Override
	public String name() {
		return this.name;
	}

	@Override
	public void parameterName(String name) {
		this.name = name;
	}

	@Override
	public Object value() {
		return this.value;
	}

	@Override
	public void value(Object value) {
		this.value = value;
	}

	@Override
	public String dataType() {
		return this.dataType;
	}

	@Override
	public void dataType(String dataType) {
		this.dataType = dataType;
	}

}
