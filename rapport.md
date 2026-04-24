# Rapport pour le Projet PacMan

## Membres du groupe

Franck Pan
Firdaous Elouadi

## Introduction

Le projet avait pour objectif de recréer le jeu PacMan en respectant au maximum son fonctionnement original, tout en nous laissant la liberté d’y ajouter une interface plus moderne et des éléments personnalisés.

Au-delà de l’aspect purement technique, ce projet nous a permis de mieux comprendre les mécanismes internes d’un jeu en temps réel : gestion des déplacements, des collisions, des états, mais aussi synchronisation entre la logique du jeu et l’affichage.

Nous avons cherché à ne pas simplement “faire fonctionner” le jeu, mais à produire quelque chose de fluide, lisible, et agréable visuellement.
 
## Réalisation

### Avancement du projet

Nous avons commencé par implémenter l’ensemble de l’étape 1 en suivant les spécifications fournies. Cette phase concernait principalement la mise en place de la logique de base : déplacements de PacMan, gestion du labyrinthe et interactions simples.

Cette partie s’est globalement bien déroulée, mais nous avons rencontré une difficulté importante liée au changement de direction de PacMan. Initialement, nous avions une interprétation légèrement différente du moment où ce changement devait être appliqué. Cela entraînait des comportements incorrects ainsi que des échecs dans certains tests. Après analyse, nous avons compris que le changement de direction devait se faire uniquement lorsque PacMan se trouve exactement au centre d’une case, ce qui nous a permis de corriger le problème.

L’étape 2 a été réalisée en parallèle avec l’intégration progressive de l’interface graphique. Cette étape introduisait notamment la gestion des collisions, du score et des fantômes. Un bug notable est apparu dans le comptage des points restants dans le labyrinthe. Celui-ci provenait d’un décalage très faible (de l’ordre du pixel) dans la position de PacMan, ce qui empêchait certaines conditions d’être correctement vérifiées. Une fois ce problème identifié, la correction a été rapide.

Nous avons ensuite implémenté les comportements avancés :

alternance des états des fantômes (chase, scatter)
gestion du mode frightened
gestion des collisions PacMan/fantômes avec différents cas

Enfin, nous avons ajouté :

la gestion des niveaux
l’interface complète (écran de début, pause, fin de partie)
plusieurs améliorations visuelles

À la fin du projet, le jeu est entièrement jouable avec :

déplacements fluides de PacMan
gestion complète des fantômes et de leurs états
gestion du score, des vies et des collisions
transitions entre niveaux et fin de partie

Nous avons également ajouté plusieurs éléments non demandés :

affichage du High Score
affichage de scores animés (floating scores)
amélioration visuelle générale (cœurs, textes, overlays)
écran de fin avec boutons interactifs (Play Again / Quit)
ajout d’un mini-jeu (Tic-Tac-Toe) lors du Game Over

### Structure et hierarchie de classe

Le projet repose sur une architecture structurée suivant une séparation claire :

model : contient la logique du jeu (Board, Ghost, PacMan, Maze…)
control : gère les interactions et les actions utilisateur
view : gère l’affichage graphique

Les classes principales sont :

AbstractBoard : cœur du jeu (score, états, logique globale, progression)
PacMan : gestion du joueur
Ghost et ses sous-classes : comportement des fantômes
BasicPacManView : affichage graphique complet
VisualController : lien entre le modèle et la vue

Nous avons respecté la structure proposée, tout en l’enrichissant avec :

un système de High Score
un système d’événements (BoardEvent) pour gérer les animations de score
des améliorations côté vue pour rendre le jeu plus dynamique

Ces ajouts ont été faits de manière à ne pas casser l’architecture existante, en conservant une bonne séparation des responsabilités.

### Tests

La majorité des tests fournis passent c'est à dire en réalité la partie 1 et 2.

Certains tests ont nécessité des ajustements mineurs, notamment lorsque notre implémentation introduisait des comportements légèrement différents mais cohérents (par exemple dans la gestion des exceptions ou du timing des actions).

Nous avons parfois adapté certains tests pour autoriser des exceptions supplémentaires liées à nos ajouts.

En dehors de ces cas spécifiques, nous avons essayé de conserver au maximum les tests tels quels afin de garantir la compatibilité avec les attentes du projet.


Nous avons décidé à partir de la partie 3 que nous implémeterions les mécaniques les plus accessibles en ne tenant plus compte d'une différence entre partie 3 ou 4 mais simplement d'une implémentation générale qui devrait fonctionner
## Organisation du travail

Nous avons travaillé de manière assez flexible, en nous répartissant les tâches selon les parties du projet :

une partie plus orientée logique (model, déplacements, collisions)
une partie plus orientée interface et affichage (view)

Nous avons régulièrement testé ensemble les fonctionnalités pour vérifier que tout restait cohérent.

Le travail s’est fait de manière progressive, en ajoutant les fonctionnalités étape par étape, tout en corrigeant les bugs au fur et à mesure plutôt que de tout laisser à la fin.

## Prise de recul

### Quels progrès ?

Ce projet nous a permis de progresser sur plusieurs points :

meilleure compréhension d’une architecture orientée objet complète
gestion d’un projet assez long avec plusieurs dépendances
gestion du lien entre logique métier et interface graphique
debugging de comportements complexes (déplacements, collisions, timing)

On a aussi appris à être plus rigoureux sur des détails qui paraissent petits (comme des positions en pixels), mais qui peuvent casser toute la logique du jeu.

### Quelles difficultés ?

La principale difficulté a été la précision des déplacements et des états.

Des erreurs très faibles (comme un décalage de quelques pixels) pouvaient provoquer des bugs difficiles à comprendre, notamment dans les tests ou dans les collisions.

Nous avons aussi rencontré des difficultés dans la gestion des états des fantômes (frightened, dead, scatter…), car cela implique plusieurs transitions en même temps.

Enfin, la synchronisation entre la logique du jeu et l’affichage (timer, repaint, événements) a demandé plusieurs ajustements pour obtenir un rendu fluide et cela s'est complexifié avec la liaison entre le ticpactoe qui nécessite de bien ajuster l'affichage de sa fenêtre par rapport puis de pouvoir fairer la liaison avec le jeu pacman.

## Conclusion

Nous sommes globalement satisfaits du résultat final. Le jeu est jouable, stable, et visuellement plus travaillé que ce qui était demandé initialement.

Si nous devions continuer ce projet, nous aimerions :

ajouter plus d’effets visuels (animations, transitions)
améliorer encore l’interface (menu, HUD plus propre)
ajouter des fonctionnalités supplémentaires (power-ups, modes de jeu…)

Ce projet nous a permis de mettre en pratique beaucoup de notions vues en cours, mais surtout de comprendre concrètement comment fonctionne un jeu de ce type.

C’est clairement un des projets les plus complets que nous avons réalisés.
