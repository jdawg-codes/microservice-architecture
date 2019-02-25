package service;

import boundary.iInputBoundary;

public class ServiceFactory {
	public iInputBoundary newService(String serviceName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String fullServiceName = "service." + serviceName + "Service";
		
		return (iInputBoundary) Class.forName(fullServiceName).newInstance();
	}
}
