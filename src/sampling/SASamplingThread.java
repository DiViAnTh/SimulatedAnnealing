package sampling;

import java.util.ArrayList;

import model.Dataset;
import model.OptionSet;

public class SASamplingThread extends SamplingThread
{	
	public SASamplingThread(String threadName, Dataset dataset, OptionSet optionSet, int startIndex, int endIndex, int numberOfRepetitions, double samplingRate)
	{
		super(threadName, dataset, optionSet, startIndex, endIndex, numberOfRepetitions, samplingRate);
	}
	
	@Override
	public void run()
	{
		System.out.println("--- Simulated Annealing ---");
		System.out.println(optionSet);

		for (int currentTempAlpha = startIndex; currentTempAlpha < endIndex; currentTempAlpha *= samplingRate) {
			optionSet.setTemperature_alpha(currentTempAlpha);
			
			for (double currentTempDelta = currentTempAlpha / samplingRate; currentTempDelta > 0.000001; currentTempDelta /= samplingRate) {
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
						bestSequences	= new ArrayList<int[]>(dataset.getBestSequences());
					}
				}
			}
		}
	}
}
