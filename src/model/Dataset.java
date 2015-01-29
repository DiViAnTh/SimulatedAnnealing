package model;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;
import model.OptionSet.TemperatureSchedule;

// Efficiency in coding has priority!

/**
 * Loads data from file, preprocesses it.
 * @author RM
 *
 */
public class Dataset
{
	private ArrayList<City> cityList;
	// Stores when which sequence was used the last time. Used for calculating "heat" of sequences.
	private Map<Integer, Integer> sequenceUsageMap;
	
	private double optimalRouteCost;
	private String name;
	 
	// Store all "best" sequences, including the last one. 
	ArrayList<int[]> bestSequences;
	
	public Dataset(String name, String filename, double optimalRouteCost)
	{
		try {
			this.name				= name;
			this.cityList			= new ArrayList<City>();
			this.optimalRouteCost	= optimalRouteCost;
			this.sequenceUsageMap	= new HashMap<Integer, Integer>();
			this.bestSequences		= new ArrayList<int[]>();
			
			// Read file
			readFile(filename);
		}
		
		catch (IOException ex) {
			System.out.println("boo" + ex);
		}
	}
	
	public Dataset(Dataset source)
	{
		this.name				= source.name;
		this.cityList			= new ArrayList<City>(source.cityList);
		this.optimalRouteCost	= source.optimalRouteCost;
		this.sequenceUsageMap	= new HashMap<Integer, Integer>(source.sequenceUsageMap);
		this.bestSequences		= new ArrayList<int[]>(source.bestSequences);
	}
	
	// Resets attributes before new run (to flush out data from possible previous run).
	private void resetData()
	{
		sequenceUsageMap.clear();
		bestSequences.clear();
	}
	
	private void readFile(String filename) throws IOException
	{
		 //uses the class loader search mechanism:
		InputStream input		= this.getClass().getResourceAsStream(filename);
		InputStreamReader isr	= new InputStreamReader(input, StandardCharsets.UTF_8);
		BufferedReader reader	= new BufferedReader(isr);

		name		= reader.readLine();
		String line	= null;
		
		// Fast forward to actual data.
		for (int i = 0; i < 5; i++) {
			reader.readLine();
		}
		
		// Store city data.
		while ( (line = reader.readLine()) != null && line != "EOF") {
			String[] stringParts = line.split(" ");
			
			if (stringParts.length == 3) {
				int cityID = Integer.parseInt(stringParts[0]);
				cityList.add(new City(cityID, Double.parseDouble(stringParts[1]), Double.parseDouble(stringParts[2])));
			}
		}
	}
	
	private void copyArrayFromTo(int[] source, int[] target)
	{
		for (int i = 0; i < source.length; i++) {
			target[i] = source[i];
		}
	}
	
	private void copyArrayFromTo(int[] source, int[] target, Pair<Integer, Integer> swapIndices)
	{
//		for (int i = swapIndices.getKey(); i <= swapIndices.getValue(); i++) {
//			target[i] = source[i];
//		}
		
		for (int i = 0; i < source.length; i++) {
			target[i] = source[i];
		}
	}
	
	@Deprecated
	private void swapTwoElements(int[] sequence, Pair<Integer, Integer> swappedIndices)
	{
		int firstIndex			= swappedIndices.getKey();
		int secondIndex			= swappedIndices.getValue();
		int temp				= sequence[firstIndex];
		sequence[firstIndex]	= sequence[secondIndex];
		sequence[secondIndex]	= temp;		
	}
	
