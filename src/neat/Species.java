package neat;

import java.util.ArrayList;

public class Species {
	//paramaters. change these depending on tests
	private double weightC = 2;
	private double linkC = 1;
	private double normalizer = 1;
	public double differenceLimit = 12;
	
	//keeps track of average scores for improvement check
	private double previousTop = 0;
	public double currentTop = 0;
	//Counter for number of generations without improvement. Updated at processelite
	public int noImprovement = 0;
	
	public ArrayList<NEATnet> members = new ArrayList<NEATnet>();
	public ArrayList<NEATnet> elite = new ArrayList<NEATnet>();
	public ArrayList<NEATnet> decendents = new ArrayList<NEATnet>();
	public int id = 0;
	public double averageScore = 0;
	public int desiredPop = 0;
	
	public Species(int i, ArrayList<NEATnet>m) {
		id=i;
		members=m;
	}
	
	public void breedElite() {
		int assignedSlots = 0;
		if(members.size()>=1) {
			decendents.add(elite.get(0));
			assignedSlots++;
		}
		//Randomly picks 2 elite from species.
		//if cross species breeding is rolled, pick second elite from entire population.
		for(; assignedSlots<desiredPop; assignedSlots++) {
			NEATnet nn1 = members.get(Realm.rand.nextInt(members.size()));
			int roll =Realm.rand.nextInt(100);
			NEATnet nn2 = null;
			if(roll==1) {
				Species s = null;
				if(Realm.rand.nextDouble()<0.001) {
					Species[] slist = Realm.species.values().toArray(new Species[Realm.species.size()]);
					s = slist[Realm.rand.nextInt(slist.length)];
				}
				else {
					s=this;
				}
				nn2 = s.elite.get(Realm.rand.nextInt(s.elite.size()));
			}
			else {
				nn2 = members.get(Realm.rand.nextInt(members.size()));
			}
			//mate nn1 and nn2, put the result into decendents of some species
			NEATnet nn3 = Realm.mate(nn1, nn2);
			Realm.Mutate(nn3);
			boolean check = false;
			check = addDecendent(nn3);
			if(!check) {
				for(Species s : Realm.species.values()) {
					check = s.addDecendent(nn3);
					if(check) {
						break;
					}
				}
			}
			//if no success, completely new species
			if (!check) {
				ArrayList<NEATnet> empty = new ArrayList<NEATnet>();
				empty.add(nn3);
				Realm.speciesNumber++;
				Species newSpecies = new Species (Realm.speciesNumber, empty);
				newSpecies.decendents.add(nn3);
				newSpecies.members.add(nn3);
				newSpecies.elite.add(nn3);
				Realm.species.put(newSpecies.id, newSpecies);
				System.out.println("New species:"+newSpecies.id);
			}
			
		}
	}
	
	//Resets things in preperation for next generation.
	public void Inherit() {
		if(currentTop>previousTop) {
			previousTop=currentTop;
		}
		currentTop = 0;
		averageScore = 0;
		desiredPop = 0;
		members.clear();
		for(NEATnet nn : decendents) {
			members.add(nn);
			nn.iselite=false;
		}
		for(NEATnet nn : elite) {
			if(nn.iselite) {
				Pools.free(nn);
			}
		}
		elite.clear();
		decendents.clear();
		for(NEATnet nn : members) {
			nn.check=true;
			nn.score=0;
		}
	}
	
	/**
	 * function to add network as decendent of species
	 * checks network against top network of species
	 * @param notMember network to add
	 * @return false if adding failed, true if adding was successful
	 */
	public boolean addDecendent(NEATnet notMember) {
		boolean check = commenCheck(elite.get(0), notMember);
		if(check) {
			decendents.add(notMember);
			notMember.speciesID=id;
			return true;
		}
		return false;
	}
	
	
	//Sorts species and calculates average score.
	public void processElite() {
		int eliteNum = members.size()/4;
		if(eliteNum<1) {
			eliteNum = 1;
		}
		for(NEATnet nn : members) {
			nn.check = true;
		}
		for(int i=0; i<eliteNum; i++) {
			double largest = -100;
			NEATnet largestnn = null;
			for(NEATnet nn : members) {
				if((nn.score>largest)&&nn.check) {
					largestnn = nn;
					largest = nn.score;
				}
			}
			largestnn.iselite=true;
			elite.add(largestnn);
			largestnn.check=false;
		}
		for(NEATnet nn : members) {
			averageScore+=nn.score;
		}
		averageScore=averageScore/members.size();
		
		currentTop = elite.get(0).score;
		System.out.println("Top Score:"+currentTop+" Average Score:"+averageScore+" Size:"+members.size()+" Id:"+id+" Improvement count:"+noImprovement);
		if(currentTop<=previousTop) {
			noImprovement++;
		}
		else {
			noImprovement=0;
		}
	}
	/**
	 * Function to check if member is part of a species
	 * @param net1 network of the species that is testing
	 * @param test network that is being tested
	 * @return true if test is part of same species as net1, false if it is not
	 */
	public boolean commenCheck(NEATnet net1, NEATnet test) {
		double totalDiffrence = 0;
		double weightDiffrence = 0;
		int numWeights = 0;
		int connectionDiffrence = 0;
		for(Connection c : test.connectionList.values()) {
			if(net1.connectionList.containsKey(c.id)) {
				weightDiffrence += absDiff(c.weight, net1.connectionList.get(c.id).weight);
				numWeights++;
			}
			else {
				connectionDiffrence++;
			}
		}
		for(Connection c : net1.connectionList.values()) {
			if(!test.connectionList.containsKey(c.id)) {
				connectionDiffrence++;
			}
		}
		double weightValue = (weightDiffrence/numWeights)*weightC;
		totalDiffrence = weightValue+(connectionDiffrence*linkC);
		if(totalDiffrence>differenceLimit) {
			return false;
		}
		return true;
	}
	
	//function to calculate percentage diffrence
	public double absDiff(double d1, double d2) {
		double a = Math.abs(d1);
		double b = Math.abs(d2);
		double ab = a-b;
		return Math.abs(ab);
	}

}
