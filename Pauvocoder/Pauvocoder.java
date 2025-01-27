/**
 * PAUVOCODEUR - Groupe 7
 * Baptiste DIDIER & Noé Sonet
 * *
 * *
 * N'hésitez pas à nous contacter en cas de soucis aux adresses mails suivantes
 * - baptiste.didier@edu.univ-fcomte.fr
 * - noe.sonet@edu.univ-fcomte.fr
 */


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static java.lang.System.*;

public class Pauvocoder{

    // Processing SEQUENCE size (100 msec with 44100Hz samplerate)
    final static int SEQUENCE = StdAudio.SAMPLE_RATE / 10;

    // Overlapping size (20 msec) : Taille chevauchement
    final static int OVERLAP = SEQUENCE / 5;

    // Best OVERLAP offset seeking window (15 msec)
    final static int SEEK_WINDOW = 3 * OVERLAP / 4;

    public static void main(String[] args) {

        //REDIRECTION DES PRINTS DANS UN FICHIER TEXTE
        try {
            // Créer un fichier pour enregistrer les sorties
            PrintStream out = new PrintStream(new File("output_log.txt"));
            // Rediriger System.out vers le fichier
            System.setOut(out);
        } catch (IOException e) {
            System.err.println(
                    "Erreur de redirection du flux de sortie : "
                            + e.getMessage());
        }

        //Message d'erreur lorsqu'on a rien mis en paramètres
        if (args.length < 2) {
            System.out.println("usage: pauvocoder <input.wav> <freqScale>\n");
            exit(1);
        }



        String wavInFile = args[0];
        double freqScale = Double.valueOf(args[1]);
        String outPutFile = wavInFile.split("\\.")[0] + "_" + freqScale + "_";

        // Open input .wev file
        double[] inputWav = StdAudio.read(wavInFile);

        // Resample test
        double[] newPitchWav = resample(inputWav, freqScale);
        //Message d'erreur lorsque la fréquence est plus petite ou égale à 0
        if (freqScale <= 0) {
            System.out.println(
                    "[ ERREUR ] Impossible de lancer le programme, " +
                            "veuillez entrer une valeur strictement supérieur à 0 !");
            exit(1);
        }
        StdAudio.save(outPutFile + "Resampled.wav", newPitchWav);


        System.out.println(" ");

        // Simple dilatation
        double[] outputWav = vocodeSimple(newPitchWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "Simple.wav", outputWav);
        // Afficher la taille du tableau de sortie pour prouver la dilatation

        // Simple dilatation with overlaping
        outputWav = vocodeSimpleOver(newPitchWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "SimpleOver.wav", outputWav);

        // Simple dilatation with overlaping and maximum cross correlation search
        outputWav = vocodeSimpleOverCross(newPitchWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "SimpleOverCross.wav", outputWav);

        joue(outputWav);

        // Some echo above all
        outputWav = echo(outputWav, 100, 0.7);
        StdAudio.save(outPutFile + "SimpleOverCrossEcho.wav", outputWav);

        // Display waveform
        displayWaveform(newPitchWav);
    }