	public double executeLocalSearch(OptionSet optionSet)
	{
		// Contains currently selected sequence.
		int[] solutionSequence					= new int[cityList.size()];
		// Contains a sequence randomly selected from currenSequence's neighbourhood. 
		int[] neighbourSequence					= new int[cityList.size()];
		// Contains best sequence found up to date.
		int[] bestSequence						= new int[cityList.size()];
		
		// Variables storing various algorithm parameters.
		double currentSolutionDelta				= Double.MAX_VALUE;
		double acceptableDelta					= optionSet.getSolution_deltaFraction() * optimalRouteCost;
		int maxNumberOfStepsWOImprovement		= optionSet.getStepsWithoutImprovement();
		long maxNumberOfIterations				= optionSet.getMaxNumberOfIterations();
		
		// Cost of currently selected solution/sequence.
		double solutionCost						= Double.MAX_VALUE;
		// Cost of examined neighbour.
		double neighbourCost					= Double.MAX_VALUE;
		// Cost of best route found.
		double bestRouteCost					= Double.MAX_VALUE;
		
		// Stores how many steps have been taken without improvement.
		int stepsSinceImprovement			 	= Integer.MAX_VALUE;
		
		// Auxiliary variable used for updating neighbour sequences.
		Pair<Integer, Integer> swappedIndices	= null;

		// ----------------
		
		// Reset data
		resetData();
		
		// ----------------
		
		// Generate start sequence.
		solutionCost	= initSequences(solutionSequence, neighbourSequence, bestSequence);
		neighbourCost	= solutionCost;
		bestRouteCost	= solutionCost;
			
		// ----------------
		
		// Execute calculation until terminating conditions are met.
		//for (int currentIteration = 0; (currentIteration < maxNumberOfIterations) && (Math.abs(currentSolutionDelta) > acceptableDelta); currentIteration++) {
		for (int currentIteration = 0; (currentIteration < maxNumberOfIterations); currentIteration++) {
			// Generate candidate from neighbourhood, store indices of swapped elements. 
			swappedIndices	= generateNeighbour_2opSwap(neighbourSequence);
			
			// Calculate cost of generated neighbour (neighbourSequence) of currentSequence.
			neighbourCost	= evaluateSequence(neighbourSequence);

			// Check if new solution is better than old solution. If so: Accept neighbour as new solution.
			if (neighbourCost < solutionCost) {
				// Adapt cost of current solution.
				solutionCost = neighbourCost;

				// Update variable containing how many steps were taken since the last improvement.
				stepsSinceImprovement = 0;
				
				// Update solutionSequence to match neighbourSequence.
				copyArrayFromTo(neighbourSequence, solutionSequence, swappedIndices);
				
				// Update variables regarding best route found yet.
				if (solutionCost < bestRouteCost) {
					bestRouteCost = solutionCost;
					copyArrayFromTo(solutionSequence, bestSequence);
					
					// Copy current best solution to list of best solutions.
					int[] bestSequenceCopy = new int[bestSequence.length];
					copyArrayFromTo(bestSequence, bestSequenceCopy);
					bestSequences.add(bestSequenceCopy);
				}
			}
			
			// Reset neighbourSequence (to match solutionSequence again).
			else {
				// Update neighbourSequence to match solutionSequence.
				copyArrayFromTo(solutionSequence, neighbourSequence);
			}
			
			// Update how many steps were taken since improvement.
			if (stepsSinceImprovement++ == maxNumberOfStepsWOImprovement) {
//			if (Math.random() < 0.01) {
//			if ((bestRouteCost * 1.25) < solutionCost) {
				// Generate new, random start solution; then restart.
				swappedIndices			= generateNeighbour_2opSwap(solutionSequence);
				
				// If limit reached: .
				solutionCost			= evaluateSequence(solutionSequence);		
				stepsSinceImprovement	= 0;
				
				// Update neighbourSequence to match solutionSequence.
				copyArrayFromTo(solutionSequence, neighbourSequence);
			}
		}
		
		return bestRouteCost;
	}
	
