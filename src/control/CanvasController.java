package control;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import model.City;
import model.Dataset;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class CanvasController implements Initializable
{
	@FXML private Canvas canvas;
	
	private Dataset dataset;
	private ArrayList<int[]> bestSequences;
	
	private double offset;
	private double translationFactor;
	
	public CanvasController()
	{
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
	}

	public void setDataset(Dataset dataset)
	{
		this.dataset = dataset;
	}

	public void setBestSequences(ArrayList<int[]> bestSequences)
	{
		this.bestSequences = bestSequences;
	}

	public void draw()
	{
		GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		ArrayList<City> cityList		= dataset.getCityList();
		double maxX						= 0;
		double maxY						= 0;
		
		// Determine maximal x and y value of cities for scaling.
		for (int i = 0; i < cityList.size(); i++) {
			maxX = cityList.get(i).getX() > maxX ? cityList.get(i).getX() : maxX;
			maxY = cityList.get(i).getY() > maxY ? cityList.get(i).getY() : maxY;
		}
		
		offset				= canvas.getWidth() * 0.05;
		translationFactor 	= (canvas.getWidth() * 0.9) / maxX;

		graphicsContext.setLineCap(StrokeLineCap.ROUND);
		graphicsContext.setLineJoin(StrokeLineJoin.ROUND);

		RouteDrawingTimer timer = new RouteDrawingTimer(cityList, bestSequences, graphicsContext, canvas, translationFactor, offset);
	    timer.start();

		System.out.println("maxX = " + maxX + ", maxY = " + maxY);
	}
}