    /**
     * Resample inputWav with freqScale
     *
     * @param input
     * @param freqScale
     * @return resampled wav
     */
    public static double[] resample(double[] input, double freqScale) {
        System.out.println("\nDébut de la Méthode RESAMPLE");
        System.out.println("[ RESAMPLE ] Facteur de fréquence : " + freqScale);
        System.out.println(
                "[ RESAMPLE ] Taille d'entrée initiale : " + input.length
        );

        // Tableau de sortie qui est retourné à la fin du programme
        // avec les données redimensionnées
        double outputWav[];

        // Variable pour calculer le nb d'échantillons à sauter ou ajouter
        double nb_echantillon;

        // Calcul de la taille de sortie selon la fréquence
        int tailleSortie;
        // Déclarer la bonne formule de la taille de sortie selon la fréquence
        if (freqScale >= 1) {
            tailleSortie = (int)(input.length / freqScale);
            System.out.println("" +
                    "[ RESAMPLE ]  Cas freqScale >= 1 : nouvelle taille = "
                    + tailleSortie
            );
        } else {
            // Arrondit à l'entier supérieur pour éviter de manquer un
            // échantillon
            tailleSortie = (int)(input.length / freqScale) + 1;
            System.out.println(
                    "[ RESAMPLE ]  Cas freqScale < 1 : nouvelle taille = "
                            + tailleSortie);
        }

        //Initialisation du tableau de sortie avec la taille calculée
        outputWav = new double[tailleSortie];
        System.out.println(
                "[ RESAMPLE ] Création tableau de sortie de taille : "
                        + tailleSortie);

        // Si la fréquence est de 1, on renvoie le tableau d'entrée
        if (freqScale == 1) {
            System.out.println("" +
                    "[ RESAMPLE ] Cas particulier : freqScale = 1, copie simple"
            );
            for(int i = 0; i < tailleSortie; i++) {
                outputWav[i] = input[i];
            }
        }
        // Si la fréquence est supérieur à 1, on calcul le
        // nombre d'échantillons à ajouter (suréchantillonnage)
        else if (freqScale > 1) {
            nb_echantillon = (freqScale - 1.0) / freqScale;
            tailleSortie = (int)(input.length * (1 - nb_echantillon));
            System.out.println("" +
                    "[ RESAMPLE ] Recalcul taille pour freqScale > 1 : "
                    + tailleSortie
            );
            System.out.println("" +
                    "[ RESAMPLE ] Nombre d'échantillons : "
                    + nb_echantillon
            );
        }
        // Si la fréquence est inférieur à 1, on calcul le nombre d'échantillons à enlever (sous-échantillonnage)
        else if (freqScale < 1) {
            nb_echantillon = (1.0 - freqScale) / freqScale;
            tailleSortie = (int)(input.length * (1 + nb_echantillon));
            System.out.println(
                    "[ RESAMPLE ] Recalcul taille pour freqScale < 1 : "
                            + tailleSortie
            );
            System.out.println(
                    "[ RESAMPLE ] Nombre d'échantillons : "
                    + nb_echantillon
            );
        }

        // Parcourt le tableau de sortie et met les valeurs en
        // fonction de la fréquence
        System.out.println("\n[ RESAMPLE ] Début du rééchantillonnage...");
        int compteur = 0;
        for (int i = 0; i < tailleSortie; i++) {
            int saut = (int)(i * freqScale);
            if (saut < input.length) {
                outputWav[i] = input[saut];
                compteur++;
                if (i % (tailleSortie/10) == 0) {
                    System.out.println(
                            "[ RESAMPLE ] Progression : "
                            + (i * 100 / tailleSortie)
                            + "% - Position : "
                            + i + "/"
                            + tailleSortie
                    );
                    System.out.println("" +
                            "[ RESAMPLE ] Valeur échantillon : "
                            + outputWav[i] + " (saut=" + saut + ")");
                }
            } else {
                // insère 0.0 si l'indice dépasse les limites de l'entrée
                outputWav[i] = 0.0;
                System.out.println(
                        "[ RESAMPLE ] Position hors limites : i="
                                + i
                                + ", saut="
                                + saut
                                + " > "
                                + input.length
                );
            }
        }

        System.out.println("\n[ RESAMPLE ] Statistiques finales :");
        System.out.println(
                "[ RESAMPLE ] Nombre d'échantillons traités : "
                + compteur
        );
        System.out.println(
                "[ RESAMPLE ] Taille finale du tableau : "
                + outputWav.length
        );
        System.out.println(
                "[ RESAMPLE ] Ratio de compression : "
                        + String.format("%.2f",
                        (double)input.length/outputWav.length));
        System.out.println("Fin de la méthode RESAMPLE \n");

        return outputWav;
    }


    /**
     * Simple dilatation, without any overlapping
     *
     * @param input
     * @param timeScale factor
     * @return dilated wav
     */
    public static double[] vocodeSimple(double[] input, double timeScale) {
        System.out.println("\nDébut de la Méthode VOCODESIMPLE ");
        System.out.println("[ SIMPLE ] Facteur de dilatation : " + timeScale);
        System.out.println("[ SIMPLE ] Taille de séquence : " + SEQUENCE);

        // Tableau de sortie qui est retourné à la fin du programme
        // avec les données redimensionnées
        double outputWav[];
        // Variable saut qui calcul le saut à effectué entre chaque
        // échantillons selon la fréquence de dilatation
        double saut = SEQUENCE * timeScale;

        int tailleSortie = (int) (input.length / timeScale);

        // Affichage : traces d'éxécutions écrites
        System.out.println("[ SIMPLE ] Taille du saut : " + saut);
        System.out.println("[ SIMPLE ] Séquence : " + SEQUENCE);

        //Initialisation du tableau de sortie avec la taille calculée
        outputWav = new double[tailleSortie];

        int position = 0;
        System.out.print("[ TEST SIMPLE ] Position : ");

        // Parcourt les échantillons d'entrée et copie les séquences
        // redimensionnées dans le tableau de sortie
        for(double i = 0; i<input.length && position<tailleSortie; i+=saut){
            System.out.print(" [" + position + "]" );

            for(int j = 0;j<SEQUENCE;j++){

                //limites du tableau à ne pas franchir
                if(i+j >= input.length || position >= tailleSortie){
                    break; // quitte la boucle si les limites sont atteintes
                }

                outputWav[position++] = input[(int) (i + j)];
            }
        }
        System.out.println(
                "[ SIMPLE ] Taille de la sortie : "
                + tailleSortie
        );
        System.out.println("Fin de la méthode VOCODESIMPLE\n");
        return outputWav;
    }


