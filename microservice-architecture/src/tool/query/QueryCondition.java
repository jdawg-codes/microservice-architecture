package tool.query;

import java.util.List;

public class QueryCondition implements iQueryCondition {
	private String field;
	private String operator;
	private Object value;
	private List<Object> values;
	private String dataType;
	
	public QueryCondition(String field, String operator, List<Object> values, String dataType) {
		this.field = field;
		this.operator = operator;
		this.values = values;
		
		prepareDataType(dataType);
	}
	
	public QueryCondition(String field, String operator, Object value, String dataType) {
		this.field = field;
		this.operator = operator;
		this.value = value;
		
		prepareDataType(dataType);
	}
	
	public QueryCondition() {
		
	}
		
	@Override
	public String field() {
		return this.field;
	}

	@Override
	public iQueryCondition field(String field) {
		this.field = field;
		
		return this;
	}

	@Override
	public String operator() {
		return this.operator;
	}

	@Override
	public iQueryCondition operator(String operator) {
		this.operator = operator;
		
		return this;
	}

	@Override
	public Object value() {
		return this.value;
	}

	@Override
	public iQueryCondition value(Object value) {
		this.value = value;
		
		return this;
	}

	@Override
	public List<Object> values() {
		return this.values;
	}

	@Override
	public iQueryCondition values(List<Object> values) {
		this.values = values;
		
		return this;
	}

	@Override
	public String dataType() {
		return this.dataType;
	}

	@Override
	public iQueryCondition dataType(String dataType) {
		prepareDataType(dataType);
		
		return this;
	}

	private void prepareDataType(String dataType) {
		if(dataType.equals("class java.lang.String")) {
			this.dataType = "String";
		} else if(dataType.equals("int")) {
			this.dataType = "Integer";
		} else {
			this.dataType = "String";
		}
	}
}
