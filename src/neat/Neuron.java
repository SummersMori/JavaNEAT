package neat;

import java.util.ArrayList;

/**
* Main class for Neuron object.
* Contains input/output values, list of in/out connections for the neuron, and functions for operation of neuron.
*/

public class Neuron {
	public int id;
	public ArrayList<Connection> cout = new ArrayList<Connection>();//list of out connections
	public ArrayList<Connection> cin = new ArrayList<Connection>();//list of in connections
	public double out = 0;
	public double in = 0;
	
	public int activationCount = 0;
	
	Type type;
	
	public enum Type{
		INPUT,
		OUTPUT,
		HIDDEN,
		BIAS
	}
	/**
	 * constructor. Takes id and type
	 * @param i id of neuron
	 * @param t type of neuron
	 */
	public Neuron(int i, Type t) {
		id = i;
		this.type = t;
		if(t.equals(Neuron.Type.BIAS)) {
			this.out=1;
		}
	}
	
	
	/**
	 * copy constructor. copies id and type from given neuron
	 * @param n neuron to copy
	 */
	public Neuron(Neuron n) {
		id = n.id;
		this.type = n.type;
	}
	
	public void clear() {
		id=-1;
		cout.clear();
		cin.clear();
		out=0;
		in=0;
	}
	
	/**
	 * transfer function. currently using modified sigmoid function, scaled to the range of -1 to 1
	 * if output is already set, just return that
	 * @return processed double as output
	 */
	public double getOutput() {
		if(type.equals(Neuron.Type.BIAS)) {
			return 1;
		}
		if(out!=0) {
			return out;
		}
		double output = 1/(1+ Math.exp(-in*4));
		output = (output-0.5)*2;
		out = output;
		in = 0;
		return out;
	}
	//set output directly, without going through the transfer function
	public void setOutput(double o) {
		out = o;
	}

}
