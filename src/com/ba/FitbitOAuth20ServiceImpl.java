package com.ba;
import org.apache.commons.codec.binary.Base64;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hexren
 *
 */
public class FitbitOAuth20ServiceImpl extends OAuth20ServiceImpl implements OAuthService {
	private final Logger log = LoggerFactory.getLogger(Fitbit.class);
	private static final String VERSION = "2.0";
	private static String BASE_USERID_SECRET;

	private final FitbitScripeApi api;
	private final OAuthConfig config;
	private Response response;

	public FitbitOAuth20ServiceImpl(FitbitScripeApi api, OAuthConfig config) {
		super(api, config);
		this.api = api;
		this.config = config;
		String useridsecret = config.getApiKey() + ":" + config.getApiSecret();
		BASE_USERID_SECRET = new String(Base64.encodeBase64(useridsecret.getBytes()));
	}

	/**
	 * {@inheritDoc}
	 */
	public Token getAccessToken(Token requestToken, Verifier verifier) {
		OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
		request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
		request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
		request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
		request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
		request.addQuerystringParameter("grant_type", "authorization_code");

		if (config.hasScope())
			request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
		request.addHeader("Authorization", "Basic " + BASE_USERID_SECRET);
		response = request.send();
		return api.getAccessTokenExtractor().extract(response.getBody());
	}

	/**
	 * Call only after calling public Token getAccessToken(Token requestToken,
	 * Verifier verifier)
	 * 
	 * @return refresh token string
	 */
	public String getRefreshToken() {
		return api.getRefreshTokenExtractor().extract(response.getBody()).getToken();
	}

	public Token getAccessToken(String refreshToken) {
		try {
			OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());

			request.addQuerystringParameter("grant_type", "refresh_token");
			request.addQuerystringParameter("refresh_token", refreshToken);
			request.addHeader("Authorization", "Basic " + BASE_USERID_SECRET);
			response = request.send();
			return api.getAccessTokenExtractor().extract(response.getBody());
		} catch (Exception e) {
			log.debug("Warning! Could not use refresh token. Exception: " + e);
			return null;
		}
	}

	@Override
	public void signRequest(Token accessToken, OAuthRequest request) {
		request.addHeader("Authorization", "Bearer " + accessToken.getToken());
		request.addQuerystringParameter(OAuthConstants.ACCESS_TOKEN, accessToken.getToken());
	}

}
