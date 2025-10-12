import java.net.*;
import java.io.*;

class ClientDiamond {

	public static void usage() {
		System.err.println("usage : java ClientDiamond server_ip port");
		System.exit(1);
	}

	public static void main(String[] args) {

		if (args.length != 2) {
			usage();
		}

		int port = -1;
		ClientTCPDiamond client = null;

		try {
			port = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e) {
			usage();
		}

		try {
			client = new ClientTCPDiamond(args[0], port);
			client.mainLoop();
		}
		catch(IOException e) {
			System.err.println("cannot communicate with server");
			System.exit(1);
		}
	}
}
