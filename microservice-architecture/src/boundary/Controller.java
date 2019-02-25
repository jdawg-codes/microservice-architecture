package boundary;

import java.util.Collection;

import service.EntityService;
import tool.ErrorContainer;
import tool.iDependencyContainer;

public class Controller {
	private static final String ErrorContainer = null;
	private iDependencyContainer dependencies;
	private iInputBoundary service;
	private iRequest request;
	
	public Controller(iDependencyContainer dependencies, iRequest request) {
		Collection<Error> errorContainer = new ErrorContainer<Error>();
		dependencies.put("error", errorContainer);
		
		iInputBoundary service = new EntityService(dependencies); 
		//TODO change type of service as new services are added. Create service factory if needed.
		
		this.dependencies = dependencies;
		this.service = service;
		this.request = request;
	}
	
	public iOutputBoundary execute() {
		return this.service.execute();
	}
}
