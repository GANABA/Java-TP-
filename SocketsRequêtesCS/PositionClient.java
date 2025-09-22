import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class PositionClient {
    public static void main(String[] args) {

        BufferedReader br = null; // pour lire du texte sur la socket
        PrintStream ps = null; // pour écrire du texte sur la socket
        String line = null;
        Socket sock = null;
        int port = -1;
        Scanner sc = new Scanner(System.in);

        if (args.length != 2) {
            System.out.println("usage: EchoClient ip_server port");
            System.exit(1);
        }

        try {
            port = Integer.parseInt(args[1]); // récupération du port sous forme int
            sock = new Socket(args[0], port); // création socket client et connexion au serveur donné en args[0]
        } catch (IOException e) {
            System.out.println("problème de connexion au serveur : " + e.getMessage());
            System.exit(1);
        }

        try {
            br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // création flux lecture lignes // //                                                                           // // texte
            ps = new PrintStream(sock.getOutputStream()); // création flux écriture lignes de texte

            // lire l’identifiant du client envoyé par le serveur
            String clientId = br.readLine(); // lecture message serveur
            if (clientId != null) {
                System.out.println("Identifiant attribué par le serveur : " + clientId);
            }

            while (true) {
                System.out.println(
                        "Quel est la requete que vous souhaitez effectuer :\n1-storepos x,y,z\n2-pathlen\n3-findpos facteur_proximite,x,y,z\n4-denivel\n5-lastpos\n");
                String input = sc.nextLine();
                if (input.isEmpty()) {
                    System.out.println("Fin du client.");
                    break;
                }

                // analayser de la commande de requete
                String[] parts = input.split(" ", 2);
                String command = parts[0].toLowerCase();

                if (command.equals("storepos")) {
                    if (parts.length < 2) {
                        System.out.println("Requête malformée - paramètres invalides !");
                        continue;
                    }
                    String[] coords = parts[1].split(",");
                    if (coords.length != 3) {
                        System.out.println("Requête malformée - paramètres invalides (3 parametres attendus)!");
                        continue;
                    }
                    boolean validRequest = true;
                    for (String coord : coords) {
                        try {
                            Double.parseDouble(coord.trim());
                        } catch (NumberFormatException e) {
                            validRequest = false;
                            break;
                        }
                    }
                    if (!validRequest) {
                        System.out.println("Requête malformée - paramètres invalides !");
                        continue;
                    }
                    // envoi de la requête au serveur
                    ps.println("1");
                    ps.println(clientId);
                    for (String coord : coords) {
                        ps.println(coord.trim());
                    }
                    line = br.readLine(); // lecture réponse serveur
                    if (line.equals("OK")) {
                        System.out.println("Requête exécuté, position enregistrée !");
                    } else if (line.equals("REQ_ERR")) {
                        System.out.println("Requête malformée - paramètres invalides !");
                    }

                } else if (command.equals("pathlen")) {
                    if (parts.length != 0) {
                        System.out.println("Requête malformée - paramètres invalides (Aucun autre parametre attendu)!");
                        continue;
                    }
                    // envoi de la requête au serveur
                    ps.println("2");
                    ps.println(clientId);
                    line = br.readLine(); // lecture réponse serveur
                    try {
                        double pathLength = Double.parseDouble(line);
                        System.out.println("Longueur du chemin : " + pathLength);
                    } catch (NumberFormatException e) {
                        System.out.println("Réponse du serveur invalide !");
                    }

                } else if (command.equals("findpos")) {
                    if (parts.length < 2) {
                        System.out.println("Requête malformée - paramètres invalides !");
                        continue;
                    }
                    String[] values = parts[1].split(",");
                    if (values.length != 4) {
                        System.out.println("Requête malformée - paramètres invalides (4 parametres attendus)!");
                        continue;
                    }
                    boolean validRequest = true;
                    for (String value : values) {
                        try {
                            Double.parseDouble(value.trim());
                        } catch (NumberFormatException e) {
                            validRequest = false;
                            break;
                        }
                    }
                    if (!validRequest) {
                        System.out.println("Requête malformée - paramètres invalides !");
                        continue;
                    }
                    // envoi de la requête au serveur
                    ps.println("3");
                    ps.println(clientId);
                    for (String value : values) {
                        ps.println(value.trim());
                    }
                    line = br.readLine(); // lecture réponse serveur
                    if (line.equals("TRUE")) {
                        System.out.println("La position est déjà stockée !");
                    } else if (line.equals("FALSE")) {
                        System.out.println("La position n'est pas stockée !");
                    } else if (line.equals("REQ_ERR")) {
                        System.out.println("Requête malformée - paramètres invalides !");
                    }
                } else if (command.equals("denivel")) {
                    ps.println("4");
                    ps.println(clientId);
                    String resp = br.readLine();
                    System.out.println("Dénivelé positif,négatif = " + resp);
                } else if (command.equals("lastpos")) {
                    ps.println("5");
                    ps.println(clientId);
                    String resp = br.readLine();
                    System.out.println("Dernières positions des autres clients : " + resp);
                }

                else {
                    System.out.println("Requête malformée - paramètres invalides !");
                }

            }
            br.close();
            ps.close();
            sock.close();
            sc.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
