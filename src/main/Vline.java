package main;


public class Vline {
	public double x1;
	public double x2;
	public double y1;
	public double y2;
	
	public double high;
	public double low;
	public double right;
	public double left;
	public int id=0;
	public Vline(double x1, double y1, double x2, double y2) {
		this.x1=x1;
		this.y1=y1;
		this.x2=x2;
		this.y2=y2;
		
		if(x1>x2) {
			right=x1;
			left=x2;
		}
		else if(x1<x2) {
			left=x1;
			right=x2;
		}
		if(y1>y2) {
			high=y1;
			low=y2;
		}
		else if(y1<y2) {
			low=y1;
			high=y2;
		}
		high+=5;
		low-=5;
		right+=5;
		left-=5;
	}
	
	public Vline(double[] p1, double[] p2) {
		this.x1=p1[0];
		this.y1=p1[1];
		this.x2=p2[0];
		this.y2=p2[1];
		
		if(x1>x2) {
			right=x1;
			left=x2;
		}
		else if(x1<x2) {
			left=x1;
			right=x2;
		}
		if(y1>y2) {
			high=y1;
			low=y2;
		}
		else if(y1<y2) {
			low=y1;
			high=y2;
		}
	}

}
