# Sprint 2 - Backend - Plan de travail et tickets

## Objectif du sprint

Finaliser le backend métier du projet en connectant correctement :

- gestion complète des lieux
- gestion des tags
- collections enrichies
- export / import
- intégration complète avec les ACL
- compatibilité avec l'agrégation externe

---

## Objectifs principaux

| Objectif | Description | Priorité |
|---|---|---:|
| Finaliser le module Places | CRUD, recherche, nearby, position courante | Haute |
| Exposer les tags en API | récupération des tags et de leurs IDs | Haute |
| Finaliser Collections | tags + count + lieux d'une collection | Haute |
| Ajouter Export / Import | GPX, KML, GeoJSON | Haute |
| Brancher les ACL partout | lecture / écriture selon permissions | Haute |
| Stabiliser l'agrégation externe | fonctionnement réel sur `/api/places` | Moyenne |
| Ajouter qualité technique | validations, DTO, tests, doc API | Moyenne |

---

# Backlog Sprint 2

## Epic A - Places API

### BE-201 - Créer PlaceController
**Priorité : Haute**

Implémenter les endpoints :

- `POST /api/places`
- `GET /api/places`
- `GET /api/places/{id}`
- `PUT /api/places/{id}`
- `DELETE /api/places/{id}`

**Critères d'acceptation**
- endpoints accessibles
- statuts HTTP corrects
- réponses en DTO
- sécurité branchée via token

---

### BE-202 - Compléter PlaceService
**Priorité : Haute**

Ajouter :

- `create`
- `findById`
- `findAll`
- `update`
- `delete`
- `searchPlaces`
- `findNearby`
- gestion de la position courante

**Critères d'acceptation**
- tous les endpoints Places utilisent le service
- les règles métier sont centralisées
- exceptions métiers gérées proprement

---

### BE-203 - Compléter PlaceRepository
**Priorité : Haute**

Ajouter les méthodes nécessaires :

- recherche par utilisateur
- recherche par titre
- recherche par tags
- recherche texte
- requêtes nearby

**Critères d'acceptation**
- requêtes testées
- repository exploitable par `PlaceService`

---

### BE-204 - Corriger le mapping Place / User / Tag
**Priorité : Haute**

Corriger les incohérences de mapping JPA :

- propriété `user`
- relation many-to-many `Place <-> Tag`
- persistance correcte des tags liés à un lieu

**Critères d'acceptation**
- création d'un lieu persistée correctement
- tags associés persistés correctement
- plus d'ambiguïté sur le propriétaire de la relation

---

## Epic B - Tags et collections

### BE-205 - Créer TagController
**Priorité : Haute**

Ajouter :

- `GET /api/tags`
- éventuellement `GET /api/tags/{id}`

**Critères d'acceptation**
- retour d'une liste de tags utilisables par le frontend et les tests API
- récupération simple de `tagId`

---

### BE-206 - Enrichir CollectionController
**Priorité : Haute**

Améliorer :

- `GET /api/collections`
- `GET /api/collections/places?tagId=...`

Ajouter :
- count des lieux par tag
- DTO dédié de collection
- homogénéité des réponses

**Critères d'acceptation**
- une collection retourne au minimum :
    - `id`
    - `name`
    - `placeCount`
- contrôle ACL toujours appliqué

---

### BE-207 - Ajouter DTO CollectionResponse
**Priorité : Moyenne**

Créer un DTO propre pour les collections, au lieu de retourner directement les entités JPA.

**Critères d'acceptation**
- plus de retour brut d'entité
- payload stable pour mobile / frontend

---

## Epic C - Export / Import

### BE-208 - Créer ExportService
**Priorité : Haute**

Implémenter :

- `exportGPX(List<Place>)`
- `exportKML(List<Place>)`
- `exportGeoJSON(List<Place>)`

**Critères d'acceptation**
- contenu généré valide
- métadonnées incluses
- structure conforme au format choisi

---

### BE-209 - Créer ExportController
**Priorité : Haute**

Ajouter endpoint :

