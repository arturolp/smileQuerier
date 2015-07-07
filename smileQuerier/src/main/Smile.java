package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import smile.Network;

/**
 *
 * @author arturolp
 * Date: March 12, 2012
 */
public class Smile {

	
	//From Arguments
	public static String PATH="";
	public static String TARGET="";
	public static String InputNetwork="";
	public static String InputData="";
	public static String OutputFile="";
	
	
	 //Internal use
	public static Network NET;
	public static String CLASS;
	private static Instances DATA;
	public static HashMap<String, Boolean> NODES;
	public static int verbose = 1;
	
	public Smile(){
		
	} //constructor
	
	public Smile(String network, String my_data){
		
		InputNetwork = network;
		InputData = my_data;
		//Create Network
				NET = new Network();
				//NET.readFile(PATH + "results/EBMC_DS1-acute-32umls-config2.xdsl");
				NET.readFile(network);
				NODES = new HashMap<String, Boolean>();
				String[] s = NET.getAllNodeIds();

				for(int i = 0; i < s.length; i++) {
					NODES.put(s[i], false);
				}

				loadData(InputData);

				String output = "";
				for (int i = 0; i < DATA.numInstances(); i++) {
				//for (int i = 470; i < 472; i++) {
					////System.out.println("Case:"+(i+1));
					setEvidence(DATA.instance(i));

					output = output + computePosterior(DATA.instance(i)) + "\n";
					////System.out.println("-->"+computePosterior(DATA.instance(i)));
					////System.out.println();
					
				}
	} //constructor