	/**
	 * Executes simulated annealing.
	 * @param optionSet Contains options/parameters needed for this algorithm.
	 * @return Cost for best solution found.
	 */
	public double executeSimulatedAnnealing_linear(OptionSet optionSet)
	{
		// Contains currently selected sequence.
		int[] solutionSequence					= new int[cityList.size()];
		// Contains a sequence randomly selected from currenSequence's neighbourhood. 
		int[] neighbourSequence					= new int[cityList.size()];
		// Contains best sequence found up to date.
		int[] bestSequence						= new int[cityList.size()];
		
		// Variables storing various algorithm parameters.
		double currentSolutionDelta				= Double.MAX_VALUE;
		double acceptableDelta					= optionSet.getSolution_deltaFraction() * optimalRouteCost;
		double temperature						= optionSet.getTemperature_alpha();
		double temperature_delta				= optionSet.getTemperature_delta();
		double temperature_omega				= optionSet.getTemperature_omega();
		int maxNumberOfStepsWOImprovement		= optionSet.getStepsWithoutImprovement();
		long maxNumberOfIterations				= optionSet.getMaxNumberOfIterations();
		TemperatureSchedule temperatureSchedule	= optionSet.getTemperatureSchedule();
		
		// Cost of currently selected solution/sequence.
		double solutionCost						= Double.MAX_VALUE;
		// Cost of examined neighbour.
		double neighbourCost					= Double.MAX_VALUE;
		// Cost of best route found.
		double bestRouteCost					= Double.MAX_VALUE;
				
		// Stores how many steps have been taken without improvement.
		int stepsSinceImprovement			 	= Integer.MAX_VALUE;
		
		// Auxiliary variable used for updating neighbour sequences.
		Pair<Integer, Integer> swappedIndices	= null; 
		
		// ----------------
		
		// Reset data.
		resetData();

		// ----------------
		
		// Generate start sequence.
		solutionCost	= initSequences(solutionSequence, neighbourSequence, bestSequence);
		neighbourCost	= solutionCost;
		bestRouteCost	= solutionCost;

		// ----------------
		
		// Execute calculation until terminating conditions are met.
		//for (int currentIteration = 0; (currentIteration < maxNumberOfIterations) && (Math.abs(currentSolutionDelta) > acceptableDelta); currentIteration++) {
		for (int currentIteration = 0; (currentIteration < maxNumberOfIterations); currentIteration++) {
			// Generate candidate from neighbourhood, store indices of swapped elements. 
			swappedIndices	= generateNeighbour_2opSwap(neighbourSequence);
			
			// Calculate cost of generated neighbour (neighbourSequence) of currentSequence.
			neighbourCost	= evaluateSequence(neighbourSequence);

			// Check if new solution is better than old solution. If so: Accept neighbour as new solution.
			if (neighbourCost < solutionCost) {
				// Adapt cost of current solution.
				solutionCost = neighbourCost;

				// Update variable containing how many steps were taken since the last improvement.
				stepsSinceImprovement = 0;
				
				// Update solutionSequence to match neighbourSequence.
				copyArrayFromTo(neighbourSequence, solutionSequence, swappedIndices);
				
				// Update variables regarding best route found yet.
				if (solutionCost < bestRouteCost) {
					bestRouteCost = solutionCost;
					copyArrayFromTo(solutionSequence, bestSequence);
					
					// Copy current best solution to list of best solutions.
					int[] bestSequenceCopy = new int[bestSequence.length];
					copyArrayFromTo(bestSequence, bestSequenceCopy);
					bestSequences.add(bestSequenceCopy);
				}
			}
			
			// If new solution worse than current solution:
			else {
				// Accept despite worse costs.
				if (Math.random() < Math.exp( - (neighbourCost - solutionCost) / temperature)) {
					// Adapt cost of current solution.
					solutionCost = neighbourCost;

					// Update solutionSequence to match neighbourSequence.
					copyArrayFromTo(neighbourSequence, solutionSequence, swappedIndices);
				}
				
				// Decline - reset neighbourSequence.
				else {
					// Update neighbourSequence to match solutionSequence.
					copyArrayFromTo(solutionSequence, neighbourSequence);
				}					
			}
			
			// Store number of current iteration as last usage of solutionSequence.
//			sequenceUsageMap.put(solutionSequence.hashCode(), currentIteration);

			// Update temperature.
			// @todo Optimization target.
			temperature = temperature > temperature_delta ? temperature - temperature_delta : 0;
			
			// Update how many steps were taken since improvement.
			if (false && stepsSinceImprovement++ == maxNumberOfStepsWOImprovement) {
//			if (Math.random() < 0.01) {
//			if ((bestRouteCost * 1.25) < solutionCost) {
				// If limit reached: Restart annealing schedule with best solution found yet.
				solutionCost			= bestRouteCost;
				temperature				= optionSet.getTemperature_alpha();				
				stepsSinceImprovement	= 0;
				
				// Update solutionSequence and neighbourSequence to match bestSequence.
				copyArrayFromTo(bestSequence, solutionSequence);
				copyArrayFromTo(bestSequence, neighbourSequence);
			}
		}
		
		return bestRouteCost;
	}
	
