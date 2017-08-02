import dummyClasses.DummyNode;
import implementations.DAGImp;
import interfaces.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * This is a test suite for the DAGImp class.
 */
public class DAGImpTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void addNodesTest() {
        DAGImp dag = new DAGImp();
        DummyNode n1 = new DummyNode("node");

        dag.add(n1);

        List<Node> nodes = dag.getAllNodes();

        assertEquals(nodes.get(0).getName(), "node");
    }

}