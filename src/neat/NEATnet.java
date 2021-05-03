package neat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
/**
 * The main class for the neuralnet
 * Contains cyclecheck function, mutation functions, save/load functions, and the calculation function
 *
 */
public class NEATnet {
	
	public int speciesID = 0;//not part of constructor. Should be set independently.
	//Stores connections and neurons in a hashmap based on their id
	public HashMap<Integer, Connection> connectionList = new HashMap<Integer, Connection>();//id, connection
	public HashMap<Integer, Neuron> neuronList = new HashMap<Integer, Neuron>();//id, neuron
	
	//saves input and output neurons in array
	public ArrayList<Neuron> input = new ArrayList<Neuron>();
	public ArrayList<Neuron> output = new ArrayList<Neuron>();
	
	
	public double score = 0;
	public double maxScore = 0;
	public boolean check = true;//initialized as true.
	public boolean iselite = false;
	
	//Stores largest neuron and largest connection id
	public int largestn = 0;
	public int largestc = 0;
	
	/**
	 * Constructor that creates a NEATnet based on the shape of a given array
	 * should only be used to generate initial population
	 * @param shape Array of ints, size 2. First number is #input neurons, second is #output neurons
	 */
	public void fromshape (int[] shape) {
		//if length is less than 2, it will crash. if more then two, it ignores the rest
		if(shape.length<2) {
			System.err.print("Needs at least 2 layers");
			System.exit(0);
		}
		//generate input neurons first
		int total = 0;
		for(int i=0; i<shape[0]+1; i++) {
			Neuron n = Pools.getneuron(total, Neuron.Type.INPUT);
			neuronList.put(total, n);
			input.add(n);
			total++;//count for number of neurons
		}
		//generate output neurons
		for(int i=0; i<shape[1]; i++) {
			Neuron n = Pools.getneuron(total, Neuron.Type.OUTPUT);
			neuronList.put(total, n);
			output.add(n);
			total++;//count for number of neurons
		}
		//first set of neurons are manually generated, so the count used for ids must be directly set
		if(Realm.ncount<total-1) {
			Realm.ncount = total-1;
		}
		//connects all input neurons to output neurons
		for(Neuron a : input) {
			for(Neuron b : output) {
				MutateConnection(a, b);
			}
		}
		neuronList.get(0).in=1;//set bias node input as 1
		largestn = total-1;
	}
	/**
	 * constructor for loading from file. uses JDOM library
	 * @param filename path of file to load
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void fromstring(String filename) throws JDOMException, IOException {
		this.fromdoc(new SAXBuilder().build(filename));
	}
	public void fromdoc(Document document) {
		Element root = document.getRootElement();
		for(Element e :root.getChildren("Neuron")) {
			Neuron n = null;
			if(e.getAttributeValue("type").equals("BIAS")) {
				n = Pools.getneuron(Integer.parseInt(e.getAttributeValue("id")), Neuron.Type.BIAS);
				input.add(n);
				n.in=1;
			}
			else if(e.getAttributeValue("type").equals("INPUT")) {
				n = Pools.getneuron(Integer.parseInt(e.getAttributeValue("id")), Neuron.Type.INPUT);
				input.add(n);
			}
			else if(e.getAttributeValue("type").equals("OUTPUT")) {
				n = Pools.getneuron(Integer.parseInt(e.getAttributeValue("id")), Neuron.Type.OUTPUT);
				output.add(n);
			}
			else if(e.getAttributeValue("type").equals("HIDDEN")) {
				n = Pools.getneuron(Integer.parseInt(e.getAttributeValue("id")), Neuron.Type.HIDDEN);
			}
			neuronList.put(n.id, n);
		}
		for(Element e : root.getChildren("Connection")) {
			Connection c = Pools.getconnection();
			c.id = Integer.parseInt(e.getAttributeValue("id"));
			c.from = neuronList.get(Integer.parseInt(e.getChildText("from")));
			c.to = neuronList.get(Integer.parseInt(e.getChildText("to")));
			c.weight = Double.parseDouble(e.getChildText("weight"));
			c.enabled = Boolean.parseBoolean(e.getChildText("enabled"));
			connectionList.put(c.id, c);
			c.from.cout.add(c);
			c.to.cin.add(c);
		}
		speciesID = Integer.parseInt(root.getAttributeValue("speciesID"));
		largestn = Integer.parseInt(root.getAttributeValue("largestn"));
		largestc = Integer.parseInt(root.getAttributeValue("largestc"));
		
	}
	
	public NEATnet() {
		
	}
	
	/**
	 * Easy deep clone command. Constructs a new network through the document constructor.
	 * @return A clone of this network.
	 */
	public NEATnet deepClone() {
		NEATnet rnet = new NEATnet();
		rnet.fromdoc(this.getDoc());
		return rnet;
	}
	
