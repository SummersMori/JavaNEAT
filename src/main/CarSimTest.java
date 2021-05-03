package main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CarTest {

	@Test
	void intersectCheck1() {
		Vline l1 = new Vline(-1,0,1,0);
		Vline l2 = new Vline(0,1,0,-1);
		CarSim sim = new CarSim();
		double [] p = sim.getintersect(l1, l2);
		assertEquals(p[0],0);
		assertEquals(p[1],0);
	}
	@Test
	void intersectCheck2() {
		Vline l1 = new Vline(0,1,2,1);
		Vline l2 = new Vline(1,2,1,0);
		CarSim sim = new CarSim();
		double [] p = sim.getintersect(l1, l2);
		assertEquals(p[0],1);
		assertEquals(p[1],1);
	}
	@Test
	void intersectCheck3() {
		Vline l1 = new Vline(-2,2,3,-3);
		Vline l2 = new Vline(2,2,-3,-3);
		CarSim sim = new CarSim();
		double [] p = sim.getintersect(l1, l2);
		System.out.println(p[0]);
		System.out.println(p[1]);
		assertEquals(p[0],0);
		assertEquals(p[1],0);
	}
	@Test
	void intersectCheck4() {
		Vline l1 = new Vline(5,5,5,-5);
		Vline l2 = new Vline(5,5,5,-5);
		CarSim sim = new CarSim();
		double [] p = sim.getintersect(l1, l2);
		System.out.println(p[0]);
		System.out.println(p[1]);
		assertEquals(p[0],-9999999);
		assertEquals(p[1],-9999999);
	}
	
	@Test
	void intersectCheck5() {
		Vline l1 = new Vline(0,0,0,1);
		Vline l2 = new Vline(1,0,1,1);
		CarSim sim = new CarSim();
		double [] p = sim.getintersect(l1, l2);
		System.out.println(p[0]);
		System.out.println(p[1]);
		assertEquals(p[0],-9999999);
		assertEquals(p[1],-9999999);
	}
	
	@Test
	void intersectCheck6() {
		Vline l1 = new Vline(4,-19,9,-14);
		Vline l2 = new Vline(8,-11,13,-16);
		CarSim sim = new CarSim();
		double [] p = sim.getintersect(l1, l2);
		System.out.println(p[0]);
		System.out.println(p[1]);
		assertEquals(p[0],-9999999);
		assertEquals(p[1],-9999999);
	}
	@Test
	void anglecheck() {
		CarSim sim = new CarSim();
		sim.carACL[0]=1;
		sim.carACL[1]=-1;
		double a = sim.angleofacl();
		System.out.println(a);
		assertEquals(a, 3*0.7853981633974481);
	}
	@Test
	void relativeanglecheck() {
		CarSim sim = new CarSim();
		sim.carACL[0]=1;
		sim.carACL[1]=1;
		sim.carAngle=7*0.7853981633974481;
		double a = sim.getrelativeangle();
		System.out.println(a);
		assertEquals(a, 2*0.7853981633974481);
	}

}
