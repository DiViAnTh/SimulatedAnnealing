package sampling;

import java.util.ArrayList;

import model.Dataset;
import model.OptionSet;

abstract public class SamplingThread implements Runnable
{
	protected Thread thread;
	protected String threadName;
	
	protected Dataset dataset;
	protected OptionSet optionSet;
	protected OptionSet bestOptionSet;
	
	protected int startIndex;
	protected int endIndex;
	protected int numberOfRepetitions;
	protected int executions;
	protected double samplingRate;
	
	protected double solutionCost;
	protected double minSolutionCost;
	
	protected ArrayList<int[]> bestSequences;
	
	public SamplingThread(String threadName, Dataset dataset, OptionSet optionSet, int startIndex, int endIndex, int numberOfRepetitions, double samplingRate)
	{
		this.threadName				= threadName;
		this.dataset				= new Dataset(dataset);
		this.optionSet				= new OptionSet(optionSet);
		this.startIndex				= startIndex;
		this.endIndex				= endIndex;
		this.samplingRate			= samplingRate;
		this.numberOfRepetitions	= numberOfRepetitions;
		
		bestOptionSet				= new OptionSet(OptionSet.TemperatureSchedule.LINEAR, 0, 0, 0, 0, 0, 0);
		executions					= 0;
		solutionCost				= 0;
		minSolutionCost				= Double.MAX_VALUE;
	}
	
	public void start()
	{
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
	
	public ArrayList<int[]> getBestSequences()
	{
		return bestSequences;
	}
}
