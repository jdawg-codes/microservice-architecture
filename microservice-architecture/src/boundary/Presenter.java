package boundary;

import tool.ErrorContainer;
import tool.iDependencyContainer;

public class Presenter implements iOutputBoundary {
	private iDependencyContainer dependencies;
	
	private iResponse response;
	
	public Presenter(iDependencyContainer dependencies) {
		this.dependencies = dependencies;
	}
	
	@Override
	public void response(iResponse response) {
		this.dependencies.put("response", response);
	}

	@Override
	public iResponse response() {		
		return (iResponse) this.dependencies.get("response");
	}

	@Override
	public iResponse present() {
		this.response = (iResponse) this.dependencies.get("response");
		
		ErrorContainer<String> errors = (ErrorContainer<String>) this.dependencies.get("error");
		
		if(this.response == null) {
			errors.add("An error occurred during processing resulting in an empty response.");
		}
		
		if(!errors.isEmpty()) {
			this.response.put("error", errors);
		}
		
		return (iResponse) this.response;
	}
}
