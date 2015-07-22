package com.ba;

import static com.ba.AccessData.CLIENT_ID;
import static com.ba.AccessData.CLIENT_SECRET;
import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.kohsuke.args4j.Option;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;



public class Fitbit {
	private static final Token EMPTY_TOKEN = null;
	private final Logger log = Logger.getLogger(Fitbit.class);	
	private static final String PROPERTIES_FILE = System.getProperty("user.home")+"/.fitbitexport/properties.txt";
	private static String CLIENT_ID;// = "229MVP";
	private static String CLIENT_SECRET;// = "fa3d326ed7bcc418c1bf9bcf2fa95c58";
	private static String FITBITDB = System.getProperty("user.home")+"/.fitbitexport/fitbitdb";
	
	@Option(name="-v",usage="be verbose, log output will be written to std.err")
    private boolean verbose;
	
	@Option(name="-u",usage="name under which these tokens will be stored")
    private String user = "defaultUser";
	
	@Option(name="-h",usage="print usage and exit")
    public boolean h = false;
	
	@Option(name="-call",usage="fitbit api call", required=true)
    private String apicall;
	
	public void run() {

		if(!verbose){
			Logger log = Logger.getLogger("com.ba");
			log.setLevel(Level.ERROR);
		}
		
		Properties properties = new Properties();
		try {
		  properties.load(new FileInputStream(PROPERTIES_FILE));
		  CLIENT_ID = properties.getProperty("client_id");
		  CLIENT_SECRET = properties.getProperty("client_secret");
		  String dbLocation = properties.getProperty("client_secret");
		  if(dbLocation != null)
			  FITBITDB = dbLocation;
		  
		  if(CLIENT_ID == null || CLIENT_SECRET == null)
			  	throw new RuntimeException("Missing properties");
		} catch (Exception e) {
		  log.error(e);
		  log.error("There should be a propertiesfile:" + PROPERTIES_FILE + " containing, properties: client_id and client_secret from fitbit" );
		  System.exit(1);
		}
		
		
		try {
			FitbitOAuth20ServiceImpl service = (FitbitOAuth20ServiceImpl) new ServiceBuilder().provider(FitbitScripeApi.class).apiKey(CLIENT_ID).apiSecret(CLIENT_SECRET).callback("http://www.example.com/callback").scope("activity heartrate location nutrition sleep").build();

			String refreshToken = loadRefreshToken();
			Token accessToken = getAccessToken(service, refreshToken);
			saveRefreshToken(service.getRefreshToken());
			
//			OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.fitbit.com/1/user/-/activities/heart/date/today/1d.json");
			OAuthRequest request = new OAuthRequest(Verb.GET, apicall);
			service.signRequest(accessToken, request); 
														
			Response response = request.send();
			System.out.println(response.getBody());

		} catch (Exception e) {
			log.warn("Exception:" + e);
		}
	}

	private String loadRefreshToken() {
		log.debug("loading refreshToken...");
		String refreshToken = "";
		try {

			Options options = new Options();
			options.createIfMissing(true);
			DB db;

			db = org.iq80.leveldb.impl.Iq80DBFactory.factory.open(new File(FITBITDB), options);

			refreshToken = asString(db.get(bytes(user+"refreshToken")));
			db.close();

		} catch (IOException e) {
			log.warn("Error! Exception: " + e);

		}
		log.debug("loaded refresh_token is: " + refreshToken);
		return refreshToken;
	}
	
	private Token getAccessToken(FitbitOAuth20ServiceImpl service, String refreshToken){
		Token accessToken = null;
		
		if(refreshToken != null){
			log.debug("Trying to get an access token from the saved refresh_token");
			accessToken = service.getAccessToken(refreshToken);
		}
		if(accessToken != null){
			log.debug("Success");
			return accessToken;
		}
		log.debug("Failed, falling back to showing auth url...");
			
		String authUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
		
		Scanner in = new Scanner(System.in);

		System.out.println("Got the Authorization URL!");
		System.out.println("Now go and authorize Scribe here:");
		System.out.println(authUrl);
		System.out.println("And paste the authorization code here");
		System.out.print(">>");
		Verifier verifier = new Verifier(in.nextLine());
		System.out.println();

		log.debug("Trading the Request Token for an Access Token...");
		accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
		
		log.debug("Got the Access Token!");
		log.debug("(if your curious it looks like this: " + accessToken + " )");
		
		in.close();
		return accessToken;
	}
	
	private void saveRefreshToken(String refreshToken) {
		log.debug("saving refreshToken...");
		try {

			Options options = new Options();
			options.createIfMissing(true);
			DB db;

			db = org.iq80.leveldb.impl.Iq80DBFactory.factory.open(new File(FITBITDB), options);
			db.put(bytes(user+"refreshToken"), bytes(refreshToken));
			db.close();

		} catch (IOException e) {
			log.warn("Error! Exception: " + e);

		}
		log.debug("saved refreshToken: " + refreshToken);
		
	}


}
