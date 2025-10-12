import java.io.*;
import java.net.*;

class ServerTCPDiamond  {

    ServerSocket conn;
    Socket sockComm;
    Party party;
    private static int idThread;

    public ServerTCPDiamond(int serverPort) throws IOException {

        conn = new ServerSocket(serverPort);
        party = new Party();
        idThread = 1;
    }

    public void mainLoop() throws IOException{

        while (true) {
            // wait for connection, even if a party is already started => the party will kick off the new connection
            sockComm = conn.accept();
            System.out.println("connection from " + sockComm.getRemoteSocketAddress() + ", starting server thread.");
            ServerThreadDiamond t = new ServerThreadDiamond(sockComm, party, idThread++);
            t.start();
        }
    }
}
