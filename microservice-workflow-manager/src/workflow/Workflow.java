package workflow;

import java.util.List;

import task.iTask;

public class Workflow implements iWorkflow {
	private List<iTask> tasks;
	
	@Override
	public void addTask(iTask task) {
		tasks.add(task);
	}

	@Override
	public Object execute() {
		// TODO Auto-generated method stub
		return null;
	}

}
