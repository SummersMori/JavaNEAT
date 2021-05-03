package neat;

public class Connection {
	public Neuron from;
	public Neuron to;
	public double weight;
	public boolean enabled;
	public int id;

	public Connection (Neuron f, Neuron t, double w, boolean e, int i) {
		from = f;
		to = t;
		weight = w;
		enabled = e;
		id = i;
		from.cout.add(this);
		to.cin.add(this);
	}
	
	public Connection () {
		
	}
	
	public void clear() {
		from = null;
		to = null;
		weight = 0;
		enabled = false;
		id = -1;
	}
	
}
