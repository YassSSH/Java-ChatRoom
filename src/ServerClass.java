import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerClass implements Runnable {

    private ArrayList<ConnectionHnadler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public ServerClass() {
        connections = new ArrayList<>();
        done = false;
    }


    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHnadler Handler = new ConnectionHnadler(client);
                connections.add(Handler);
                pool.execute(Handler);
            }
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

    public void shutdown(){
        try {
        done = true;
        if(!server.isClosed()) {
            server.close();
        }
        for (ConnectionHnadler ch : connections){
            ch.shutdown();
        }
        }catch (IOException e) {
            shutdown();
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
                        broadcast(pseudo + "s'est envolé !");
                        shutdown();
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

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        ServerClass server = new ServerClass();
        server.run();
    }
}
