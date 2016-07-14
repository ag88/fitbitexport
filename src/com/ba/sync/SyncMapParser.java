package com.ba.sync;

import javax.xml.parsers.*;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.ba.FitbitSync;

import java.util.*;
import java.io.*;

public class SyncMapParser {
	
	private Logger mlogger = LoggerFactory.getLogger(SyncMapParser.class);
	
	String mapfile;
	public ArrayList<APIMapEntry> mapentries = new ArrayList<APIMapEntry>();
	public String outdir;
	
	public static enum State { INIT, INMAP, INENTRY };
	
	public SyncMapParser(String mapfile) {
		
		org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SyncMapParser.class);
		log.setLevel(Level.INFO);

		this.outdir = "";
		this.mapfile = mapfile;
	}
	
	public int parse() {
		
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    //spf.setNamespaceAware(false);
	    try {
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new SAXMapHandler());
			xmlReader.parse(convertToFileURL(mapfile));
		} catch (ParserConfigurationException e) {
			mlogger.error("SyncMapParser",e);			
			//e.printStackTrace();
			return -1;
		} catch (SAXException e) {
			mlogger.error("SyncMapParser",e);
			return -1;
		} catch (IOException e) {
			mlogger.error("SyncMapParser",e);
			return -1;
		}		
		
		return 0;
	}
	
	public String getoutdir() {
		return outdir;
	}
	
	public List<APIMapEntry> getmapentries() {
		Collections.sort(mapentries);
		return mapentries;
	}
	
	public class SAXMapHandler extends DefaultHandler {
	    
		private State state;
		private APIMapEntry entry;
		
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
			case "map":
				outdir = attributes.getValue("outdir");
				state = State.INMAP;
				mlogger.debug("outdir:" + outdir);				
				break;
			case "entry":
				entry = new APIMapEntry();
				entry.setSeq(Integer.parseInt(attributes.getValue("seq")));
				entry.setName(attributes.getValue("name"));
				state = State.INENTRY;
				mlogger.debug("seq:" + attributes.getValue("seq"));
				mlogger.debug("name:" + attributes.getValue("name"));
				break;
			case "apicall":
				if (state == State.INENTRY && entry != null) {
					entry.setApicall(attributes.getValue("url"));
					mlogger.debug("url:" + attributes.getValue("url"));
				}
				break;
			case "outfile":
				if (state == State.INENTRY && entry != null) {
					entry.setOutname(attributes.getValue("name"));
					mlogger.debug("name:" + attributes.getValue("name"));
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
			case "entry":
				if (state == State.INENTRY && entry != null) {
					mapentries.add(entry);
					entry = null;
				}
				state = State.INMAP;
				break;
			case "map":
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
	        mapentries = new ArrayList<APIMapEntry>();
	        outdir = "";
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
