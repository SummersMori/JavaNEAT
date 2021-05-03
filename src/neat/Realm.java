package neat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.jdom2.JDOMException;

import main.CarSim;
import main.Vline;

/**
* Main class for NEAT implementation.
* Contains shared variables, common utility functions, and evaluation functions.
*/

public class Realm {
	
	public static int ncount = -1;
	public static int ccount = -1;
	public static Random rand = new Random();
	
	public static int[] shape;
	
	/**
	 * HashMap for the management of connection genes.
	 * Represented in the format of From - [To, ID].
	 * From = id of the neuron that the connection takes input from
	 * To = id of the neuron that the connection outputs to
	 * id = the id of the new connection
	 */
	public static HashMap<Integer, ArrayList<Pair>> cmap = new HashMap <Integer, ArrayList<Pair>>();
	/**
	 * HashMap for the management of Neuron genes.
	 * Unlike connection genes, which are defined by
	 * the two neurons that they are connecting,
	 * neurons are only defined by the connection that they are replacing.
	 * Represented in the format of Cid-Nid.
	 * Cid = the id of the connection that the new neuron is spawning from
	 * Nid = the id of the new neuron.
	 */
	public static HashMap<Integer, Integer> nmap = new HashMap <Integer, Integer>();
	//HashMap of speciesID-species
	public static HashMap<Integer, Species> species = new HashMap<Integer, Species>();
	public static int speciesNumber = 0;
	
	public static int totalPop = 250;
	private static int ImprovementCheck = 10;
	private static double MutateNeuronRate = 0.01;
	private static double MutateConnectionRate = 0.3;
	private static double WeightShiftRate = 0.8;
	private static double WeightRandomizeRate = 0.1;
	private static double BaseDiff = 5;
	
	private static int gen = 0;
	public static long seed = 0;
	
	private static double bestscore = 0;
	private static int bestspecies = 0;
	
	public static void main(String[] args) throws InterruptedException, JDOMException, IOException {
		seed = rand.nextLong();
		rand.setSeed(seed);
		//sets the input output size of neuralnet
		int[] s = {6,2};
		shape = s;
		//generates initial species
		ArrayList<NEATnet> nnlist = new ArrayList<NEATnet>(totalPop);
		for(int i=0; i<totalPop; i++) {
			NEATnet nn = Pools.getnet(shape);
			nnlist.add(nn);
		}
		Species first = new Species(0, nnlist);
		species.put(0, first);
		
		gen = 0;
		while (gen<500) {
			System.out.println(gen);
			CarTest();
			//xorTotalTest();
			scan();
			gen++;
			System.out.println(bestscore);
			System.out.println(bestspecies);
			System.out.println("Connection "+Pools.connectiongets+" "+Pools.connectionfrees);
			System.out.println("Neuron "+Pools.neurongets+" "+Pools.neuronfrees);
			System.out.println("Net "+Pools.netgets+" "+Pools.netfrees);
		}
	}
	
	/**
	 * ArrayList copy function for making deep clones
	 * @param list The ArrayList to be copied
	 * @return The copied list
	 */
	public static ArrayList<NEATnet> copyList(ArrayList<NEATnet> list){
		ArrayList<NEATnet> rlist = new ArrayList<NEATnet>(list.size());
		for(int i=0; i<list.size(); i++) {
			NEATnet newnet = list.get(i).deepClone();
			rlist.add(newnet);
		}
		return rlist;
	}
	
