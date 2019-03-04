package gateway;

import java.util.List;

import boundary.iRequest;
import entity.iEntity;
import tool.ErrorContainer;
import tool.iDependencyContainer;

public abstract class EntityGateway implements iEntityGateway {
	protected iEntity entity;
	protected iDependencyContainer dependencies;
	protected ErrorContainer<String> error;
	protected iRequest request;
	
	private int attributeErrorCount = 0;
	
	public void entity(iEntity entity) {
		this.entity = entity;
	}
	
	public iEntity entity() {
		return this.entity;
	}
	
	public iRequest request() {
		return this.request;
	}
	
	public ErrorContainer<String> error() {
		return this.error();
	}
	
	//validate that entity has attributes in list
	public boolean hasAttributes(List<String> attributes) {
		//if no attributes are provided, we don't care
		if(attributes == null) {
			return true;
		}
		
		this.attributeErrorCount = 0;
		
		attributes.forEach((v) -> {
			if(!this.entity.hasAttribute(v)) {
				this.attributeErrorCount++;
				this.error.add("The requested entity does not have a attribute called: " + v);
			}
		});
		
		if(this.attributeErrorCount > 0) {
			return false;
		} else {
			return true;
		}	
	}
}
