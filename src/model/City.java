package model;

public class City
{
	private int id;
	private double x;
	private double y;
	
	public City(int id, double x, double y)
	{
		this.id	= id;
		this.x	= x;
		this.y	= y;
	}
	
	@Override
	public String toString()
	{
		return "City: " + id + " at x = " + x + ", y = " + y;
	}
	
	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}	
}
