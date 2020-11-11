import java.util.ArrayList;

/**
* Class to define a cluster. A cluster has datapoints and an array of ranges.
*
* @author Yara Schütt
*/
public class Cluster{
	
	private ArrayList<double[]> datapoints;
	
	private int amount = 0;
	
	private double[][] range;
	
	/**
	* Initializing a cluster, its range needs to be given
	* 
	*@param range The range of the cluster
	*/
	public Cluster(double[][] range){
		this.range = range;
		datapoints = new ArrayList<double[]>();
	}
	
	/**
	* Adding an element to the cluster and increasing the size of the cluster
	*
	* @param element The element to add to the cluster
	*/
	public void addElement(double[] element){
		datapoints.add(element);
		setAmount(getAmount() + 1);
	}
	
	/**
	* Setter for the amount of elements in the cluster
	*
	*@param newAmount The amount to set
	*/
	public void setAmount(int newAmount){
		amount = newAmount;
	}
	
	/**
	* Getter for the amount of elements in the cluster
	*
	*@return The amount  of elements in the cluster
	*/
	public int getAmount(){
		return amount;
	}
	
	/**
	* Getter for the range of the cluster
	*
	* @return The range of the cluster
	*/
	public double[][] getRange(){
		return range;
	}
	
	/**
	* Method to check whether an element is in the range of the cluster.
	*
	* @param element The element to check
	* @return if the element is in the range
	*/
	public boolean isInRange(double[] element){
		boolean inRange = true;
		for(int i = 0; i < element.length; i++){
			inRange = inRange && (range[i][0] < element[i] && element[i] <= range[i][1]);
		}
		return inRange;
	}
	
	/**
	* Getter for the elements in the cluster
	*
	* @return The elements in the cluster
	*/
	public ArrayList<double[]> getElements(){
		return datapoints;
	}
}