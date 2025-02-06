package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class TCPdemo {
    private ServerSocket server;

    public void start(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started and running on port: " + port);

            while (true) {
                Socket socket = server.accept();
                Runnable clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TCPdemo().start(8080);
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println("Hello new client. Greetings from the server");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Message from client: " + inputLine);
                    if ("bye".equals(inputLine)) {
                        out.println("Goodbye i'm shutting down");
                        break;
                    }
                    out.println("Echo:" + inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
