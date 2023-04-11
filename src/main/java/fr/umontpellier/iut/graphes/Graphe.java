package fr.umontpellier.iut.graphes;


import java.util.ArrayList;
import java.util.Collections;

public class Graphe {
    /**
     * matrice d'adjacence du graphe, un entier supérieur à 0 représentant la distance entre deux sommets
     * mat[i][i] = 0 pour tout i parce que le graphe n'a pas de boucle
     */
    private final int[][] mat;

    /**
     * Construit un graphe à n sommets
     *
     * @param n le nombre de sommets du graphe
     */
    public Graphe(int n) {
        mat = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                mat[i][j] = 0;
            }
        }
    }

    /**
     * @return le nombre de sommets
     */
    public int nbSommets() {
        return mat.length;
    }

    /**
     * Supprime l'arête entre les sommets i et j
     *
     * @param i un entier représentant un sommet
     * @param j un autre entier représentant un sommet
     */
    public void supprimerArete(int i, int j) {
        mat[i][j] = 0;
        mat[j][i] = 0;
    }

    /**
     * @param i un entier représentant un sommet
     * @param j un autre entier représentant un sommet
     * @param k la distance entre i et j (k>0)
     */
    public void ajouterArete(int i, int j, int k) {
        mat[i][j] = k;
        mat[j][i] = k;
    }

    /*** 
     * @return le nombre d'arête du graphe
     */
    public int nbAretes() {
        int nbAretes = 0;
        for (int i = 0; i < mat.length; i++) {
            for (int j = i + 1; j < mat.length; j++) {
                if (mat[i][j] > 0) {
                    nbAretes++;
                }
            }
        }
        return nbAretes;
    }

    /**
     * @param i un entier représentant un sommet
     * @param j un autre entier représentant un sommet
     * @return vrai s'il existe une arête entre i et j, faux sinon
     */
    public boolean existeArete(int i, int j) {
        boolean existe = false;
        if (mat[i][j] != 0) {
            existe = true;
        }
        return existe;
    }

    /**
     * @param v un entier représentant un sommet du graphe
     * @return la liste des sommets voisins de v
     */
    public ArrayList<Integer> voisins(int v) {
        ArrayList<Integer> voisin = new ArrayList<>();
        for (int i = 0; i < mat.length; i++) {
            if (mat[i][v] != 0) {
                voisin.add(i);
            }
        }
        return voisin;

    }

    /**
     * @return une chaîne de caractères permettant d'afficher la matrice mat
     */
    public String toString() {
        StringBuilder res = new StringBuilder("\n");
        for (int[] ligne : mat) {
            for (int j = 0; j < mat.length; j++) {
                String x = String.valueOf(ligne[j]);
                res.append(x);
            }
            res.append("\n");
        }
        return res.toString();
    }

    /**
     * Calcule la classe de connexité du sommet v
     *
     * @param v un entier représentant un sommet
     * @return une liste d'entiers représentant les sommets de la classe de connexité de v
     */
    public ArrayList<Integer> calculerClasseDeConnexite(int v) {
        ArrayList<Integer> rouge = new ArrayList<>();
        ArrayList<Integer> bleu = new ArrayList<>();
        bleu.add(v);
        while (!bleu.isEmpty()) {
            ArrayList<Integer> voisin = voisins(bleu.get(0));
            for (int i = 0; i < voisin.size(); i++) {
                if (!bleu.contains(voisin.get(i)) && !rouge.contains(voisin.get(i))) {
                    bleu.add(voisin.get(i));
                }
            }
            rouge.add(bleu.get(0));
            bleu.remove(0);
        }
        return rouge;
    }

    /**
     * @return la liste des classes de connexité du graphe
     */
    public ArrayList<ArrayList<Integer>> calculerClassesDeConnexite() {
        boolean faitPartie;
        ArrayList<ArrayList<Integer>> cdc = new ArrayList<>();
        ArrayList<Integer> classeTrouver;
        for (int i = 0; i < mat.length; i++) {
            faitPartie = false;
            classeTrouver = calculerClasseDeConnexite(i);
            if (cdc.isEmpty()) {
                cdc.add(classeTrouver);
            } else {
                int j = 0;
                while (j < cdc.size() & !faitPartie) {
                    if (estPareil(cdc.get(j), classeTrouver)) {
                        faitPartie = true;
                    }
                    j++;
                }
                if (!faitPartie) {
                    cdc.add(classeTrouver);
                }
            }
        }
        return cdc;
    }


    public boolean estPareil(ArrayList<Integer> liste, ArrayList<Integer> liste2) {
        boolean estPareil = false;
        ArrayList<Integer> l1 = new ArrayList<>();
        ArrayList<Integer> l2 = new ArrayList<>();
        l1.addAll(liste);
        l2.addAll(liste2);
        Collections.sort(l1);
        Collections.sort(l2);
        if (liste.size() == liste2.size()) {
            if (liste.containsAll(liste2) && liste2.containsAll(liste)) {
                estPareil = true;
            }
        }
        return estPareil;
    }

    /**
     * @return le nombre de classes de connexité
     */
    public int nbCC() {
        return calculerClassesDeConnexite().size();
    }

    /**
     * @param u un entier représentant un sommet
     * @param v un entie représentant un sommet
     * @return vrai si (u,v) est un isthme, faux sinon
     */
    public boolean estUnIsthme(int u, int v) {
        boolean estIsthme = false;
        int nbCC = calculerClassesDeConnexite().size();
        supprimerArete(u, v);
        int nbCC2 = calculerClassesDeConnexite().size();
        if (nbCC2 == nbCC + 1) {
            estIsthme = true;
        }
        return estIsthme;
    }


    /**
     * Calcule le plus long chemin présent dans le graphe
     *
     * @return une liste de sommets formant le plus long chemin dans le graphe
     */
    public ArrayList<Integer> plusLongChemin() {
        ArrayList<Integer> liste = new ArrayList<>();
        if(this.estUnArbre()){
            //dijstra
        }
        else if(this.existeParcoursEulerien()){
            /*NOPE3
            for(int i=0;i<mat.length;i++){
                liste.add(i);
            }
            */
        }
        else{

        }
        return liste;
    }


    /**
     * @return vrai s'il existe un parcours eulérien dans le graphe, faux sinon
     */
    public boolean existeParcoursEulerien() {
        boolean euler = false;
        int nbImpair = 0;
        if (this.estConnexe()) {
            for (int i = 0; i < mat.length; i++) {
                int deg = voisins(i).size();
                if (deg % 2 != 0) {
                    nbImpair++;
                }
            }
            if (nbImpair == 2 | nbImpair == 0) {
                euler = true;
            }
        }
        return euler;
    }

    /**
     * @return vrai si le graphe est un arbre, faux sinon
     */
    public boolean estUnArbre() {
        return this.estConnexe() & this.nbAretes() < this.nbSommets();
    }


    /**
     * @return vrai si le graphe est connexe, faux sinon
     */
    public boolean estConnexe() {
        boolean connexe = true;
        for (int i = 0; i < mat.length; i++) {
            if (this.nbCC() != 1) {
                connexe = false;
            }
        }
        return connexe;
    }

}