	public static void loadData(String path) {
		try {
			ArffLoader arff = new ArffLoader();
			arff.setFile(new File(path)); 
			DATA = arff.getDataSet();
			int classIndex = DATA.numAttributes() - 1;
			DATA.setClassIndex(classIndex);

		} catch (IOException ex) {
			Logger.getLogger(Smile.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public static void setEvidence(Instance inst) {
		NET.clearAllEvidence();

		
		
		//Object[] names =  NODES.keySet().toArray();
		//for (int i = 0; i < names.length; i++) {
			//  NODES.put(names[i].toString(), Boolean.FALSE);
			//}


		// ---- We want to compute P("Success" = Failure | "Forecast" = Good) ----
		// Introducing the evidence in node "Forecast":
		for (int i = 0; i < inst.numAttributes(); i++) {
			String name = inst.attribute(i).name();
			if (NODES.containsKey(name)) {
				if (!name.equals(TARGET)) {
					String value = inst.stringValue(i);
					//double val = inst.value(i);
					boolean b = inst.isMissing(i);

					if (!b) {
						////System.out.println("--"+value+"--"+val+"--"+b);
						//System.out.println(name+", "+value);
						try{
							////System.out.println(""+NET.getOutcomeIds(name)[0]);

							NET.setEvidence(name, value);
							////System.out.println(name+", "+ NET.getEvidence(name));
						}
						catch(Exception e){
							//System.out.println("\t"+name+ " >> "+value+" >> "+e.getMessage());
							////System.out.println(""+inst.toString());
						}
					}

				}
			}
		}
		
		
		////System.out.println("Influence = "+NET.getInfluenceDiagramAlgorithm());
		
		
		// Updating the network:
		NET.updateBeliefs();
		
		String[] aSuccessOutcomeIds = NET.getOutcomeIds(TARGET);
		//util.Tools.print(aSuccessOutcomeIds);
		double[] aValues = NET.getNodeValue(TARGET);
		//util.Tools.print(aValues);
		//System.out.println("P(Influenza="+aSuccessOutcomeIds[0]+"|evidence) = "+aValues[0]);
		//System.out.println("P(Influenza="+aSuccessOutcomeIds[1]+"|evidence) = "+aValues[1]);
		
		
		//Diagnosis network
		//DiagNetwork diagNet = new DiagNetwork(NET);
		//diagNet.setDSep(true);
		//int fault = diagNet.findMostLikelyFault();
		////System.out.println("fault = "+ fault);
		//sysodiagNet.getMultiFaultAlgorithm();
		
		//int faultIndex = diagNet.findMostLikelyFault();
		//diagNet.setPursuedFault(faultIndex);

		//DiagResults diagResult = diagNet.update();
		
		
	}

	public static String computePosterior(Instance inst) {
		String output = "";
		
		// Getting the handle of the node "Success":
		//NET.getNode(TARGET);

		// Getting the index of the "Failure" outcome:
		String[] aSuccessOutcomeIds = NET.getOutcomeIds(TARGET);
		int outcomeIndex=0;
		for (outcomeIndex = 0; outcomeIndex < aSuccessOutcomeIds.length; outcomeIndex++) {
			if (CLASS.equals(aSuccessOutcomeIds[outcomeIndex])) {
				break;
			}
		}
		
		//util.Tools.print(aSuccessOutcomeIds);
		////System.out.println(outcomeIndex);

		// Getting the value of the probability:
		////System.out.println("-D"+ NET.getNodeValue(TARGET));
		double[] aValues = NET.getNodeValue(TARGET);
		
		//util.Tools.print(aValues);
		
		double P_SuccIsFailGivenForeIsGood = aValues[outcomeIndex];

		if(verbose > 1){
			//System.out.print("P(\"" + TARGET + "\" = 1 |");
			Object[] names =  NODES.keySet().toArray();
			for (int i = 0; i < names.length; i++) {
				Boolean b = NODES.get(names[i].toString()).booleanValue();
				if (b == true) {
					int evidence = NET.getEvidence(names[i].toString());
					//System.out.print(" \""+names[i].toString() +"\" = " + evidence + ","); 
				}
			}
			//System.out.println(") = " + (P_SuccIsFailGivenForeIsGood));
		}

		if(verbose == 1){
			double d = 1-inst.value(inst.classIndex());

			String v = "";
			if(d==1){
				v = "1";
			}
			else if(d==0){
				v = "0";
			}
			////System.out.println(v+" " + (P_SuccIsFailGivenForeIsGood));
			output = v + " " + P_SuccIsFailGivenForeIsGood;
		}
		
		return output;

	}

	public static void writeOutput(String stream) throws IOException {
		
		FileWriter output = new FileWriter(OutputFile);
		
		output.write(stream);
		
		output.close();

		
		
	}

	public static void runner() {

		//Create Network
		NET = new Network();
		//NET.readFile(PATH + "results/EBMC_DS1-acute-32umls-config2.xdsl");
		NET.readFile(InputNetwork);
		NODES = new HashMap<String, Boolean>();
		String[] s = NET.getAllNodeIds();

		for(int i = 0; i < s.length; i++) {
			NODES.put(s[i], false);
		}

		loadData(InputData);

		String output = "";
		for (int i = 0; i < DATA.numInstances(); i++) {
		//for (int i = 470; i < 472; i++) {
			////System.out.println("Case:"+(i+1));
			setEvidence(DATA.instance(i));

			output = output + computePosterior(DATA.instance(i)) + "\n";
			////System.out.println("-->"+computePosterior(DATA.instance(i)));
			////System.out.println();
			
		}

		try {
			writeOutput(output);
			System.out.println("[done]");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		
		if((args.length != 6) && (args.length != 8) && (args.length != 10)){
			System.err.println("Incorrect arguments.");
			System.err.println("Expected commands format: -network model.xdsl -data data.arff -target BC0021400 -class T [-output results/]");
			System.err.println("-network file");
			System.err.println("-data file");
			System.err.println("-target name. For example: BC0021400");
			System.err.println("-class name. For example: T");
			System.err.println("-output file. Default: same as network file");
			System.exit(1);
		}


		for(int i = 0; i < args.length; i++){
			if(args[i].equalsIgnoreCase("-network")){
				InputNetwork = args[(i+1)];
			}
			else if(args[i].equalsIgnoreCase("-data")){
				InputData = args[(i+1)];
			}
			else if(args[i].equalsIgnoreCase("-target")){
					TARGET = args[(i+1)];
			}
			else if(args[i].equalsIgnoreCase("-class")){
				CLASS = args[(i+1)];
			}
			else if(args[i].equalsIgnoreCase("-output")){
				OutputFile = args[(i+1)];
			}
		}
		
		if(OutputFile.equals("")){
			OutputFile = tools.Util.replaceFileNameSuffix(InputNetwork, "txt");
		}
		
		runner();

	}
}
