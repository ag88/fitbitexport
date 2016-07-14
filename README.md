# fitbitexport

* original readme by: Hexren

* create ~/.fitbitexport directory
* create ~/.fitbitexport/properties.txt containing values from dev.fitbit.com:
* client_id = YOUR_CLIENT_ID
* client_secret = YOUR_CLIENT_SECRET


* call it somewhat like: java -jar target/FitbitBAHeart-0.0.1-SNAPSHOT.jar -call="https://api.fitbit.com/1/user/-/activities/heart/date/today/1d.json"

* added by: ag88

* make an xml file named "map.xml" (an example is in the root folder)

* this file needs to go into ~/.fitbitexport/map.xml

* this file has the following components:
<!-- in the element "map" the attribute "outdir" is the folder where you place your downloaded/exported files -->
<map outdir="/home/user/fitbit/"> 
	<entry seq="1" name="heart rate">  
		<!-- each "entry" has attributes "seq" sequence and "name" which is basically a label" -->
		<!-- each "entry" has an apicall and outfile element -->
		<!-- the "apicall" element is fitbit apicall url, indicate that in the "url" attribute -->
		<!-- the "outfile" is the filename to save the file as, indicate that in the "name" attribute -->
		<!-- both "apicall" and "outfile" provides a date parameter substitution -->
		<!-- "ISO_DATE" is the date in yyyyMMdd format -->
		<!-- "ISO_DATE_DASH" is the date in yyyy-MM-dd format -->
		<!-- e.g. if you state the outfile - name as "step-{ISO_DATE_DASH}.json" 
		     the exported file would look like "step-2016-05-01.json" -->

		<apicall
			url="https://api.fitbit.com/1/user/-/activities/heart/date/{ISO_DATE_DASH}/1d/1sec.json" />
		<outfile name="hr-{ISO_DATE}-1s.json" />
	</entry>
	<entry seq="2" name="steps">
		<apicall
			url="https://api.fitbit.com/1/user/-/activities/steps/date/{ISO_DATE_DASH}/1d/15min.json" />
		<outfile name="step-{ISO_DATE}.json" />
	</entry>
	<entry seq="3" name="sleep">
		<apicall
			url="https://api.fitbit.com/1/user/-/sleep/date/{ISO_DATE_DASH}.json" />
		<outfile name="sleep-{ISO_DATE}.json" />
	</entry>
</map>

* run fitbitexport using the -s (sync) option, e.g. java -jar target/FitbitBAHeart-0.0.1-SNAPSHOT.jar -s

* that would download/export into files according to the "map.xml" spec above

* when you run fitbitexport -s the next time, it would download the files since the last date(day) you last left off till the current system date hence the option -s 'sync'