	/**
	 * Initializes city sequence lists needed for algorithm.
	 * @param currentSequence
	 * @param neighbourSequence
	 * @return Cost of start sequence.
	 */
	private double initSequences(int[] solutionSequence, int[] neighbourSequence, int[] bestSequence)
	{
		for (int i = 0; i < solutionSequence.length; i++) {
			solutionSequence[i]		= i;
		}
		
		generateNeighbour_2opSwap(solutionSequence);
		
		// Init sequences. For now: 1 2 3 ... n.
		for (int i = 0; i < solutionSequence.length; i++) {
			neighbourSequence[i]	= solutionSequence[i];
			bestSequence[i]			= solutionSequence[i];
		}
		
		return evaluateSequence(solutionSequence);
	}
	
	/**
	 * Swaps two random elements to generate a potential next solution.
	 * @param sequence
	 * @return Indices of the two elements being swapped.
	 */
	private Pair<Integer, Integer> generateNeighbour_randomSwap(int[] sequence)
	{
		int firstIndex	= (int) Math.floor(sequence.length * Math.random());
		int secondIndex = (int) Math.floor(sequence.length * Math.random());
		
		// Check conditions.
		while (firstIndex == secondIndex || firstIndex == sequence.length || secondIndex == sequence.length) {
			firstIndex	= (int) Math.floor(sequence.length * Math.random());
			secondIndex = (int) Math.floor(sequence.length * Math.random());
		}
				
		// Swap elements with randomly generated indices.
		int temp				= sequence[firstIndex];
		sequence[firstIndex]	= sequence[secondIndex];
		sequence[secondIndex]	= temp;

		return new Pair<Integer, Integer>(firstIndex, secondIndex);
	}
	
	/**
	 * Swaps two adjacent elements to generate a potential next solution.
	 * @param sequence
	 * @return Indices of the two elements being swapped.
	 */
	private Pair<Integer, Integer> generateNeighbour_adjacentSwap(int[] sequence)
	{
		int firstIndex	= (int) Math.floor(sequence.length * Math.random());
		int secondIndex = firstIndex == sequence.length ? firstIndex - 1 : firstIndex + 1;
		
		// Check conditions.
		while (firstIndex == secondIndex || firstIndex == sequence.length || secondIndex == sequence.length) {
			firstIndex	= (int) Math.floor(sequence.length * Math.random());
			secondIndex = (int) Math.floor(sequence.length * Math.random());
		}
				
		// Swap elements with randomly generated indices.
		int temp				= sequence[firstIndex];
		sequence[firstIndex]	= sequence[secondIndex];
		sequence[secondIndex]	= temp;

		return new Pair<Integer, Integer>(firstIndex, secondIndex);
	}
	
	/**
	 * Randomly scrambles whol sequence.
	 * @param sequence
	 * @return
	 */
	private Pair<Integer, Integer> generateNeighbour_scramble(int[] sequence)
	{
		int swapIndex = 0;
		for (int i = 0; i < sequence.length; i++) {
			swapIndex = (int) Math.floor(sequence.length * Math.random());
		
			// Check conditions.
			while (swapIndex == i || swapIndex == sequence.length) {
				swapIndex = (int) Math.floor(sequence.length * Math.random());
			}
			
			// Swap elements with randomly generated indices.
			int temp			= sequence[i];
			sequence[i]			= sequence[swapIndex];
			sequence[swapIndex]	= temp;
		}

		return new Pair<Integer, Integer>(0, sequence.length - 1);
	}
	
