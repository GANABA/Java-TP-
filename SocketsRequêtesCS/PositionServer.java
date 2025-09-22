import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class PositionServer {
    public static void main(String[] args) {

        BufferedReader br = null; // pour lire du texte sur la socket
        PrintStream ps = null; // pour envoyer du texte sur la socket
        String line = null; // la ligne reçu/envoyée
        ServerSocket conn = null;
        Socket sock = null;
        int port = -1;
        // Liste des positions stockées
        HashMap<Integer, ArrayList<Position>> clientPositions = new HashMap<>();

        if (args.length != 1) {
            System.out.println("usage: Server port");
            System.exit(1);
        }

        try {
            port = Integer.parseInt(args[0]); // récupération du port sous forme int
            conn = new ServerSocket(port); // création socket serveur
        } catch (IOException e) {
            System.out.println("problème création socket serveur : " + e.getMessage());
            System.exit(1);
        }

        int idClient = 0; // identifiant client

        while (true) { // acceptation et gestion des connexions clients (plusieurs clients possibles)
            try {
                sock = conn.accept(); // attente connexion client
                br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // creation flux lecture lignes
                ps = new PrintStream(sock.getOutputStream()); // création flux écriture lignes de texte

                System.out.println("Nouveau client connecté : " + sock);
                idClient++;
                ps.println(idClient);

                while ((line = br.readLine()) != null) { // permet d'échanger plusieurs messages avec le meme client
                    // condition d'arrêt : ligne vide
                    if (line.isEmpty()) {
                        System.out.println("Client a envoyé une ligne vide → fin de session.");
                        break;
                    }
                    // Lire le nombre d'entiers à recevoir
                    int numRequete;
                    try {
                        numRequete = Integer.parseInt(line.trim());
                    } catch (NumberFormatException e) {
                        ps.println("REQ_ERR"); // envoyer un message d'erreur au client
                        continue; // passer à l'itération suivante de la boucle
                    }

                    // le client renvoie son ID après le numéro de requête
                    int clientId;
                    try {
                        clientId = Integer.parseInt(br.readLine().trim());
                    } catch (NumberFormatException e) {
                        ps.println("NOID_ERR");
                        continue;
                    }

                    if (clientId != idClient) {
                        ps.println("NOID_ERR");
                        continue;
                    }

                    // récupérer la liste du client
                    clientPositions.putIfAbsent(clientId, new ArrayList<>());
                    ArrayList<Position> listePos = clientPositions.get(clientId);

                    if (numRequete == 1) { // stocker une position requete 1
                        Double[] coords = new Double[3];
                        // Lire les coordonnées envoyées par le client
                        for (int i = 0; i < 3; i++) {
                            String posLine = br.readLine();
                            try {
                                coords[i] = Double.parseDouble(posLine.trim());
                            } catch (NumberFormatException e) {
                                ps.println("ERR_REQ");
                                break;
                            }
                        }

                        double x = coords[0];
                        double y = coords[1];
                        double z = coords[2];
                        // Stocker la position dans la liste
                        listePos.add(new Position(x, y, z));
                        // Associer la liste des positions au client
                        clientPositions.put(idClient, listePos);
                        ps.println("OK");

                    } else if (numRequete == 2) { // calculer la somme des distances entre les positions stockées
                                                  // requete 2
                        double sommeDistances = 0.0;
                        // Lire les indices des positions envoyées par le client
                        for (int i = 1; i < listePos.size(); i++) {
                            Position pos1 = listePos.get(i - 1);
                            Position pos2 = listePos.get(i);
                            sommeDistances += pos1.distanceTo(pos2);
                        }
                        ps.println(sommeDistances); // renvoi de la somme des distances au client
                    } else if (numRequete == 3) { // verifie si une position envoyé par le client est déjà stockée
                                                  // requete 3
                        boolean found = false;
                        Double[] valeurs = new Double[4];
                        // Lire les coordonnées envoyées par le client
                        for (int i = 0; i < 4; i++) {
                            String posLine = br.readLine();
                            try {
                                valeurs[i] = Double.parseDouble(posLine.trim()); // lire chaque valeur
                            } catch (NumberFormatException e) {
                                ps.println("ERR_REQ");
                                break;
                            }
                        }
                        double eps = valeurs[0]; // lire la valeur epsilon
                        Position posToCheck = new Position(valeurs[1], valeurs[2], valeurs[3]);
                        for (Position p : listePos) { // parcourir la liste des positions stockées
                            if (p.equals(posToCheck, eps)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            ps.println("TRUE"); // envoyer un message d'erreur
                        } else {
                            ps.println("FALSE"); // envoyer un message d'erreur
                        }
                    } else if (numRequete == 4) { // dénivelé positif/négatif
                        ArrayList<Position> positions = clientPositions.get(clientId);
                        if (positions == null || positions.size() < 2) {
                            ps.println("0,0"); // pas assez de points
                        } else {
                            double denivPos = 0.0;
                            double denivNeg = 0.0;
                            for (int i = 1; i < positions.size(); i++) {
                                double delta = positions.get(i).getZ() - positions.get(i - 1).getZ();
                                if (delta > 0)
                                    denivPos += delta;
                                else if (delta < 0)
                                    denivNeg += -delta;
                            }
                            ps.println(denivPos + "," + denivNeg);
                        }
                    } else if (numRequete == 5) { // dernières positions des autres clients
                        StringBuilder sb = new StringBuilder();
                        for (Integer otherId : clientPositions.keySet()) {
                            if (otherId != clientId) {
                                ArrayList<Position> posList = clientPositions.get(otherId);
                                if (posList != null && !posList.isEmpty()) {
                                    Position last = posList.get(posList.size() - 1);
                                    sb.append("client ").append(otherId).append(": ")
                                            .append(last.toString()).append("; ");
                                }
                            }
                        }
                        if (sb.length() == 0) {
                            ps.println("NONE");
                        } else {
                            ps.println(sb.toString());
                        }
                    } else {
                        System.out.println("Requête malformée - paramètres invalides !"); // envoyer un message d'erreur
                        continue; // passer à l'itération suivante de la boucle
                    }
                }
                System.out.println("Client déconnecté.");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }
}
