package task;

public class TaskFactory {
	public iTask newTask(String taskType) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String fullTaskName = "workflow.task." + taskType + "Task";		
		return (iTask) Class.forName(fullTaskName).newInstance();
	}
}
