package implementations.algorithm;

import interfaces.algorithm.AlgorithmNode;

import java.util.Objects;

/**
 * This class stores a representation of a node and core from the
 * algorithm class. This allows us to check for validity and calculate
 * time to run without extra baggage of the node class.
 */
public class AlgorithmNodeImp implements AlgorithmNode {
	final private String _nodeName;
	private int _core;

	public AlgorithmNodeImp(String nodeName) {
		_nodeName = nodeName;
	}

	/**
	 * Create a copy of the AlgorithmNodeImp object
	 *
	 * @return a copy of the AlgorithmNodeImp
	 */
	@Override
	public AlgorithmNodeImp createClone() {
		return new AlgorithmNodeImp(_nodeName);
	}

	@Override
	public String getNodeName() {
		return _nodeName;
	}

	@Override
	public int getCore() {
		return _core;
	}

	@Override
	public void setCore(int core) {
		_core = core;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_nodeName + _core);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof AlgorithmNodeImp) {
			AlgorithmNodeImp node = (AlgorithmNodeImp) obj;
			if (node.getNodeName().equals(_nodeName) && node.getCore() == _core) {
				return true;
			}
		}
		return false;
	}
}
