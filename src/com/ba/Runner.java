package com.ba;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class Runner {

	String CONFIG_DIR;
		
	public Runner() {
		
		org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FitbitSync.class);
		log.setLevel(Level.INFO);

		this.CONFIG_DIR = System.getProperty("user.home") + "/.fitbitexport/";
		log.debug("configdir:" + CONFIG_DIR);
	}


	public void run(String args[]) {
		Fitbit fit = new Fitbit(CONFIG_DIR);
		CmdLineParser parser = new CmdLineParser(fit);
        try {
                parser.parseArgument(args);
                if(fit.h){
                	parser.printUsage(System.err);
                	System.exit(0);
                }
                
                if (fit.getsync() == true) {
                	FitbitSync fsync = new FitbitSync(CONFIG_DIR);
                	if (fsync.readconfig() !=0)
                		System.exit(-1);
                	fsync.dosync(fit);                	
                } else {
                	if (fit.getAPIcall() == null) {
                		System.err.println("Option -call is required");
                		parser.printUsage(System.err);
                		System.exit(-1);
                	}                		
                	fit.run();                	
                }
                	
        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }		
	}


	public static void main(String[] args) {
		Runner r = new Runner();
		r.run(args);		
	}
}
