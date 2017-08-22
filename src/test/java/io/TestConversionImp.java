package io;

import implementations.io.ConversionImp;
import implementations.io.InputImp;
import interfaces.structures.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestConversionImp {
    private List<String[]> _input;

    @Before
    public void setup() {
        _input = new ArrayList<>();
        String[] node1 = {"a", "1"};
        String[] node2 = {"b", "2"};
        String[] arc1 = {"a b", "1"};

        _input.add(node1);
        _input.add(node2);
        _input.add(arc1);
    }

    @Test
    public void testConversionToDAG() {
        InputImp input = mock(InputImp.class);
        when(input.getGraphData()).thenReturn(_input);
        ConversionImp conversion = new ConversionImp(input);

        List<Node> nodes = conversion.getDAG().getAllNodes();
        assertEquals(2, nodes.size());

        //Test correct node exists in the right place
        assertEquals("a", nodes.get(0).getName());
        assertEquals(1, nodes.get(0).getWeight());

        assertEquals("b", nodes.get(1).getName());
        assertEquals(2, nodes.get(1).getWeight());

        //Test correct successors and predecessors
        assertEquals("b", nodes.get(0).getSuccessors().get(0).getName());
        assertEquals("a", nodes.get(1).getPredecessors().get(0).getName());
    }
}
