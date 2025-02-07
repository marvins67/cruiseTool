# CruiseTool

CruiseTool est un projet Java permettant la lecture et l’extraction de données à partir de fichiers de configuration de volume du jeu "Croisière pour un cadavre".
Il s'agit d'une adaptation en Java de ce qui est fait dans ScummVM.
Il comprend plusieurs classes responsables du traitement des fichiers binaires et de la gestion des volumes.

## Structure du projet

Le projet est organisé comme suit :

```
cruiseTool/
│── src/com/iterevg/
│   ├── Cruise.java
│   ├── Main.java
│   ├── Unpack.java
│   ├── VolumeConfigReader.java
│── src/com/iterevg/vars/
│   ├── DataFileName.java
│   ├── VolumeDataStruct.java
│── cruiseTool.iml
```

### Fichiers principaux

- **Main.java** : Point d’entrée du programme.
- **Cruise.java** : Gère l’initialisation du volume à partir de `VolumeConfigReader`.
- **VolumeConfigReader.java** : Lit et analyse le fichier `VOL.CNF`, extrait et décompresse les fichiers des volumes.
- **Unpack.java** : Implémente un algorithme de décompression de données binaires.
- **DataFileName.java** et **VolumeDataStruct.java** : Définitions de structures pour stocker les informations des fichiers et des volumes.

## Fonctionnalités

- Lecture du fichier `VOL.CNF` pour identifier les volumes disponibles.
- Extraction des fichiers stockés dans les volumes.
- Décompression des fichiers si nécessaire en utilisant `Unpack.java`.
- Gestion des structures de données pour manipuler les informations des volumes et fichiers.

## Contributions

Les contributions sont les bienvenues ! Pour proposer une modification :
1. Fork le projet.
2. Crée une branche.
3. Apporte tes modifications.
4. Fais une pull request.

## Licence

Ce projet est sous licence MIT.

---
Développé par marvins67
