package task;

import workflow.iWorkflow;

public interface iGatewayTask {
	public void addSubWorkflow(iWorkflow workflow);
}
