package com.ba;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ba.sync.APIMapEntry;
import com.ba.sync.LastDateFile;
import com.ba.sync.SyncMapParser;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FitbitSync {

	private Logger mlogger = LoggerFactory.getLogger(FitbitSync.class);
			
	private String configdir;
	public final String MAPFILE = "map.xml";
	private String mapfile;	
	
	public String outdir;
	public List<APIMapEntry> entries;
	
	public FitbitSync(String configdir) {
		this.configdir = configdir;
		mapfile = configdir.concat(MAPFILE);
		org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FitbitSync.class);
		log.setLevel(Level.INFO);
		mlogger.debug("Mapfile:".concat(mapfile));
		
	}
	
	public int readconfig() {
		SyncMapParser parser = new SyncMapParser(mapfile);
		int ret = parser.parse();
		if (ret==0) {
			entries = parser.getmapentries();
			outdir = parser.getoutdir();
			
			mlogger.debug("Output dir:" + outdir);
			Iterator<APIMapEntry> iter = entries.iterator();
			while (iter.hasNext()) {
				APIMapEntry entry = iter.next();
				mlogger.debug(entry.toString());
			}			
		}
		return ret;
	}
	
	public int dosync(Fitbit fit) {
		if (entries == null || outdir == null || configdir == null) return -1;			
		
		Date today, lastdate, syncdate;
		//get the lastdate
		LastDateFile lastdatefile = new LastDateFile(configdir);
		today = dateonly(new Date());
		lastdate = dateonly(lastdatefile.getLastdate());
		syncdate = lastdate;			
		
		mlogger.debug("today:" + today.toString());
		mlogger.debug("last date:" + lastdate.toString());
		
		while (syncdate.before(today) || syncdate.equals(today)) {
			
			mlogger.debug("sync date:" + lastdate.toString());
			
			SimpleDateFormat isodash = new SimpleDateFormat("yyyy'-'MM'-'dd");
			SimpleDateFormat isodate = new SimpleDateFormat("yyyyMMdd");
			
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("ISO_DATE", isodate.format(syncdate));
			params.put("ISO_DATE_DASH", isodash.format(syncdate));
			
			Iterator<APIMapEntry> iter = entries.iterator();
			while(iter.hasNext()) {
				APIMapEntry entry = iter.next();
				
				System.out.println("downloading " + entry.getName() + 
						" " + isodash.format(syncdate));
						
				String url = substargs(entry.getApicall(), params);
				String outfile = substargs(entry.getOutname(), params);
				outfile = outdir.concat(outfile);
				
				mlogger.debug("sync: url:" + url);
				mlogger.debug("outfile:" + outfile);
				
				fit.setAPIcall(url);
				fit.setoutfile(outfile);
				fit.run();
				
			}
			
			syncdate = incrday(syncdate);
		}
		
		lastdatefile.setLastdate(today);
		lastdatefile.writedatefile();
		
		return 0;
	}

	
	private Pattern tokenPattern = Pattern.compile("\\{([^}]*)\\}");

	private String substargs(String template, Map<String, String> params) {
	    StringBuffer sb = new StringBuffer();
	    Matcher myMatcher = tokenPattern.matcher(template);
	    while (myMatcher.find()) {
	        String field = myMatcher.group(1);
	        myMatcher.appendReplacement(sb, "");
	        sb.append(doParameter(field, params));
	   }
	    myMatcher.appendTail(sb);
	    return sb.toString();
	}
	
	private String doParameter(String field, Map<String, String> params) {
		String value = "";
		
		value = params.get(field);
		if(value == null) value = "";
		
		return value; 
	}
	
	public Date dateonly(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();		
	}
	
	public Date incrday(Date date) {
		long tm = date.getTime();
		tm += 24*60*60*1000;
		
		return new Date(tm);
	}
	
}
