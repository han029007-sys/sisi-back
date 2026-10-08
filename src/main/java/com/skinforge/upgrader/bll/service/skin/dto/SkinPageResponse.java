package com.skinforge.upgrader.bll.service.skin.dto;

import java.util.List;

public record SkinPageResponse(
        List<SkinOfferResponse> items,
        String nextCursor,
        boolean hasMore
) {}