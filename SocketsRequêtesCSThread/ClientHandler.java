import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

class ClientHandler implements Runnable {
    private Socket sock;
    private int clientId;

    ClientHandler(Socket sock, int clientId) {
        this.sock = sock;
        this.clientId = clientId;
    }

    public void run() {
        try (
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintStream ps = new PrintStream(sock.getOutputStream())
        ) {
            // envoyer l’ID au client
            ps.println(clientId);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) break;

                int numReq;
                try {
                    numReq = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    ps.println("REQ_ERR");
                    continue;
                }

                int idFromClient;
                try {
                    idFromClient = Integer.parseInt(br.readLine().trim());
                } catch (Exception e) {
                    ps.println("NOID_ERR");
                    continue;
                }
                if (idFromClient != clientId) {
                    ps.println("NOID_ERR");
                    continue;
                }

                // récupérer la liste associée à ce client
                synchronized (PositionServer.clientPositions) {
                    PositionServer.clientPositions.putIfAbsent(clientId, new ArrayList<>());
                }
                ArrayList<Position> listePos = PositionServer.clientPositions.get(clientId);

                switch (numReq) {
                    case 1: // storepos
                        try {
                            double x = Double.parseDouble(br.readLine().trim());
                            double y = Double.parseDouble(br.readLine().trim());
                            double z = Double.parseDouble(br.readLine().trim());
                            synchronized (listePos) {
                                listePos.add(new Position(x, y, z));
                            }
                            ps.println("OK");
                        } catch (Exception e) {
                            ps.println("ERR_REQ");
                        }
                        break;

                    case 2: // pathlen
                        double sommeDistances = 0.0;
                        synchronized (listePos) {
                            for (int i = 1; i < listePos.size(); i++) {
                            Position pos1 = listePos.get(i - 1);
                            Position pos2 = listePos.get(i);
                            sommeDistances += pos1.distanceTo(pos2);
                        }
                        }
                        ps.println(sommeDistances);
                        break;

                    case 3: // findpos
                        try {
                            double eps = Double.parseDouble(br.readLine().trim());
                            double x = Double.parseDouble(br.readLine().trim());
                            double y = Double.parseDouble(br.readLine().trim());
                            double z = Double.parseDouble(br.readLine().trim());
                            Position toCheck = new Position(x, y, z);
                            boolean found = false;
                            synchronized (listePos) {
                                for (Position p : listePos) {
                                    if (p.equals(toCheck, eps)) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            ps.println(found ? "TRUE" : "FALSE");
                        } catch (Exception e) {
                            ps.println("ERR_REQ");
                        }
                        break;

                    case 4: // denivel
                        double pos = 0, neg = 0;
                        synchronized (listePos) {
                            for (int i = 1; i < listePos.size(); i++) {
                                double delta = listePos.get(i).getZ() - listePos.get(i - 1).getZ();
                                if (delta > 0) pos += delta;
                                else if (delta < 0) neg += -delta;
                            }
                        }
                        ps.println(pos + "," + neg);
                        break;

                    case 5: // lastpos
                        StringBuilder sb = new StringBuilder();
                        synchronized (PositionServer.clientPositions) {
                            for (Integer otherId : PositionServer.clientPositions.keySet()) {
                                if (otherId != clientId) {
                                    ArrayList<Position> otherList = PositionServer.clientPositions.get(otherId);
                                    if (otherList != null && !otherList.isEmpty()) {
                                        Position last = otherList.get(otherList.size() - 1);
                                        sb.append("client ").append(otherId).append(": ").append(last).append("; ");
                                    }
                                }
                            }
                        }
                        ps.println(sb.length() == 0 ? "NONE" : sb.toString());
                        break;

                    default:
                        ps.println("REQ_ERR");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur avec client " + clientId + ": " + e.getMessage());
        } finally {
            try {
                sock.close();
                System.out.println("Client " + clientId + " déconnecté.");
            } catch (IOException ignored) {}
        }
    }
}
