package com.skinforge.upgrader.auth;

import com.skinforge.upgrader.integration.steam.SteamUserClient;
import com.skinforge.upgrader.model.User;
import com.skinforge.upgrader.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class SteamAuthService {

    private static final String STEAM_OPENID =
            "https://steamcommunity.com/openid/login";

    private static final String CALLBACK =
            "https://api.sisiskins.best/api/auth/steam/callback";

    private static final String STEAM_OPENID_ID_PREFIX =
            "https://steamcommunity.com/openid/id/";

    private final UserRepository userRepository;
    private final SteamOpenIdValidator validator;
    private final SteamUserClient steamUserClient;

    public String buildLoginUrl() {
        return UriComponentsBuilder
                .fromUriString(STEAM_OPENID)
                .queryParam(
                        "openid.ns",
                        "http://specs.openid.net/auth/2.0"
                )
                .queryParam(
                        "openid.mode",
                        "checkid_setup"
                )
                .queryParam(
                        "openid.return_to",
                        CALLBACK
                )
                .queryParam(
                        "openid.realm",
                        "http://localhost:8080"
                )
                .queryParam(
                        "openid.identity",
                        "http://specs.openid.net/auth/2.0/identifier_select"
                )
                .queryParam(
                        "openid.claimed_id",
                        "http://specs.openid.net/auth/2.0/identifier_select"
                )
                .build()
                .toUriString();
    }

    public User authenticate(HttpServletRequest request) {

        if (!validator.validate(request)) {
            throw new IllegalArgumentException(
                    "Invalid Steam authentication"
            );
        }

        String claimedId =
                request.getParameter("openid.claimed_id");

        if (claimedId == null ||
                !claimedId.startsWith(STEAM_OPENID_ID_PREFIX)) {
            throw new IllegalArgumentException(
                    "Invalid Steam claimed id"
            );
        }

        String steamId =
                claimedId.substring(
                        claimedId.lastIndexOf("/") + 1
                );

        var steamPlayer = steamUserClient.getPlayer(steamId);

        User user = userRepository
                .findBySteamId(steamId)
                .orElseGet(() -> createUser(
                        steamId,
                        steamPlayer.personaname(),
                        steamPlayer.avatarfull()
                ));

        user.updateProfile(
                steamPlayer.personaname(),
                steamPlayer.avatarfull()
        );

        return userRepository.save(user);
    }

    private User createUser(
            String steamId,
            String username,
            String avatarUrl
    ) {
        return new User(
                steamId,
                username,
                avatarUrl
        );
    }
}