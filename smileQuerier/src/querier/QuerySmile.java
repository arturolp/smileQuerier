package querier;

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
import tools.Tools;
import tools.FileTools;

/**
 *
 * @author arturolp
 * Date: March 12, 2012
 */
public class QuerySmile {


	private Instances data;
	//Internal use
	public Network net;
	public HashMap<String, Boolean> nodes;

	public QuerySmile(){

	} //constructor


	public String computePosterior(Instance inst, String target) {
		String output = "";

		// Getting the posterior for each target outcome
		// Updating the network:
		net.updateBeliefs();

		//String[] aSuccessOutcomeIds = net.getOutcomeIds(target);
		//Tools.print(aSuccessOutcomeIds);
		double[] aValues = net.getNodeValue(target);
		for(int i = 0; i < aValues.length; i++){
			output += aValues[i];
			if((i+1)<aValues.length){
				output += ",";
			}
		}

		return output;
	}

	public String computePredictions(String target) {
		String predictions = "value_in_test_dataset,";
		int targetIndex = getTargetIndex(target);

		String[] aOutcomeIds = net.getOutcomeIds(target);
		for(int t = 0; t < aOutcomeIds.length; t++){
			predictions += "predicted_" + aOutcomeIds[t];
			if((t+1) < aOutcomeIds.length){
				predictions += ", ";
			}
		}
		predictions += "\n";

		for (int i = 0; i < data.numInstances(); i++) {
			//System.out.print("Case: "+(i+1));
			setEvidence(data.instance(i), target);

			String prediction = computePosterior(data.instance(i), target);

			String real = data.instance(i).stringValue(targetIndex);
			predictions += real+","+ prediction + "\n";
		}
		return predictions;
	}

	private int getTargetIndex(String target) {
		int targetIndex = 0;

		for(int i = 0; i < data.numAttributes(); i++){
			if(data.attribute(i).name().equals(target)){
				targetIndex = i;
				break;
			}
		}

		return targetIndex;
	}


