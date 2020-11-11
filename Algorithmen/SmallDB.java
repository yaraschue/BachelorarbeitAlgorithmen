import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;

/**
* Implementing the algorithm Small Database Mechanism, presented in the
* publication "The Algorithmic Foundations of Differential Privacy" by Cynthia Dwork
* and Aaron Roth in  2014.
*
* @author Yara Schuett
*/

public class SmallDB{
		
	// variable defining the (epsilon, delta) differential privacy
	private double epsilon;
	
	// size of query class depending on possible queries on databases
	
	private int sizeQueryClass;
	
	// variable alpha, the desired approximation accuracy
	private double alpha;
	
	private int numLines;
	
	private int numQI;

	private int m;
	
	private double exp;
	
	private int amount;
	
	private int threshold;
	
	public ArrayList<ArrayList<double[]>> clusters = new ArrayList<ArrayList<double[]>>();
	
	private int size;
	
	private int numSections;
	
	private String path;
	
	/**
	* Constructor where all variable parameters for the algorithms are set. Also the path where the synthetic dabase will be stored is set.
	*
	* @param alpha The approximation accuracy
	* @param epsilon The parameter of (epsilon,delta)-DP
	* @param numSections The amount of sections for the queries which influences the utility-function
	* @param m The threshold for deciding whether a found cluster is relevant
	* @param exp The parameter in exponential function for deciding whether a cluster lower than m still is relevant
	* @param amount The amount of synthetic databases created for the range
	* @param numQI the number of QIs in the given database
	*/
	public SmallDB(double alpha, double epsilon, int numSections, int m, double exp, int amount, int numQI){
		this.epsilon = epsilon;
		this.alpha = alpha;
		this.numQI = numQI;
		this.numSections = numSections;
		this.m = m;
		this.exp = exp;
		
		this. path = "../SyntheticData/SmallDB/SynthSmallDB2.csv";
		this.sizeQueryClass = numQI * numSections;
		this.amount = amount;
	}
	
	/**
	* Method that implements the idea of SmallDB. Setting the parameters for the algorithm exponential mechanism.
	*
	* @param data The original data a synthetic dataset needs to be created
	*
	* @return The synthetic dataset returned by the exponential mechanism with the parameter like smallDB
	*/
	public double[][] smallDB(double[][] data){
		
		// calculating the size of the databases like defined in SmallDB
		size = (int) (Math.log(sizeQueryClass)/(alpha * alpha));
		
		// setting the range for the algorithm
		ArrayList<double[][]> range = buildRange(data, size);
		
		// call exponential mechanism with the defined parameters
		double[][] anonymizedData = exponentialMechanism(data, range);
		
		// output the synthetic dataset to a csv file
		writeData(anonymizedData);
		
		return anonymizedData;
	}
	
	/**
	* Creating databases for the range. First step is to cluster the given data.
	* In the second step from a partition of the clusters datapoints are produced.
	* The amount of datasets we have to produce is defined in the constructor.
	* 
	* @param data The original dataset
	* @param size The size of the synthetic datasets.
	* 
	* @return The range including all database with a certain amount of elements.
	*
	*/
	public ArrayList<double[][]> buildRange(double[][] data, int size){
		
		// storing the elements in data in an ArrayList
		ArrayList<double[]> dataList = new ArrayList<double[]>();
		for(int i = 0; i < data.length; i++){
			dataList.add(data[i]);
		}
		
		// Call an algorithm to cluster the given dataset
		IndependentAnonym anonym = new IndependentAnonym(dataList);
		
		// get the anonymized data and store it for the next step
		ArrayList<double[]> anonymizedData = anonym.anonymize();
		
		// recursively sort all elements in all dimensions and put same values in one cluster
		sortArrayList(anonymizedData, 0);
		
		// initialize the cluster
		ArrayList<double[][]> range = new ArrayList<double[][]>();
		
		// create as much datasets as defined and add them to the range
		for(int j = 0; j < amount; j++){
			double[][] tempDatabase = createDataset(size);
			range.add(tempDatabase);
		}
		return range;
	}
	
	/**
	* Sorts a given arrayList recursively by sorting it in every step in a new dimension.
	* Therefor the current considered dimension is given. 
	* For all elements with the same value in this dimension call the method again.
	* When it is sorted in all dimensions make a cluster for the leftover elements.
	*
	* @param data The data that needs to be sorted
	* @param dimension The dimension to sort in
	*/
	public void sortArrayList(ArrayList<double[]> data, int dimension){
		
		// check if there are elements left to sort
		if(data.size() > 0){
			
			// check if it was already sorted in every dimension
			if(dimension < numQI - 1){
				
				// sort the elements recording to their value in the considered dimension
				Collections.sort(data, new Comparator<double[]>() {
					@Override
					public int compare(double[] first, double[] second) {
						return Double.compare(first[dimension], second[dimension]);
					}
				});
				
				// temporary list to store elements in with the same value for the considered dimension
				ArrayList<double[]> temp = new ArrayList<double[]>();
				
				// add the first element and go through the rest of the given data
				temp.add(data.get(0));
				for(int i = 1; i < data.size(); i++){
					
					// when the element has the same value in the considered dimension add it to temporary and check the next element
					if(data.get(i)[dimension] == data.get(i-1)[dimension]){
						temp.add(data.get(i));
					}
					
					// if the next value differs from before call the method for all elements in temp restart the temp list
					else{
						sortArrayList(temp, dimension + 1);
						temp.clear();
					}
				}
				
				// for the last elements also call the method
				sortArrayList(temp, dimension + 1);
			
			}
			// when the list is already sorted for each dimension make a cluster with the leftover elements 
			else{
				clusters.add(data);
			}
		}	
	}
	
