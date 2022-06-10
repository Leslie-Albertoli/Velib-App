# Velib-app : ALBERTOLI Leslie et BENICHOU Yacine

### Avant de commencer : 
allez sur le fichier local.properties puis copiez-collez la ligne :
```
API_KEY="AIzaSyDME_xIWig2edHHrUODZ09y6xwNzemdg9A"
```

### Fonctionnalités de l'application :
- Respect des contraintes exigées.
- Écran de démarrage jusqu'au chargement de la carte.
- Bouton de géolocalisation permettant de trouver la position actuelle si la géolocalisation est autorisée. 
- Marqueurs customisés affichant par défaut le nombre de vélos disponible dans la station sur la carte.
- Regroupement des Marqueurs sous une seule balise selon le zoom depuis la carte
- Bouton de synchronisation permettant de mettre à jour les données des stations en rappelant l'API.
- En cliquant sur bouton de synchronisation de l'API, l'image tourne jusqu'à ce que le processus soit terminé.
- Ce bouton ne bloque pas l'interface utilisateur et informe l'utilisateur de la réussite ou de l'échec du processus.
- Barre de recherche avec suggestions permettant de rechercher une station particulière par son nom.
- Bouton de filtrage des informations sur la carte en dessous de la barre de recherche : 

    - Le bouton de gauche filtre les Velibs mécaniques et change les marqueurs en vert ainsi que l'affichage à l'intérieur de celui-ci pour que cela soit le nombre de Velibs mécaniques.
    - Le bouton de droite a la même fonction mais pour les Vélibs éléctriques. Dans ce cas, les markeurs deviendront bleus foncés.
- Sur le détail d'une station, information sur la dernière mise à jour de la station,
- Application fonctionnelle en mode paysage sur tous les écrans.
- Apllication fonctionnelle en mode hors connexion sauf pour le bouton de synchronisation de l'API.
