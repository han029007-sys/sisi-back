package com.skinforge.upgrader.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class SteamOpenIdValidator {

    private static final String STEAM_OPENID =
            "https://steamcommunity.com/openid/login";

    private final RestClient restClient =
            RestClient.create();

    public boolean validate(HttpServletRequest request) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) {
                params.add(key, value);
            }
        });

        params.set("openid.mode", "check_authentication");

        String response = restClient
                .post()
                .uri(STEAM_OPENID)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(String.class);

        System.out.println("Steam validation response:");
        System.out.println(response);

        return response != null
                && response.contains("is_valid:true");
    }
}