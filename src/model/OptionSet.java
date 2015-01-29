package model;

public class OptionSet
{
	public enum TemperatureSchedule
	{
		LINEAR, LOGARITHMIC, PROPORTIONAL
	};
	
	// Temperature variables.
	private TemperatureSchedule temperatureSchedule;
	private double temperature_delta;
	private double temperature_alpha;
	private double temperature_omega;

	/**
	 * Defines how close a heuristic solution has to approximate the real solution to be counted as acceptable.
	 * I.e.: Real solution is 314, solution_deltaFraction is 10, therefore heuristic solution x has to be in the
	 * bounds of 314 * 9/10 < x < 314 * 11/10.
	 */
	private double solution_deltaFraction;
	
	/**
	 * Defines how many steps without improvement may be taken before the annealing procedure is restarted.
	 */
	private int stepsWithoutImprovement;
	
	/**
	 * Maximal number of iterations before calculation is stopped.
	 */
	private long maxNumberOfIterations;

	public OptionSet(TemperatureSchedule temperatureSchedule,
			double temperature_delta, double temperature_alpha,
			double temperature_omega, double solution_deltaFraction,
			int stepsWithoutImprovement, long maxNumberOfIterations)
	{
		this.temperatureSchedule		= temperatureSchedule;
		this.temperature_delta			= temperature_delta;
		this.temperature_alpha			= temperature_alpha;
		this.temperature_omega			= temperature_omega;
		this.solution_deltaFraction		= solution_deltaFraction;
		this.stepsWithoutImprovement	= stepsWithoutImprovement;
		this.maxNumberOfIterations		= maxNumberOfIterations;
	}
	
	public OptionSet(OptionSet source)
	{
		this.temperatureSchedule		= source.temperatureSchedule;
		this.temperature_delta			= source.temperature_delta;
		this.temperature_alpha			= source.temperature_alpha;
		this.temperature_omega			= source.temperature_omega;
		this.solution_deltaFraction		= source.solution_deltaFraction;
		this.stepsWithoutImprovement	= source.stepsWithoutImprovement;
		this.maxNumberOfIterations		= source.maxNumberOfIterations;
	}

	public TemperatureSchedule getTemperatureSchedule()
	{
		return temperatureSchedule;
	}

	public double getTemperature_delta()
	{
		return temperature_delta;
	}

	public double getTemperature_alpha()
	{
		return temperature_alpha;
	}

	public double getTemperature_omega()
	{
		return temperature_omega;
	}

	public double getSolution_deltaFraction()
	{
		return solution_deltaFraction;
	}

	public long getMaxNumberOfIterations()
	{
		return maxNumberOfIterations;
	}
	
	public int getStepsWithoutImprovement()
	{
		return stepsWithoutImprovement;
	}

	public void setStepsWithoutImprovement(int stepsWithoutImprovement)
	{
		this.stepsWithoutImprovement = stepsWithoutImprovement;
	}

	public void setTemperatureSchedule(TemperatureSchedule temperatureSchedule)
	{
		this.temperatureSchedule = temperatureSchedule;
	}

	public void setTemperature_omega(double temperature_omega)
	{
		this.temperature_omega = temperature_omega;
	}

	public void setSolution_deltaFraction(double solution_deltaFraction)
	{
		this.solution_deltaFraction = solution_deltaFraction;
	}

	public void setMaxNumberOfIterations(long maxNumberOfIterations)
	{
		this.maxNumberOfIterations = maxNumberOfIterations;
	}

	public void setTemperature_alpha(double temperature_alpha)
	{
		this.temperature_alpha = temperature_alpha;
	}
	
	public void setTemperature_delta(double temperature_delta)
	{
		this.temperature_delta = temperature_delta;
	}
	
	@Override
	public String toString()
	{
		String result;
		
		result =	"\tTemperature:\n\t\tAnnealing schedule = " + temperatureSchedule.name() + "\n\t\talpha = " + temperature_alpha + "\n\t\tdelta = " + temperature_delta + "\n\t\tomega = " + temperature_omega;
		result +=	"\n\tSolution:\n\t\tdeltaFraction = " + solution_deltaFraction;
		result +=	"\n\tIterations:\n\t\tmaxNumber =\t\t" + maxNumberOfIterations + "\n\t\twithoutImprovement =\t" + stepsWithoutImprovement;
		
		return result;
	}
}
