package lnu.mida.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;


public class PreprocessWithSimpleMovingAverage {

	//	final File file = new File("resources.txt");

	public static ArrayList<ArrayList<Double>> quality;
	public static ArrayList<ArrayList<Double>> quality_fairnesses;
	public static ArrayList<ArrayList<Double>> energy;
	public static ArrayList<ArrayList<Double>> energy_fairnesses;
	
	public static int movingAverageFactor = 10;

	public static void main(String[] args) {				
		
		convert("exp_assembly_emergent_1606467599102");
//		convert("02exp_assembly_quality_fair_1606402790567");
//		convert("04exp_assembly_quality_fair_1606402684094");
//		convert("06exp_assembly_quality_fair_1606402716917");
//		convert("08exp_assembly_quality_fair_1606402814832");
//		convert("10exp_assembly_quality_fair_1606402728322");
		
	}

	public static void elaborate(String starts_with) {

		File dir = new File("C:\\Users\\Mirko\\eclipse-workspace\\Decentralized-Learning-Service-Assembly");
		File[] foundFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(starts_with);
			}
		});

		quality = new ArrayList<>(foundFiles.length);
		quality_fairnesses = new ArrayList<>(foundFiles.length);
		energy = new ArrayList<>(foundFiles.length);
		energy_fairnesses = new ArrayList<>(foundFiles.length);
		
//		int cycles = Configuration.getInt("simulation.cycles",1);
		
		int cycles = 600;

		// initialize lists
		for (int i = 0; i < cycles-1; i++) {
			quality.add(new ArrayList<Double>());
			quality_fairnesses.add(new ArrayList<Double>());
			energy.add(new ArrayList<Double>());
			energy_fairnesses.add(new ArrayList<Double>());
		}

		// load lists
		for (File file : foundFiles) {
			System.out.println(file.getName());
			extract(file.getName());
		}

		generateAverage();
	}

	public static void extract(String fileName) {

		try {

			BufferedReader in = new BufferedReader(new FileReader(fileName));

			String str;
			String delims = " ";

			int count = 0;

			// store in array values
			while ((str = in.readLine()) != null) {
				String[] tokens = str.split(delims);

				ArrayList<Double> q = quality.get(count);
				ArrayList<Double> q_f = quality_fairnesses.get(count);
				ArrayList<Double> e = energy.get(count);
				ArrayList<Double> e_f = energy_fairnesses.get(count);

				count++;

				double qualityPoint = Double.valueOf(tokens[1]);
				double qualityFairnessPoint = Double.valueOf(tokens[2]);
				double energyPoint = Double.valueOf(tokens[3]);
				double energyFairnessPoint = Double.valueOf(tokens[4]);

				q.add(qualityPoint);
				q_f.add(qualityFairnessPoint);
				e.add(energyPoint);
				e_f.add(energyFairnessPoint);

			}

			in.close();

		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
		}

	}

	public static void generateAverage() {

		try {

			PrintStream ps = new PrintStream(new FileOutputStream("final.txt"));

			int time = 1;
			for (int i = 0; i < quality.size(); i++) {

				ArrayList<Double> currentQuality = quality.get(time - 1);
				ArrayList<Double> currentQualityFairness = quality_fairnesses.get(time - 1);
				ArrayList<Double> currentEnergy = energy.get(time - 1);
				ArrayList<Double> currentEnergyFairness = energy_fairnesses.get(time - 1);

				double averageQuality = 0;
				// average quality
				for (Double q : currentQuality) {
					averageQuality += q;
				}
				averageQuality /= currentQuality.size();

				double averageQualityFairness = 0;
				// average fairness
				for (Double qf : currentQualityFairness) {
					averageQualityFairness += qf;
				}
				
				double averageEnergy = 0;
				// average fairness
				for (Double e : currentEnergy) {
					averageEnergy += e;
				}
				
				double averageEnergyFairness = 0;
				// average fairness
				for (Double ef : currentEnergy) {
					averageEnergyFairness += ef;
				}
				
				

				ps.println(time + " " + averageQuality + " " + averageQualityFairness+ " " + averageEnergy+ " " + averageEnergyFairness);
				time++;
			}

			ps.close();

		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
		}

	}

	public static void convert(String fileName) {

		try {

			PrintStream ps = new PrintStream(new FileOutputStream(fileName + "_2.txt"));
			BufferedReader in = new BufferedReader(new FileReader(fileName + ".txt"));

			String str;
			String delims = " ";

			double count = 0;

			ArrayList<Double> movingAverageQuality = new ArrayList<>();
			ArrayList<Double> movingAverageQualityFairness = new ArrayList<>();
			ArrayList<Double> movingAverageEnergy = new ArrayList<>();
			ArrayList<Double> movingAverageEnergyFairness = new ArrayList<>();

			// store in array values
			while ((str = in.readLine()) != null) {
				String[] tokens = str.split(delims);

				count++;

				double qualityPoint = Double.valueOf(tokens[1]);
				double qualityFairnessPoint = Double.valueOf(tokens[2]);
				double energyPoint = Double.valueOf(tokens[3]);
				double energyFairnessPoint = Double.valueOf(tokens[4]);

				movingAverageQuality.add(qualityPoint);
				movingAverageQualityFairness.add(qualityFairnessPoint);
				movingAverageEnergy.add(energyPoint);
				movingAverageEnergyFairness.add(energyFairnessPoint);

			}

			int time = 1;
			for (int i = 0; i < movingAverageQuality.size(); i++) {

				double currentAverageQuality = calculateCentralMovingAverage(movingAverageQuality, i, movingAverageFactor);
				double currentAverageQualityFairness = calculateCentralMovingAverage(movingAverageQualityFairness, i, movingAverageFactor);
				double currentAverageEnergy = calculateCentralMovingAverage(movingAverageEnergy, i, movingAverageFactor);
				double currentAverageEnergyFairness = calculateCentralMovingAverage(movingAverageEnergyFairness, i, movingAverageFactor);

				ps.println(time + " " + currentAverageQuality + " " + currentAverageQualityFairness+ " " + currentAverageEnergy + " " + currentAverageEnergyFairness);
				time++;
			}

			ps.close();
			in.close();

		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
		}

	}

	private static double calculateCentralMovingAverage(ArrayList<Double> array, int elemPosition, int window) {

		int arraySize = array.size();

		int elementOnLeftSide = elemPosition;
		int elementOnRightSide = arraySize - elemPosition - 1;

		int maxPossibleElements = Integer.min(elementOnLeftSide, elementOnRightSide);
		int maxShift = Integer.min(maxPossibleElements, window / 2);

		int count = 0;
		double sum = 0;

		//		System.out.print("elem position="+elemPosition+"       ");
		for (int i = elemPosition - maxShift; i < elemPosition + maxShift + 1; i++) {
			sum += array.get(i);
			count++;

			//			System.out.print(i+" ");
		}
		//		System.out.println();

		return sum / count;
	}

}
