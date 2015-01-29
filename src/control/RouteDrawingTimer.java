package control;

import java.util.ArrayList;

import model.City;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RouteDrawingTimer extends AnimationTimer
{
	private ArrayList<City> cityList;
	private ArrayList<int[]> bestSequences;
	private int currentIndex;
	
	private GraphicsContext graphicsContext;
	private Canvas canvas;
	private double translationFactor;
	private double offset;
	
	public RouteDrawingTimer(ArrayList<City> cityList, ArrayList<int[]> bestSequences, GraphicsContext graphicsContext, Canvas canvas, double translationFactor, double offset)
	{
		currentIndex			= 0;
		this.cityList			= cityList;
		this.bestSequences		= bestSequences;
		this.graphicsContext	= graphicsContext;
		this.canvas				= canvas;
		this.translationFactor	= translationFactor;
		this.offset				= offset;
	}
	
	@Override
	public void handle(long now)
	{
		if (currentIndex < bestSequences.size()) {
			int[] sequence = bestSequences.get(currentIndex);
			
			// Clear canvas.
			graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			
			// Draw cities.
			drawCities(graphicsContext, cityList);
			
			// Draw path between cities.
			graphicsContext.beginPath();
			graphicsContext.setLineWidth(0.5);

			for (int i = 0; i < sequence.length - 1; i++) {
				double currX = cityList.get(sequence[i]).getX() * translationFactor + offset;
				double currY = cityList.get(sequence[i]).getY() * translationFactor + offset;
				double nextX = cityList.get(sequence[i + 1]).getX() * translationFactor + offset;
				double nextY = cityList.get(sequence[i + 1]).getY() * translationFactor + offset;
				
				graphicsContext.moveTo(currX, currY);
				graphicsContext.lineTo(nextX, nextY);
			}
			
			graphicsContext.closePath();
			graphicsContext.stroke();
			
			currentIndex++;
		}
		
		else {
			this.stop();
		}
	}
	
	private void drawCities(GraphicsContext graphicsContext, ArrayList<City> cityList)
	{
		float circleRadius = 5;
		graphicsContext.setFill(Color.RED);
		
		for (int i = 0; i < cityList.size(); i++) {
			graphicsContext.fillOval(cityList.get(i).getX() * translationFactor + offset - circleRadius / 2, cityList.get(i).getY()  * translationFactor + offset - circleRadius / 2, circleRadius, circleRadius);
		}
		
		graphicsContext.setFill(Color.BLACK);
	}
}
