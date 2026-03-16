# Places API

Base URL : `http://localhost:8080/api/places`

Chaque requête doit avoir le header `Authorization: Bearer <token>`.

---

## Créer un lieu

`POST /api/places`

Le body attend un titre, une latitude et une longitude obligatoires. La description, l'image et les tags sont optionnels. Si un tag n'existe pas encore en base, il est créé automatiquement.

```json
{
  "title": "ISIMA",
  "description": "École d'ingénieurs à Clermont-Ferrand",
  "latitude": 45.7596,
  "longitude": 3.1098,
  "imageUrl": null,
  "tags": ["École", "Clermont"]
}
```

Retourne un `201` avec le lieu créé, incluant son `id` généré et les tags attachés.

---

## Voir un lieu

`GET /api/places/{id}`

Retourne un `200` avec les détails du lieu. Si le lieu n'existe pas, retourne `404`. Si le lieu appartient à quelqu'un d'autre, retourne `403`.

---

## Lister ses lieux

`GET /api/places`

Retourne une page de lieux. Par défaut, page 0 avec 20 éléments. Pour naviguer, ajouter `page` et `size` en paramètres.

```
GET /api/places?page=0&size=10
```

La réponse contient un tableau `content` avec les lieux, et un objet `page` avec `number`, `size`, `totalElements` et `totalPages`.

---

## Modifier un lieu

`PUT /api/places/{id}`

Même body que la création. Tous les champs sont remplacés — les anciens tags sont supprimés et remplacés par les nouveaux.

---

## Supprimer un lieu

`DELETE /api/places/{id}`

Retourne `204` si la suppression a réussi. Les liens avec les tags sont supprimés mais les tags eux-mêmes restent.

---

## Recherche

`GET /api/places/search`

Deux filtres disponibles, utilisables séparément ou ensemble.

`query` cherche dans le titre et la description. `tag` filtre par nom de tag exact.

```
GET /api/places/search?query=école&tag=Clermont&page=0&size=10
```

---

## Position courante

La position courante est un lieu spécial mis à jour par le GPS du téléphone.

Pour mettre à jour : `PUT /api/places/position?lat=45.7596&lng=3.1098`

Pour récupérer : `GET /api/places/position`

Pour supprimer : `DELETE /api/places/position`

Si aucune position n'existe encore, elle est créée automatiquement lors du premier `PUT`.

---

## Codes de réponse

`200` succès, `201` créé, `204` supprimé, `400` données invalides, `403` accès refusé, `404` introuvable, `500` erreur serveur.