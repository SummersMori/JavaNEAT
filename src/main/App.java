package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jdom2.JDOMException;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import neat.NEATnet;
import neat.Pools;

public class App extends Application{
	public int height = 900;
	public int width = 1600;
	private char button = ' ';
	private int xoffset = 800;
	private int yoffset = 400;
	private int count=0;
	
	private Line[] linelist = new Line[10];
	@Override
	public void start(Stage stage) {
		CarSim sim = new CarSim();
		sim.readfile("Track.txt");
		NEATnet nn=new NEATnet();
		try {
			nn = Pools.getnet("DrivingNet.txt");
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sim.nn=nn;
		
		Circle circle1 = new Circle();
		circle1.setCenterX(cx(sim.carCenter[0]));
		circle1.setCenterY(cy(sim.carCenter[1]));
		circle1.setFill(Color.GREEN);
		circle1.setStrokeWidth(20);
		circle1.setRadius(5);
		
		sim.updatePoints();
		
		Group root = new Group(circle1);
		
		for(int i=0; i<sim.eyenumber; i++) {
			Line l = new Line();
			linelist[i]=l;
			adjustLine(sim.lines[i], l);
			root.getChildren().add(l);
		}
		
		for(Vline l : sim.walls) {
			Line line = new Line();
			adjustLine(l,line);
			root.getChildren().add(line);
		}
		
		for(Vline l : sim.goals) {
			Line line = new Line();
			adjustLine(l,line);
			line.setStyle("-fx-stroke: red;");
			root.getChildren().add(line);
		}
		
		Scene scene = new Scene(root, width, height);
		
		EventHandler<KeyEvent> scenekey = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.A) {
                    button='a';
                }
				else if (event.getCode() == KeyCode.D) {
                    button='d';
                }
				else if (event.getCode() == KeyCode.W) {
                    button='w';
                }
				else if (event.getCode() == KeyCode.S) {
                    button='s';
                }
			}
			
		};
		
		scene.setOnKeyPressed(scenekey);
		stage.setScene(scene);
		stage.show();
		
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		
		Runnable task1 = new Runnable() {
			@Override
			public void run() {
				if(!stage.isShowing()) {
					System.exit(1);
				}
				//this is code for manual mode
				/*if (button=='w') {
					sim.absoluteACL += 0.2;
                }
				else if (button=='s') {
					sim.absoluteACL += -0.2;
                }
				else if (button=='a') {
					sim.angleACL+=0.01;
                }
				else if (button=='d') {
					sim.angleACL-=0.01;
                }
				button = ' ';
				sim.carACL[0]=(sim.absoluteACL*Math.sin(sim.carAngle));
				sim.carACL[1]=(sim.absoluteACL*Math.cos(sim.carAngle));
				sim.absoluteACL = sim.absoluteACL-sim.absoluteACL*0.02;
				sim.angleACL = sim.angleACL-sim.angleACL*0.02;
				sim.updatePoints();
				for(Vline l : sim.walls) {
					if(sim.colissioncheck(l)) {
						sim.alive=false;
					}
				}
				sim.goalcheck();*/
				//ai driver
				sim.step();
				count++;
				System.out.println(sim.alive);
				System.out.println(sim.totalgoals);
				System.out.println(count);
				System.out.println(sim.carCenter[0]);
				System.out.println(sim.carCenter[1]);
				
				circle1.setCenterX(cx(sim.carCenter[0]));
				circle1.setCenterY(cy(sim.carCenter[1]));
				for(int i=0; i<sim.eyenumber; i++) {
					adjustLine(sim.lines[i], linelist[i]);
				}
				
			}
		};
		
		ScheduledFuture<?> loop = ses.scheduleAtFixedRate(task1, 50, 25, TimeUnit.MILLISECONDS);
		
	}
	
	private void adjustLine(Vline vl, Line l) {
		l.setStartX(cx(vl.x1));
		l.setStartY(cy(vl.y1));
		l.setEndX(cx(vl.x2));
		l.setEndY(cy(vl.y2));
	}
	
	//adjustment from standard graph to JFX graph
	private double cy(double y) {
		return -y+yoffset;
	}
	
	private double cx(double x) {
		return x+xoffset;
	}
	
}