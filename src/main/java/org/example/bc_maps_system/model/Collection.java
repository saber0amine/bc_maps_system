package org.example.bc_maps_system.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "collections")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tagId;

    @OneToOne
    @JoinColumn(name = "tagId", insertable = false, updatable = false)
    private Tag tag;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "collection_places",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "place_id")
    )
    private List<Place> places = new ArrayList<>();

    public String exportGPX() {
        // Implementation for GPX export
        return "";
    }

    public String exportKML() {
        // Implementation for KML export
        return "";
    }

    public String exportGeoJSON() {
        // Implementation for GeoJSON export
        return "";
    }
}