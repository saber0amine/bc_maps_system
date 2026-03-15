package org.example.bc_maps_system.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User introuvable avec l'id : " + id);
    }
}