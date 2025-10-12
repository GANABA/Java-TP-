import java.util.*;

public class Party {

    private static Random loto = new Random(Calendar.getInstance().getTimeInMillis());

    public static int PARTY_STATE_IDLE = 0;
    public static int PARTY_STATE_PLAYING = 1;
    public static int PARTY_STATE_END = 2;

    int state; // state of the party : 0 = wait to start, 1 = playing, 2 = end

    Board board;
    int currentPlayer; // when state is playing, gives the current player's id : 0 or 1, alternating
    List<String> playersPseudo;

    Semaphore semPartyStart; // semaphore to wait the beginning of the party, using nbPartyStart as a counter
    int nbPartyStart; // number of threads waiting for the party to start
    Semaphore semCurrentPlayed; // semaphore to wait that current player played sthg. CAUTION: the current thread MUST also use this semaphore
    int nbCurrentPlayed; // number of thread that are waiting for current player played
    Semaphore semEndParty; // semaphore to wait the end of the party
    int nbPartyEnd;

    List<Integer> pawnValuesPlayer0; // same but for the pleyr
    List<Integer> pawnValuesPlayer1; // same but for the pleyr

    public Party()  {
        // just create what must be created a single time
        // because reset() will be called to initialize attributes values
        board = new Board();
        semPartyStart = new Semaphore(0);
        semEndParty = new Semaphore(0);
        playersPseudo = new ArrayList<>();
        pawnValuesPlayer0 = new ArrayList<>();
        pawnValuesPlayer1 = new ArrayList<>();

        reset();
    }


    // reset() is only called by the main server before player's thread are created => not synchronized
    public void reset() {
        board.clearBoard();
        state = PARTY_STATE_IDLE;

        playersPseudo.clear();

        nbPartyStart = 0;
        nbPartyEnd = 0;

        // recreate semaphore to be sure there are no tokens in it.
        semCurrentPlayed = new Semaphore(0);
        nbCurrentPlayed = 0;

        currentPlayer = -1;
        pawnValuesPlayer0.clear();
        pawnValuesPlayer1.clear();
        for(int i=1;i<=6;i++) {
            pawnValuesPlayer0.add(i);
            pawnValuesPlayer1.add(i);
        }
        Collections.shuffle(pawnValuesPlayer0);
        Collections.shuffle(pawnValuesPlayer1);
    }

    public synchronized String getBoard() {
        return board.toString();
    }

    public synchronized int getCurrentPlayer() {
        return currentPlayer;
    }

    public synchronized int getState() {
        return state;
    }

    /*
    get the pawn value that must be played by a player during its turn #playerTurn
     */
    public synchronized int getPawnValue(int playerTurn) {
        if (currentPlayer == 0) {
            return pawnValuesPlayer0.get(playerTurn);
        }
        else if (currentPlayer == 1) {
            return pawnValuesPlayer1.get(playerTurn);
        }
        return -1;
    }

    // returns true if pseudo already exists, false otherwise
    public synchronized boolean isPseudoExists(String pseudo) {
        return playersPseudo.contains(pseudo);
    }

    // try to register the pseudo so that the thread is considered to be a valid player for the party to come.
    public synchronized int setPseudo(String pseudo) {
        if ((playersPseudo.contains(pseudo)) || (playersPseudo.size() == 2)) {
            return -1;
        }
        playersPseudo.add(pseudo);
        return playersPseudo.size()-1;
    }

    /* BEWARE : This method MUST NOT be synchronized or it will lead to a dead-lock

    Explanation assuming it is synchronized :
       - the first thread that calls this method lock the mutex of the object that owns the method, i.e. the party object (created in the main server)
       - then it increments nbPartyStart to 1
       - since the test is false, it goes directly to semPartyStart.get(1) and blocks until there are tokens put in the semaphore

       The problem is that when the thread calls wait() that is within the get() method, it just releases the mutex of the semaphore
       object AND NOT the mutex of the party object. If another thread wants to enter in waitPartyStart(), it is not possible because the mutex of party is
       still locked, thus the thread must wait. It means that no thread is able to reach the instruction put() that is used to put tokens in the semaphore
        and to unblock waiting threads. It is a dead-lock situation.

        BUT, if waitPartyStart() IS NOT synchronized, any thread can freely enters in it.

     */
    public void waitPartyStart() {
        /* NB: the second thread to enter in this method puts 2 tokens
        then tries to get one, which is necessarily succesful. Meanwhile,
        the first thread is already waiting in the get() method. Since 2 tokens are put, there is one left
        that allows the first thread to be unblocked.

        This is the classical way to implement a synchronization barrier using semaphore and it can be easily
        adadpted for X threads (i.e. the Xth thread to enter puts X tokens)
         */
        nbPartyStart += 1;
        if (nbPartyStart == 2) {
            // reset to 0 for the next party
            nbPartyStart = 0;
            // 0 is the first player
            currentPlayer = 0;
            state = PARTY_STATE_PLAYING;
            semPartyStart.put(2); // put 2 tokens so that both threads can stop waiting
        }
        semPartyStart.get(1); // get one token
    }

    public synchronized boolean testAndPlay(int idCell, int value) {
        /* A COMPLETER :
           - si la case idCell est vide :
              - mettre value dans cette case
              - retourne true
         */
        if (board.isCellEmpty(idCell)){
            board.setPawn(idCell, (byte) value);
            return true;
        }
        return false;
    }

    public void waitCurrentPlayed() {
        /* A COMPLETER :
           en s'inspirant de waitPartyStart(), écrire le code qui permet d'utiliser le sémaphore semCurentPlayed
           afin que les 2 threads puissent attendre que celui qui "joue" ait fini.
           Attention : pour que cela fonctionne comme dans waitPartyStart(), il faudra que les deux thread appellent
           cette méthode (cf. ServerThreadDiamond)
         */
        nbCurrentPlayed += 1;
        if (nbCurrentPlayed == 2) {
            nbCurrentPlayed = 0;
            semCurrentPlayed.put(2); // put 2 tokens so that both threads can stop waiting
        }
        semCurrentPlayed.get(1); // get one token
    }

    public synchronized void setNextPlayer() {
        currentPlayer = (currentPlayer+1)%2;
    }

    public synchronized void computePartyResult() {
        board.computeScore();
    }

    public synchronized String getPartyScore() {
        return board.getBlueScore()+ " " + board.getRedScore();
    }

    public void waitPartyEnd() {
        /* A COMPLETER :
           - mettre l'état de la partie à fin
           - mettre 2 tokens dans semCurrentPlayed, afin de débloquer un thread qui serait en attente de tokens.
           - incrémenter nbPartyEnd
           - si nbPartyEnd == 2 :
              - réinitialiser party
              - mettre 2 tokens dans semEndParty
            - prendre un token dans semEndParty.
         */
        state = PARTY_STATE_END;
        semCurrentPlayed.put(2);
        nbPartyEnd += 1;
        if (nbPartyEnd == 2) {
            reset();
            semEndParty.put(2);
        }
        semEndParty.get(1);
    }
}
