import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerClass implements Runnable {

    private ArrayList<ConnectionHnadler> connections;


    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(9999);
            Socket client = server.accept();
            ConnectionHnadler Handler = new ConnectionHnadler(client)
            connections.add(Handler)
        } catch (IOException e) {
            // TODO: handle
        }

    }
    public void broadcast(String message) {
        for (ConnectionHnadler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    class ConnectionHnadler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String pseudo;

        public ConnectionHnadler(Socket client) {
            this.client =  client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Entrez votre pseudo : ");
                pseudo = in.readLine();
                System.out.println(pseudo + "Connécté ! ");
                broadcast(pseudo + " a join le chat !");
                String message;
                while((message = in.readLine()) != null) {
                    if (message.startsWith("!p ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2){
                            broadcast(pseudo + " a changé de pseudo pour " + messageSplit[1]);
                            System.out.println(pseudo + " a changé de pseudo pour " + messageSplit[1]);
                            pseudo = messageSplit[1];
                            out.println("Le pseudo a été changé en :" + pseudo);
                        }else {
                            out.println("Pas de pseudo renseigné.");
                        }
                    }else if (message.startsWith("!quit ")) {
                        //TODO : quit
                    }else {
                        broadcast(pseudo + ": " + message);
                    }
                }
            } catch (IOException e) {
                // TODO: handle
            }

        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
