import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Starter {
	public static void main(String[] args) throws LifecycleException {
		SimpleAgentNode node = (SimpleAgentNode) new ClassPathXmlApplicationContext("Node.xml").getBean("CO2EmissionsNode");
	}
}
