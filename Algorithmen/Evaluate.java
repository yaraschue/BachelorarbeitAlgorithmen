import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
* Generates a synthetic database based on another given database with one of two possible algorithms.
* Afterwards evaluates the synthetic database in comparison to the original database by calling an utility function. 
*
* @author Yara Schuett
*/
public class Evaluate{
	
	private static double[][] synthDatabase;
	
	private static double[][] origDatabase;
	
	private static int sizeQueryClass;
	
	private static int numQI;
	
	private static SDGS sdgs;
	
	private static int numLinesSynth;
	
	private static int numLinesOrig;
	
	// necessary for using filehandling 
	private static int numCA = 0;
	
	private double estimatedValue;
	
	// change to decide which algorithm to test
	private static boolean sdgsSelected = false;
	// set to the directiory for the chosen algorithm
	// Quelle für synthDatabase
	private static String pathSynth;
	
	// modify when different dataset
	// Quelle für synthDatabase
	private static String pathOrig;
	
	private static ArrayList<double[][]> databases;
	private static ArrayList<int[]> params;
	
	private static String path;
	
	public static void main(String[] args){
		
		if(sdgsSelected){
			path = "../Ergebnisse/SDGS/Mondrian/testStatic.csv";
			//path = "../Ergebnisse/SDGS/IndependentAnonym/16SDGS2.csv";
		}else{
			path = "../Ergebnisse/SmallDB/testStatic.csv";
		}
		String daten = "";
		databases = new ArrayList<double[][]>();
		
		params = new ArrayList<int[]>();
		if(sdgsSelected){
			try {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(path, true)));
				bw.write("k;beta;" + "CensusCov" + ";"+ "Census1;Census2;" + "Cloud1Cov;Cloud11;CLoud12;" + "Cloud2Cov;Cloud21;Cloud22;EIACov;EIA1;EIA2; TARRAGONACov; TARRAGONA1;TARRAGONA2; UCICov; UCI1;UCI2" + "\n");
				bw.close();
			} catch (IOException e) {
			System.out.println("Oooops!");
			}
		}else{
			try {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(path, true)));
				bw.write(" alpha;epsilon; numSections;m; exp;Amount DB;" + "CensusCov" + ";"+ "Census1;Census2;" + "Cloud1Cov;Cloud11;CLoud12;" + "Cloud2Cov;Cloud21;Cloud22;EIACov;EIA1;EIA2; TARRAGONACov; TARRAGONA1;TARRAGONA2; UCICov; UCI1;UCI2" + "\n");
				bw.close();
			} catch (IOException e) {
			System.out.println("Oooops!");
			}

		}
		
		
		pathOrig = "../StandardizedData/S_Census.csv";
		numQI = 13;
		numLinesOrig = 1080;
		getOrigDatabase(pathOrig, numQI, numLinesOrig);
		
		pathOrig = "../StandardizedData/S_Cloud1.csv";
		numQI = 10;
		numLinesOrig = 1024;
		getOrigDatabase(pathOrig, numQI, numLinesOrig);
		
		pathOrig = "../StandardizedData/S_Cloud2.csv";
		numQI = 10;
		numLinesOrig = 1024;
		getOrigDatabase(pathOrig, numQI, numLinesOrig);
		
		pathOrig = "../StandardizedData/S_EIA.csv";
		numQI = 11;
		numLinesOrig = 4092;
		getOrigDatabase(pathOrig, numQI, numLinesOrig);
		
		pathOrig = "../StandardizedData/S_TARRAGONA.csv";
		numQI = 13;
		numLinesOrig = 834;
		getOrigDatabase(pathOrig, numQI, numLinesOrig);
		
		pathOrig = "../StandardizedData/S_UCI.csv";
		numQI = 3;
		numLinesOrig = 48842;
		getOrigDatabase(pathOrig, numQI, numLinesOrig);
		
		// SDGS
		if(sdgsSelected){
			for(int k = 2; k <= 10; k++){
			
				for(int i = 1; i < 10; i++){
					double beta = (double)i * 0.1;
					writeParamsSDGS(k, beta);
				
					for(int j = 0; j < 6; j++){
				
						switch(j){
						case 0:
							daten = "Census";
							break;
						case 1:
							daten = "Cloud1";
						break;
						case 2:
							daten = "Cloud2";
							break;
						case 3:
							daten = "EIA";
						break;
						case 4:
							daten = "TARRAGONA";
						break;
						case 5:
							daten = "UCI";
						break;
						}
					
					pathSynth = "../SyntheticData/SDGS/SynthSDGS.csv";
					// modify params
					sdgs = new SDGS(beta,k, params.get(j)[0]);
			
					// execute algorithm sdgs
					
					// mondrian
					sdgs.algorithmSDGS(databases.get(j));

					// independent anonymization
					//sdgs.algorithmSDGSIndependent(databases.get(j));

					// store size of new database for the next step: loading database
					numLinesSynth = sdgs.getSize();
				
					filehandling.Dataset ds2 = new filehandling.Dataset(pathSynth, params.get(j)[0], numCA, numLinesSynth);
					double[][] synthDatabase = ds2.getDataDouble();
		
					UtilityCov utilCov = new UtilityCov(databases.get(j), synthDatabase);
					// calculate utility for the given original database and the generated synthetic database
					double utilityValueCov = utilCov.utility();
				
					Utility1 util1 = new Utility1(databases.get(j), synthDatabase);
					// calculate utility for the given original database and the generated synthetic database
					double utilityValue1 = util1.utility();
					
					Utility2 util2 = new Utility2(databases.get(j), synthDatabase);
					// calculate utility for the given original database and the generated synthetic database
					double utilityValue2 = util2.utility();
					
					writeData(j, utilityValueCov, utilityValue1, utilityValue2);
				}
				
				}
			}
		}
		// SmallDB
		else{
			for(double exp = -5; exp < 0; exp+= 1){
			for(int amount = 5; amount <= 205; amount += 50){
			for(int numSections = 5; numSections < 100; numSections += 20){
				for(int m = 2; m <= 8; m += 2){
				for(double alpha = 0.1;alpha <=0.2; alpha += 0.05){
							
				for(double epsilon = 0.0001; epsilon <= 0.1; epsilon = epsilon * 10){
						writeParamsSmallDB(alpha, epsilon, numSections, m, exp, amount);
								
								for(int j = 0; j < 6; j++){
				
									switch(j){
										case 0:
											daten = "Census";
											break;
										case 1:
											daten = "Cloud1";
											break;
										case 2:
											daten = "Cloud2";
											break;
										case 3:
											daten = "EIA";
											break;
										case 4:
											daten = "TARRAGONA";
											break;
										case 5:
											daten = "UCI";
											break;
									}
								
								pathSynth = "../SyntheticData/SmallDB/SynthSmallDB2.csv";
								// execute algorithm smallDB
								SmallDB smDB = new SmallDB(alpha, epsilon, numSections, m, exp, amount, params.get(j)[0]);
								smDB.smallDB(databases.get(j));
			
								// store size of new database for the next step: loading database
								numLinesSynth = smDB.getSize();
								
								filehandling.Dataset ds2 = new filehandling.Dataset(pathSynth, params.get(j)[0], numCA, numLinesSynth);
								double[][] synthDatabase = ds2.getDataDouble();
		
								UtilityCov utilCov = new UtilityCov(databases.get(j), synthDatabase);
								// calculate utility for the given original database and the generated synthetic database
								double utilityValueCov = utilCov.utility();
				
								Utility1 util1 = new Utility1(databases.get(j), synthDatabase);
								// calculate utility for the given original database and the generated synthetic database
								double utilityValue1 = util1.utility();
								
								Utility2 util2 = new Utility2(databases.get(j), synthDatabase);
								// calculate utility for the given original database and the generated synthetic database
								double utilityValue2 = util2.utility();
					
								writeData(j, utilityValueCov, utilityValue1, utilityValue2);
								}
								}
		
							}
						
						}
					}
				}
			}
		}
	}
	
	
	private static void getOrigDatabase(String pathOrig, int numQI, int numLinesOrig){
		filehandling.Dataset ds = new filehandling.Dataset(pathOrig, numQI, numCA, numLinesOrig);
		double[][] origDatabase = ds.getDataDouble();
		
		databases.add(origDatabase);
		
		params.add(new int[]{numQI, numLinesOrig});
	}
	
	private static void writeData(int column, double utilityValueCov, double utilityValue1, double utilityValue2){
		try {
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(path, true)));
			
			bw.write(+ utilityValueCov + ";"+ utilityValue1 + ";" + utilityValue2);
			
			if(column == 5){
				bw.write("\n");
			}else{
				bw.write(";");
			}
			bw.close();
		} catch (IOException e) {
			System.out.println("Oooops!");
		}
	}
	
	private static void writeParamsSDGS( int k, double beta){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true)));
			bw.write( k + "; " + beta + ";");
			bw.close();
		} catch (IOException e) {
			System.out.println("Oooops!");
		}
	}
	private static void writeParamsSmallDB(double alpha, double epsilon, int numSections, int m, double exp, int amount){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true)));
			bw.write(alpha + ";" + epsilon + ";" + numSections + ";" + m + ";" + exp + "; " + amount + ";");
			bw.close();
		} catch (IOException e) {
			System.out.println("Oooops!");
		}
	}
	
}

