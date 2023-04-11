package fr.umontpellier.iut.graphes;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class GrapheTest {

    //ajouter controle des exceptions et boucles infinies

    private static Graphe buildGraphe(int n, int[][] aretes){
        Graphe res = new Graphe(n);
        for(int i=0;i<aretes.length;i++){
            res.ajouterArete(aretes[i][0],aretes[i][1],aretes[i][2]);
        }
        return res;
    }

    @Test
    void testCCdeV() {
        Graphe g = buildGraphe(4,new int[][]{{0,1,1},{2,3,1}});
        ArrayList<Integer> res = g.calculerClasseDeConnexite(2);
        //conversion en set pour que le equals ci-dessous soit bien une égalité de set et pas une égalité d'arrayList (qui tiendrait compte de l'ordre)
        HashSet<Integer> resSet = new HashSet<>(res);

        HashSet<Integer> answer = new HashSet<>();
        answer.add(3);
        answer.add(2);



        assertEquals(resSet, answer);
    }

    @Test
    void testCC() {
        Graphe g = buildGraphe(4,new int[][]{{0,1,1},{2,3,1}});
        ArrayList<ArrayList<Integer>> res = g.calculerClassesDeConnexite();
        //conversion en set pour que le equals ci-dessous soit bien une égalité de set et pas une égalité d'arrayList (qui tiendrait compte de l'ordre)
        HashSet<HashSet<Integer>> resSet = new HashSet<>();
        for(ArrayList<Integer> cc : res){
            resSet.add(new HashSet<>(cc));
        }

        HashSet<Integer> cc1 = new HashSet<>();
        cc1.add(3);
        cc1.add(2);

        HashSet<Integer> cc2 = new HashSet<>();
        cc2.add(0);
        cc2.add(1);

        HashSet<HashSet<Integer>> answer = new HashSet<>();
        answer.add(cc1);
        answer.add(cc2);
        //System.out.println(res);
        System.out.println(resSet);

        assertEquals(resSet, answer);
    }

    @Test
    void testCCPlusDur() {
        Graphe g = buildGraphe(4,new int[][]{{2,3,1},{2,1,1},{1,3,1}});
        ArrayList<ArrayList<Integer>> res = g.calculerClassesDeConnexite();
        System.out.println(res);
    }

    @Test
    void testEstUnIsthme() {
        Graphe g = buildGraphe(4,new int[][]{{0,1,1},{2,3,1},{2,1,1},{1,3,1}});
        assertTrue(g.estUnIsthme(0,1));
        assertFalse(g.estUnIsthme(1,3));
    }

    @Test
    void testPlusLongChemin() {
        Graphe g = buildGraphe(5,new int[][]{{3,4,1},{4,1,1},{0,1,1},{2,3,1},{2,1,1},{1,3,1}});

        ArrayList<Integer> L = new ArrayList<Integer>();
        L.add(0);
        L.add(1);
        L.add(2);
        L.add(3);
        L.add(1);
        L.add(4);
        L.add(3);
        assertEquals(g.plusLongChemin(),L);
    }


    @Test
    void testExisteParcoursEulerien() {
        Graphe g = buildGraphe(5,new int[][]{{0,1,1},{1,2,1},{2,0,1},{1,3,1},{2,4,1}});

        assertFalse(g.existeParcoursEulerien());
    }

    @Test
    void testEstUnArbre() {
        Graphe g = buildGraphe( 13, new int[][]{{0,1,1},{1,2,1},{2,3,1},{2,4,1},{2,8,1},{4,5,1},{5,6,1},{5,7,1},{8,9,1},{8,10,1},{8,11,1},{11,12,1}});
        assertTrue(g.estUnArbre());
    }

    @Test
    void testEstUnArbreFaux(){
        Graphe e = buildGraphe(4, new int[][]{{0,1,1}, {0,2,1}, {1,2,1}, {2,3,1}});
        assertFalse(e.estUnArbre());
    }

    @Test
    void testArbre(){
        Graphe g = buildGraphe(6, new int[][]{{0,1,1}, {1,2,1}, {2,4,1}, {4,5,1}});
        assertFalse(g.estConnexe());
    }
    
    
}