	/**
	* Creates a dataset with a given size based on the clustering from the step before. 
	* At the beginning the found clusters are partioned into relevant and not relevant clusters.
	* There are two cases: More relevant clusters than the required size or the other way around.
	* Because all elements in one cluster are equal, to create a datapoint from a cluster just pick one
	*
	* @param size The required size for the synthetic dataset
	* @return The synthetic dataset
	*/
	public double[][] createDataset(int size){
		
		// First step: divide in relevant and not relevant clusters
		
		// count the number of clusters that hold more than m elements
		ArrayList<double[]> relevantClusters = new ArrayList<double[]>();
		Random rdm = new Random();
		
		// for every cluster check how many elements the cluster holds
		for(int i = 0; i < clusters.size(); i++){
			if(clusters.get(i).size()>0){
			
				// more than threshold m or draw with probability of exponential function
				if(clusters.get(i).size() >= m || expFunction(clusters.get(i).size(), m) < rdm.nextDouble()){
					relevantClusters.add(clusters.get(i).get(0));
				}
			}
			
		}
		
		// Second step: Create dataset from relevant clusters
		
		double[][] dataset = new double[size][numQI];
		
		// check if more possible elements than the required size of the synthetic dataset
		if(relevantClusters.size() <= size){
			
			// from each relevant cluster create one datapoint
			for(int i = 0; i < relevantClusters.size(); i++){
				dataset[i] = relevantClusters.get(i);
			}
			
			// for the rest of the elements randomly pick one of the relevant clusters
			for(int i = relevantClusters.size(); i < size; i++){
				
				// select random cluster and draw an element from it
				int temp = (int)(Math.random() * relevantClusters.size());
				dataset[i] = relevantClusters.get(temp);
			}
		}
		
		// more relevant clusters than the required size
		else{
			for(int i = 0; i < size; i++){
				
				// draw randomly clusters to create a datapoint from and afterwards remove the cluster from the relevant clusters
				int temp = (int)(Math.random() * relevantClusters.size());
				dataset[i] = relevantClusters.get(temp);
				relevantClusters.remove(temp);
			}
		}
		
		relevantClusters.clear();
		return dataset;
	}
	
	/**
	* Calculates the probability for a cluster with size value to be drawn as relevant in the method smallDB
	* 
	* @param value The size of the considered cluster
	* @param limit The threshold m
	* @return The probability a cluster with size value will be drawn as relevant
	*/
	public double expFunction(int value, int limit){
		// parameter der die WS beeinflusst, dass etwas mit geringerer Größe ausgewählt wird
		return - Math.exp((limit - value)/exp);
	}

	/**
	* The main part of the exponential mechanism that is used by the smallDB.
	* We give the specified parameters towards smallDB and the database we work with
	*
	* @param double[][] database The database we work with
	* @param ArrayList<double[][]> range The range the exponential mechanism chooses a database from
	* 
	* @return ArrayList<double[]> selectedDatabase The database that is selected by the exponential algorithm
	*/
	public double[][] exponentialMechanism(double[][] database, ArrayList<double[][]> range){

		//calculate delta u depending on the chosen/given range
		double deltaU = calculateDeltaU(range);
		
		// array to store the probability to be chosen for each possible database
		double[] probability = new double[range.size()];
		
		Random random = new Random();
		
		/*
		* we calculate the probability for each database to be chosen by the given formula and store it in an array
		* Also we sum up all values to be able to norm the values so we can really handle them as 
		* probabilities.
		*/
		double sum = 0;
		for(int i = 0; i < range.size(); i++){
			double tempProb = calculateProbability(database, range.get(i), deltaU);
			probability[i] = tempProb;
			sum += tempProb;
		}
		
		/*
		* Dividing by the sum of all values to get the mathematical relations between the calculated
		* values and the sum. We overwrite the old values we used as probability because these values 
		* will further be used.
		*/
		for(int i = 0; i < probability.length; i++){
			if(i == 0){
				probability[0] = probability[0]/sum;
			}else{
				probability[i] = probability[i-1] + probability[i]/sum;
			}
		}
		
		// the database we are going to select from the given range considering the calculated probabilities
		double[][] selectedDatabase = new double[range.get(0).length][range.get(0)[0].length];
		
		// getting a random value between 0 and 1
		double prob = random.nextDouble();
		
		// checking which database is chosen by comparing with the randomly generated value 
		for(int i = 0; i < range.size(); i++){
			if(i == 0){
				if(prob < probability[0]){
					selectedDatabase = range.get(0);
					break;
				}
			}
			else if(probability[i-1] < prob && prob < probability[i]){
				selectedDatabase = range.get(i);
				break;
			}
		}
		return selectedDatabase;
		
	}
	
