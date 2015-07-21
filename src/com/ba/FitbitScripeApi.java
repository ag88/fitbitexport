package com.ba;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

public class FitbitScripeApi extends org.scribe.builder.api.DefaultApi20 {
	// private static String AUTH_URL =
	// "https://www.fitbit.com/oauth2/authorize";

	private static String TOKEN_URL = "https://api.fitbit.com/oauth2/token";

	private static String AUTH_URL = "https://www.fitbit.com/oauth2/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s";

	@Override
	public String getAccessTokenEndpoint() {
		return TOKEN_URL;
	}

	@Override
	public OAuthService createService(OAuthConfig config) {
		return new FitbitOAuth20ServiceImpl(this, config);
	}

	@Override
	public String getAuthorizationUrl(OAuthConfig config) {
		String authUrl = String.format(AUTH_URL, config.getApiKey(), OAuthEncoder.encode("http://www.example.com/callback"), OAuthEncoder.encode(config.getScope()));
		return authUrl;
	}

	public Verb getAccessTokenVerb() {
		return Verb.POST;
	}

	@Override
	public AccessTokenExtractor getAccessTokenExtractor() {
		return new JsonTokenExtractor();
	}

	public AccessTokenExtractor getRefreshTokenExtractor() {
		return new JsonRefreshTokenExtractor();
	}

	public class JsonRefreshTokenExtractor implements AccessTokenExtractor {
		public Pattern accessTokenPattern = Pattern.compile("\"refresh_token\":\\s*\"(\\S*?)\"");

		  public Token extract(String response)
		  {
		    Preconditions.checkEmptyString(response, "Cannot extract a token from a null or empty String");
		    Matcher matcher = accessTokenPattern.matcher(response);
		    if(matcher.find())
		    {
		      return new Token(matcher.group(1), "", response);
		    }
		    else
		    {
		      throw new OAuthException("Cannot extract an access token. Response was: " + response);
		    }
		  }
		
	}

}
