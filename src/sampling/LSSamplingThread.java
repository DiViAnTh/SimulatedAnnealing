package sampling;

import java.util.ArrayList;

import model.Dataset;
import model.OptionSet;

public class LSSamplingThread extends SamplingThread
{
	public LSSamplingThread(String threadName, Dataset dataset, OptionSet optionSet, int startIndex, int endIndex, int numberOfRepetitions, double samplingRate)
	{
		super(threadName, dataset, optionSet, startIndex, endIndex, numberOfRepetitions, samplingRate);
	}
	
	@Override
	public void run()
	{
		System.out.println("--- Local Search ---");
		System.out.println(optionSet);

		for (int currentTempAlpha = startIndex; currentTempAlpha < endIndex; currentTempAlpha *= samplingRate) {
			optionSet.setTemperature_alpha(currentTempAlpha);
			
			for (double currentTempDelta = currentTempAlpha / samplingRate; currentTempDelta > 0.000001; currentTempDelta /= samplingRate) {
				optionSet.setTemperature_delta(currentTempDelta);
						
				for (int currentMaxStepsBeforeRestart = 1024; currentMaxStepsBeforeRestart < 1025; currentMaxStepsBeforeRestart *= samplingRate) {
					optionSet.setStepsWithoutImprovement(currentMaxStepsBeforeRestart);
					
					// Reset solution cost
					solutionCost = 0;
					
					for (int i = 0; i < numberOfRepetitions; i++) {
						solutionCost += dataset.executeLocalSearch(optionSet);
						executions++;
					}
					
					if (solutionCost < minSolutionCost) {
						minSolutionCost	= solutionCost;
						bestOptionSet	= new OptionSet(optionSet);
						bestSequences	= new ArrayList<int[]>(dataset.getBestSequences());
					}
				}
			}
		}
	}
	
	public void start()
	{
		System.out.println("Starting " + threadName);
		
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.start();
		}
	}

	public double getBestSolutionCost()
	{
		return minSolutionCost;
	}
	
	public Thread getThread()
	{
		return thread;
	}
	
	public int getNumberOfExecutions()
	{
		return executions;
	}
	
	public OptionSet getBestOptionSet()
	{
		return bestOptionSet;
	}
}
