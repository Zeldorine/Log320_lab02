package log320_lab02;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Stack;

/**
 *
 * @author Zeldorine
 */
public class LOG320_LAB02 {

    private static int plateau[][];
    private static final Stack<Coup> solution = new Stack();
    private static int nbNoeudsExplores = 0;
    private static boolean puzzleValide = true;
    private static int nbLignes;
    private static int nbColones;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Erreur nombre d'argument.");
            System.exit(0);
        }

        try {
            if (!lirePlateau(args[0])) {
                System.exit(-1);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du plateau. Chemin du fichier : " + args[0] + ".\n");
            e.printStackTrace();
        }

        System.out.println("Plateau initial :");
        afficherPlateau();

        Timer.start();
        puzzleValide = resoudre();
        Timer.stop();

        afficherResultat();
    }

    // Pour ameliorer le temps d'execution, il faudrait partir 4 Thread. 
    // Chacun testera une direction de depart {Haut,Bas,Gauche,Droite}.
    // Regle :
    //    1 : Le seul mouvement possible est le saut par-dessus une tige si la destination est libre;
    //    2 : Lors d’un saut par dessus une tige, la tige en question est éliminée du plateau;
    //    3 : Il y a 4 directions possibles : haut, bas, gauche, droite
    private static boolean resoudre() {
        nbNoeudsExplores++;

        if (estResolue()) {
            return true;
        }

        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColones; j++) {
                Position position = new Position(i, j);

                if (plateau[i][j] != 0 && plateau[i][j] != 2) {
                    for (Mouvement mouvement : Mouvement.values()) {
                        if (coupValide(position, mouvement)) {
                            if (jouer(position, mouvement)) {
                                return true;
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    private static boolean jouer(Position position, Mouvement mouvement) {
        Coup coup = new Coup(position, mouvement);
        // System.out.println("coup -> " + coup.toString());
        //afficherPlateau();

        // stocke resultat
        solution.push(coup);

        // essayer un prochain mouvement
        if (resoudre()) {
            return true;
        }

        //Rollback
        retourArriere(position, mouvement);
        solution.pop();

        return false;
    }

    private static void retourArriere(Position depart, Mouvement mouvement) {
        switch (mouvement) {
            case HAUT:
                plateau[depart.x - 1][depart.y] = 1;
                plateau[depart.x][depart.y] = 1;
                plateau[depart.x - 2][depart.y] = 2;
                break;
            case BAS:
                plateau[depart.x + 1][depart.y] = 1;
                plateau[depart.x][depart.y] = 1;
                plateau[depart.x + 2][depart.y] = 2;
                break;
            case DROITE:
                plateau[depart.x][depart.y + 1] = 1;
                plateau[depart.x][depart.y] = 1;
                plateau[depart.x][depart.y + 2] = 2;
                break;
            case GAUCHE:
                plateau[depart.x][depart.y - 1] = 1;
                plateau[depart.x][depart.y] = 1;
                plateau[depart.x][depart.y - 2] = 2;
                break;
        }
    }

    private static boolean estResolue() {
        int nbTige = 0;

        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColones; j++) {
                if (plateau[i][j] == 1) {
                    if (nbTige == 0) {
                        nbTige++;
                    } else {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static Position trouverDepart() {
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColones; j++) {
                if (plateau[i][j] == 2) {
                    return new Position(i, j);
                }
            }
        }

        return null;
    }

    // Regle :
    //    1 : Le seul mouvement possible est le saut par-dessus une tige si la destination est libre;
    //    2 : Lors d’un saut par dessus une tige, la tige en question est éliminée du plateau;
    private static boolean coupValide(Position depart, Mouvement direction) {
        switch (direction) {
            case HAUT:
                if (depart.x - 1 < 0 || depart.x - 2 < 0) {
                    return false;
                }

                if (plateau[depart.x - 1][depart.y] == 1 && plateau[depart.x - 2][depart.y] == 2) {
                    // remove tige du plateau
                    plateau[depart.x - 1][depart.y] = 2;

                    // executer mouvement
                    plateau[depart.x][depart.y] = 2;
                    plateau[depart.x - 2][depart.y] = 1;

                    return true;
                }

                return false;
            case BAS:
                if (depart.x + 1 >= nbLignes || depart.x + 2 >= nbLignes) {
                    return false;
                }

                if (plateau[depart.x + 1][depart.y] == 1 && plateau[depart.x + 2][depart.y] == 2) {
                    // remove tige du plateau
                    plateau[depart.x + 1][depart.y] = 2;

                    // executer mouvement
                    plateau[depart.x][depart.y] = 2;
                    plateau[depart.x + 2][depart.y] = 1;

                    return true;
                }

                return false;
            case DROITE:
                if (depart.y + 1 >= nbColones || depart.y + 2 >= nbColones) {
                    return false;
                }

                if (plateau[depart.x][depart.y + 1] == 1 && plateau[depart.x][depart.y + 2] == 2) {
                    // remove tige du plateau
                    plateau[depart.x][depart.y + 1] = 2;

                    // executer mouvement
                    plateau[depart.x][depart.y] = 2;
                    plateau[depart.x][depart.y + 2] = 1;

                    return true;
                }

                return false;
            case GAUCHE:
                if (depart.y - 1 < 0 || depart.y - 2 < 0) {
                    return false;
                }

                if (plateau[depart.x][depart.y - 1] == 1 && plateau[depart.x][depart.y - 2] == 2) {
                    // remove tige du plateau
                    plateau[depart.x][depart.y - 1] = 2;

                    // executer mouvement
                    plateau[depart.x][depart.y] = 2;
                    plateau[depart.x][depart.y - 2] = 1;

                    return true;
                }

                return false;
        }

        return false;
    }

    private static boolean resoudreThread() {
        Position depart = trouverDepart();
        if (depart == null) {
            System.out.println("Il y a aucun trou vide dans le plateau");
            return false;
        }

        return true;
    }

    private static boolean lirePlateau(String path) throws IOException {
        String[] lignes = Files.readAllLines(Paths.get(path)).toArray(new String[0]);
        nbLignes = lignes.length;

        if (nbLignes == 0 || nbLignes > 7) {
            System.out.println("Erreur il y a 0 ou plus de 7 lignes dans le fichier.");
            return false;
        }

        plateau = new int[nbLignes][];

        for (int i = 0; i < nbLignes; i++) {
            char[] colones = lignes[i].toCharArray();
            nbColones = colones.length;

            if (nbColones == 0 || nbColones > 7) {
                System.out.println("Erreur il y a 0 ou plus de 7 colones dans le fichier.");
                return false;
            }

            plateau[i] = new int[nbColones];

            for (int j = 0; j < nbColones; j++) {
                int chiffre = Integer.parseInt(String.valueOf(colones[j]));
                if (chiffre < 0 || chiffre > 2) {
                    System.out.println("Erreur un chiffre different de 0, 1 et 2 a ete trouve dans le fichier.");
                    return false;
                } else {
                    plateau[i][j] = chiffre;
                }
            }
        }

        return true;
    }

    private static void afficherPlateau() {
        for (int i = 0; i < plateau.length; i++) {
            for (int j = 0; j < plateau[i].length; j++) {
                System.out.print(plateau[i][j]);
            }
            System.out.print("\n");
        }

        System.out.print("\n");
    }

    private static void afficherResultat() {
        if (puzzleValide) {
            afficherListeMouvement();
            System.out.println("Plateau final :");
            afficherPlateau();
            System.out.println("Nombre de noeuds explore : " + nbNoeudsExplores);
            System.out.format("Temps d'execution : %.18f secondes \n", Timer.getTime());
        } else {
            System.out.println("Le puzzle ne peut pas etre resolue");
            System.out.println("Nombre de noeuds explore : " + nbNoeudsExplores);
            System.out.format("Temps d'execution : %.18f secondes \n", Timer.getTime());
        }
    }

    private static void afficherListeMouvement() {
        StringBuilder sb = new StringBuilder("Liste des mouvements pour obtenir la solution : \n");

        for (Coup coup : solution) {
            sb.append(coup.toString());
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }

    private static enum Mouvement {
        DROITE, BAS, GAUCHE, HAUT;
    }

    public static class Timer {

        private static long startTime, endTime;

        public static void start() {
            startTime = System.nanoTime();

        }

        public static void stop() {
            endTime = System.nanoTime();
        }

        public static float getTime() {
            return ((float) (endTime - startTime)) / 1000000000.0f;
        }
    }

    private static class Position {

        int x;
        int y;

        private Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Coup {

        Position position;
        Mouvement mouvement;

        private Coup(Position position, Mouvement mouvement) {
            this.position = position;
            this.mouvement = mouvement;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("Tige position(");
            sb.append(position.x + 1);
            sb.append(",");
            sb.append(position.y + 1);
            sb.append(") deplacement a ");
            sb.append(mouvement.toString());
            return sb.toString();
        }
    }
}
