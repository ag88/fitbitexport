package com.ba;

import static com.ba.AccessData.CLIENT_ID;
import static com.ba.AccessData.CLIENT_SECRET;
import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fitbit {
	private static final Token EMPTY_TOKEN = null;
	private final Logger log = LoggerFactory.getLogger(Fitbit.class);


	public void initialise() {
		try {
			FitbitOAuth20ServiceImpl service = (FitbitOAuth20ServiceImpl) new ServiceBuilder().provider(FitbitScripeApi.class).apiKey(CLIENT_ID).apiSecret(CLIENT_SECRET).callback("http://www.example.com/callback").scope("activity heartrate location nutrition sleep").build();

			String refreshToken = loadRefreshToken();
			Token accessToken = getAccessToken(service, refreshToken);
			saveRefreshToken(service.getRefreshToken());
			
			OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.fitbit.com/1/user/-/activities/heart/date/today/1d.json");
			
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

			db = org.iq80.leveldb.impl.Iq80DBFactory.factory.open(new File("fitbitdb"), options);

			refreshToken = asString(db.get(bytes("refreshToken")));
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

			db = org.iq80.leveldb.impl.Iq80DBFactory.factory.open(new File("fitbitdb"), options);
			db.put(bytes("refreshToken"), bytes(refreshToken));
			db.close();

		} catch (IOException e) {
			log.warn("Error! Exception: " + e);

		}
		log.debug("saved refreshToken: " + refreshToken);
	}


}
