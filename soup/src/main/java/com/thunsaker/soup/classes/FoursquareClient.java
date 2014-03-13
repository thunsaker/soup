package com.thunsaker.soup.classes;

/*
 * Created by @thunsaker
 */
public class FoursquareClient {
	private String ClientId = "";
	private String ClientSecret = "";
	private String CallbackUrl = "";

	public FoursquareClient(String FoursquareClientId, String FoursquareClientSecret, String FoursquareCallbackUrl) {
		setClientId(FoursquareClientId);
		setClientSecret(FoursquareClientSecret);
		setCallbackUrl(FoursquareCallbackUrl);
	}

	public String getClientId() {
		return ClientId;
	}
	public void setClientId(String clientId) {
		ClientId = clientId;
	}

	public String getClientSecret() {
		return ClientSecret;
	}
	public void setClientSecret(String clientSecret) {
		ClientSecret = clientSecret;
	}

	public String getCallbackUrl() {
		return CallbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		CallbackUrl = callbackUrl;
	}
}
