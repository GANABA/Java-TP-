import java.io.*;
import java.net.*;
import java.util.Scanner;

class SommeClient {

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
				System.out.println("Combien de nombre vous souhaitez calculer : ");
				String nbrSaisi = sc.nextLine();
				System.out.println("Veuillez saisir la série de nombres à envoyer au serveur : ");
				messageToSend = sc.nextLine();

                if (nbrSaisi.isEmpty()) {
					System.out.println("Fin du client.");
					break;
				}
				if (messageToSend.isEmpty()) {
					System.out.println("Fin du client.");
					break;
				}
                
                // verification client de la validité de la requete
                // découper la ligne saisie au clavier en morceaux
                // vérifier que chaque morceau se convertit en entier
                // si le cas, envoyer chaque entier au serveur sinon ne rien faire
                int entierSaisi = Integer.parseInt(nbrSaisi.trim());
                String[] numbers = messageToSend.split(",");
                if(numbers.length != entierSaisi){
                    System.out.println("Requête malformée - paramètres invalides !");
                    continue;
                }   
                int[] nums = new int[numbers.length];
                boolean validRequest = true;
                for (int i = 0; i < numbers.length; i++) {
                    try {
                        nums[i] = Integer.parseInt(numbers[i].trim());
                    } catch (NumberFormatException e) {
                        validRequest = false;
                        break;
                    }
                }   

                if(!validRequest){
                    System.out.println("Requête malformée !");
                    continue;
                }
                // envoyer au serveur le premier entier qui est le nombre d'entiers à suivre
                ps.println(entierSaisi);

                for (int num : nums) {
                    ps.println(num); // envoyer chaque entier au serveur
                }
				//ps.println(messageToSend); // envoi du message au serveur

				line = br.readLine(); // lecture réponse serveur

                if(line.equals("REQ_ERR")){
                    System.out.println("Requête malformée !");
                }else{
                    System.out.println("Somme reçue du serveur : " + line); // affichage debug
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