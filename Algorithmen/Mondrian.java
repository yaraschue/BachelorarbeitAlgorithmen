import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Comparator;
import java.lang.Double;

/**
* Implementing the multidimensional K-Anonymity algorithm Mondrian 
* out of the paper "Mondrian Multidimensional K-Anonymity" by LeFevre, 
* Kristen and DeWitt, David J. and Ramakrishnan, Raghu published in 2006
* 
* @author Yara Schütt
*/

public class Mondrian{
	
	// we consider every identifier as quasi identifier (safe k-Anonymity)
	private int numQI;
	
	// the parameter of the k-Anonymity
	private int k;
	
	// the anonymized partition created by the algorithm
	private ArrayList<double[]> anoynmizedPartition;
	
	// set of the clusters the algorithm creates
	private ArrayList<ArrayList<double[]>> clusterSet;
	
	/**
	* Constructor that initializes the requirements for the algorithm
	*/
	public Mondrian(int numQI, int k){
		this.numQI = numQI;
		this.k = k;
		anoynmizedPartition = new ArrayList<double[]>();
		clusterSet = new ArrayList<ArrayList<double[]>>();
	}
	
	/**
	* Choose the dimension with the largest width from all the given attributes.
	* 
	* @param partition The given data including all dimensions and all individuals.
	* @return maxDim Dimension with the maximum range of values.
	*/
	public int chooseDimension(ArrayList<double[]> partition){
		// setting max values to a default value
		int maxDim = -1;
		double maxWidth = 0;
		
		/*
		* for each dimension calculate the width and get the maximum of all the widths
		*/
		for(int i = 0; i < partition.get(0).length; i++){
			double width = getWidth(partition, i);
			if(width > maxWidth){
				maxWidth = width;
				maxDim = i;
			}
		}
		return maxDim;
	}
	
	/**
	* Get the width of a dimension by calculating the distance between the highest and the lowest value.
	* 
	* @param partition The given data including all dimensions and all individuals
	* @param dim The dimension to calculate the width of
	* @return width The width of a given dimension of the given data
	*/
	public double getWidth(ArrayList<double[]> partition, int dim){
		
		// temporary array with all values of the given dimension
		double[] valuesWithDim = new double[partition.size()];
		for(int j = 0; j < partition.size(); j++){
			valuesWithDim[j] = partition.get(j)[dim];
		}
		
		// sort the values and return the difference
		Arrays.sort(valuesWithDim);
		return Math.abs(valuesWithDim[0]-valuesWithDim[partition.size()-1]);
	}
	
	/**
	* Calculates the median of all the values in one dimension
	
	* 
	* @param partition The given data including all dimensions and all individuals
	* @param dim the dimension that is evaluated
	* @return splitVal Median of the dimension and the value where the dimension is going to be splitted
	*/
	public double findMedian(ArrayList<double[]> partition, int dim){
		
		// temporary hash set to store the different values in
		ArrayList<Double> sortedValues = new ArrayList<Double>();
		
		for(int i = 0; i < partition.size(); i++){
			sortedValues.add(partition.get(i)[dim]);
		}
		
		// sort the values in an ascending order
		Collections.sort(sortedValues);
		
		// calculate the median using the sorted array and under consideration 
		// of the difference between uneven and even amount of numbers
		if(sortedValues.size() % 2 == 1){
			return sortedValues.get(sortedValues.size() / 2);
		}else{
			// Calculating median if the number of values is even 
			return (sortedValues.get(sortedValues.size() / 2)
			+sortedValues.get((sortedValues.size() - 1) / 2)) / 2;
		}
	}
	
	/**
	* Recursively partition the given data into groups as long it is allowed.
	* Two steps, first clustering the data recursively and if not partitioning is posiible anymore 
	* step two: anonymizing the data.
	*
	* @param data The given data including all dimensions and all individuals
	*/
	public void mondrian(ArrayList<double[]> data){
		
		/*
		* check if it is possible to split the partition again that is by checking if 
		* k-Anonymity is still guaranteed after this cut by checking if the amount of 
		* individuals in the new partitions is k ore more
		*/
		if(data.size() >= 2*k){
			cluster(data);
		}
		// if there are not enough elements left, we anonymize the data
		else{
			anonymize(data);
		}
	}
	
	/**
	* Splits the given data in two clusters so both partitions have the same amount of elements.
	* 
	* @param data The partition of the data that is considered.
	*/
	public void cluster(ArrayList<double[]> data){
		// set the dimension to make the cut in
		int dim = chooseDimension(data);
		if(dim != -1){
			ArrayList<double[]> lhs = new ArrayList<double[]>();
			ArrayList<double[]> rhs = new ArrayList<double[]>();
			
			// split the partition into two partitions(lhs and rhs) so the same amount of elements is in each partition
			Collections.sort(data, new Comparator<double[]>() {
            public int compare(double[] values, double[] otherValues) {
                return new Double(values[dim]).compareTo(new Double(otherValues[dim]));
            }});
			
			for(int i = 0; i < data.size(); i++){
				if(i < data.size()/2){
					lhs.add(data.get(i));
				}else{
					rhs.add(data.get(i));
				}
			}
				
			// use algorithm mondrian recursively for the new partitions
			mondrian(rhs);
			mondrian(lhs);
		}
	}
	
	/**
	* Anoynmize a given partition of the data by assigning each element in the partition the same value
	* 
	* @param data The partition of the data that is considered.
	*/
	public void anonymize(ArrayList<double[]> data){
		// anonymize for all dimensions
		for(int j = 0; j < numQI; j++){
		
			double midValue = findMedian(data, j);
			for(int i = 0; i < data.size(); i++){
				data.get(i)[j] = midValue;
			}
		}
		
		getClusterSet().add(0, new ArrayList<double[]>());
		// returning the anonymized partitions
		for(int i = 0; i < data.size(); i++){
			getAnonymizedPartition().add(data.get(i));
			getClusterSet().get(0).add(data.get(i));
		}
	}
	
	/**
	* Getter for the anonymized data after using mondrian
	
	* @return anoynmizedPartition the anonymized data
	*/
	public ArrayList<double[]> getAnonymizedPartition(){
		return anoynmizedPartition;
	}
	
	/**
	* Getter for the set of clusters
	*
	* @return clusterSet the set of clusters
	*/
	public ArrayList<ArrayList<double[]>> getClusterSet(){
		return clusterSet;
	}
	
}