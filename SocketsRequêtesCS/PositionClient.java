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
        String coordonneeToSend = null;

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
            br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // création flux lecture lignes de //                                                                              // texte
            ps = new PrintStream(sock.getOutputStream()); // création flux écriture lignes de texte

            while (true) {
                System.out.println("Quel est le numero(1, 2 ou 3) de requete que vous souhaitez envoyer : ");
                String numReq = sc.nextLine();
                if (numReq.isEmpty()) {
                    System.out.println("Fin du client.");
                    break;
                }
                if (!numReq.equals("1") && !numReq.equals("2") && !numReq.equals("3")) {
                    System.out.println("Requête malformée - paramètres invalides !");
                    continue;
                }
                int req = Integer.parseInt(numReq.trim());

                // selon le numero de requete, demander les paramètres nécessaires
                if (req == 1) {
                    System.out.println("Veuillez saisir les coordonnées de la position à stocker par le serveur (x,y,z): ");
                    coordonneeToSend = sc.nextLine();

                    if (coordonneeToSend.isEmpty()) {
                        System.out.println("Fin du client.");
                        break;
                    }
                    // valider la requête
                    String[] coords = coordonneeToSend.split(",");
                    if (coords.length != 3) {
                        System.out.println("Requête malformée - paramètres invalides !");
                        continue;
                    }
                    Double[] nums = new Double[coords.length];
                    boolean validRequest = true;
                    for (int i = 0; i < coords.length; i++) {
                        try {
                            nums[i] = Double.parseDouble(coords[i].trim());
                        } catch (NumberFormatException e) {
                            validRequest = false;
                            break;
                        }
                    }

                    if (!validRequest) {
                        System.out.println("Requête malformée !");
                        continue;
                    }

                    ps.println(req); // envoyer au serveur le numero de requete

                    for (Double num : nums) {
                        ps.println(num); // envoyer au serveur chaque coordonnée
                    }
                    line = br.readLine(); // lecture réponse serveur

                    if (line.equals("OK")) {
                        System.out.println("Requête exécuté, position enregistrée !");
                    }
                    else if (line.equals("REQ_ERR")) {
                        System.out.println("Requête malformée - paramètres invalides !");
                    }
                } else if (req == 2) { // calcul longueur chemin
                    ps.println(req); // envoyer au serveur le numero de requete
                    line = br.readLine(); // lecture réponse serveur
                    if(line != null){
                        System.out.println("La longueur du chemin est de : " + line);
                    }
                } else if (req == 3) { // position proche d'une position stockée
                    ps.println(req); // envoyer au serveur le numero de requete
                    System.out.println("Veuillez saisir le facteur de proximité et les coordonnées de la position à vérifier par le serveur (eps,x,y,z): ");
                    coordonneeToSend = sc.nextLine();
                    if (coordonneeToSend.isEmpty()) {
                        System.out.println("Fin du client.");
                        break;
                    }
                    String[] params = coordonneeToSend.split(",");
                    if (params.length != 4) {
                        System.out.println("Requête malformée - paramètres invalides !");
                        continue;
                    }
                    boolean validRequest = true;
                    Double[] nums = new Double[params.length];
                    for (int i=0; i < params.length; i++) {
                        try {
                            nums[i] = Double.parseDouble(params[i].trim());
                        } catch (NumberFormatException e) {
                            validRequest = false;
                            break;
                        }
                    }
                    if (!validRequest) {
                        System.out.println("Requête malformée - paramètres invalides !");
                        continue;
                    }
                    for (Double num : nums) {
                        ps.println(num); // envoyer au serveur chaque coordonnée
                    }
                    line = br.readLine(); // lecture réponse serveur
                    if(line.equals("TRUE")){
                        System.out.println("La position correspond/est proche d'une position déjà stockée !"); 
                    }
                    else if(line.equals("FALSE")){
                        System.out.println("La position n'est pas proche/ne correspond pas à une position stockée !");
                    }
                    else if(line.equals("ERR_REQ")){
                        System.out.println("Requête malformée - paramètres invalides !");
                    }
                }else {
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
