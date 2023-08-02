package com.sparta.i_mu.global.util;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class SpotifyUtil {

    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final SpotifyApi spotifyApi;
    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiryTime;

    @Autowired
    public SpotifyUtil(@Value("${secret.CLIENT_ID}") String clientId, @Value("${secret.CLIENT_SECRET}") String clientSecret) {
        this.CLIENT_ID = clientId;
        this.CLIENT_SECRET = clientSecret;
        spotifyApi = new SpotifyApi.Builder().setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET).build();
    }

    public String getAccessToken() {
        if (accessToken == null || isTokenExpired()) {
            accessToken();
        }
        return accessToken;
    }

    private boolean isTokenExpired() {
        if (tokenExpiryTime == null) {
            return true;
        }
        return Instant.now().isAfter(tokenExpiryTime);
    }

    public void accessToken() {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            this.accessToken = spotifyApi.getAccessToken();

            int expiresIn = clientCredentials.getExpiresIn(); // expiresIn is in seconds
            this.tokenExpiryTime = Instant.now().plusSeconds(expiresIn - 300); // subtract 300 seconds to account for possible delays

        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            System.out.println("Error: " + e.getMessage());
            this.accessToken = "error";
        }
    }
}
