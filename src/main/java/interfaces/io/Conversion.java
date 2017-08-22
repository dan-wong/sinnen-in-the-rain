package interfaces.io;

import interfaces.structures.DAG;

public interface Conversion {
	/**
	 * Converts raw input data into DAG object
	 * @return DAG object from input
	 */
	DAG getDAG();
}
