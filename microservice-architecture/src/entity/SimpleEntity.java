package entity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import tool.ErrorContainer;
import tool.iDependencyContainer;

public abstract class SimpleEntity implements iEntity {
	protected String id = null;
	protected String primaryKey;
	protected iDependencyContainer dependencies;
	protected ErrorContainer<String> error;
	
	public void dependencies(iDependencyContainer dependencies) {
		this.dependencies = dependencies;
		
		this.error = (ErrorContainer<String>) dependencies.get("error");
	}
	
	public void createId() {
		if(this.id == null) {
			PrimaryKey key = new PrimaryKey();
			this.id = key.get();
		}
	}
	
	public String id() {
		if(this.id == null) {
			this.createId();
		}
		
		return this.id;
	}
	
	public void id(String id) {
		if(this.id == null) {
			this.id = id;
		}
	}
	
	public boolean hasAttribute(String attributeName) {
		if(this.attributeNames().contains(attributeName)) {
			return true;
		}
		
		return false;
	}
	
	public boolean validateAttribute(String attributeName, Map<String,Object> attributes) {
		if(!this.hasAttribute(attributeName)) {
			this.error.add("The attribute " + attributeName + " is not a valid attribute for the requested entity.");
			return false;
		}

		Method method = null;
		
		try {
			Class<?> parameterType = this.getClass().getMethod(attributeName, null).getReturnType();
			method = this.getClass().getMethod(attributeName,parameterType);
		} catch (NoSuchMethodException | SecurityException e) {
			this.error.add("Unable to access the requested entity attribute");
			return false;
		}
	
		try {
			method.invoke(this,attributes.get(attributeName));
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			this.error.add("Unable to validate " + attributeName);
			return false;
		}
	}
}
