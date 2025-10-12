import java.io.*;
import java.net.*;

class ClientTCPDiamond {

	Socket sockComm;
	PrintStream ps;
	BufferedReader br;
	BufferedReader consoleIn; // keyboard readings
	int idPlayer; // sent by the server after accepting pseudo and = to 0 or 1

	public ClientTCPDiamond(String serverIp, int serverPort) throws IOException {

		sockComm = new Socket(serverIp, serverPort);
		ps = new PrintStream(sockComm.getOutputStream());
		br = new BufferedReader(new InputStreamReader(sockComm.getInputStream()));
		consoleIn = new BufferedReader(new InputStreamReader(System.in));
	}

	private void stopParty() {
		System.out.println("Party is over because of a network disconnection");
		System.exit(0);
	}

	public void mainLoop() throws IOException {
		String line = null;

		/* A COMPLETER :
		   - lire une ligne sur la socket
		   - si ligne == null ou ligne != OK, return (=> arrêt du client)
		   - sinon appeler setPseudo()
		   - lire une ligne sur la socket
		   - si ligne == null ou ligne != OK, return (=> arrêt du client)
		   - sinon appeler partyLoop()
		 */
		line = br.readLine();
		if (line == null || !line.equals("OK")) return;
		setPseudo();
		line = br.readLine();
		if (line == null || !line.equals("OK")) return;
		partyLoop();
		
	}

	private void setPseudo() throws IOException {

		String line = null;
		String[] lineParts = null;
		boolean ok = false;

		/* A COMPLETER :
		   - tant que ok == false :
		      - message pour demander de taper le pseudo
		      - lire ligne au clavier
		      - si ligne == null, arrêt du client
		      - sinon si ligne vide, affichage message erreur, style "pseudo invalide"
		      - sinon si ligne non vide :
		         - envoi de la ligne au serveur
		         - reception de la réponse
		         - si réponse == "OK", ok = true
		         - sinon affiche message d'erreur, style "pseudo déjà pris"
		 */
		line = null;
		while (!ok) {
			System.out.println("Entrez votre pseudo: ");
			line = consoleIn.readLine();
			if (line == null) stopParty();
			else if (line.isEmpty()) System.out.println("Pseudo invalide");
			else {
				ps.println(line);
				line = br.readLine();
				if (line == null) stopParty();
				else if (line.equals("OK")) ok = true;
				else System.out.println("Pseudo déjà pris");
			}
		}
	}


	private void receiveAndDisplayBoard() throws IOException {
		/* NB: the textual representation of the board is composed of 6 lines, hence the loop with 6 iterations
		*/
		String line = null;
		for(int i=0;i<6;i++) {
			line = br.readLine();
			if (line == null) stopParty();
			System.out.println(line);
		}
	}

	private void partyLoop() throws IOException {

		String line = null;
		String[] lineParts = null;
		boolean stop = false;
		int currentPlayer = -1; // the id of the current player
		int pawnValue = 0; // the vlaue of the pawn to play
		boolean valid = false;

		/* A COMPLETER :
		   - lire une ligne sur la socket
		   - si ligne == null, appeler stopParty() (=> arrêt du client)
		   - transformer ligne en integer et l'affecter à idPlayer
		 */
		line = br.readLine();
		if (line == null) stopParty();
		idPlayer = Integer.parseInt(line);
		System.out.println("You are player " + idPlayer);

		while (!stop) {
			// start a new turn
			receiveAndDisplayBoard();


			/* A COMPLETER : lire l'id du joueur courant
			   - lire une ligne sur la socket
			   - si ligne == null, appeler stopParty() (=> arrêt du client)
		       - transformer ligne en integer et l'affecter à currentPlayer
			 */
			line = br.readLine();
			if (line == null) stopParty();
			currentPlayer = Integer.parseInt(line);

			// if I am the current player
			if (idPlayer == currentPlayer) {
				System.out.println("It is your turn to play");
				valid = false;
				/* A COMPLETER : lire la valeur du pion à jouer
				   - lire une ligne sur la socket
				   - si ligne == null, appeler stopParty() (=> arrêt du client)
		           - transformer ligne en integer et l'affecter à pawnValue
		           - tant que valid == false :
		              - afficher message poue demander de taper un numéro de case
		              - lire ligne a clavier
		              - si ligne == null, appeler stopParty() (=> arrêt du client)
		              - envoyer ligne au serveur
		              - reception de la réponse
		              - si réponse == null, appeler stopParty() (=> arrêt du client)
		              - sinon si réponse == "OK", valid = true
		              - sinon affiche message d'erreur, style "case invalide"
				 */
				line = br.readLine();
				if (line == null) stopParty();
				pawnValue = Integer.parseInt(line);
				while (!valid) {
					System.out.println("Enter le numéro de la case " + pawnValue + ": ");
					line = consoleIn.readLine();
					if (line == null) stopParty();
					ps.println(line);
					line = br.readLine();
					if (line == null) stopParty();
					else if (line.equals("OK")) valid = true;
					else System.out.println("case invalide");
				}
			}
			// If I'm not the current player
			else {
				System.out.println("Waiting for the other player to play...");
			}

			/* A COMPLETER : reception de l'état de la partie
			   - lire une ligne sur la socket
			   - si ligne == null, appeler stopParty() (=> arrêt du client)
			   - découper la ligne -> lineParts
			   - si lineParts[0] == "END" :
			      - recevoir et afficher le plateau de jeu
			      - transformer lineParts[1] et lineParts[2] en integer, pour avoir les scores de chaque joueur
			      - afficher qui gange ou bien partie null
			      - stop = true
			 */
			line = br.readLine();
			if (line == null) stopParty();
			lineParts = line.split(" ");
			if (lineParts[0].equals("END")) {
				receiveAndDisplayBoard();
				int score0 = Integer.parseInt(lineParts[1]);
				int score1 = Integer.parseInt(lineParts[2]);
				if (score0 > score1) System.out.println("Player 0 wins!");
				else if (score1 > score0) System.out.println("Player 1 wins!");
				else System.out.println("It's a draw!");
				stop = true;
			}
		}
	}
}