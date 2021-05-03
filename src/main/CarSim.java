package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import neat.NEATnet;

public class CarSim {
	public ArrayList<Vline> walls = new ArrayList<Vline>();
	public ArrayList<Vline> goals = new ArrayList<Vline>();
	public double[] carCenter = {-650,130};//starting pos. adjust to track
	public double absoluteACL = 0;//start angle. starts facing 12 o'clock
	public double[] carACL = new double[2]; //use this to figure out angle and velocity
	public double angleACL = 0;//changing angle
	public double carAngle = 0;//angle currently facing
	private double[] pointF = new double[2];
	private double[] pointR = new double[2];
	private double[] pointL = new double[2];
	private double[] pointRR = new double[2];
	private double[] pointLL = new double[2];
	
	public Vline Fline;
	public Vline Rline;
	public Vline Lline;
	public Vline Bline;
	public Vline LLline;
	public Vline RRline;
	
	public Vline[] lines;
	public int eyenumber = 5;
	public double[] distance = {300,300,300,300,300};
	public double[] distP = new double[eyenumber];
	
	public NEATnet nn;
	
	public boolean alive = true;
	
	private int lineLength = 305;
	private double offset = 0.7853981633974481;//45 degrees
	private double radianNum = Math.PI/180;//this equals 1 degree
	private double nogoalcount = 0;
	
	private double pacl=0;
	private double pangle=0;
	
	public int nextgoal = 0;
	public int totalgoals=0;
	
	public CarSim(ArrayList<Vline> w, ArrayList<Vline> g, NEATnet net) {
		walls = w;
		goals=g;
		nn = net;
		updatePoints();
	}
	
	public CarSim() {
		updatePoints();
	}
	//adjusts angle to be between 0 and 360 degrees
	private double adjustangle(double angle) {
		if(angle<0) {
			int mul = (int) (angle/(360*radianNum));
			angle = angle+((mul+1)*360*radianNum);
		}
		else if(angle>(360*radianNum)) {
			int mul = (int) (angle/(360*radianNum));
			angle = angle-(mul*360*radianNum);
		}
		return angle;
	}
	
	public double angleofacl() {
		double hypo = Math.sqrt(carACL[0]*carACL[0]+carACL[1]*carACL[1]);
		double a=Math.abs(carACL[0]);
		double b=Math.abs(carACL[1]);
		double angle = Math.acos((hypo*hypo+a*a-b*b)/(2*hypo*a));
		if(carACL[0]>0) {
			if(carACL[1]<0) {
				angle=angle+2*offset;
			}
			if(carACL[1]>0) {
				angle=2*offset-angle;
			}
		}
		if(carACL[0]<0) {
			if(carACL[1]<0) {
				angle=6*offset-angle;
			}
			if(carACL[1]>0) {
				angle=6*offset+angle;
			}
		}
		Double ra = angle;
		if(ra.isNaN()) {
			return -1;
		}
		return angle;
	}
	
	public double getrelativeangle() {
		double aclangle = angleofacl();
		double relativeangle = aclangle-carAngle;
		if(Math.abs(relativeangle)>4*offset) {
			relativeangle=adjustangle(aclangle+4*offset)-adjustangle(carAngle+4*offset);
		}
		Double ra = relativeangle;
		if(ra.isNaN()||aclangle==-1) {
			return 0;
		}
		return relativeangle;
	}
	
public void updatePoints() {
		
		carAngle+=angleACL;
		angleACL=0;
		
		carCenter[0]+=carACL[0];
		carCenter[1]+=carACL[1];
		
		//normalize angle to range
		
		adjustangle(carAngle);
		
		pointF[0] = (int) (lineLength*Math.sin(carAngle)+carCenter[0]);
		pointF[1] = (int) (lineLength*Math.cos(carAngle)+carCenter[1]);
		pointR[0] = (int) (lineLength*Math.sin(carAngle+offset)+carCenter[0]);
		pointR[1] = (int) (lineLength*Math.cos(carAngle+offset)+carCenter[1]);
		pointL[0] = (int) (lineLength*Math.sin(carAngle-offset)+carCenter[0]);
		pointL[1] = (int) (lineLength*Math.cos(carAngle-offset)+carCenter[1]);
		pointLL[0] = (int) (lineLength*Math.sin(carAngle-offset*2)+carCenter[0]);
		pointLL[1] = (int) (lineLength*Math.cos(carAngle-offset*2)+carCenter[1]);
		pointRR[0] = (int) (lineLength*Math.sin(carAngle+offset*2)+carCenter[0]);
		pointRR[1] = (int) (lineLength*Math.cos(carAngle+offset*2)+carCenter[1]);
		
		Fline = new Vline(carCenter, pointF);
		Rline = new Vline(carCenter, pointR);
		Lline = new Vline(carCenter, pointL);
		RRline = new Vline(carCenter, pointRR);
		LLline = new Vline(carCenter, pointLL);
		
		Vline[] linestemp = {Fline,Rline,Lline,LLline,RRline};
		lines = linestemp;
	}
	
