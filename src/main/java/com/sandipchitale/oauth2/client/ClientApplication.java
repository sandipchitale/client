package com.sandipchitale.oauth2.client;

// import static org.springframework.boot.Banner.Mode.OFF;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(ClientApplication.class)
				// .bannerMode(OFF)
				.initializers(context -> {
					ConfigurableEnvironment environment = context.getEnvironment();
					if (environment.acceptsProfiles(Profiles.of("okta"))) {
						environment.addActiveProfile("client");
					}
					System.out.println("Active Profiles: " + Arrays.asList(environment.getActiveProfiles()));
				})
				.web(WebApplicationType.NONE)
				.run(args);
	}

	@Component
	@Profile("client")
	public static class CLR implements CommandLineRunner {

		// Inject the OAuth authorized client authorized client manager
		// from the OAuthClientConfiguration class
		@Autowired
		private AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager;

		@Override
		public void run(String... args) throws Exception {
			if (this.authorizedClientServiceAndManager == null) {
				System.out.println("No authorizedClientServiceAndManager");
				return;
			}
			// Build an OAuth2 request for the Okta provider
			OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("okta")
					.principal("0oa9fkf9a744SMgHt5d7")
					.build();

			// Perform the actual authorization request using the authorized client service and authorized client
			// manager. This is where the JWT is retrieved from the Okta servers.
			OAuth2AuthorizedClient authorizedClient = this.authorizedClientServiceAndManager.authorize(authorizeRequest);

			// Get the token from the authorized client object
			OAuth2AccessToken accessToken = Objects.requireNonNull(authorizedClient).getAccessToken();

			System.out.println("Issued: " + accessToken.getIssuedAt().toString() + ", Expires:"
					+ accessToken.getExpiresAt().toString());
			System.out.println("Scopes: " + accessToken.getScopes().toString());
			System.out.println("Token: " + accessToken.getTokenValue());

			// Add the JWT to the RestTemplate headers
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue());
			headers.add(HttpHeaders.ACCEPT, "application/json; okta-version=1.0.0");
			HttpEntity<?> request = new HttpEntity<>(headers);

			// Make the actual HTTP GET request
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

			ResponseEntity<String> response = restTemplate.exchange(
					"https://dev-76041835-admin.okta.com/idp/myaccount/emails",
					HttpMethod.GET,
					request,
					String.class);

			String result = response.getBody();
			System.out.println("Reply = " + result);
		}
	}
}
