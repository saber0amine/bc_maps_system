# Sprint 1 - Bilan 

le sprint 1, avec un focus sur :

- Authentification
- ACL via Token / Permission
- Interception et validation des accès
- Gestion des sources externes
- Consultation des collections protégées

---

## Vue d'ensemble

| Bloc | Statut | Niveau |
|---|---|---:|
| Authentification | Fait | 100% |
| Entités ACL (Token / Permission) | Fait | 100% |
| Repositories ACL | Fait | 100% |
| Services ACL | Fait | 85% |
| Interceptor d'authentification | Fait | 100% |
| API Tokens | Fait avec correctifs | 85% |
| Sources externes + agrégation | Fait avec dépendances | 80% |
| API Collections avec contrôle ACL | Fait partiellement | 75% |

---

## Travaux réalisés

### 8.1 - Authentification
**Statut : Fait**

Implémentation de l'authentification backend avec :

- inscription utilisateur
- connexion utilisateur
- hashage des mots de passe
- retour d'un token maître après authentification

### 8 - Entités Token + Permission
**Statut : Fait**

Implémentation des entités ACL :

- `Token`
- `Permission`
- relation `Token 1-N Permission`
- enums `ResourceType` et `AccessType`
- gestion de la révocation et de l'expiration
- distinction `master token`

### 9 - Repositories Token + Permission
**Statut : Fait**

Repositories implémentés avec les méthodes principales :

- recherche d'un token par valeur
- récupération des tokens actifs d'un utilisateur
- récupération des permissions d'un token
- vérification de permission par ressource et type d'accès

### 10 - Service TokenService + PermissionService
**Statut : Fait partiellement finalisé**

Implémentation des services métier :

#### TokenService
- génération de token
- récupération de token
- révocation
- validation
- vérification expiration
- liste des tokens actifs d'un utilisateur

#### PermissionService
- ajout de permissions à un token
- vérification `canRead`
- vérification `canWrite`
- gestion du cas `master token`

### 11 - TokenInterceptor
**Statut : Fait**

Implémentation de l'intercepteur :

- lecture du header `Authorization: Bearer ...`
- extraction du token
- validation du token
- vérification d'expiration
- stockage du token résolu dans les attributs de la requête
- protection des routes `/api/**`
- exclusion des routes `/api/auth/**`

### 12 - TokenController
**Statut : Fait avec un bug à corriger**

Endpoints déjà exposés :

- `POST /api/tokens`
- `GET /api/tokens`
- `DELETE /api/tokens/{id}`

Fonctionnalités présentes :

- génération d'un token avec permissions
- listing des tokens de l'utilisateur courant
- construction de l'URL serveur dans la réponse

### 13 - ExternalSource + AggregatorService
**Statut : Fait avec dépendances externes**

Implémentation de :

- entité `ExternalSource`
- ajout d'une source externe
- agrégation des lieux via appel HTTP externe
- transmission du token dans le header
- gestion des erreurs `401` / `403`
- désactivation automatique d'une source sur `401`

### 14 - CollectionController
**Statut : Fait partiellement**

Endpoints présents :

- `GET /api/collections`
- `GET /api/collections/places?tagId=...`

Fonctionnalités présentes :

- filtrage des collections selon les permissions du token
- contrôle d'accès avant lecture des lieux d'une collection
- retour `403` si accès refusé

---

## Points à corriger / raffiner

## Correctifs prioritaires

### 1. Correction du DELETE token
**Priorité : Haute**

Le endpoint `DELETE /api/tokens/{id}` doit retrouver le token par son `id`, et non par sa `value`.

### 2. Durcir les validations DTO
**Priorité : Haute**

Ajouter les annotations de validation sur :

- `RegisterRequest`
- `LoginRequest`
- `CreateTokenRequest`
- `ExternalSourceRequest`

Exemples attendus :

- `@NotBlank`
- `@Email`
- `@NotNull`
- `@Future`

### 3. Uniformiser les réponses API
**Priorité : Moyenne**

Éviter de retourner directement certaines entités JPA dans les contrôleurs.  
Passer progressivement sur des DTO de réponse dédiés.

### 4. Clarifier les règles ACL
**Priorité : Moyenne**

Raffiner la logique métier autour de :

- duplication de permissions
- cohérence entre `master token` et token limité
- contrôle d'accès futur pour les endpoints d'écriture

---

## Dépendances côté projet global

Ma partie backend est déjà opérationnelle sur les briques ACL, mais certaines fonctionnalités restent dépendantes d'autres morceaux du backend global :

- l'agrégation externe appelle `GET /api/places`, endpoint encore absent
- les collections retournent actuellement les tags filtrés, mais pas encore le `count`
- l'usage complet des ACL dépendra aussi des futurs endpoints Places / Export / Import

---

## État final du sprint 1 sur ma partie

## Livré
- Auth backend
- ACL Token / Permission
- Repositories ACL
- Services ACL
- Interceptor sécurité
- API Tokens
- Sources externes
- Agrégation
- Contrôle d'accès sur collections

## À finaliser
- correction du endpoint revoke token
- validation plus stricte des DTO
- meilleure homogénéité des réponses
- branchement complet avec les endpoints métier Places

---

## Synthèse

Mon bloc du sprint 1 est fonctionnel sur le noyau sécurité / partage / fédération :

- authentification
- tokens ACL
- permissions
- sécurisation des endpoints
- agrégation multi-sources

Le reste à faire concerne surtout :
- les finitions
- les correctifs
- l'intégration complète avec les endpoints métier des lieux