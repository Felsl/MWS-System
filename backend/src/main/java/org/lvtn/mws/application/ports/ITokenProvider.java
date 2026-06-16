package org.lvtn.mws.application.ports;

import java.util.List;

public interface ITokenProvider {
    String generateToken(String userId, String username, String roleCode, List<String> permissions);
    boolean validateToken(String token);
    String extractUsername(String token);
    String extractUserId(String token);
}
