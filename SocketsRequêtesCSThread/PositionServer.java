import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PositionServer {
    // Map partagée : un seul espace mémoire pour tous les threads
    static HashMap<Integer, ArrayList<Position>> clientPositions = new HashMap<>();
    static int nextId = 0; // compteur d’ID clients

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: Server port");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur démarré sur le port " + port);

            while (true) {
                Socket clientSock = serverSocket.accept(); // attendre un client
                synchronized (PositionServer.class) {
                    nextId++;
                }
                int clientId = nextId;
                System.out.println("Nouveau client connecté (ID=" + clientId + ")");
                // lancer un thread pour gérer ce client
                new Thread(new ClientHandler(clientSock, clientId)).start();
            }
        } catch (IOException e) {
            System.out.println("Erreur serveur : " + e.getMessage());
        }
    }
}
