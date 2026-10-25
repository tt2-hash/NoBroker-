package com.example.pgfinder;

import java.net.URI;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Only https links on nobroker.in may be stored as the external listing URL. */
public final class UrlGuard {
    private static final Set<String> HOSTS = Set.of("nobroker.in", "www.nobroker.in");
    private UrlGuard() {}

    public static String normalize(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            URI u = URI.create(url.trim());
            if ("https".equalsIgnoreCase(u.getScheme()) && u.getHost() != null
                    && HOSTS.contains(u.getHost().toLowerCase())) {
                return u.toString();
            }
        } catch (IllegalArgumentException ignored) { /* fall through */ }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "externalUrl must be an https nobroker.in link");
    }
}
