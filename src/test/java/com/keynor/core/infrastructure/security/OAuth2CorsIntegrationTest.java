package com.keynor.core.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for a real bug: /oauth2/token is served by the
 * Authorization Server filter chain, which historically had no CORS
 * configuration — only the Resource Server chain (/api/**) did. This meant
 * the browser-based PKCE login flow (aniannoth-overview, cross-origin
 * fetch()) could never read the token response, even though the server
 * processed it successfully (200 OK server-side, blocked client-side).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OAuth2CorsIntegrationTest {

    static {
        // Java's HttpURLConnection silently drops "restricted" headers (Origin
        // among them) unless this is set — must happen before HttpURLConnection
        // is first used. Without this, this test cannot actually send Origin at
        // all and every assertion about CORS behavior would be testing nothing.
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    @LocalServerPort
    private int port;

    @Test
    void oauth2TokenEndpoint_shouldReturnCorsHeader_onDirectCrossOriginPost() {
        // application/x-www-form-urlencoded is a CORS-safelisted content type,
        // so the browser sends the POST directly — no preflight OPTIONS first.
        // This is exactly what aniannoth-overview's PKCE login flow does, and
        // exactly where the real bug showed up: the server responded 200, but
        // without Access-Control-Allow-Origin, so the browser's fetch() threw
        // "Failed to fetch" even though the request succeeded on the wire.
        //
        // Uses a plain buffered RestTemplate rather than the injected
        // TestRestTemplate: the request is expected to get a 401 (no such
        // client in the test DB) with a WWW-Authenticate challenge, and
        // TestRestTemplate's default (streaming) request factory cannot
        // retry a streamed body against that challenge — a client quirk
        // unrelated to the CORS behavior under test.
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Origin", "http://localhost:4173");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", "not-a-real-code");
        body.add("redirect_uri", "http://localhost:4173/auth/callback");
        body.add("client_id", "aniannoth-admin");
        body.add("code_verifier", "not-a-real-verifier");

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(
                    "http://localhost:" + port + "/oauth2/token", new HttpEntity<>(body, headers), String.class);
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            response = ResponseEntity.status(ex.getStatusCode()).headers(ex.getResponseHeaders()).body(ex.getResponseBodyAsString());
        }

        // The token exchange itself is expected to fail (fake code/client) —
        // what matters is that whatever response comes back carries the CORS
        // header, so the browser can actually read it instead of blocking it.
        assertThat(response.getHeaders().getFirst("Access-Control-Allow-Origin"))
                .isEqualTo("http://localhost:4173");
    }
}
