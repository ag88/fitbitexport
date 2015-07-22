# fitbitexport
* create ~/.fitbitexport directory
* create ~/.fitbitexport/properties.txt containing values from dev.fitbit.com:
* client_id = YOUR_CLIENT_ID
* client_secret = YOUR_CLIENT_SECRET


* call it somewhat like: java -jar target/FitbitBAHeart-0.0.1-SNAPSHOT.jar -call="https://api.fitbit.com/1/user/-/activities/heart/date/today/1d.json"
