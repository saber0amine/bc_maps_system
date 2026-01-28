
#Architecture
┌─────────────────┐
│  Client Mobile  │
│  (Android/iOS)  │
└────────┬────────┘
│ HTTP/REST
│
┌────────▼────────┐
│   Spring Boot   │
│   REST API      │
└────────┬────────┘
│
┌────────▼────────┐
│   H2/SQLite     │
│   (embarquée)   │
└─────────────────┘

#Scénarios
### Scénario 1 : Usage basique

```
Alice ouvre l'app web
→ Voit une carte avec ses lieux
→ Clique "Ajouter un lieu"
→ Place un pin sur la carte
→ Remplit : "Restaurant Le Bistrot", tags: ["Restaurant", "Paris"]
→ Le lieu apparaît dans 3 collections:
   - "Tous mes lieux"
   - "Restaurant" (créée auto)
   - "Paris" (créée auto)
```

### Scénario 2 : Export

```
Alice veut utiliser ses restos dans Waze
→ Va dans collection "Restaurant"
→ Clique "Exporter"
→ Choisit GPX
→ Télécharge le fichier
→ L'importe dans Waze
```

### Scénario 3 : Partage simple

```
Partage Collection + Postionne Courante :
 
Alice veut montrer ses restos à Bob
→ Collection "Restaurant" → "Partager"
→ App génère un token : "xyz789"
→ App affiche : "https://alice-server.com + token: xyz789"
→ Alice envoie ça à Bob par WhatsApp

Bob reçoit le message
→ Ouvre son app web
→ Va dans "Ajouter une source"
→ Colle l'URL et le token
→ Maintenant Bob voit les restos d'Alice dans sa carte
```

### Scénario 4 : Partage entre serveurs

```
Alice (serveur alice.com)
Bob (serveur bob.com)
Charlie (serveur charlie.com)

Alice partage ses "Restos Paris" avec Bob
Bob partage ses "Bars Lyon" avec Charlie
Charlie partage ses "Musées" avec Alice

Résultat dans l'app d'Alice:
Carte affichant:
├── Mes lieux (alice.com)
│   ├── Restos Paris
│   └── Mes autres lieux
└── Sources externes
    └── Musées de Charlie (charlie.com)
```

## Interface type

**Vue principale : Carte**
```
┌─────────────────────────────────┐
│ [Menu☰] Gestion Lieux    [+Lieu]│
├─────────────────────────────────┤
│                                 │
│        🗺️ CARTE                 │
│     📍 📍   📍                   │
│   📍     📍                      │
│                                 │
├─────────────────────────────────┤
│ Collections:                    │
│ • Tous (45)                     │
│ • Restaurant (12)               │
│ • Paris (8)                     │
│ • Voyage (5)                    │
│                                 │
│ Sources externes:               │
│ • Bars de Bob (bob.com)         │
└─────────────────────────────────┘
```

**Fiche d'un lieu**
```
┌─────────────────────────────────┐
│ Restaurant Le Bistrot      [×]  │
├─────────────────────────────────┤
│ [Photo du restaurant]           │
│                                 │
│ 📍 48.8566, 2.3522              │
│                                 │
│ Description:                    │
│ Super resto italien...          │
│                                 │
│ Tags: #Restaurant #Paris        │
│                                 │
│ [Ouvrir dans Maps]              │
│ [Partager]                      │
│ [Modifier] [Supprimer]          │
└─────────────────────────────────┘
```

**Page de partage**
```
┌─────────────────────────────────┐
│ Partager "Restaurant"           │
├─────────────────────────────────┤
│ Générer un nouveau token:       │
│                                 │
│ Droits: ○ Lecture ○ Écriture   │
│                                 │
│ [Générer]                       │
│                                 │
│ Tokens actifs:                  │
│ • abc123 (lecture) - Bob        │
│   https://alice.com + abc123    │
│   [Révoquer]                    │
│                                 │
│ • xyz789 (écriture) - Charlie   │
│   https://alice.com + xyz789    │
│   [Révoquer]                    │
└─────────────────────────────────┘
```

C'est plus clair maintenant ?