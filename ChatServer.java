
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 1234;
    private static Set<ClientHandler> clients = new HashSet<>();
    private static int userIdCounter = 1;

    public static void main(String[] args) {
        System.out.println("Chat Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket, userIdCounter++);
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private int userId;

        ClientHandler(Socket socket, int userId) {
            this.socket = socket;
            this.userId = userId;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Connected as User " + userId);
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("User " + userId + ": " + message);
                    broadcast("User " + userId + ": " + message, this);
                }
            } catch (IOException e) {
                System.out.println("User " + userId + " disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {}
                removeClient(this);
            }
        }

        void sendMessage(String message) {
            out.println(message);
        }
    }
}
