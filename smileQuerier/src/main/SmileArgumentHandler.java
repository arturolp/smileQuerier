package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import querier.QuerySmile;
import tools.FileTools;

/**
 *
 * @author Arturo Lopez Pineda <arl68@pitt.edu>
 * Date: July 12, 2015
 */
public class SmileArgumentHandler {

	public static String network = "";
	public static String data = "";
	public static String target = "";
	public static String outputFile = "";

	public static void showInfo(){
		System.out.println("Expected commands format: -network model.xdsl -data data.arff [-target last] [-output out.csv]");
		System.out.println("-network file");
		System.out.println("-data file");
		System.out.println("-target name. For example: first, last, variable name, or variable index (starting from 0). Default is: last");
		System.out.println("-output file. Default: same as network file");
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {


		if((args.length < 4)){
			System.out.println("Insuficient arguments: "+ args.length + " provided. Required 4.");
			showInfo();
			System.exit(1);
		}


		//Capture all arguments and check for consistency in their numbers
		for(int i = 0; i < args.length; i++){
			if(args[i].equalsIgnoreCase("-network")){
				if(i+1 < args.length){
					network = args[i+1];
				}
				else{
					System.out.println("-- No argument provided for -network");
					showInfo();
					System.exit(1);
				}
			}
			else if(args[i].equalsIgnoreCase("-data")){
				if(i+1 < args.length){
					data = args[i+1];
				}
				else{
					System.out.println("-- No argument provided for -data");
					showInfo();
					System.exit(1);
				}
			}
			else if(args[i].equalsIgnoreCase("-target")){
				if(i+1 < args.length){
					target = args[i+1];
				}
				else{
					System.out.println("-- No argument provided for -target");
					showInfo();
					System.exit(1);
				}
			}
			else if(args[i].equalsIgnoreCase("-output")){
				if(i+1 < args.length){
					outputFile = args[i+1];
				}
				else{
					System.out.println("-- No argument provided for -output");
					showInfo();
					System.exit(1);
				}
			}
		}

		//Set default values in case they were not provided
		if(network.equals("")){
			System.out.println("-network was not provided.");
			showInfo();
			System.exit(1);
		}
		if(data.equals("")){
			System.out.println("-data was not provided.");
			showInfo();
			System.exit(1);
		}
		if(target.equals("")){
			target = "last";
		}
		if(outputFile.equals("")){
			outputFile = "out.csv";
		}
		
		
		//Runner
		System.out.print("Querying: ");
		QuerySmile qs = new QuerySmile();
		qs.runner(network, data, target, outputFile);

		//Finalize
		//System.out.println("outputFile: "+outputFile);
		System.out.println("----\n");

	}
}
