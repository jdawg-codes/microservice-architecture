import java.util.Properties;

import boundary.Controller;
import boundary.JSONRequest;
import boundary.JSONResponse;
import boundary.Presenter;
import boundary.iOutputBoundary;
import boundary.iRequest;
import boundary.iResponse;
import gateway.MySQLGateway;
import gateway.iGateway;
import tool.DependencyContainer;
import tool.ErrorContainer;

public class Main {
	public String main(String jsonString) {
		//dependency configuration
		DependencyContainer dependencies = new DependencyContainer();

			ErrorContainer<String> errorContainer = new ErrorContainer<String>();
			dependencies.put("error", errorContainer);
			
			iRequest request = new JSONRequest(jsonString);
			dependencies.put("request", request);
			
			Properties gatewayConnection = new Properties();
			gatewayConnection.setProperty("driver", "com.mysql.cj.jdbc.Driver");
			gatewayConnection.setProperty("host", "jdbc:mysql://localhost:3306");
			gatewayConnection.setProperty("database", "MicroserviceTest");
			gatewayConnection.setProperty("user", "testuser");
			gatewayConnection.setProperty("password", "password");
			iGateway gateway = new MySQLGateway(dependencies,gatewayConnection);
			dependencies.put("gateway", gateway);
			
			iResponse response = new JSONResponse();
			dependencies.put("response", response);
	
			iOutputBoundary presenter = new Presenter(dependencies);
			dependencies.put("output-boundary", presenter);

		//instantiate controller
		Controller controller = new Controller(dependencies,request);

		try {
			String result = controller.execute().present().toJSONString();
			return result;			
		} catch(Throwable e) {
			System.out.println("failed to present the presenter: " + e.getMessage());
			return null;
		}
	}
}