	public void readData(String dataFile) {
		try {
			ArffLoader arff = new ArffLoader();
			File myData = new File(dataFile);
			System.out.println("   data: "+myData.getName()+"");
			arff.setFile(myData.getAbsoluteFile()); 
			data = arff.getDataSet();
			int classIndex = data.numAttributes() - 1;
			data.setClassIndex(classIndex);

		} catch (IOException ex) {
			Logger.getLogger(QuerySmile.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	public void readNetwork(String network) {
		//try{
		net = new Network();
		File myNetwork = new File(network);
		System.out.println("   network: "+myNetwork.getName() + "");
		net.readFile(myNetwork.getAbsolutePath());
		String extension = FileTools.getExtension(myNetwork.getAbsolutePath());
		if(extension.equals("net")){
			//improve the outcome names
			for(int i = 0; i < net.getNodeCount(); i++){
				for(int j = 0; j < net.getOutcomeCount(i); j++){
					String outcome = net.getOutcomeId(i, j);
					outcome = getGenieCanonicalName(outcome);
					net.setOutcomeId(i, j, outcome);
				}
			}
		}
		/*} catch (Exception ex) {
			System.err.println(ex);
			System.exit(1);
		}*/

	}




	public void setEvidence(Instance inst, String target) {
		net.clearAllEvidence();

		// ---- We want to compute P("disease" = T | "gene" = On) ----
		// Introducing the evidence in node:
		for (int i = 0; i < inst.numAttributes(); i++) {
			String varName = inst.attribute(i).name();
			//System.out.print(name+" = ");
			if (!varName.equals(target)) {
				String varValue = getGenieCanonicalName(inst.stringValue(i));

				if (!inst.isMissing(i)) {
					try{
						//System.out.println(""+net.getOutcomeIds(varName)[0]);
						if(hasOutcome(varName, varValue)){
							//System.out.print(varName+" = "+varValue+", ");
							net.setEvidence(varName, varValue);
						}
						//System.out.println(name+", "+ NET.getEvidence(name));
					}
					catch(Exception e){
						//System.out.println("\t"+name+ " >> "+value+" >> "+e.getMessage());
						////System.out.println(""+inst.toString());
					}
				}


			}
		}


	}

	private boolean hasOutcome(String varName, String varValue) {
		boolean hasOutcome = false;

		//System.out.println("varName: "+varName);
		for(int i = 0; i < net.getOutcomeCount(varName); i++){
			String iOutcome = net.getOutcomeId(varName, i);
			//System.out.println("   "+iOutcome + " == "+varValue+" ["+varValue.equals(iOutcome)+"]");
			if(varValue.equals(iOutcome)){
				hasOutcome = true;
				break;
			}
		}

		return hasOutcome;
	}


	private String getGenieCanonicalName(String oldName) {
		String canonical = "";


		if(!oldName.substring(0, 1).matches("\\w")){

			canonical += "x_"+ oldName;

			canonical = canonical.replace(".", "_");
			canonical = canonical.replace("-", "_");
			canonical = canonical.replace("(", "");
			canonical = canonical.replace(")", "");
			canonical = canonical.replace("[", "");
			canonical = canonical.replace("]", "");
			canonical = canonical.replace("\\", "");
			canonical = canonical.replace("'", "");




		}
		else{
			if(oldName.substring(0,2).equals("x_")){

				if(oldName.substring(oldName.length()-1).equals("_")){
					oldName = oldName.substring(0, oldName.length()-1);
				}

				//canonical = canonical.replace("___", "__");

				canonical += oldName.substring(0,2);

				int index = 2;
				boolean bottomNeg = false;

				//does bottom starts with negative?
				if(oldName.substring(index,(index+1)).equals("_")){
					canonical += "_";
					index++;
					bottomNeg = true;
				}
				//either a letter or inf
				if(oldName.substring(index,(index+1)).equals("i")){
					canonical += "inf";
					index += 3;
				}
				else{
					canonical += oldName.charAt(index)+"_";
					index += 2;
					while(oldName.substring(index,(index+1)).matches("\\d")){
						canonical += oldName.charAt(index);
						index++;
					}
				}

				//is upper also negative?
				if(bottomNeg == true && oldName.substring(index,(index+3)).equals("___")){
					canonical += "__";
					index += 3;
				}
				else{
					canonical += "_";
					index++;
				}

		
				if(oldName.substring(index,(index+1)).equals("i")){
					canonical += "inf";
				}
				else{
					canonical += oldName.charAt(index)+"_";
					index += 2;
					
					while(oldName.substring(index,(index+1)).matches("\\d")){
						canonical += oldName.charAt(index);
						index++;
						if(index >= oldName.length()){
							break;
						}
					}
				}
			}
			else{ //anything else is not in Weka->Genie Format
				canonical = oldName;
			}
		}
		//System.out.println("oldname: "+oldName+", canonical: "+canonical);
		return canonical;
	}

	public void writePredictions(String predictions, String outputFile) {
		// TODO Auto-generated method stub
		try {
			FileWriter output = new FileWriter(outputFile);
			output.write(predictions);
			output.close();

			System.out.println("   output: "+outputFile+"");

			System.out.println("   [done]");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void runner(String network, String dataFile, String target, String outputFile) {

		//-----------------------
		//1. Read network
		readNetwork(network);

		//-----------------------
		//2. Read data file
		readData(dataFile);

		//-----------------------
		//3. Obtain predictions
		String targetName = "";
		if(target.equals("last")){
			int classIndex = data.numAttributes() - 1;
			targetName = data.attribute(classIndex).name();
		}
		else if(target.equals("first")){
			targetName = data.attribute(0).name();
		}
		else if(Tools.isNumeric(target)){
			int classIndex = Integer.parseInt(target);
			targetName = data.attribute(classIndex).name();
		}
		else{
			targetName = target;
		}
		System.out.println("   target: "+targetName+ "");
		String predictions = computePredictions(targetName);
		//System.out.println(predictions);

		//-----------------------
		//4. Read network
		writePredictions(predictions, outputFile);

	}


}
