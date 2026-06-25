package org.lvtn.mws.application.ports;

import java.util.List;

public interface ITokenProvider {
    String generateToken(String userId, String username, String roleCode, List<String> permissions);
    String generateRefreshToken(String userId, String username);
    boolean validateToken(String token);
    String extractUsername(String token);
    String extractUserId(String token);
    /** "access" | "refresh" — phân biệt loại token để chặn dùng access token làm refresh và ngược lại. */
    String extractTokenType(String token);
}