- `GET /api/collections/{tagId}/export?format=gpx|kml|geojson`

**Critères d'acceptation**
- retourne un fichier téléchargeable
- content-type correct
- contrôle ACL avant export

---

### BE-210 - Créer ImportController
**Priorité : Haute**

Ajouter endpoint :

- `POST /api/places/import`

Support attendu :

- GPX
- KML
- GeoJSON

**Critères d'acceptation**
- parsing du fichier
- création des lieux
- gestion propre des erreurs de format

---

## Epic D - ACL et sécurité

### BE-211 - Corriger DELETE /api/tokens/{id}
**Priorité : Haute**

Corriger la recherche du token cible :
- recherche par `id`
- non par `value`

**Critères d'acceptation**
- révocation fonctionnelle
- `404` uniquement si token inexistant
- `403` si token d'un autre utilisateur

---

### BE-212 - Ajouter validations sur les DTO
**Priorité : Haute**

Couvrir :

- auth
- tokens
- places
- sources externes

**Critères d'acceptation**
- validation automatique côté contrôleur
- erreurs homogènes
- champs obligatoires correctement bloqués

---

### BE-213 - Étendre les ACL aux endpoints métier
**Priorité : Haute**

Définir les règles de lecture / écriture pour :

- places
- collections
- export
- import

**Critères d'acceptation**
- endpoints protégés selon la ressource
- cohérence entre master token et token limité

---

## Epic E - Sources externes et fédération

### BE-214 - Rendre AggregatorService réellement opérationnel
**Priorité : Moyenne**

Faire en sorte que l'agrégation fonctionne sur un vrai endpoint distant.

Pré-requis :
- existence de `GET /api/places`

**Critères d'acceptation**
- agrégation réelle depuis une source externe
- mise à jour `lastSync`
- gestion `401` / `403` / indisponibilité

---

### BE-215 - Ajouter logs et gestion d'erreurs d'intégration
**Priorité : Moyenne**

Améliorer :
- logs d'appel distant
- timeout
- messages d'erreur
- suivi de statut des sources

**Critères d'acceptation**
- comportement observable
- erreurs externes non bloquantes

---

## Epic F - Qualité technique

### BE-216 - Créer tests d'intégration backend
**Priorité : Moyenne**

Couvrir au minimum :

- auth register / login
- create token
- revoke token
- list collections
- places by tag
- create place

**Critères d'acceptation**
- tests automatisés exécutables
- scénario nominal + erreurs courantes

---

### BE-217 - Ajouter documentation API
**Priorité : Moyenne**

Ajouter Swagger / OpenAPI pour documenter :
- auth
- tokens
- places
- collections
- import / export
- sources externes

**Critères d'acceptation**
- endpoints consultables dans une interface
- payloads documentés

---

### BE-218 - Nettoyage backend final
**Priorité : Moyenne**

Nettoyer :
- anciens fichiers obsolètes
- doublons
- mappings ambigus
- réponses API incohérentes

**Critères d'acceptation**
- structure claire
- code backend homogène
- dette technique réduite

---

# Ordre recommandé d'exécution

## Phase 1
- BE-211
- BE-201
- BE-202
- BE-203
- BE-204
- BE-205

## Phase 2
- BE-206
- BE-207
- BE-213
- BE-214

## Phase 3
- BE-208
- BE-209
- BE-210

## Phase 4
- BE-212
- BE-215
- BE-216
- BE-217
- BE-218

---

# Définition de fin de sprint

Le sprint 2 sera considéré terminé si :

- les endpoints Places sont complets
- les tags sont récupérables via API
- les collections retournent les données attendues
- l'export fonctionne
- l'import fonctionne
- les ACL couvrent les routes métier
- l'agrégation externe fonctionne réellement
- les tests backend critiques passent

---

# Livrables attendus

- PlaceController
- TagController
- PlaceService complet
- PlaceRepository complet
- ExportService
- ExportController
- ImportController
- DTO propres pour Places / Tags / Collections
- correctifs ACL
- tests backend
- documentation API