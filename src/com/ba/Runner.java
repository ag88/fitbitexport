package com.ba;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class Runner {

	
	public static void main(String[] args) {
		Fitbit fit = new Fitbit();
		CmdLineParser parser = new CmdLineParser(fit);
        try {
                parser.parseArgument(args);
                if(fit.h){
                	parser.printUsage(System.err);
                	System.exit(0);
                }
                
                fit.run();
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }

		
		
		
	}
}