	//Evaluation function for car driving
	public static void CarTest() {
		CarSim sim = new CarSim();
		sim.readfile("Track.txt");
		for(Species s:species.values()) {
			for(NEATnet nn:s.members) {
				nn.iselite=false;
				//using constructor as reset instead of programming an actual reset function
				sim = new CarSim(sim.walls,sim.goals, nn);
				int count = 0;
				//runs simulation for 10000 steps, or until death
				while(count<10000) {
					count++;
					sim.step();
					if(!sim.alive) {
						break;
					}
				}
				//evaluation function. Each goal is worth exponentially more, and taking too long reduces points
				double exp = Math.pow(1.01, sim.totalgoals);
				int score = (int) ((sim.totalgoals*exp)-(count/500));
				if(score<1) {
					score=1;
				}
				//breakpoint. after it meets this condition, the neuralnet is saved and the program ends
				//adjust this based on distance between goals in track.
				//with reasonable goals, can train a network to perfect a track in around a minute
				if(sim.totalgoals>=600) {
					System.out.println(gen);
					printNEATnet(nn);
					File f = new File("DrivingNet.txt");
					try {
						FileOutputStream fos = new FileOutputStream(f);
						nn.saveNet(fos);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.exit(1);
				}
				nn.score = score;
			}
		}
	}
	//wrapper for xor test. runs the text on every member of all species.
	public static void xorTotalTest() {
		for(Species s:species.values()) {
			for(NEATnet nn:s.members) {
				xorTest(nn);
			}
		}
	}
	/**
	 * xorTest. Takes a neuralnet and checks it against a number of random xor inputs.
	 * @param nn The neuralnet to be tested.
	 */
	public static void xorTest(NEATnet nn) {
		int max = 100;
		nn.iselite=false;
		nn.score=0;
		for(int i=0; i<max; i++) {
			//generatest 2 random numbers a computes the xor
			int a = rand.nextInt(2);
			int b = rand.nextInt(2);
			int xor = 0;
			if(a!=b) {
				xor = 1;
			}
			
			//sets the 2 random numbers as input to neuralnet
			double[] input = {a, b};
			nn.reset();
			nn.setInput(input);
			nn.calculate();
			
			//grabs output and checks if answer is right. if it is, +1 point
			double output = nn.output.get(0).getOutput();
			int answer = (int)(output+0.5);
			if(answer == xor) {
				nn.score++;
			}
			//reset neuralnet at the end.
			nn.reset();
		}
		
		//breakpoint. after it meets this score, is tested against another 100 inputs, and the result is printed
		if(nn.score>max-2) {
			System.out.println(nn.score+"/"+max);
			nn.score=0;
			System.out.println("Sucess");
			for(int i=0; i<100; i++) {
				System.out.println("Trial "+i);
				int a = rand.nextInt(2);
				int b = rand.nextInt(2);
				System.out.println("a:"+a);
				System.out.println("b:"+b);
				int xor = 0;
				if(a!=b) {
					xor = 1;
				}
				System.out.println("Expected output:"+xor);
				double[] input = {a, b};
				nn.setInput(input);
				nn.calculate();
				double output = nn.output.get(0).getOutput();
				int answer = (int)(output+0.5);
				System.out.println("Actual output:"+answer);
				if(answer == xor) {
					nn.score++;
					System.out.println("Right");
				}
				else {
					System.out.println("Wrong");
				}
				nn.reset();
			}
			System.out.println(nn.score+"/"+100);
			System.out.println(gen);
			printNEATnet(nn);
			File f = new File("SaveNet.txt");
			try {
				FileOutputStream fos = new FileOutputStream(f);
				nn.saveNet(fos);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(1);
		}
	}
	/**
	 * Utility function to print out the form of the neuralnet
	 * Format is [NeuronID]: All the neurons its connected to
	 * @param nn the neuralnet to be printed
	 */
	public static void printNEATnet(NEATnet nn) {
		for(Neuron n : nn.neuronList.values()) {
			System.out.println("Neuron:"+n.id);
			System.out.print("Goes to:");
			for(Connection c : n.cout) {
				if(c.enabled) {
					System.out.print(" "+c.to.id+",");
				}
			}
			System.out.println("");
		}
	}
	
	
	//The scan function culls, breeds, and mutates all species
	public static void scan() {
		//grab elite from each species
		for(Species s : species.values()) {
			if(s.members.isEmpty()) {
				continue;
			}
			s.processElite();
			if(s.currentTop>bestscore) {
				bestspecies = s.id;
				bestscore=s.currentTop;
			}
		}
		//improvement check. if no improvement for 15 cycles, delete species
		Species[] slist = species.values().toArray(new Species[species.size()]);
		for(int i=0; i<slist.length; i++) {
			if(slist[i].noImprovement>ImprovementCheck&&slist[i].id!=bestspecies) {
				System.out.println("Species removed:"+slist[i].id);
				for(NEATnet nn : slist[i].members) {
					Pools.free(nn);
				}
				slist[i].members.clear();
				for(NEATnet nn : slist[i].elite) {
					Pools.free(nn);
				}
				slist[i].elite.clear();
				for(NEATnet nn : slist[i].decendents) {
					Pools.free(nn);
				}
				slist[i].decendents.clear();
				species.remove(slist[i].id);
			}
			if(slist[i].members.isEmpty()) {
				species.remove(slist[i].id);
			}
		}
		//calculate total average score
		double totalScore = 0;
		for(Species s : species.values()) {
			totalScore += s.averageScore;
		}
		//assign population numbers based on relative average score
		int currentPop = 0;
		for(Species s : species.values()) {
			s.desiredPop=(int) ((totalPop*(s.averageScore/totalScore))+0.5);
			currentPop += s.desiredPop;
		}
		int leftover = totalPop-currentPop;
		slist = species.values().toArray(new Species[species.size()]);
		for (int i=0; i<leftover; i++) {
			slist[i].desiredPop++;
		}
		//breed and refresh species.
		for(Species s : species.values().toArray(new Species[species.size()])) {
			s.breedElite();
		}
		for(Species s : species.values()) {
			s.Inherit();
		}
	}
	
	/**
	 * Function to get the id of a connection.
	 * If connection already exists in array, just returns number from cmap
	 * If connection does not exist, increments total connection counter and adds new element to cmap. returns new number
	 * @param from Neuron that connections takes input from
	 * @param to Neuron that connection gives output to
	 * @return the id of the connection
	 */
	public static int getCID (int from, int to) {
		//first checks From, then To. If both are found, then return the id
		if(cmap.containsKey(from)) {
			for(Pair p : cmap.get(from)) {
				if(p.to==to) {
					return p.id;
				}
			}
			//if only From found, then adds new [To, id] pair to From key and returns new id
			ccount++;
			Pair p = new Pair(to, ccount);
			cmap.get(from).add(p);
			return ccount;
		}
		//if From does now match, create new from and add [To, id] pair. return new id.
		else {
			ccount++;
			Pair p = new Pair(to, ccount);
			ArrayList<Pair> ap = new ArrayList<Pair>();
			ap.add(p);
			cmap.put(from, ap);
			return ccount;
		}
	}
	
	
	/**
	 * Function to get NID. Works same as getCID, but it
	 * only depends on the connection that the neuron is spawning from
	 * @param cid The id of the connection that the neuron spawns from
	 * @return 
	 */
	public static int getNID(int cid) {
		if(nmap.containsKey(cid)) {
			return nmap.get(cid);
		}
		else {
			ncount++;
			nmap.put(cid, ncount);
			return ncount;
		}
	}
	/**
	 * Returns -2 to 2. Used for connection weights
	 * @return a double from -2 to 2
	 */
	public static double getRand() {
		double r1 = rand.nextDouble();
		double r2 = rand.nextDouble();
		double r = r1+r2;
		r -= 1;
		return r*2;
	}
	
	/**
	 * Mating function. Takes two neuralnets and breeds them according to NEAT
	 * The two nets are compared for fitness, and the more fit one has their disjoint genes inherited
	 * If both are of equal fitness(determined by the compareFitness function), both disjoint genes are inherited
	 * @param nn1 First net
	 * @param nn2 Second net
	 * @return The new child net
	 */
	public static NEATnet mate(NEATnet nn1, NEATnet nn2) {
		int a = compareFitness(nn1.score, nn2.score);
		NEATnet mainNet=nn1;
		NEATnet otherNet=nn2;
		boolean equal = false;
		//mainNet is set as net with highest fitness
		switch (a){
		
		case 1:
			mainNet = nn1;
			otherNet = nn2;
			break;
		
		case 2:
			mainNet = nn2;
			otherNet = nn1;
			break;
			
		case 3:
			mainNet = nn1;
			otherNet = nn2;
			equal = true;
			break;
			
		default:
			System.err.println("compare fitness error. this should not happen");
			System.exit(1);
		
		}
		//generates deep clone of mainNet to adjust
		NEATnet returnNet = mainNet.deepClone();
		//If both nets are equal, adds in disjoint genes from otherNet
		if(equal) {
			//for the id of every connection in otherNet
			for(int k : otherNet.connectionList.keySet()) {
				//if returnNet does not have the connection, and the connection is enabled
				if(!returnNet.connectionList.containsKey(k)&&otherNet.connectionList.get(k).enabled) {
					//grab the connection
					Connection c = otherNet.connectionList.get(k);
					//if returnNet does not have the neurons of the connection, adds the neurons
					if(!returnNet.neuronList.containsKey(c.to.id)) {
						Neuron n = Pools.getneuron(otherNet.neuronList.get(c.to.id));
						returnNet.neuronList.put(c.to.id, n);
					}
					if(!returnNet.neuronList.containsKey(c.from.id)) {
						Neuron n = Pools.getneuron(otherNet.neuronList.get(c.from.id));
						returnNet.neuronList.put(c.from.id, n);
					}
					/**
					 * adds the connections, which checking and rejecting any connections that form cycles.
					 * this means that some neurons are left hanging without doing anything, and some innovation is lost
					 * but this unavoidable without going into recursive memory
					 */
					if(returnNet.cycleCheck(c.from.id, c.to.id)) {
						returnNet.MutateConnection(returnNet.neuronList.get(c.from.id), returnNet.neuronList.get(c.to.id));
						returnNet.connectionList.get(c.id).weight=c.weight;
					}
				}
			}
		}
		//for every connection, randomly assigns a weight from either mainNet or otherNet
		//chances are relative to scores
		for(Connection c : returnNet.connectionList.values()) {
			if(otherNet.connectionList.containsKey(c.id)) {
				double chance = otherNet.score/(mainNet.score+otherNet.score);
				double roll = rand.nextDouble();
				if(chance>roll) {
					c.weight = otherNet.connectionList.get(c.id).weight;
				}
			}
		}
		return returnNet;
		
	}
	/**
	 * Mutation function.
	 * Rolls random numbers. If number is less then the stated rate, mutates.
	 * @param target The neuralnet to be mutated.
	 */
	public static void Mutate(NEATnet target) {
		//mutate weights
		double weightChange = getRand()/4;
		double roll = rand.nextDouble();
		if(roll<WeightShiftRate) {
			for(Connection c: target.connectionList.values()) {
				weightChange = getRand()/4;
				if(Math.abs(c.weight+weightChange)<3) {
					c.weight+=weightChange;
				}
				roll = rand.nextDouble();
				if(roll<WeightRandomizeRate) {
					c.weight=getRand();
				}
			}
		}
		//mutate nodes
		roll = rand.nextDouble();
		if(roll<MutateNeuronRate) {
			int pick = rand.nextInt(target.connectionList.size());
			target.MutateNeuron(target.connectionList.values().toArray(new Connection[target.connectionList.size()])[pick]);
		}
		
		//mutate link.
		int run = 6;
		roll = rand.nextDouble();
		if(roll<MutateConnectionRate) {
			run=0;
		}
		/**
		 * Check if mutated link makes a cycle. If it dosn't, carry out the mutation.
		 * If it does, generated a new link to mutate, up to 5 times.
		 * The first neuron picked cannot be an output neuron. Output neurons don't give to other neurons
		 * After 5 attempts, gives up on the mutation
		 */
		while (run<5){
			int pick = rand.nextInt(target.neuronList.size());
			Neuron n1 = target.neuronList.values().toArray(new Neuron[target.neuronList.size()])[pick];
			int pick2 = rand.nextInt(target.neuronList.size());
			Neuron n2 = target.neuronList.values().toArray(new Neuron[target.neuronList.size()])[pick2];
			if(target.cycleCheck(n1.id, n2.id)&&!target.output.contains(n1)&&n2.id!=0) {
				target.MutateConnection(n1, n2);
				run=6;
			}
			run++;
		}
	}
	
	/**
	 * Comparison function. Takes in two numbers and calculates their difference rate.
	 * @param a First number
	 * @param b Second number
	 * @return 3 if difference rate is less then 0.1, 1 if a is bigger, 2 if b is bigger
	 */
	public static int compareFitness(double a, double b) {
		double diffrence = Math.abs(a-b)/((a+b)/2);
		if(diffrence<0.1) {
			return 3;
		}
		else if(a>b) {
			return 1;
		}
		else {
			return 2;
		}
	}

}
