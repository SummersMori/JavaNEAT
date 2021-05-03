package neat;

import java.io.IOException;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.JDOMException;

import neat.Neuron.Type;

public class Pools {
	public final static int size = Realm.totalPop;//initial and max pool size
	public static ArrayList<NEATnet> netpool = new ArrayList<NEATnet>(size);
	public static ArrayList<Neuron> neuronpool = new ArrayList<Neuron>(size*5);
	public static ArrayList<Connection> connectionpool = new ArrayList<Connection>(size*15);
	public static int netgets = 0;
	public static int neurongets = 0;
	public static int connectiongets = 0;
	public static int netfrees = 0;
	public static int neuronfrees = 0;
	public static int connectionfrees = 0;
	
	public static NEATnet getnet() {
		netgets++;
		if(netpool.size()>0) {
			NEATnet nn = netpool.get(netpool.size()-1);
			netpool.remove(netpool.size()-1);
			return nn;
		}
		else {
			return new NEATnet();
		}
	}
	
	public static NEATnet getnet(int[] shape) {
		netgets++;
		if(netpool.size()>0) {
			NEATnet nn = netpool.get(netpool.size()-1);
			nn.fromshape(shape);
			return nn;
		}
		else {
			NEATnet nn = new NEATnet();
			nn.fromshape(shape);
			return nn;
		}
		
	}
	public static NEATnet getnet(Document document) {
		netgets++;
		if(netpool.size()>0) {
			NEATnet nn = netpool.get(netpool.size()-1);
			nn.fromdoc(document);
			return nn;
		}
		else {
			NEATnet nn = new NEATnet();
			nn.fromdoc(document);
			return nn;
		}
	}
	public static NEATnet getnet(String s) throws JDOMException, IOException {
		netgets++;
		if(netpool.size()>0) {
			NEATnet nn = netpool.get(netpool.size()-1);
			nn.fromstring(s);
			return nn;
		}
		else {
			NEATnet nn = new NEATnet();
			nn.fromstring(s);
			return nn;
		}
	}
	
	public static Neuron getneuron(int i, Type t) {
		neurongets++;
		if(neuronpool.size()>0) {
			Neuron n = neuronpool.get(neuronpool.size()-1);
			neuronpool.remove(neuronpool.size()-1);
			n.id=i;
			n.type=t;
			return n;
		}
		else {
			return new Neuron(i,t);
		}
	}
	
	public static Neuron getneuron(Neuron tocopy) {
		neurongets++;
		if(neuronpool.size()>0) {
			Neuron n = neuronpool.get(neuronpool.size()-1);
			neuronpool.remove(neuronpool.size()-1);
			n.id=tocopy.id;
			n.type=tocopy.type;
			return n;
		}
		else {
			return new Neuron(tocopy);
		}
	}
	
	public static Connection getconnection(Neuron f, Neuron t, double w, boolean e, int i) {
		connectiongets++;
		if(connectionpool.size()>0) {
			Connection c = connectionpool.get(connectionpool.size()-1);
			connectionpool.remove(connectionpool.size()-1);
			c.from = f;
			c.to = t;
			c.weight = w;
			c.enabled = e;
			c.id = i;
			c.from.cout.add(c);
			c.to.cin.add(c);
			return c;
		}
		else {
			return new Connection(f,t,w,e,i);
		}
	}
	
	public static Connection getconnection() {
		connectiongets++;
		if(connectionpool.size()>0) {
			Connection c = connectionpool.get(connectionpool.size()-1);
			connectionpool.remove(connectionpool.size()-1);
			return c;
		}
		else {
			return new Connection();
		}
	}
	
	public static void free(Connection c) {
		connectionfrees++;
		if(connectionpool.size()<size*15) {
			connectionpool.add(c);
		}
		c.clear();
	}
	public static void free(Neuron n) {
		neuronfrees++;
		if(neuronpool.size()<size*5) {
			neuronpool.add(n);
		}
		n.clear();
	}
	public static void free(NEATnet nn) {
		netfrees++;
		for(Connection c : nn.connectionList.values()) {
			free(c);
		}
		nn.connectionList.clear();
		for(Neuron n : nn.neuronList.values()) {
			free(n);
		}
		if(netpool.size()<size) {
			netpool.add(nn);
		}
		nn.neuronList.clear();
		nn.input.clear();
		nn.output.clear();
		nn.score=0;
		nn.maxScore=0;
		nn.largestc=0;
		nn.largestn=0;
		nn.check=true;
		nn.iselite=false;
		
	}
	
}