	public void step() {
		for(int i=0;i<eyenumber;i++) {
			distance[i]=300;
		}
		for (Vline w : walls) {
			for(int i=0; i<eyenumber; i++) {
				double temp = getintersectdistance(lines[i], w);
				if(temp<distance[i]) {
					distance[i]=temp;
				}
			}
		}
		for(Vline l : walls) {
			if(colissioncheck(l)) {
				alive=false;
			}
		}
		
		double i1=distance[0]/300;
		double i2=distance[1]/300;
		double i3=distance[2]/300;
		double i4=distance[3]/300;
		double i5=distance[4]/300;
		double i6=absoluteACL/10;
		
		for(int i=0; i<eyenumber; i++) {
			distP[i]=distance[i];
		}
		
		double[] input = {i1,i2,i3,i4,i5,i6};
		
		nn.reset();
		nn.setInput(input);
		nn.calculate();
		double[] output = nn.getOutput();
		absoluteACL += 0.1*output[0];
		if(absoluteACL>10) {
			absoluteACL=10;
		}
		if(absoluteACL<-10) {
			absoluteACL=-10;
		}
		pacl=absoluteACL;
		carACL[0]=(absoluteACL*Math.sin(carAngle));
		carACL[1]=(absoluteACL*Math.cos(carAngle));
		
		angleACL=0.05*output[1];
		pangle=angleACL;
		
		updatePoints();
		goalcheck();
		nogoalcount++;
		//kills driver if it hasn't gotten a goal in this timeframe
		//adjust based on distance between goals in map
		if(nogoalcount>200) {
			alive=false;
		}
	}
	
	//Collision formula. Can't read perfectly straight lines for some reason.
	//Make sure that everything is at least slightly slanted.
	public void goalcheck() {
		Vline check = goals.get(nextgoal);
		if(colissioncheck(check)) {
			nextgoal++;
			totalgoals++;
			nogoalcount=0;
			if(nextgoal==goals.size()) {
				nextgoal=0;
			}
			return;
		}
	}
	
	public double getintersectdistance(Vline l1, Vline l2) {
		double [] point = getintersect(l1,l2);
		if(point[0]==-9999999) {
			return 100;
		}
		double a = Math.abs(point[0]-carCenter[0]);
		double b = Math.abs(point[1]-carCenter[1]);
		double c = Math.sqrt(a*a+b*b);
		return c-5;
	}
	
	double[] getintersect(Vline l1, Vline l2) {
		double x1 = l1.x1;
		double x2 = l1.x2;
		double x3 = l2.x1;
		double x4 = l2.x2;
		double y1 = l1.y1;
		double y2 = l1.y2;
		double y3 = l2.y1;
		double y4 = l2.y2;
		
		double[] p = new double[2];
		if((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4)==0) {
			p[0]= -9999999;
			p[1]= -9999999;
			return p;
		}
		p[0]=((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
		p[1]=((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
		if(!rangeCheck(l1, l2, p[0], p[1])) {
			p[0]= -9999999;
			p[1]= -9999999;
			return p;
		}
		return p;
		
	}
	
	public boolean rangeCheck(Vline l1, Vline l2, double x, double y) {
		double[] data = new double[8];
		double[] pair = new double [2];
		data[0] = l1.x1;
		data[1] = l1.x2;
		data[2] = l1.y1;
		data[3] = l1.y2;
		data[4] = l2.x1;
		data[5] = l2.x2;
		data[6] = l2.y1;
		data[7] = l2.y2;
		
		pair[0]=x;
		pair[1]=y;
		
		for(int i=0; i<8; i+=2) {
			double a = data[i];
			double b = data[i+1];
			double c = pair[(i/2)%2];
			if(a>c&&b>c||a<c&&b<c) {
				return false;
			}
		}
		return true;
	}
	
	public boolean colissioncheck(Vline l) {
		double x = carCenter[0];
		double y = carCenter[1];
		double top = Math.abs((l.y2-l.y1)*x-(l.x2-l.x1)*y+l.x2*l.y1-l.y2*l.x1);
		
		double xp = l.x2-l.x1;
		double yp = l.y2-l.y1;
		
		double bot = Math.sqrt(xp*xp+yp*yp);
		
		double dist = top/bot;
		
		if(l.high<carCenter[1]||l.low>carCenter[1]) {
			return false;
		}
		if(l.right<carCenter[0]||l.left>carCenter[0]) {
			return false;
		}
		
		if(dist<5) {
			return true;
		}
		return false;
	}
	
	public void readfile(String filename) {
		File file=new File(filename);
		FileReader fr;
		try {
			fr = new FileReader(file);
			BufferedReader br=new BufferedReader(fr);
			StringBuffer sb=new StringBuffer();
			String line;
			while((line=br.readLine())!=null) {
				String[] strArray = line.split("\\s");
				double a = Double.parseDouble(strArray[0]);
				double b = Double.parseDouble(strArray[1]);
				double c = Double.parseDouble(strArray[2]);
				double d = Double.parseDouble(strArray[3]);
				int e = Integer.parseInt(strArray[4]);
				String type = strArray[5];
				Vline l = new Vline(a,b,c,d);
				l.id=e;
				if(type.equals("wall")) {
					walls.add(l);
				}
				else if(type.equals("goal")) {
					goals.add(l);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   //reads the file  
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
