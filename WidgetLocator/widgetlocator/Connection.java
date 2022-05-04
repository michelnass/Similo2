package widgetlocator;

import java.util.Random;

public class Connection
{
	private double weight=1;
	private double lastWeight=1;
	private Neuron connectedTo=null;
	private double deltaWeight=1;
	
	private Random random=new Random(System.currentTimeMillis());

	public Connection(double weight)
	{
		this.weight = weight;
	}

	public Connection(double weight, Neuron connectedTo)
	{
		this.weight = weight;
		this.connectedTo = connectedTo;
	}

	public double getWeight()
	{
		return weight;
	}

	public void setWeight(double weight)
	{
		this.weight = weight;
	}

	public Neuron getConnectedTo()
	{
		return connectedTo;
	}

	public void setConnectedTo(Neuron connectedTo)
	{
		this.connectedTo = connectedTo;
	}
	
	/**
	 * Update weight randomly based on deltaWeight
	 */
	public void updateWeight()
	{
/*
		lastWeight=weight;
		double delta=Math.random()*deltaWeight-(deltaWeight/2);
		weight+=delta;
		if(weight<0)
		{
			weight=0;
		}
*/
		lastWeight=weight;
		double step = 0.25;
		boolean up = true;
		if(weight == 1)
		{
			up = false;
		}
		else if(weight == 0)
		{
			up = true;
		}
		else
		{
			up = (random.nextInt(2) == 0);
		}
		if(up)
		{
			weight += step;
		}
		else
		{
			weight -= step;
		}
//		weight = random.nextInt(3) * 0.5;
	}
	
	public void undoUpdateWeight()
	{
		weight=lastWeight;
	}

	public double getDeltaWeight()
	{
		return deltaWeight;
	}

	public void setDeltaWeight(double deltaWeight)
	{
		this.deltaWeight = deltaWeight;
	}
}
