package org.lvtn.mws.domain.repository;

/**
 * Domain port for generating opaque unique identifiers.
 * Keeps the domain layer free of any infrastructure dependency
 * (implemented in infrastructure by delegating to IdGeneratorService).
 */
public interface IIdGenerator {
    String generate();
}
