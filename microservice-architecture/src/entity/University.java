package entity;

import java.util.Arrays;
import java.util.List;

public class University extends SimpleEntity {
	protected List<String> attributeList = Arrays.asList("id","name");
	
	public List<String> attributeNames() {
		return this.attributeList;
	}
	
	//class attributes	
	private String name;
		
	public String name() {
		return this.name;
	}
	
	public void name(String fieldValue) {
		this.name = fieldValue;
	}
	
	public University() {
		this.createId();
		//this.attributeList.add("name");
	}
}