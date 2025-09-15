import java.io.*;
import java.net.*;

class SommeServer {

	public static void main(String[] args) {

		BufferedReader br = null; // pour lire du texte sur la socket
		PrintStream ps = null; // pour envoyer du texte sur la socket
		String line = null; // la ligne reçu/envoyée
		ServerSocket conn = null;
		Socket sock = null;
		int port = -1;

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

		while (true) { // acceptation et gestion des connexions clients (plusieurs clients possibles)
			try {
				sock = conn.accept(); // attente connexion client
				br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // creation flux lecture lignes
				ps = new PrintStream(sock.getOutputStream()); // création flux écriture lignes de texte

				System.out.println("Nouveau client connecté : " + sock);

				while ((line = br.readLine()) != null) { // permet d'échanger plusieurs messages avec le meme client
					// condition d'arrêt : ligne vide
					if (line.isEmpty()) {
						System.out.println("Client a envoyé une ligne vide → fin de session.");
						break;
					}

					System.out.println("Le client veut calculer : " + line + " nombres");

					// Lire le nombre d'entiers à recevoir
					try {
						int nbrEntiers = Integer.parseInt(line);
						int sum = 0;
						// Lire les entiers envoyés par le client
						for (int i = 0; i < nbrEntiers; i++) {
							String entier = br.readLine();
							sum += Integer.parseInt(entier);
						}
						ps.println(sum); // renvoi de la somme au client
					} catch (NumberFormatException e) {
						ps.println("REQ_ERR"); // envoyer un message d'erreur au client
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