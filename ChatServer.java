package com.mycompany.tcp_ip;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final int PORT = 6000;

    private static Set<PrintWriter> clientWriters =
            Collections.synchronizedSet(new HashSet<>());

    private static int clientCount = 0; // To assign client names

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket socket = serverSocket.accept();
                clientCount++;

                String clientName = "Client" + clientCount;
                System.out.println(clientName + " connected: " + socket);

                new Thread(new ClientHandler(socket, clientName)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket, String clientName) {
            this.socket = socket;
            this.clientName = clientName;
        }

        public void run() {
            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                clientWriters.add(out);

                broadcast(clientName + " joined the chat");

                String message;

                while ((message = in.readLine()) != null) {
                    String formattedMessage = clientName + ": " + message;

                    System.out.println(formattedMessage);

                    broadcast(formattedMessage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    socket.close();
                } catch (IOException e) {
                }

                clientWriters.remove(out);
                broadcast(clientName + " left the chat");
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}