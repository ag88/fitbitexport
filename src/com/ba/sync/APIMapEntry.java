package com.ba.sync;

public class APIMapEntry implements Comparable<APIMapEntry> {
	
	public int seq;
	public String name;
	public String apicall;
	public String outname; 

	public APIMapEntry() {
	}

	public APIMapEntry(String apicall, String outname) {
		this.apicall = apicall;
		this.outname = outname;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApicall() {
		return apicall;
	}

	public void setApicall(String apicall) {
		this.apicall = apicall;
	}

	public String getOutname() {
		return outname;
	}

	public void setOutname(String outname) {
		this.outname = outname;
	}

	public String toString() {
		
		return Integer.toString(seq).concat(" ")
				.concat(name).concat(" ")
				.concat(apicall).concat(" ")
				.concat(outname);				
	}
	
	@Override
	public int compareTo(APIMapEntry o) {		
		return this.seq - o.seq;
	}	
	
}