	/**
	 * Check if new connection would form a cycle
	 * If not, gets the ID of connection with getCID()
	 * Makes new connection and adds it to the connectionList
	 * @param n1 Input neuron
	 * @param n2 Output neuron
	 */
	public void MutateConnection(Neuron n1, Neuron n2) {
		if(!this.cycleCheck(n1.id, n2.id)) {
			return;
		}
		int cid = Realm.getCID(n1.id, n2.id);
		if(connectionList.containsKey(cid)) {
			return;
		}
		Connection c = Pools.getconnection(n1, n2, Realm.getRand(), true, cid);
		connectionList.put(cid, c);
		if(cid>largestc) {
			largestc = cid;
		}
	}
	/**
	 * Makes a neuron to replace the given connection
	 * The given connection is disabled, and a neuron and 2 connections are generated to replace it.
	 * @param c Connection to replace
	 */
	public void MutateNeuron(Connection c) {
		int nid = Realm.getNID(c.id);
		c.enabled = false;
		Neuron newn = Pools.getneuron(nid, Neuron.Type.HIDDEN);
		neuronList.put(nid, newn);
		MutateConnection(c.from, newn);
		MutateConnection(newn, c.to);
		if(nid>largestn) {
			largestn = nid;
		}
	}
	
	/**
	 * Sets value of input neurons according to given array
	 * @param dlist Double array of same size and number of input neurons
	 */
	public void setInput(double [] dlist){
		if(dlist.length != input.size()-1) {//-1 size for bias node
			System.err.println("Input has wrong length");
			System.exit(0);
		}
		for(int i = 1; i<input.size(); i++) {
			input.get(i).setOutput(dlist[i-1]);
		}
	}
	/**
	 * Returns value of output as an array. Must call calculate function beforehand
	 * @return Double array of outputs
	 */
	public double[] getOutput() {
		double[] r = new double[output.size()];
		for(int i=0; i<output.size(); i++) {
			r[i]=output.get(i).getOutput();
		}
		return r;
	}
	
	/**
	 * Traverses the network as a directed graph, activating each neuron, until there are no more neurons left
	 * Output neurons cannot be activated, and are only activated at the end, by getOutput()
	 * Has a limit on number of steps, to shut down absurdly large networks.
	 * This loop limit also handles rare loops that form.
	 */
	public void calculate() {
		ArrayList<Neuron> activeNeurons = new ArrayList<Neuron>();
		ArrayList<Neuron> next = new ArrayList<Neuron>();
		for(Neuron n : input) {
			activeNeurons.add(n);
		}
		int loopcount = 0;
		while(!activeNeurons.isEmpty()) {
			loopcount++;
			for(Neuron n : activeNeurons) {
				double out = n.getOutput();
				n.out=0;
				n.in=0;
				for(Connection c : n.cout) {
					if(c.enabled) {
						c.to.in+=out*c.weight;
						if(!output.contains(c.to)&&!next.contains(c.to)) {//outputs have nothing to fire to
							next.add(c.to);
						}
					}
				}
			}
			activeNeurons=next;
			next = new ArrayList<Neuron>();
			if(loopcount>300) {
				break;
			}
		}
	}
	/**
	 * Function that checks when a cycle forms. Call this when making new connections.
	 * Starting from the Out neuron traverses network graph until end. If at any point it 
	 * find the In neuron, cycle is confirmed, and returns true
	 * @param in Input neuron of connection
	 * @param out Output neuron of connection
	 * @return true if there is cycle, false if no cycle
	 */
	public boolean cycleCheck(int in, int out) {
		if(in==out) {
			return false;
		}
		ArrayList<Neuron> current = new ArrayList<Neuron>();
		ArrayList<Neuron> next = new ArrayList<Neuron>();
		current.add(neuronList.get(out));
		int run = 0;
		while(!current.isEmpty()) {
			for(Neuron n : current) {
				for(Connection c : n.cout) {
					if(c.enabled&&!next.contains(c.to)&&!output.contains(c.to)) {
						next.add(c.to);
					}
				}
			}
			for(Neuron n : next) {
				if(n.id==in) {
					return false;
				}
			}
			current = next;
			next = new ArrayList<Neuron>();
			run++;
			if(run==200) {
				return false;
			}
		}
		return true;
	}
	
	public void reset() {
		for(Neuron n : neuronList.values()) {
			n.in=0;
			n.out=0;
		}
	}
	
	//Randomizes all weights
	public void randomizeWeights() {
		for(Connection c : connectionList.values()) {
			c.weight = Realm.getRand();
		}
	}
	//Saves the network as a file
	public void saveNet(FileOutputStream fos) throws IOException {
		Document doc = getDoc();
		XMLOutputter writer = new XMLOutputter(Format.getPrettyFormat());
		writer.output(doc, fos);
	}
	
	/**
	 * Converts the network into a JDOM document.
	 * @return a JDOM document of the network
	 */
	public Document getDoc() {
		Document doc = new Document();
		Element neat = new Element("NEATnet");
		neat.setAttribute("speciesID",""+speciesID);
		neat.setAttribute("largestn",""+largestn);
		neat.setAttribute("largestc",""+largestc);
		doc.setRootElement(neat);
		
		for(Neuron n : neuronList.values()) {
			Element neuron = new Element("Neuron");
			neuron.setAttribute("id", ""+n.id);
			neuron.setAttribute("type", ""+n.type);
			doc.getRootElement().addContent(neuron);
		}
		for(Connection c : connectionList.values()) {
			Element connection = new Element("Connection");
			connection.setAttribute("id", ""+c.id);
			connection.addContent(new Element("from").setText(""+c.from.id));
			connection.addContent(new Element("to").setText(""+c.to.id));
			connection.addContent(new Element("weight").setText(""+c.weight));
			connection.addContent(new Element("enabled").setText(""+c.enabled));
			doc.getRootElement().addContent(connection);
		}
		return doc;
	}

}