	/**
	* calculate the probability a database is chosen with by using the formula described in the paper
	* 
	* @param double[][] database the given database to work with
	* @param double[][] element the database to calculate the probability of
	* @param double deltaU 
	*
	* @return double probability the probability a specific database from the range is chosen
	*/
	public double calculateProbability(double[][] database, double[][] element, double deltaU){
		return Math.exp((epsilon * utilityFunction(database, element))/2 * deltaU);
	}
	
	/**
	* Calculates the worst case in difference of two databases, a database y and a database x.
	* In this case the query class is defined by queries as: How many elements partially have the attribute a in the range i to j.
	* I and j are concrete values defined by the sections the total range is splitted in.
	*
	* @param double[][] databaseX the given database to work with
	* @param double[][] databaseY the database to measure the utility of
	*
	* @return double worstCase The biggest difference over all queries on two different given database
	*/
	public double utilityFunction(double[][] databaseX, double[][] databaseY){
		// get the ranges for both databases
		double[][] rangeX = calculateRangeDatabase(databaseX);
		double[][] rangeY = calculateRangeDatabase(databaseY);
		
		// get the worst case difference
		double maxValue = 0;
		for(int i = 0; i < sizeQueryClass; i++){
			double tempValue = Math.abs(query(databaseX, i, rangeX) - query(databaseY, i, rangeY));
			if(tempValue > maxValue){
				maxValue = tempValue;
			}
		}
		
		return maxValue * (-1);
	}
	
	/**
	* Gets the range of a database. For  each dimension it stores the min and the max value in an array.
	*
	* @param database The database to calculate the ranges for
	*/
	public double[][] calculateRangeDatabase(double[][] database){
		double[][] range = new double[numSections][2];
		
		// for each section store the min and the max values
		for(int section = 0; section < numSections; section++){
			
			// 2D array with length numQID and for each QID the min and max value
			double[] tempRange = new double[2];
		
			// in the range [-5,5] is the probability close to 1 that an element is in this range
			tempRange[0] = - 5 + 10.0/numSections * section;
			tempRange[1] = - 5 + 10.0/numSections * (section + 1);
			
			range[section] = tempRange;
		}
		
		// return array with min and max values for each qid
		return range;
	}
	
	/**
	* Implementation of the queries we are allowing on our given database.
	* Currently: How many elements have a value as a given attribute in a given section.
	*
	* @param database The considered database.
	* @param indexQuery The index of the query out of the query class that is considered.
	* @param range The range of the values for all dimensions
	*
	* @return proportion Proportion of elements with attribute i in this range
	*/
	public double query(double[][] database, int indexQuery, double[][] range){
		
		// we go through all sections of one QID and than go to the next QID
		
		// calculate the QID the given query is demanding
		int regardedQI = indexQuery / numSections;
		
		// calculate the section of the values in the regarded QID the given query is demanding
		int regardedSection = indexQuery % numSections;
		
		// store the begin and the end of the range
		double min = range[regardedSection][0];
		
		double max = range[regardedSection][1];
		
		// counter for the amount of elements
		int numElements = 0;
		
		// check for each element in the database if it has the value in the regarded QI that is in the regarded section
		for(int k = 0; k < database.length; k++){
			
			// store the current considered entry
			double temp = database[k][regardedQI];
			
			// check if it is the last because then the range is [from, to]
			// otherwise it ist [from, to)
			if(regardedSection == numSections - 1){
				
				//check if the considered value is in the regarded section
				if(temp >= min && temp <= max){
					// increase the amount of elements
					numElements += 1;
				}
			}else{
				//check if the considered value is in the regarded section
				if(temp >=  min && temp < max){
					// increase the amount of elements
					numElements += 1;
				}
			}	
		}
		
		// return proportion of elements with value in the given section
		return (double) numElements/database.length;
	}
	
	/**
	* calculates delta u with the given formula. Therefor it goes overall elements in the range. 
	* It calculates the utility values of a database out of the range with a databaseX and gets the difference
	* to the utility value of the same database out of the range and a database y. It is necessary that database x and database y 
	* differ only in one entry.
	* 
	* @param ArrayList<double[][]> range The range of all possible databases.
	*
	* @return double deltaU
	*/
	public double calculateDeltaU(ArrayList<double[][]> range){
		return 1;
	}
	
	public int getSize(){
		return size;
	}
	
	// von Florians StandardizeDataset modifiziert übernommen
	private void writeData(double[][] data){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			for (int k = 0; k < data.length; k++) {
				for (int j = 0; j < data[0].length; j++) {
					if (j != data[0].length - 1) {
						bw.write(Double.toString(data[k][j]) + ",");
					} else {
						bw.write(Double.toString(data[k][j]) + "\n");
						
					}
				}
			}
			bw.close();
		} catch (IOException e) {
			System.out.println("Oooops!");
		}
	}
	
}