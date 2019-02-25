package workflow;

import task.iTask;

public interface iWorkflow {
	public void addTask(iTask task);
	public Object execute();
}
