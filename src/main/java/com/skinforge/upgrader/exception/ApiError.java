package com.skinforge.upgrader.exception;

import java.time.Instant;

public record ApiError(
        String message,
        Instant timestamp
) {
}