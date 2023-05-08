package com.sandipchitale.oauth2.client.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
@Profile("client")
public class OAuthClientConfiguration {

    // Create the Github client registration
    @Bean
    @Profile("okta")
    ClientRegistration oktaClientRegistration() {
        ClientRegistration clientRegistration =
                ClientRegistrations.fromIssuerLocation("https://dev-76041835.okta.com/oauth2/default")
                .registrationId("okta")
                .clientName("")
                .clientId("")
                .clientSecret("LQK75daBc9IobYOoIrbXnJXV4Ew_v_AUGexdgAMA")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("okta.myAccount.email.read")
                .build();
        return clientRegistration;
    }

    // Create the client registration repository
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(ClientRegistration oktaClientRegistration) {
        InMemoryClientRegistrationRepository inMemoryClientRegistrationRepository = new InMemoryClientRegistrationRepository(oktaClientRegistration);
        return inMemoryClientRegistrationRepository;
    }

    // Create the authorized client service
    @Bean
    public OAuth2AuthorizedClientService auth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        InMemoryOAuth2AuthorizedClientService inMemoryOAuth2AuthorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        return inMemoryOAuth2AuthorizedClientService;
    }

    // Create the authorized client manager and service manager using the
    // beans created and configured above
    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager (
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

}