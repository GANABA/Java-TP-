import java.io.*;
import java.net.*;

class ServerThreadDiamond extends Thread  {

    private Socket sockComm;
    private PrintStream ps;
    private BufferedReader br;
    private int idPseudo; // the id of my pseudo in the list managed by party
    private int idPlayer; // the unique identifier of the client and thus the thread.
    private Party party;
    private int idThread;
    private String pseudo;

    public ServerThreadDiamond(Socket sockComm, Party party, int idThread) {
        this.sockComm = sockComm;
        this.party = party;
        this.idThread = idThread;
        idPlayer = -1;
        idPseudo = -1;
        pseudo = null;
    }

    public void run() {

        /* A COMPLETER : initialisation des flux br et ps
           NB : mettre cette initialisation dans un try/catch, et s'il y a catch, return (=> arrêt du thread)
         */
        try{
            br = new BufferedReader(new InputStreamReader(sockComm.getInputStream())); // création flux lecture lignes                                                                          // // texte
            ps = new PrintStream(sockComm.getOutputStream()); // création flux écriture lignes de texte

        }catch(IOException e){
            System.out.println("Erreur Thread : "+ idThread);
        }

        // NB : until party is really started, there may be more than 2 threads that are
        // competing to participate to the party. It is only when trying to register a pseudo
        // that the thread will know if it can really participate or have to disconnect because
        // to other threads are already taken.

        /* A COMPLETER : phase du pseudo
           - si partie déjà en cours, envoyer "ERR PARTY_STARTED" au client, puis return (=> arrêt du thread)
           - sinon envoyer "OK"
           - appeler getPseudo() et si valeur retournée = false, return (=> arrêt du thread)
           - appeler party.setPseudo() avec le pseudo ainsi obtenu et affecter la valeur retournée à idPlayer
           - si idPlayer == -1, envoyer au client "ERR PSEUDO_REJECTED"
           - sinon envoyer au client "OK"
         */
        if (party.getState() == 1) {
            ps.println("PARTY_STARTED");
            return;
        }else{
            ps.println("OK");
        }

        if (!getPseudo()) { return; } // appel de getPseudo()

        idPlayer = party.setPseudo(pseudo); // appel de party.setPseudo()

        if (idPlayer == -1) {
            ps.println("ERR PSEUDO_REJECTED");
        }else{
            ps.println("OK");
        }


        // waiting for two clients, in order to start the party
        party.waitPartyStart();

        // now play
        partyLoop();

        System.out.println("Thread ["+idThread+"] - party seems over. Waiting the other thread to get out of the party.");
        // waiting for the two thread to finish the party, normally or suddenly
        // only the second resets the party object
        party.waitPartyEnd();

        // close the socket so that client knows that party is over, even if it ended abnormally.
        try {
            sockComm.close();
        }
        catch(IOException e) {}
        System.out.println("Thread ["+idThread+"] - stop.");
    }


    public boolean getPseudo() {

        System.out.println("Thread ["+idThread+"] - entering getPseudo().");

        boolean stop = false;
        /* A COMPLETER :
           - tant que stop == false :
              - lire une ligne sur la socket, dns un try/catch IOException
              - si catch retourner false
              - si ligne == null retourner false
              - sinon si pseudo existe (cf. party.isPseudoExists() ), renvoyer au client "ERR PSEUDO"
              - sinon envoyer au client "OK" et stop = true.
         */
        while (stop == false) {
            String line = null;
            try{
                line = br.readLine();
            }catch(IOException e){
                return false;
            }

            if(line == null){
                return false;
            }

            if(party.isPseudoExists(line)){
                ps.println("ERR PSEUDO");
            }else{
                ps.println("OK");
                pseudo = line;
                stop = true;
            }
        }

        // At this point the pseudo is valid but the thread is not considered to represent a valid player for
        // the party to come.
        return true;
    }

    public void partyLoop() {

        System.out.println("Thread ["+idThread+"] - entering partyLoop().");

        String line = null;
        int pawnValue = -1;

        // send the player id to my client
        ps.println(idPlayer);

        // both players are playing 6 times, so there are 12 turns in a party
        for(int i=0;i<12;i++) {
            /* A COMPLETER :
               - si state party == fin, return (=> le thread quitte la partie)
               - envoyer au client le visuel du plateau de jeu (cf. Board.toString())
               - envoyer au client l'id du joueur courant (cf. Party.getCurrentPlayer())
               - si je suis le thread du joueur courant :
                  - obtenir la valeur du jeton à jouer (cf. Party.getPawnValue())
                  - appeler getOrder() avec cette valeur en paramètre,
                  - si getOrder() retourne false, return (=> le thread quitte la partie)
                  - déterminer l'id du prochain joueur (cf. Party.setNextPlayer())
                  - si dernier tour, calculer le résultat de la partie (cf. Party.ComputePartyResult())
               - attendre la fin du tour de jeu (cf. Party.waitCurrentPlayed())
               - si n° tour entre 0 et 10, envoyer au client "CONT"
            */
            if(party.getState() == party.PARTY_STATE_END){
                return;
            }

            ps.println(party.getBoard());
            ps.println(party.getCurrentPlayer());

            if(party.getCurrentPlayer() == idPlayer){
                int jeton = party.getPawnValue(i/2);

                if(!getOrder(jeton)){
                    return;
                }

                party.setNextPlayer();

                if(i == 11) {
                    party.computePartyResult();
                }

                party.waitCurrentPlayed();

                if (i < 11) {
                    ps.println("CONT");
                }
            }
        }
        // abnormal end of the party
        if (party.getState() == Party.PARTY_STATE_END) return;

        // normal end of the party
        ps.println("END "+party.getPartyScore());
        ps.println(party.getBoard());
    }

    public boolean getOrder(int pawnValue) {
        boolean valid = false;
        String line = null;
        int idCell = -1;

        /* A COMPLETER :
           - envoyer au client pawnValue
           - tant que valid == false :
              - lire une ligne sur la socket, dans un try/catch
              - si catch, retourner false
              - si ligne vide, retourner false
              - transformer ligne en un integer -> idCell
              - si transformation invalide, envoyer au client "ERR INVALID_CELL"
              - sinon, essayer de placer pawnValue dans la case idCell (cf. Party.testAndPlay())
              !! ATTENTION !! pour le joueur 1, il faut ajouter 6 à pawnValue, car dans Board, ses pions
              doivent être numérotés de 7 à 12.
              - si placement valide, envoyer "OK" au client et valid = true
              - sinon envoyer "ERR INVALID CELL" au client.
         */
        ps.println(pawnValue);
        while (valid == false) {
            try{
                line = br.readLine();
            }catch(IOException e){
                return false;
            }

            if(line == null || line.trim().isEmpty()){
                return false;
            }

            try{
                idCell = Integer.parseInt(line.trim());
            }catch(NumberFormatException e){
                ps.println("ERR INVALID_CELL");
                continue;
            }

            if(idPlayer == 1){
                pawnValue += 6;
            }

            if(party.testAndPlay(idCell, pawnValue)){
                ps.println("OK");
                valid = true;
            }else{
                ps.println("ERR INVALID_CELL");
            }
        }
        return true;
    }
}
