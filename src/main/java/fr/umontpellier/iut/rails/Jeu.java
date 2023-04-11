package fr.umontpellier.iut.rails;

import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Jeu implements Runnable {
    /**
     * Liste des joueurs
     */
    private List<Joueur> joueurs;
    /**
     * Le joueur dont c'est le tour
     */
    private Joueur joueurCourant;
    /**
     * Liste des villes représentées sur le plateau de jeu
     */
    private List<Ville> villes;
    /**
     * Liste des routes du plateau de jeu
     */
    private List<Route> routes;
    /**
     * Pile de pioche (face cachée)
     */
    private List<CouleurWagon> pileCartesWagon;
    /**
     * Cartes de la pioche face visible (normalement il y a 5 cartes face visible)
     */
    private List<CouleurWagon> cartesWagonVisibles;
    /**
     * Pile de cartes qui ont été défaussée au cours de la partie
     */
    private List<CouleurWagon> defausseCartesWagon;
    /**
     * Pile des cartes "Destination" (uniquement les destinations "courtes", les
     * destinations "longues" sont distribuées au début de la partie et ne peuvent
     * plus être piochées après)
     */
    private List<Destination> pileDestinations;
    /**
     * File d'attente des instructions recues par le serveur
     */
    private BlockingQueue<String> inputQueue;
    /**
     * Messages d'information du jeu
     */
    private List<String> log;

    public Jeu(String[] nomJoueurs) {
        // initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        // création des villes et des routes
        Plateau plateau = Plateau.makePlateauEurope();
        villes = plateau.getVilles();
        routes = plateau.getRoutes();

        // création des piles de pioche, cartes face visible et défausse
        pileCartesWagon = new ArrayList<>();
        cartesWagonVisibles = new ArrayList<>();
        defausseCartesWagon = new ArrayList<>();
        for (CouleurWagon c : CouleurWagon.getCouleursSimples()) {
            for (int i = 0; i < 12; i++)
                pileCartesWagon.add(c);
        }
        for (int i = 0; i < 14; i++) {
            pileCartesWagon.add(CouleurWagon.LOCOMOTIVE);
        }
        Collections.shuffle(pileCartesWagon);
        remplirCartesWagonVisibles();

        // création des destinations
        pileDestinations = Destination.makeDestinationsEurope();
        Collections.shuffle(pileDestinations);

        // création des joueurs
        ArrayList<Joueur.Couleur> couleurs = new ArrayList<>(Arrays.asList(Joueur.Couleur.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();
        for (String nomJoueur : nomJoueurs) {
            Joueur j = new Joueur(nomJoueur, this, couleurs.remove(0));
            for (int k = 0; k < 4; k++) {
                j.ajouterCarteWagon(piocherCarteWagon());
            }
            joueurs.add(j);
        }
        this.joueurCourant = joueurs.get(0);
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public Joueur getJoueurCourant() {
        return joueurCourant;
    }

    public List<Ville> getVilles() {
        return villes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<CouleurWagon> getPileCartesWagon() {
        return pileCartesWagon;
    }

    public List<CouleurWagon> getCartesWagonVisibles() {
        return cartesWagonVisibles;
    }

    public List<CouleurWagon> getDefausseCartesWagon() {
        return defausseCartesWagon;
    }

    public List<Destination> getPileDestinations() {
        return pileDestinations;
    }

    /**
     * Modifie l'attribut joueurCourant pour passer au joueur suivant dans l'ordre
     * du tableau joueurs
     * (le tableau est considéré circulairement)
     */
    public void passeAuJoueurSuivant() {
        int i = joueurs.indexOf(joueurCourant);
        i = (i + 1) % joueurs.size();
        joueurCourant = joueurs.get(i);
    }

    /**
     * Exécute la partie
     */
    public void run() {
        // Choix des destinations initiales pour chaque joueur (au moins deux parmi 3
        // courtes et 1 longue)
        ArrayList<Destination> pileDestinationsLongues = Destination.makeDestinationsLonguesEurope();
        Collections.shuffle(pileDestinationsLongues);
        for (int i = 0; i < joueurs.size(); i++) {
            ArrayList<Destination> destinationsInitiales = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                destinationsInitiales.add(piocherDestination());
            }
            destinationsInitiales.add(pileDestinationsLongues.remove(0));
            List<Destination> destinationsDefaussees = joueurCourant.choisirDestinations(destinationsInitiales, 2);
            if (destinationsDefaussees.isEmpty()) {
                log(joueurCourant.toLog() + " ne défausse aucune destination.");
            } else if (destinationsDefaussees.size() == 1) {
                log(joueurCourant.toLog() + " défausse 1 destination.");
            } else {
                log(joueurCourant.toLog() + " défausse " + destinationsDefaussees.size() + " destinations.");
            }
            passeAuJoueurSuivant();
        }

        // Boucle principale (tours des joueurs)
        while (true) {
            joueurCourant.jouerTour();
            if (joueurCourant.getNbWagons() <= 2) {
                // un joueur a moins de 2 wagons restants à la fin de son tour
                // -> plus qu'un tour de jeu
                passeAuJoueurSuivant();
                break;
            }
            passeAuJoueurSuivant();
        }
        // Dernier tour de jeu
        for (int i = 0; i < joueurs.size(); i++) {
            joueurCourant.jouerTour();
            passeAuJoueurSuivant();
        }
        // Fin de la partie
        prompt("Fin de la partie.", new ArrayList<>(), true);
    }

    /**
     * Calcule les scores des joueurs à la fin de la partie
     *
     * @return une liste d'entiers, le ième entier étant le score du ième joueur
     */
    public List<Integer> calculerLesScores() {
        throw new RuntimeException("Méthode non implémentée !");
    }

    /**
     * Ajoute une carte wagon dans la pile de défausse
     *
     * @param c carte à défausser
     */
    public void defausserCarteWagon(CouleurWagon c) {
        defausseCartesWagon.add(c);
        remplirCartesWagonVisibles(); // si jamais il n'y avait pas assez de cartes disponibles
    }

    /**
     * Pioche une carte de la pile de pioche
     * Si la pile est vide, les cartes de la défausse sont replacées dans la pioche
     * puis mélangées avant de piocher une carte
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CouleurWagon piocherCarteWagon() {
        if (pileCartesWagon.isEmpty()) {
            if (defausseCartesWagon.size() > 0) {
                pileCartesWagon.addAll(defausseCartesWagon);
                defausseCartesWagon.clear();
                Collections.shuffle(pileCartesWagon);
            } else {
                return null;
            }
        }
        return pileCartesWagon.remove(0);
    }

    /**
     * Retire une carte wagon de la pile des cartes wagon visibles.
     * Si une carte a été retirée, la pile de cartes wagons visibles est recomplétée
     * (remise à 5, éventuellement remélangée si 3 locomotives visibles)
     */
    public void retirerCarteWagonVisible(CouleurWagon c) {
        if (cartesWagonVisibles.remove(c)) {
            remplirCartesWagonVisibles();
        }
    }

    /**
     * Pioche et renvoie la destination du dessus de la pile de destinations.
     *
     * @return la destination qui a été piochée (ou `null` si aucune destination
     * disponible)
     */
    public Destination piocherDestination() {
        if (pileDestinations.isEmpty())
            return null;
        return pileDestinations.remove(0);
    }

    /**
     * Replace une liste de destinations à la fin de la pile de destinations
     */
    public void defausserDestinations(List<Destination> destinations) {
        pileDestinations.addAll(destinations);
    }

    /**
     * Teste si la pile de destinations est vide
     * (pour préserver l'encapsulation du Jeu et de sa pile de destination)
     */
    public boolean pileDestinationsEstVide() {
        return pileDestinations.isEmpty();
    }

    /**
     * Teste si la pile de cartes wagon est vide
     */
    public boolean pileCartesWagonEstVide() {
        return pileCartesWagon.isEmpty() && defausseCartesWagon.isEmpty();
    }

    /**
     * Révèle des cartes wagon de la pioche jusqu'à ce qu'il y ait 5 cartes visibles
     * (ou plus aucune carte disponible à piocher).
     * <p>
     * Après avoir retourné 5 cartes, si au moins 3 des 5 cartes retournées sont des
     * locomotives, les 5 cartes sont défaussées et 5 nouvelles cartes sont
     * piochées.
     * <p>
     * Remarque: pour éviter les boucles infinies, s'il y a 3 locomotives
     * retournées on ne mélange les cartes que s'il reste au moins 3 cartes qui ne
     * sont pas des locomotives dans la pioche, la défausse et les cartes révélées)
     */
    public void remplirCartesWagonVisibles() {
        while (cartesWagonVisibles.size() < 5) {
            CouleurWagon c = piocherCarteWagon();
            if (c == null)
                break; // plus aucune carte disponible à piocher
            cartesWagonVisibles.add(c);
        }

        if (Collections.frequency(cartesWagonVisibles, CouleurWagon.LOCOMOTIVE) >= 3) {
            // 3 locomotives -> défausser les 5 cartes et piocher 5 nouvelles
            // mais seulement s'il y a assez d'autres cartes pour avoir 5 cartes sans 3
            // locomotives
            if (cartesWagonVisibles.size() + pileCartesWagon.size() + defausseCartesWagon.size()
                    - Collections.frequency(cartesWagonVisibles, CouleurWagon.LOCOMOTIVE)
                    - Collections.frequency(pileCartesWagon, CouleurWagon.LOCOMOTIVE)
                    - Collections.frequency(defausseCartesWagon, CouleurWagon.LOCOMOTIVE) >= 3) {
                defausseCartesWagon.addAll(cartesWagonVisibles);
                cartesWagonVisibles.clear();
                remplirCartesWagonVisibles();
            }
        }
    }

    /**
     * Ajoute un message au log du jeu
     */
    public void log(String message) {
        log.add(message);
    }

    /**
     * Ajoute un message à la file d'entrées
     */
    public void addInput(String message) {
        inputQueue.add(message);
    }

    /**
     * Lit une ligne de l'entrée standard
     * C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
     * l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})
     *
     * @return une chaîne de caractères correspondant à l'entrée suivante dans la
     * file
     */
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix
     *
     * @param instruction l'instruction qui est donnée au joueur
     * @param boutons     labels des choix proposés s'il y en a
     * @param peutPasser  indique si le joueur peut passer sans faire de choix
     */
    public void prompt(String instruction, Collection<String> boutons, boolean peutPasser) {
        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<\n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (String bouton : boutons) {
                joiner.add(bouton);
            }
            System.out.printf(">>> %s: %s [%s] <<<\n", joueurCourant.getNom(), instruction, joiner);
        }

        Map<String, Object> data = Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("prompt", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("instruction", instruction),
                        new AbstractMap.SimpleEntry<>("boutons", boutons),
                        new AbstractMap.SimpleEntry<String, Object>("nomJoueurCourant", getJoueurCourant().getNom()),
                        new AbstractMap.SimpleEntry<String, Object>("peutPasser", peutPasser))),
                new AbstractMap.SimpleEntry<>("villes", villes.stream().map(Ville::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<>("routes", routes.stream().map(Route::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("joueurs",
                        joueurs.stream().map(Joueur::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("piles", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("pileCartesWagon", pileCartesWagon.size()),
                        new AbstractMap.SimpleEntry<String, Object>("pileDestinations", pileDestinations.size()),
                        new AbstractMap.SimpleEntry<String, Object>("defausseCartesWagon", defausseCartesWagon),
                        new AbstractMap.SimpleEntry<String, Object>("cartesWagonVisibles", cartesWagonVisibles))),
                new AbstractMap.SimpleEntry<String, Object>("log", log));
        GameServer.setEtatJeu(new Gson().toJson(data));
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }
}
