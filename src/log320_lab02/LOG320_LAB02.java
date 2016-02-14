package log320_lab02;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author Zeldorine
 */
public class LOG320_LAB02 {

    private static final int CASENULL = 0;
    private static final int OCCUPE = 1;
    private static final int VIDE = 2;

    private static int plateau[][];
    private static int maxMove;
    private static HashMap<Integer, List<Map<String, Coup>>> traceurHash = new HashMap<>(20000);
    private static final Stack<Coup> solution = new Stack();
    private static int nbNoeudsExplores = 0;
    private static boolean puzzleValide = true;
    private static int nbLignes;
    private static int nbColones;
    private static int nbUn;

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
        maxMove = getMaxMove();
        nbUn = maxMove;

        try {
            Timer.start();
            puzzleValide = resoudre(1);
            Timer.stop();
        } catch (OutOfMemoryError e) {
            System.err.println("StackOverflowError !!!");
            puzzleValide = false;
        }

        afficherResultat();
    }

    private static int getMaxMove() {
        int nbUnt = 0;
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColones; j++) {
                if (plateau[i][j] == 1) {
                    nbUnt++;
                }
            }

        }

        return nbUnt;
    }

    private static boolean dejaTest = false;
    private static int nbElagage = 0;

    private static boolean resoudre(int move) {
        if (nbUn == 1) {
            return true;
        }

        nbNoeudsExplores++;
        for (int i = 0; move <= 31 && i < nbLignes; i++) {
            for (int j = 0; j < nbColones; j++) {
                if (plateau[i][j] == OCCUPE) {
                    for (Mouvement mouvement : Mouvement.values()) {
                        Coup coup = new Coup(new Position(i, j), mouvement);
                        if (move < maxMove && coupValide(coup) && !coupDejaConnu(coup)) {
                            dejaTest = false;
                            if (jouer(coup, move)) {
                                return true;
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    private static boolean jouer(Coup coup, int move) {
        // System.out.println("NB Noeud = " + nbNoeudsExplores);
        faireDeplacement(coup);

        if (resoudre(move + 1)) {
            solution.push(coup);
            return true;
        }

        retourArriere(coup);

        trace(coup);

        return false;
    }

    private static void trace(Coup coup) {
        String copiePlateau = copiePlateauToString();
        int hash = copiePlateau.hashCode();
        if (traceurHash.containsKey(hash)) {
            List<Map<String, Coup>> value = traceurHash.get(hash);
            Map<String, Coup> h = new HashMap();
            h.put(copiePlateau, coup);
            value.add(h);
            traceurHash.put(hash, value);
        } else {
            List<Map<String, Coup>> value = new ArrayList<>();
            Map<String, Coup> h = new HashMap();
            h.put(copiePlateau, coup);
            value.add(h);
            traceurHash.put(hash, value);
        }
    }

    private static boolean coupDejaConnu(Coup coup) {
        if (dejaTest) {
            return false;
        }

        String copiePlateau = copiePlateauToString();
        int hash = copiePlateau.hashCode();
        if (traceurHash.containsKey(hash)) {
            for (Map<String, Coup> h : traceurHash.get(hash)) {
                Iterator it = h.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Coup oldCoup = (Coup) pair.getValue();
                    String plateauTrace = (String) pair.getKey();

                    if (plateauTrace.equalsIgnoreCase(copiePlateau) && coup.equals(oldCoup)) {
                        dejaTest = true;
                        nbElagage++;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static String copiePlateauToString() {
        StringBuilder sb = new StringBuilder(49);
        for (int i = 0; i < nbLignes; i++) {
            for (int j = 0; j < nbColones; j++) {
                sb.append(plateau[i][j]);
            }
        }

        return sb.toString();
    }

    //refactorer
    private static void retourArriere(Coup coup) {
        Position depart = coup.position;
        nbUn++;
        switch (coup.mouvement) {
            case HAUT:
                plateau[depart.x - 1][depart.y] = OCCUPE;
                plateau[depart.x][depart.y] = OCCUPE;
                plateau[depart.x - 2][depart.y] = VIDE;
                break;
            case BAS:
                plateau[depart.x + 1][depart.y] = OCCUPE;
                plateau[depart.x][depart.y] = OCCUPE;
                plateau[depart.x + 2][depart.y] = VIDE;
                break;
            case DROITE:
                plateau[depart.x][depart.y + 1] = OCCUPE;
                plateau[depart.x][depart.y] = OCCUPE;
                plateau[depart.x][depart.y + 2] = VIDE;
                break;
            case GAUCHE:
                plateau[depart.x][depart.y - 1] = OCCUPE;
                plateau[depart.x][depart.y] = OCCUPE;
                plateau[depart.x][depart.y - 2] = VIDE;
                break;
        }
    }

    //refactorer
    private static boolean coupValide(Coup coup) {
        Position depart = coup.position;
        switch (coup.mouvement) {
            case HAUT:
                if (depart.x - 1 < 0 || depart.x - 2 < 0) {
                    return false;
                }

                if (plateau[depart.x - 1][depart.y] == OCCUPE && plateau[depart.x - 2][depart.y] == VIDE) {
                    return true;
                }
                return false;
            case BAS:
                if (depart.x + 1 >= nbLignes || depart.x + 2 >= nbLignes) {
                    return false;
                }

                if (plateau[depart.x + 1][depart.y] == OCCUPE && plateau[depart.x + 2][depart.y] == VIDE) {
                    return true;
                }

                return false;
            case DROITE:
                if (depart.y + 1 >= nbColones || depart.y + 2 >= nbColones) {
                    return false;
                }

                if (plateau[depart.x][depart.y + 1] == OCCUPE && plateau[depart.x][depart.y + 2] == VIDE) {
                    return true;
                }

                return false;
            case GAUCHE:
                if (depart.y - 1 < 0 || depart.y - 2 < 0) {
                    return false;
                }

                if (plateau[depart.x][depart.y - 1] == OCCUPE && plateau[depart.x][depart.y - 2] == VIDE) {
                    return true;
                }
                return false;
        }

        return false;
    }

    private static void faireDeplacement(Coup coup) {
        Position depart = coup.position;
        nbUn--;
        switch (coup.mouvement) {
            case HAUT:
                plateau[depart.x - 1][depart.y] = VIDE;
                plateau[depart.x][depart.y] = VIDE;
                plateau[depart.x - 2][depart.y] = OCCUPE;
                break;
            case BAS:
                plateau[depart.x + 1][depart.y] = VIDE;
                plateau[depart.x][depart.y] = VIDE;
                plateau[depart.x + 2][depart.y] = OCCUPE;
                break;
            case DROITE:
                plateau[depart.x][depart.y + 1] = VIDE;
                plateau[depart.x][depart.y] = VIDE;
                plateau[depart.x][depart.y + 2] = OCCUPE;
                break;
            case GAUCHE:
                plateau[depart.x][depart.y - 1] = VIDE;
                plateau[depart.x][depart.y] = VIDE;
                plateau[depart.x][depart.y - 2] = OCCUPE;
                break;
        }
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
        StringBuilder sb = new StringBuilder("Nombre de coup : " + solution.size() + ". Combinaisons : ");
        for (Mouvement mv : Mouvement.values()) {
            sb.append(mv.toString()).append(" ");
        }

        if (puzzleValide) {
            afficherListeMouvement();
            System.out.println("Plateau final :");
            afficherPlateau();
            System.out.println(sb.toString());
        } else {
            System.out.println("Le puzzle ne peut pas etre resolue");
        }

        System.out.println("Nombre d'elagage de l'arbre : " + nbElagage);
        System.out.println("Taille du traceur : " + traceurHash.size());
        System.out.println("Nombre de noeuds explore : " + nbNoeudsExplores);
        System.out.format("Temps d'execution : %.13f secondes \n", Timer.getTime());
    }

    private static void afficherListeMouvement() {
        System.out.println("Liste des mouvements pour obtenir la solution : \n");
        Object[] coup = solution.toArray();

        for (int i = coup.length - 1; i >= 0; i--) {
            System.out.println(coup[i].toString());
        }
        System.out.println();
    }

    private static enum Mouvement {
        GAUCHE, HAUT, BAS, DROITE;
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

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj instanceof Position) {
                return this.x == ((Position) obj).x && this.y == ((Position) obj).y;
            } else {
                return false;
            }
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

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj instanceof Coup) {
                return this.position.equals(((Coup) obj).position) && this.mouvement.equals(((Coup) obj).mouvement);
            } else {
                return false;
            }
        }
    }
}
