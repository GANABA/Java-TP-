import java.io.*;
import java.net.*;
import java.util.Scanner;

class EchoClient {

	public static void main(String[] args) {

		BufferedReader br = null; // pour lire du texte sur la socket
		PrintStream ps = null; // pour écrire du texte sur la socket
		String line = null;
		Socket sock = null;
		int port = -1;
		Scanner sc = new Scanner(System.in);
		String messageToSend = null;

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
			br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // création flux lecture lignes de																			// texte
			ps = new PrintStream(sock.getOutputStream()); // création flux écriture lignes de texte

			while (true) {
				System.out.println("Veuillez saisir le message à envoyer au serveur : ");
				messageToSend = sc.nextLine();

				if (messageToSend.isEmpty()) {
					System.out.println("Fin du client.");
					break;
				}

				ps.println(messageToSend); // envoi du message au serveur
				line = br.readLine(); // lecture réponse serveur
				System.out.println("le serveur me repond : " + line); // affichage debug
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