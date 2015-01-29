package application;
	
import java.util.ArrayList;

import control.CanvasController;
import sampling.LSSamplingThread;
import sampling.SASamplingThread;
import sampling.SamplingThread;
import model.Dataset;
import model.OptionSet;
import model.OptionSet.TemperatureSchedule;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;


public class Main extends Application 
{
	private ArrayList<int[]> bestSequences;
	private Dataset dataset;
	
	@Override
	public void start(Stage primaryStage)
	{
		try {
	        // Execute simulatedAnnealing.
	        generateData(true, true, 2, 1, 100000);
	        
			// Load core .fxml file. 
			FXMLLoader fxmlLoader				= new FXMLLoader();
			Pane root							= (Pane) fxmlLoader.load(getClass().getResource("/view/TravelChart.fxml").openStream());
	        CanvasController canvasController	= (CanvasController) fxmlLoader.getController();
	        Scene scene							= new Scene(root);
			
	        primaryStage.setTitle("Simulated Annealing");
	        primaryStage.setScene(scene);
	        primaryStage.show();
	        
	        // Deliver model data to controller.
	        canvasController.setDataset(dataset);
	        canvasController.setBestSequences(bestSequences);
	        
	        // Draw best sequences.
	        canvasController.draw();
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void generateData(boolean samplingMode, boolean threadingEnabled, double samplingRate, int numberOfRepetitions, int numberOfIterations)
	{
		double solutionCost				= 0;
		double minSolutionCost			= Double.MAX_VALUE;
		
//		dataset						= new Dataset("Berlin52", "/data/berlin52.tsp", 7542); // 7544
		dataset						= new Dataset("CH130", "/data/ch130.tsp", 6110); // 6205
//		dataset						= new Dataset("CH150", "/data/ch150.tsp", 6528); // 6703
		
		// OptionSet:
		// TemperateSchedule,	double,				double,				double,				double,					int,						long
		// temperatureSchedule, temperature_delta, 	temperature_alpha, 	temperature_omega, 	solution_deltaFraction, stepsWithoutImprovement,	maxNumberOfIterations
		OptionSet optionSet			= new OptionSet(OptionSet.TemperatureSchedule.LINEAR, 0.01, 100, 0, 0.01, 100, numberOfIterations);
		OptionSet bestOptionSet		= new OptionSet(OptionSet.TemperatureSchedule.LINEAR, 0, 0, 0, 0, 0, 0);
		
		// Variables needed for time measurements.
		long start					= 0;
		long end					= 0;
		int executions				= 0;
		
		
		// Start to measure time.
		start = System.nanoTime();
		
		if (samplingMode) {
			// Execute multiple threads to speed up sampling.
			if (threadingEnabled) {
				ArrayList<SamplingThread> threadList = new ArrayList<SamplingThread>();
				
				threadList.add(new SASamplingThread("thread1", dataset, optionSet, 1, 17, numberOfRepetitions, samplingRate));
				threadList.add(new SASamplingThread("thread2", dataset, optionSet, 32, 513, numberOfRepetitions, samplingRate));
				threadList.add(new SASamplingThread("thread3", dataset, optionSet, 1024, 8193, numberOfRepetitions, samplingRate));
				
//				threadList.add(new LSSamplingThread("thread1", dataset, optionSet, 1, 17, numberOfRepetitions, samplingRate));
//				threadList.add(new LSSamplingThread("thread2", dataset, optionSet, 32, 257, numberOfRepetitions, samplingRate));
//				threadList.add(new LSSamplingThread("thread3", dataset, optionSet, 512, 8193, numberOfRepetitions, samplingRate));
				
				// Start threads.
				for (int i = 0; i < threadList.size(); i++) {
					threadList.get(i).start();
				}
				
				try {
					// Wait for all threads to finish.
					for (int i = 0; i < threadList.size(); i++) {
						threadList.get(i).getThread().join();
					}
				}
				
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				for (int i = 0; i < threadList.size(); i++) {
					System.out.println("#" + i + ": " + threadList.get(i).getNumberOfExecutions());
					executions += threadList.get(i).getNumberOfExecutions();
					if (threadList.get(i).getBestSolutionCost() < minSolutionCost) {
						minSolutionCost = threadList.get(i).getBestSolutionCost();
						bestOptionSet	= threadList.get(i).getBestOptionSet();
						bestSequences	= new ArrayList<int[]>(threadList.get(i).getBestSequences());
					}
				}
			}
			// Execute sampling single-threaded.
			else {
				for (int currentTempAlpha = 1; currentTempAlpha < 1000; currentTempAlpha *= samplingRate) {
					optionSet.setTemperature_alpha(currentTempAlpha);

					for (double currentTempDelta = currentTempAlpha / samplingRate; currentTempDelta > 0.001; currentTempDelta /= samplingRate) {
						optionSet.setTemperature_delta(currentTempDelta);
								
						for (int currentMaxStepsBeforeRestart = 1000; currentMaxStepsBeforeRestart < 1001; currentMaxStepsBeforeRestart *= 10) {
							optionSet.setStepsWithoutImprovement(currentMaxStepsBeforeRestart);
							
							// Reset solution cost
							solutionCost = 0;
							
							for (int i = 0; i < numberOfRepetitions; i++) {
								solutionCost += dataset.executeSimulatedAnnealing_linear(optionSet);
								executions++;
							}
							
							if (solutionCost < minSolutionCost) {
								minSolutionCost	= solutionCost;
								bestOptionSet	= new OptionSet(optionSet);
							}
						}
					}
				}
			}
			
			// Stop measuring time.
			end = System.nanoTime();
			// 147 ms per exec. / 145 ms per exec.
			System.out.println("\n\n*** Runtime: " + ((end - start) / 1000000) + " ms in total, " + (((end - start) / executions) / 1000000) + " ms per execution.");
			
			System.out.println("\n\n*** Best solution ***");
			System.out.println("Cost: " + minSolutionCost / numberOfRepetitions);
			System.out.println("Configuration:\n" + bestOptionSet);
		}
		
		else {
			optionSet.setTemperatureSchedule(TemperatureSchedule.LINEAR);
//			optionSet.setTemperature_delta(0.015625);	// SA
//			optionSet.setTemperature_alpha(2);	// SA
			optionSet.setTemperature_delta(1);	// LS
			optionSet.setTemperature_alpha(256);	// LS
			optionSet.setTemperature_omega(0);
			optionSet.setSolution_deltaFraction(0.01);
			optionSet.setStepsWithoutImprovement(1000);
			optionSet.setMaxNumberOfIterations(numberOfIterations);
			
			start = System.nanoTime();
			for (int i = 0; i < numberOfRepetitions; i++) {
//				solutionCost += dataset.executeSimulatedAnnealing_linear(optionSet);
				solutionCost += dataset.executeLocalSearch(optionSet);
			}
			end = System.nanoTime();
			
			System.out.println("\n\n*** Solution ***");
			System.out.println("Cost: " + solutionCost / numberOfRepetitions);
			System.out.println("\n\n*** Runtime: " + ((end - start) / 1000000) + " ms in total, " + (((end - start) / numberOfRepetitions) / 1000000) + " ms per execution.");
		}
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
}
