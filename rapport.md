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

Nous avons commencé par implémenter entièrement l’étape 1 en suivant les instructions fournies. Cette partie s’est globalement bien passée, même si certains comportements, notamment liés au centre des tiles et aux intentions de déplacement de PacMan, ont nécessité plusieurs ajustements.

Au début, nous avions une interprétation légèrement différente du moment où PacMan devait changer de direction, ce qui entraînait des erreurs dans les tests. Après analyse, nous avons corrigé la logique en nous rapprochant du comportement attendu.

L’étape 2 a été réalisée en parallèle avec une amélioration progressive de l’interface graphique. Cette partie a été plus intuitive, mais nous avons rencontré un problème sur le comptage des points restants dans le maze. Celui-ci provenait d’un décalage très léger (0.5 pixel) dans les déplacements de PacMan, ce qui faussait certaines conditions. Une fois identifié, le bug a été corrigé rapidement.

Au final, le jeu est entièrement jouable avec :

déplacements fluides de PacMan
gestion des fantômes avec leurs états (chase, scatter, frightened…)
gestion des vies, du score et des collisions
transitions entre niveaux et fin de partie

Nous avons également ajouté plusieurs éléments non demandés :

affichage du High Score
affichage de floating scores (points animés lorsqu’on mange un fantôme ou un bonus)
amélioration visuelle (cœurs, textes, boutons)
écran de fin avec boutons Play Again / Quit

### Structure et hierarchie de classe

Le projet repose sur une architecture assez claire basée sur la séparation :

model : logique du jeu (Board, Ghost, PacMan, Maze…)
control : gestion des actions utilisateur
view : affichage graphique

Nous avons principalement travaillé avec :

AbstractBoard : gestion globale du jeu (score, vies, états, logique principale)
AbstractGhost : comportement commun des fantômes
PacMan : logique spécifique du joueur
BasicPacManView : affichage graphique complet (maze, acteurs, UI)

Nous avons respecté globalement la structure proposée, mais nous avons enrichi certaines classes, notamment :

ajout du high score dans le board
ajout d’un système d’événements (BoardEvent) pour gérer les floating scores
ajout de logique UI directement dans la vue pour améliorer l’expérience utilisateur

Ces choix nous ont permis de garder une logique propre tout en ajoutant des fonctionnalités visuelles sans casser l’architecture existante.

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
