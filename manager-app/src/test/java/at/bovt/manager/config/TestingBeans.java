package at.bovt.manager.config;

import at.bovt.manager.client.RestClientProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return mock(ClientRegistrationRepository.class);
    }

    @Bean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return mock(OAuth2AuthorizedClientRepository.class);
    }

    @Bean
    @Primary
    public RestClientProductsRestClient testRestClientProductsRestClient(
            @Value("${selmag.services.catalogue.uri:http://localhost:12345}") String catalogueBaseUri
    ) {
        return new RestClientProductsRestClient(RestClient.builder()
                .baseUrl(catalogueBaseUri)
                .requestFactory(new JdkClientHttpRequestFactory())
                .build());
    }
}
