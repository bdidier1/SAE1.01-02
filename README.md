# SAE1.01-02 - Pauvocodeur 🎵

Le **Pauvocodeur** est un projet Java permettant de manipuler des fichiers audio en appliquant diverses transformations telles que la dilatation temporelle, l'ajustement de la fréquence, l'écho, et bien plus encore. Ce projet a été développé par **Baptiste Didier** et **Noé Sonet**.

## 📚 Description
Pauvocodeur effectue des transformations audio avancées sur des fichiers `.wav` en utilisant des techniques telles que le resampling, la dilatation simple et avec chevauchement, et la recherche de la meilleure corrélation croisée pour une synchronisation améliorée. Il est conçu pour expérimenter et explorer les effets audio en temps réel sur des fichiers audio.

## 🛠️ Fonctionnalités

- **Dilatation simple** : Modifie la durée du signal sans chevauchement.
- **Dilatation simple avec chevauchement** : Applique un chevauchement entre les séquences pour une transition plus fluide.
- **Dilatation simple avec chevauchement et recherche de la corrélation croisée maximale** : Effectue un chevauchement avec la recherche de la meilleure corrélation pour un alignement optimal des séquences.
- **Ajout d'écho** : Permet d'ajouter un effet d'écho à l'audio.
- **Rééchantillonnage** : Modifie la fréquence d'échantillonnage du signal pour changer sa hauteur (frequencies).
- **Affichage de la forme d'onde** : Visualisation de la forme d'onde du signal.

## 🚀 Installation

### Prérequis
- Java 8 ou version supérieure.
- La bibliothèque `StdAudio` pour la lecture et l'enregistrement des fichiers audio.
- La bibliothèque `StdDraw` pour l'affichage graphique des formes d'onde.

### Cloner le projet

1. Clonez le dépôt du projet :
    ```bash
    git clone https://github.com/bdidier1/SAE1.01-02/
    ```

2. Assurez-vous que Java est installé sur votre machine. Vous pouvez vérifier en exécutant la commande suivante :
    ```bash
    java -version
    ```

3. Compilez le projet :
    ```bash
    javac Pauvocoder.java
    ```

4. Exécutez le programme avec un fichier WAV d'entrée et un facteur de fréquence comme arguments :
    ```bash
    java Pauvocoder <input.wav> <freqScale>
    ```
-   `<input.wav>` : Le fichier audio d'entrée.
-   `<freqScale>` : Le facteur de fréquence (ex. `1.5` pour accélérer ou `0.5` pour ralentir).

## 📂 Structure du projet

-   `Pauvocoder.java` : Le fichier principal du projet contenant la logique de transformation audio.
-   `StdAudio.java` : Bibliothèque pour la lecture et l'enregistrement des fichiers audio.
-   `StdDraw.java` : Bibliothèque pour afficher les formes d'onde audio.

## 📝 Logs

Les logs d'exécution sont redirigés vers un fichier `output_log.txt` dans lequel vous pouvez trouver des informations détaillées sur les transformations appliquées, comme la taille des fichiers et les progrès des différentes étapes.

## 📧 Contact
Si vous avez des questions ou des suggestions, n'hésitez pas à nous contacter :

-   **Baptiste Didier** : baptiste.didier@edu.univ-fcomte.fr
-   **Noé Sonet** : noe.sonet@edu.univ-fcomte.fr
