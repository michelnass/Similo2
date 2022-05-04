package widgetlocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Neuron
{
	private String name=null;
	private double value=0;
	private double bias=0;
	private List<Connection> connections=new ArrayList<Connection>();
	private Connection lastUpdatedConnection=null;
	private Random rand = new Random();

	/**
	 * @return The (output) value
	 */
	public double getValue()
	{
		return value;
	}

	public void setValue(double value)
	{
		this.value = value;
	}

	public double getBias()
	{
		return bias;
	}

	public void setBias(double bias)
	{
		this.bias = bias;
	}
	
	public void connectNeuron(Neuron neuron, double weight)
	{
		Connection connection=new Connection(weight, neuron);
		connections.add(connection);
	}

	private static double sigmoid(double x)
	{
    return 1 / (1 + Math.exp(-x));
  }
	
	/**
	 * Recalculate the (output) value of this neuron based on weights and values of connected neurons.
	 */
	public void recalculateValue()
	{
		if(connections.size()>0)
		{
			double sum=0;
			for(Connection connection:connections)
			{
				Neuron connectedTo=connection.getConnectedTo();
				double weight=connection.getWeight();
				connectedTo.recalculateValue();
				sum+=connectedTo.getValue()*weight;
			}
			setValue(sum);
//			setValue(sigmoid(sum+bias));
		}
	}

	public List<Neuron> getConnectedNeurons()
	{
		List<Neuron> neurons=new ArrayList<Neuron>();
		if(connections.size()>0)
		{
			for(Connection connection:connections)
			{
				Neuron connectedTo=connection.getConnectedTo();
				neurons.add(connectedTo);
			}
		}
		return neurons;
	}

	public List<Connection> getConnections()
	{
		return connections;
	}

	public void setConnections(List<Connection> connections)
	{
		this.connections = connections;
	}
	
	/**
	 * Update one (randomly selected) of the connected weights randomly
	 */
	public void updateWeight()
	{
		int updateConnectionNo=rand.nextInt(connections.size());
		lastUpdatedConnection=connections.get(updateConnectionNo);
		lastUpdatedConnection.updateWeight();
	}
	
	public void keepWeightUpdate()
	{
		if(lastUpdatedConnection!=null)
		{
//			lastUpdatedConnection.setDeltaWeight(lastUpdatedConnection.getDeltaWeight()*2);
		}
	}

	public void undoWeightUpdate()
	{
		if(lastUpdatedConnection!=null)
		{
			lastUpdatedConnection.undoUpdateWeight();
//			lastUpdatedConnection.setDeltaWeight(lastUpdatedConnection.getDeltaWeight()*0.5);
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
