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
		this.dataType = dataType;
	}
	
	public QueryCondition(String field, String operator, Object value, String dataType) {
		this.field = field;
		this.operator = operator;
		this.value = value;
		this.dataType = dataType;
	}
		
	@Override
	public String field() {
		return this.field;
	}

	@Override
	public void field(String field) {
		this.field = field;
	}

	@Override
	public String operator() {
		return this.operator;
	}

	@Override
	public void operator(String operator) {
		this.operator = operator;
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
	public List<Object> values() {
		return this.values;
	}

	@Override
	public void values(List<Object> values) {
		this.values = values;
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