	/**
	 * Inverted random subsequence in given sequence to generate a potential nex solution.
	 * @param sequence
	 * @return Start and end indices of inverted subsequence.
	 */
	private Pair<Integer, Integer> generateNeighbour_2opSwap(int[] sequence)
	{
		// Define offset to encourage longer sequences to be reversed / guarantee minimal size of sequence to be reversed.
		int firstIndex	= (int) Math.floor(sequence.length * Math.random());
		int offset		= 20;
		int secondIndex = (int) Math.floor(sequence.length * Math.random()) + offset;
		
		// Check conditions.
		while (firstIndex == secondIndex || firstIndex >= sequence.length || secondIndex >= sequence.length) {
			firstIndex	= (int) Math.floor(sequence.length * Math.random());
			secondIndex = (int) Math.floor(sequence.length * Math.random()) + offset;
		}
		
		// Make sure that firstIndex is smaller than secondIndex.
		if (firstIndex >= secondIndex) {
			int temp	= firstIndex;
			firstIndex	= secondIndex;
			secondIndex	= temp;
		}
		
		// Inverse sequence of cities located between firstIndex and secondIndex.
		for (int i = 0; i < (secondIndex - firstIndex + 1) / 2; i++) {
			int temp					= sequence[firstIndex + i];
			sequence[firstIndex + i]	= sequence[secondIndex - i];
			sequence[secondIndex - i]	= temp;	
		}

//		if (sequenceUsageMap.containsKey(sequence.hashCode()))
//			System.out.println("Sequence occured last in iteration #" + sequenceUsageMap.get(sequence.hashCode()));
		
		return new Pair<Integer, Integer>(firstIndex, secondIndex);
	}
	
	/**
	 * Slides subsequence to the right.
	 * @param sequence
	 * @return Start and end index of slided subsequence. 
	 */
	private Pair<Integer, Integer> generateNeighbour_slideSwap(int[] sequence)
	{
		// Define offset to encourage longer sequences to be reversed / guarantee minimal size of sequence to be reversed.
		int offset		= 0;
		int firstIndex	= (int) Math.floor(sequence.length * Math.random());
		int secondIndex = (int) Math.floor(sequence.length * Math.random()) + offset;
		
		// Check conditions.
		while (firstIndex == secondIndex || firstIndex >= sequence.length || secondIndex >= sequence.length) {
			firstIndex	= (int) Math.floor(sequence.length * Math.random());
			secondIndex = (int) Math.floor(sequence.length * Math.random()) + offset;
		}
		
		// Make sure that firstIndex is smaller than secondIndex.
		if (firstIndex >= secondIndex) {
			int temp	= firstIndex;
			firstIndex	= secondIndex;
			secondIndex	= temp;
		}
		
		// Slide sequence of cities located between firstIndex and secondIndex to the right.
		int temp = sequence[firstIndex];
		for (int i = 0; i < (secondIndex - firstIndex); i++) {
			sequence[firstIndex + i] = sequence[firstIndex + i + 1];
		}
		sequence[secondIndex] = temp;
		
		if (sequenceUsageMap.containsKey(sequence.hashCode()))
			System.out.println("Sequence occured last in iteration #" + sequenceUsageMap.get(sequence.hashCode()));
		
		return new Pair<Integer, Integer>(firstIndex, secondIndex);
	}
	
	/**
	 * Calculates cost of a sequence.
	 * @param sequence
	 * @return Cost of examined sequence.
	 */
	private double evaluateSequence(int[] sequence)
	{
		double cost		= 0;
		double diffX	= 0;
		double diffY	= 0;
		
		for (int i = 0; i < sequence.length; i++) {
			diffX = cityList.get(sequence[i]).getX() - cityList.get(sequence[(i + 1) % sequence.length]).getX();
			diffY = cityList.get(sequence[i]).getY() - cityList.get(sequence[(i + 1) % sequence.length]).getY();
			cost += Math.sqrt(diffX * diffX + diffY * diffY);
		}
		
		// Distance from last to first city.
//		diffX = cityList.get(sequence[0]).getX() - cityList.get(sequence[sequence.length - 1]).getX();
//		diffY = cityList.get(sequence[0]).getY() - cityList.get(sequence[sequence.length - 1]).getY();
//		cost += Math.sqrt(diffX * diffX + diffY * diffY);
		
		return cost;
	}

	public ArrayList<int[]> getBestSequences()
	{
		return bestSequences;
	}
	
	public ArrayList<City> getCityList()
	{
		return cityList;
	}
}