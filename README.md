# LEVELOP - Plateforme Communautaire Gaming

Une plateforme communautaire gaming complète développée en Java avec JavaFX, combinant e-commerce, forums de discussion, gestion d'événements, services de coaching et assistance gaming basée sur l'IA pour les joueurs.

## Table des Matières

- [À propos](#à-propos)
- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Structure du Projet](#structure-du-projet)
- [Technologies Utilisées](#technologies-utilisées)
- [Configuration](#configuration)
- [Contribuer](#contribuer)
- [Licence](#licence)
- [Contact](#contact)

## À propos

LEVELOP est une application desktop Java qui offre une solution complète pour la communauté gaming, permettant aux utilisateurs de :
- Gérer des sessions de coaching gaming
- Effectuer des achats de produits gaming
- Participer à des événements communautaires
- Recevoir une assistance gaming basée sur l'IA
- Gérer leurs réservations et paiements
- Interagir avec la communauté
- Estimer les performances de jeu selon leur configuration matérielle

## Fonctionnalités

### Gestion des Sessions de Coaching
- Création et gestion des sessions de coaching
- Système de réservation en temps réel
- Gestion des disponibilités des coachs
- Sessions promotionnelles
- Système de paiement intégré via Stripe

### E-commerce
- Catalogue de produits gaming
- Système de panier
- Paiement sécurisé via Stripe
- Gestion des commandes
- Support multi-devises
- Estimation FPS basée sur la configuration matérielle

### Assistant Gaming IA
- Intégration avec Gemini AI
- Recommandations personnalisées
- Assistance en temps réel
- Analyse des performances de jeu
- Estimation FPS basée sur l'IA
- Détection automatique des spécifications matérielles

### Gestion des Événements
- Création et gestion d'événements
- Système de réservation
- Notifications par email
- Gestion des participants

### Interface Utilisateur
- Design moderne et gaming-oriented
- Thème sombre avec accents néon
- Animations fluides
- Support des emojis et réactions
- Interface responsive
- Alertes personnalisées

## Prérequis

- Java JDK 17 ou supérieur
- Maven 3.6 ou supérieur
- MySQL 8.0 ou supérieur
- XAMPP (pour le serveur local)
- Dossier d'images configuré à `C:\xampp\htdocs\img\`

## Installation

1. Cloner le dépôt :
```bash
git clone https://github.com/feresad/PidevjavaLevelOP.git
cd PidevjavaLevelOP
```

2. Configurer la base de données :
- Démarrer XAMPP
- Créer une base de données MySQL
- Importer le schéma de la base de données

3. Configurer les variables d'environnement :
- Créer un fichier `.env` à la racine du projet
- Ajouter les configurations nécessaires (voir section Configuration)

4. Compiler le projet :
```bash
mvn clean install
```

5. Lancer l'application :
```bash
mvn javafx:run
```

## Structure du Projet

```
src/main/java/tn/esprit/
├── Controllers/     # Contrôleurs JavaFX
├── Models/         # Classes de modèle
├── Services/       # Services métier
├── Interfaces/     # Interfaces du projet
├── utils/          # Utilitaires
└── MainFx.java     # Point d'entrée de l'application
```

## Technologies Utilisées

- Java 17
- JavaFX 21
- MySQL 8.0
- Maven
- Stripe API
- Gemini AI API
- BCrypt pour le hachage des mots de passe
- Apache POI pour la manipulation de documents
- PDFBox pour la génération de PDF
- ZXing pour la génération de QR codes
- OSHI pour la détection du matériel
- OkHttp pour les requêtes HTTP
- Jackson pour le traitement JSON

## Configuration

Créez un fichier `.env` avec les configurations suivantes :

```env
DB_URL=jdbc:mysql://localhost:3306/your_database
DB_USER=your_username
DB_PASSWORD=your_password
STRIPE_API_KEY=your_stripe_key
GEMINI_API_KEY=your_gemini_key
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email
SMTP_PASSWORD=your_email_password
```

## Contribuer

1. Forker le projet
2. Créer une branche pour votre fonctionnalité
3. Commiter vos changements
4. Pousser vers la branche
5. Ouvrir une Pull Request

## Contact

- Email : levelopcorporation@gmail.com
- GitHub : [github.com/feresad/PidevjavaLevelOP](https://github.com/feresad/PidevjavaLevelOP)

## Équipe de Développement

### Développeurs Principaux
- [@feresad](https://github.com/feresad)
- [@rayN3A7](https://github.com/rayN3A7) 
- [@hsounaSellami](https://github.com/hsounaSellami) 
- [@hazemmtir0](https://github.com/hazemmtir0) 
- [@H1000Rekik](https://github.com/H1000Rekik)

### Technologies Maîtrisées
- Java & JavaFX
- MySQL & Base de données
- Intégration API (Stripe, Gemini)
- Sécurité & Performance
- Design UI/UX Gaming

## Licence

Propriétaire - Tous droits réservés 