    /**
     * Simple dilatation, with overlapping
     * @param input
     * @param timeScale factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOver(double[] input, double timeScale) {
        System.out.println("\nDébut de la Méthode VOCODESIMPLEOVER ");
        System.out.println(
                "[ SIMPLEOVER ] Facteur de dilatation : "
                        + timeScale
        );
        System.out.println(
                "[ SIMPLEOVER ] Taille de chevauchement : "
                        + OVERLAP
        );

        int sequence = SEQUENCE + OVERLAP;
        // Taille d'une séquence avec le// chevauchement
        int saut = (int) (SEQUENCE * timeScale);
        // calcul du saut
        int position = OVERLAP;
        // definition de position
        int tailleSortie = (int) (input.length / timeScale) + 1;
        // Taille du tableau de sortie
        double[] outputWav = new double[tailleSortie];
        // Tableau de sortie

        for (int i = 0; i < input.length; i += saut) {
            int debut_seq = i;
            if (i + SEQUENCE >= input.length) {
                debut_seq = input.length - SEQUENCE;
            }

            double[] listeCoeff = new double[OVERLAP];
            // Coefficients  pour le chevauchement

            for (int j = 0; j < sequence; j++) {
                int entree_index = debut_seq + j;

                // Arrête si on dépasse les limites
                if (entree_index >= input.length ||
                        position >= outputWav.length) {
                    break;
                }

                //pondération ascendante : début
                if (j < OVERLAP) {
                    double coefficient = (double) j / OVERLAP;
                    listeCoeff[j] = coefficient;
                    outputWav[position++] += input[entree_index] * coefficient;
                }
                //pas de pondération
                else if (j >= OVERLAP && j < sequence - OVERLAP) {
                    outputWav[position++] += input[entree_index];
                }
                //pondération descendante : fin
                else {
                    double coefficient = (double) (sequence - j) / OVERLAP;
                    listeCoeff[j - sequence + OVERLAP] += coefficient;
                    outputWav[position++] += input[entree_index] * coefficient;
                }
            }
        }
        System.out.println(
                "[ SIMPLEOVER ] Taille finale du signal : "
                        + tailleSortie
        );
        System.out.println("Fin de la méthode VOCODESIMPLEOVER \n");
        return outputWav;
    }

    /**
     * Simple dilatation, with overlapping and maximum cross correlation search
     *
     * @param input
     * @param timeScale factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOverCross( double[] input, double timeScale) {
        System.out.println("\nDébut de la méthode OVERCROSS");
        System.out.println("[ OVERCROSS ] Facteur de dilatation : " + timeScale);

        int sequence = SEQUENCE + OVERLAP;
        int saut = (int) (SEQUENCE * timeScale);
        // Calcul du saut en fonction du facteur de dilatation
        int tailleSortie = (int) (input.length / timeScale) + 1;
        double[] outputWav = new double[tailleSortie];  // Tableau de sortie

        int position = OVERLAP;  // Début de la position dans la sortie
        int decalage = 0;  // Décalage initial

        // Boucle à travers le tableau d'entrée
        for (int i = 0; i < input.length; i += saut) {
            System.out.println(
                    "[ OVERCROSS ] Traitement position : "
                            + i + "/" + input.length);

            // Décalage et mixage des données
            position -= OVERLAP;  // Décalage pour chevauchement
            for (int j = 0; j < sequence; j++) {
                int index = i + j + decalage;
                if (index >= input.length || position >= outputWav.length)
                    break;

                // Pondération ascendante et descendante
                if (j < OVERLAP) {
                    // Coefficient de pondération pour les premières
                    // positions du segment (montée)
                    double coef = (double) j / OVERLAP;
                    outputWav[position++] += input[index] * coef;
                } else if (j >= sequence - OVERLAP) {
                    // Coefficient de pondération pour
                    // les dernières positions du segment (descente)
                    double coef = (double) (sequence - j) / OVERLAP;
                    outputWav[position++] += input[index] * coef;
                } else {
                    // Les valeurs sont directement ajoutées
                    // sans modification (pondération neutre)
                    outputWav[position++] += input[index];
                }
            }

            // Recherche du meilleur décalage
            double meilleureSimilarite = -Double.MAX_VALUE;
            int meilleurDecalage = 0;

            for (int decalageTest = 1; decalageTest < SEEK_WINDOW; decalageTest++) {
                double sim = 0;

               // Calcul de la similarité pour chaque position de chevauchement
                for (int j = 0; j < OVERLAP; j++) {
                    // Vérification des limites du tableau
                    if (i + sequence + decalage - OVERLAP + j < input.length &&
                            i + saut + OVERLAP + decalageTest - j < input.length) {
                   // Calcule la similarité en multipliant les éléments
                   // correspondants des deux segments
                   // Ces segments sont situés dans les zones de chevauchement
                        sim += input[i + sequence + decalage - OVERLAP + j]
                                * input[i + saut + OVERLAP + decalageTest - j];
                    }
                }

                // Si la similarité est meilleure que celle précédemment
                // enregistrée, on met à jour
                // la similarité maximale et le décalage optimal
                if (sim > meilleureSimilarite) {
                    meilleureSimilarite = sim;
                    meilleurDecalage = decalageTest;
                }
            }

            decalage = meilleurDecalage;  // Met à jour le décalage optimal
        }

        System.out.println("[ OVERCROSS ] Taille finale du signal : " + tailleSortie);
        System.out.println("Fin de la méthode OVERCROSS \n");
        return outputWav;
    }

    /**
     * Play the wav
     *
     * @param wav
     */
    public static void joue(double[] wav) {
        System.out.println("\nDébut de la méthode JOUE");
        System.out.println("[ JOUE ] Lecture du signal audio...");
        displayWaveform(wav);
        StdAudio.play(wav);
        System.out.println("Fin de la méthode JOUE\n");
    }


