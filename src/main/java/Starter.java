import de.dailab.jiactng.agentcore.SimpleAgentNode;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Starter {
	public static void main(String[] args) {
		SimpleAgentNode node = (SimpleAgentNode) new ClassPathXmlApplicationContext("Node.xml").getBean("CO2EmissionsNode");
	}
}
