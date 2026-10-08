package com.skinforge.upgrader.integration.steam.dto;

public record SteamPlayer(
        String steamid,
        String personaname,
        String avatar,
        String avatarmedium,
        String avatarfull
) {
}