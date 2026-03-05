package org.example.bc_maps_system.exception;

public class PlaceNotFoundException extends RuntimeException {
    public PlaceNotFoundException(Long id) {
        super("Lieu introuvable avec l'id : " + id);
    }
}