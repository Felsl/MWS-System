package org.lvtn.mws.application.ports;

public interface IPasswordHasher {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
