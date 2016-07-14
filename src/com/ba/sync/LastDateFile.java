package com.ba.sync;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class LastDateFile {

	private Logger mlogger = LoggerFactory.getLogger(LastDateFile.class);
	
	private String configdir;
	private String lastdatefile = "lastdate.xml"; 
		
	public Date lastdate;
	SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd");
	
	public static enum State { INIT, INDATES };
	
	public LastDateFile(String configdir) {
		this.configdir = configdir;
		
		org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LastDateFile.class);
		log.setLevel(Level.INFO);
		
		lastdatefile = configdir.concat(lastdatefile);
		
		mlogger.debug("lastdate file:".concat(lastdatefile));
	}

	public Date getLastdate() {
		if (lastdate == null) {
			int ret = parse();
			if (ret != 0 || lastdate == null)
				lastdate = new Date(); //today!
		} 
		
		return lastdate;
	}

	public void setLastdate(Date lastdate) {
		this.lastdate = lastdate;
	}

	/**
	 * @return 
	 * 0 success
	 * -1 error
	 * -2 config dir not found
	 */	
	public int writedatefile() {
		if (configdir == null || lastdatefile == null)
			return -1;
		
		File d = new File(configdir);
		if(!d.exists())
			return -2;
		
		try {
			mlogger.debug("writing date file:" + lastdatefile);
			
			PrintWriter writer = new PrintWriter(lastdatefile);
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println("<dates>");
			String slastdate = "date=\"" + mdformat.format(lastdate) + "\"";
			writer.println("\t<lastdate " + slastdate + " />");
			writer.println("</dates>");
			
			writer.flush();
			writer.close();			
		} catch (IOException e) {
			mlogger.error("error writing lastdate file",e);
			return -1;
		}		
		
		return 0;
	}
	
	
	/**
	 * @return 
	 * 0 success
	 * -1 error
	 * -2 file not found
	 */
	public int parse() {			
		
		File f = new File(lastdatefile);
		if (!f.exists()) { // file not found
			mlogger.info("no last date file found");
			return -2;
		}
		
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    //spf.setNamespaceAware(false);
	    try {
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new SAXMapHandler());
			xmlReader.parse(convertToFileURL(lastdatefile));
		} catch (ParserConfigurationException e) {
			mlogger.error("LastDateFile",e);			
			//e.printStackTrace();
			return -1;
		} catch (SAXException e) {
			mlogger.error("LastDateFile",e);
			return -1;
		} catch (IOException e) {
			mlogger.error("LastDateFile",e);
			return -1;
		}		
		
		return 0;
	}
	
	
	public class SAXMapHandler extends DefaultHandler {
	    
		private State state;
		
	    public SAXMapHandler() {
		}
	    	    

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {

			String element;
			if (localName.equals(""))
				element = qName;
			else
				element = localName;
			
			mlogger.debug("Element:" + element);
			switch(element) {
			case "dates":
				state = State.INDATES;
				break;
			case "lastdate":
				if (state != State.INDATES) {
					mlogger.warn("last date file non-conformal format");
				}
				String sdate = attributes.getValue("date");
				try {
					lastdate = mdformat.parse(sdate);
					mlogger.debug("lastdate:".concat(sdate));
				} catch (ParseException e) {
					mlogger.error("invalid last date", e);
					lastdate = null;
				} catch (NullPointerException ie) {
					mlogger.error("invalid last date (null)", ie);
					lastdate = null;
				}
				break;
			default:
				break;
			}					
		}


		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String element;
			if (localName.equals(""))
				element = qName;
			else
				element = localName;
			
			mlogger.debug("Element:" + element);

			switch (element) {
			case "lastdate":
				break;
			case "dates":
				state = State.INIT;				
				break;
			default:
				break;
			}
			
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
		}

		public void startDocument() throws SAXException {
	        state = State.INIT;
	    }

	    public void endDocument() throws SAXException {
	 
	    }
	}
	
    private static String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }


	
}