    /**
     * Add an echo to the wav
     *
     * @param input
     * @param delayMs in msec
     * @param attn
     * @return wav with echo
     */
    public static double[] echo(double[] input, double delayMs, double attn) {
        System.out.println("\nDébut de la méthode ECHO :");
        System.out.println("[ ECHO ] Délai (ms) : " + delayMs);
        System.out.println("[ ECHO ] Atténuation : " + attn);

        // Conversion du délai en nombre d'échantillons
        int delay = (int)(delayMs * StdAudio.SAMPLE_RATE / 1000);

        for(int i = delay; i < input.length; i++) {
            // Récupération de l'échantillon , écho
            double echoSample = input[i - delay];

            // Application de l'atténuation pour l'écho
            double attenuationEcho = echoSample * attn;

            // Addition de l'écho atténué au signal original
            double melangeSignal = input[i] + attenuationEcho;

            // Limitation de l'amplitude entre -1.0 et 1.0 comme demande
            if (melangeSignal > 1.0) {
                melangeSignal = 1.0;
            }
            else if (melangeSignal < -1.0) {
                melangeSignal = -1.0;
            }else{
                input[i] = melangeSignal;
            }
        }
        System.out.println("Fin de la méthode ECHO \n");
        return input;
    }

    /**
     * Display the waveform
     *
     * @param wav
     */
    public static void displayWaveform(double[] wav) {
            System.out.println("\nDébut de la méthode DISPLAYWAVEFORM");
            System.out.println("[ WAVEFORM ] Affichage de la forme d'onde...");

            StdDraw.enableDoubleBuffering();
            StdDraw.setXscale(0, wav.length);
            StdDraw.setYscale(-1, 1);
            StdDraw.setPenRadius(0.005);
            StdDraw.clear();
            StdDraw.setPenColor(StdDraw.GRAY);

            int i = 0;
            while (i < wav.length - 1) {
                double x1 = i;
                double y1 = wav[i];
                double x2 = i + 1;
                double y2 = wav[i + 1];
                StdDraw.line(x1, y1, x2, y2);
                i++;
            }

            StdDraw.show();
            System.out.println("Fin de la méthode DISPLAYWAVEFORM\n");
        }
    }