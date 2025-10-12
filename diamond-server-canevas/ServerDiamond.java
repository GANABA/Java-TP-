import java.io.*;

class ServerDiamond {

    public static void usage() {

        System.err.println("usage : java ServerDiamond port");
        System.exit(1);
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            usage();
        }
        int port = 0;
        ServerTCPDiamond server = null;
        try {
            port = Integer.parseInt(args[0]);
            server = new ServerTCPDiamond(port);
            server.mainLoop();
        }
        catch(NumberFormatException e ) {
            usage();
        }
        catch(IOException e) {
            System.err.println("cannot setup server or accept clients");
            System.exit(1);
        }
    }
}
