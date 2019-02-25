import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import task.TaskFactory;
import workflow.Workflow;

class WorkflowIntegrationTest {

	@Test
	void testWorkflow() {
		Workflow workflow = new Workflow();		
		TaskFactory taskFactory = new TaskFactory();
		
		try {
			workflow.addTask(taskFactory.newTask("Entity"));
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			fail("failed");
			e.printStackTrace();
		}
		
		workflow.execute();
	}

}
