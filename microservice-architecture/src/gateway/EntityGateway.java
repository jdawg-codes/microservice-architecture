package gateway;

import java.util.List;

public abstract class EntityGateway implements iEntityGateway {
	
	//validate that
	public boolean attributesExist(List<String> attributes) {
		//if no attributes are provided, we don't care
		if(attributes == null) {
			return true;
		}
		
		attributes.forEach((v) -> {
			
		});
		
		
		return false;		
	}
}
