package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DrawApp extends Application{
	public int height = 800;
	public int width = 1600;
	private int xoffset = 800;
	private int yoffset = 400;
	private ArrayList<Line> linelist = new ArrayList<Line>();
	private ArrayList<Line> goallist = new ArrayList<Line>();
	private char button = 'a';
	private boolean save = false;
	
	private Line currentline;
	@Override
	public void start(Stage stage) {
		CarSim sim = new CarSim();
		//Draws green circle at car start pos
		Circle circle1 = new Circle();
		circle1.setCenterY(-sim.carCenter[1]+yoffset);
		circle1.setCenterX(sim.carCenter[0]+xoffset);
		circle1.setFill(Color.GREEN);
		circle1.setStrokeWidth(20);
		circle1.setRadius(5);
		Group root = new Group(circle1);
		Scene scene = new Scene(root, 1600, 900);
		//press s to draw goals, a to draw walls, x to confrim save
		EventHandler<KeyEvent> scenekey = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.A) {
                    button='a';
                    System.out.println("Drawing walls");
                }
				else if (event.getCode() == KeyCode.S) {
                    button='s';
                    System.out.println("DrawingGoals");
                }
				else if (event.getCode() == KeyCode.X) {
					save=true;
				}
			}
			
		};
		scene.setOnKeyPressed(scenekey);
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent mouseEvent) {
		    	Line line = new Line();
		    	if(button=='a') {
		    		linelist.add(line);
		    	}
		    	else if(button=='s') {
		    		goallist.add(line);
		    	}
		    	currentline = line;
		        root.getChildren().add(line);
		        line.setStartX(mouseEvent.getSceneX());
		        line.setStartY(mouseEvent.getSceneY());
		        line.setEndX(mouseEvent.getSceneX());
		        line.setEndY(mouseEvent.getSceneY());
		    }
		});
		scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent mouseEvent) {
		        currentline.setEndX(mouseEvent.getSceneX());
		        currentline.setEndY(mouseEvent.getSceneY());
		    }
		});
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent t) {
		    	if(save) {
		    		try {
						FileWriter myWriter = new FileWriter("Track.txt");
						int w = 0;
						for(Line l : linelist) {
				    		double x1 = cx(l.getStartX());
				    		double y1 = cy(l.getStartY());
				    		double x2 = cx(l.getEndX());
				    		double y2 = cy(l.getEndY());
				    		myWriter.write(x1+" "+y1+" "+x2+" "+y2+" "+w+" wall\n");
				    		w++;
				    	}
						int g = 0;
						for(Line l : goallist) {
							double x1 = cx(l.getStartX());
				    		double y1 = cy(l.getStartY());
				    		double x2 = cx(l.getEndX());
				    		double y2 = cy(l.getEndY());
				    		myWriter.write(x1+" "+y1+" "+x2+" "+y2+" "+g+" goal\n");
				    		g++;
						}
						myWriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    }
		});
	}
	//adjustment from JFX graph to standard graph
	private double cy(double y) {
		return -(y-yoffset);
	}
	
	private double cx(double x) {
		return x-xoffset;
	}
}