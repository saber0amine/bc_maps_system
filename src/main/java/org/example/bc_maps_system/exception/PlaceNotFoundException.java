package org.example.bc_maps_system.exception;

import java.util.UUID;

public class PlaceNotFoundException extends RuntimeException {
    public PlaceNotFoundException(UUID id) {
        super("Lieu introuvable avec l'id : " + id);
    }
    public PlaceNotFoundException(String message) {
        super(message);
    }
}