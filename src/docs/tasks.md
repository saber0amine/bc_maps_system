
## Yassine

### #1 - Setup projet Spring Boot
Initialiser projet Spring Boot avec dépendances (Spring Web, JPA, H2/SQLite, Validation, Lombok). Configurer application.properties pour base embarquée et ports. Créer structure packages (controller, service, repository, model, dto, config).

### #2 - Entités Place + Tag + PlaceTag
Créer entités JPA Place (id, title, description, latitude, longitude, imageUrl, isCurrentPosition, userId, timestamps) et Tag (id, name, createdAt). Relation many-to-many via PlaceTag. Annotations JPA complètes.

### #3 - Repositories Place + Tag
Créer PlaceRepository avec méthodes: findByUserId, findByTitleContaining, findByTagsContaining, findNearby (calcul distance). Créer TagRepository avec findByName, findAllWithPlaceCount.

### #4 - Service PlaceService
Implémenter PlaceService avec CRUD complet (create, findById, findAll, update, delete). Méthode searchPlaces (titre/description/tags). Méthode findNearby avec calcul distance Haversine. Gestion position courante.

### #5 - Controller PlaceController
Endpoints REST: POST /api/places, GET /api/places, GET /api/places/{id}, PUT /api/places/{id}, DELETE /api/places/{id}. GET /api/places/search?query=X, GET /api/places/nearby?lat=X&lng=Y&radius=Z. ResponseEntity avec status HTTP corrects.

### #6 - Service ExportService (GPX/KML/GeoJSON)
Implémenter méthodes exportGPX, exportKML, exportGeoJSON prenant List<Place> et retournant String (XML/JSON formaté). Respecter formats standards. Gérer métadonnées (titre, description, timestamps).

### #7 - Controller ExportController + ImportController
Endpoint GET /api/collections/{tagId}/export?format=gpx|kml|geojson retournant fichier. POST /api/places/import acceptant MultipartFile, parser le contenu, créer Places. Content-Type appropriés (application/gpx+xml, application/vnd.google-earth.kml+xml, application/json).

---

## Amine

### #8.1 Authentification 
Implement Auth
### #8 - Entités Token + Permission (ACL)
Créer entités Token (id, value, userId, description, createdAt, expiresAt, isRevoked) et Permission (id, tokenId, resourceType enum, resourceId, accessType enum). Relations Token 1-N Permission.

### #9 - Repositories Token + Permission
Créer TokenRepository avec findByValue, findByUserIdAndIsRevokedFalse. Créer PermissionRepository avec findByTokenId, findByTokenIdAndResourceTypeAndResourceId. Méthodes de vérification token valide.

### #10 - Service TokenService + PermissionService
TokenService: generateToken (génère string unique), revokeToken, isValid, isExpired. PermissionService: canRead, canWrite (vérifie permissions selon resourceType/resourceId/accessType). Logique complète ACL.

### #11 - TokenInterceptor (Authentification)
Créer HandlerInterceptor vérifiant header Authorization: Bearer {token}. Extraire token, valider via TokenService, vérifier expiration. Stocker Token dans request attributes. Retourner 401/403 selon cas. Enregistrer dans WebMvcConfigurer.

### #12 - Controller TokenController
Endpoints: POST /api/tokens (générer token avec permissions), GET /api/tokens (lister mes tokens), DELETE /api/tokens/{id} (révoquer). POST accepte DTO avec resourceType, resourceId, accessType. Retourner token généré + URL serveur.

### #13 - Entité ExternalSource + AggregatorService
Créer ExternalSource (id, userId, serverUrl, token, name, isActive, lastSync). Repository avec findByUserIdAndIsActiveTrue. AggregatorService avec méthode aggregatePlaces appelant GET externes avec RestTemplate + token dans headers. Gestion erreurs 401/403.

### #14 - Controller CollectionController
Endpoints: GET /api/collections (lister tags avec count), GET /api/places?tagId={id} (places d'une collection). Appliquer vérifications permissions via PermissionService. Retourner 403 si accès refusé.


---

## Ordre d'exécution recommandé

**Phase 1** (Karim #1-3 + Binôme #8-9) : Setup + Modèle données  
**Phase 2** (Karim #4-5 + Binôme #10-11) : Services + Auth  
**Phase 3** (Karim #6-7 + Binôme #12-13) : Features avancées  
**Phase 4** (Binôme #14) : Collections + intégration

*Chaque ticket = 2-4h de travail*