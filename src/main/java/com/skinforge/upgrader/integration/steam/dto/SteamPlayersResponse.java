package com.skinforge.upgrader.integration.steam.dto;

import java.util.List;

public record SteamPlayersResponse(
        List<SteamPlayer> players
) {
}
