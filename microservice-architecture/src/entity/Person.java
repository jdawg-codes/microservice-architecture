package entity;

import java.util.Arrays;
import java.util.List;

public class Person extends SimpleEntity {
	protected List<String> attributeList = Arrays.asList(
			"firstName","lastName");
	
	public List<String> attributeNames() {
		return this.attributeList;
	}
	
	//class attributes	
	private String firstName;
	private String lastName;
		
	public String firstName() {
		return this.firstName;
	}
	
	public void firstName(String fieldValue) {
		this.firstName = fieldValue;
	}
	
	public String lastName() {
		return this.lastName;
	}
	
	public void lastName(String fieldValue) {
		this.lastName = fieldValue;
	}
	
	public Person() {		
		this.createId();
	}
}
