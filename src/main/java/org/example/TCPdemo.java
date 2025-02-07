package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TCPdemo {
    private ServerSocket server;


    public void start(int port) {
        try {
            server = new ServerSocket(port); // ServerSocket object created with port 8080, server will accept client connections on that port.
            System.out.println("Server started and running on port: " + port);




            while (true) { // infinite loop, server will run forever.
                Socket socket = server.accept(); // Waits for client to establish connection. When a client connects, it returns a Socket object that represents the client connection.
                Runnable clientHandler = new ClientHandler(socket); // A new client handler is created for each connected client, and it's executed in a separate thread using new Thread(clientHandler).start().
                new Thread(clientHandler).start(); // This makes the server multi-threaded and capable of handling multiple clients concurrently.
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TCPdemo().start(8080); // Creates instance of TCPdemo and calls the start method, passing the port 8080 as the argument to start the server.
    }

    private static class ClientHandler implements Runnable { // Nested static class implementing Runnable, which means it can be executed by a thread.
        private Socket clientSocket; // Socket object representing the connection to the client.
        private PrintWriter out; // A PrintWriter to send data (response) back to the client.
        private BufferedReader in; // A BufferReader to read data (request) from the client.
        private String username; // Stores the client's username (entered via the console)

        public ClientHandler(Socket socket) {
            this.clientSocket = socket; // Initialize the clientSocket with the passed-in Socket object.
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter your username: ");
            String name = scanner.nextLine();
            this.username = name;
        }

        @Override
        public void run() {
            try {
                 out = new PrintWriter(clientSocket.getOutputStream(), true); // Sends data to the client.
                 in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Reads data from the client.
                 out.println("Greetings " + username + ", you can start chatting now.");

                String inputLine; // Stores each line the client sends.
                while ((inputLine = in.readLine()) != null) { // An infinite loop, reading each line the client sends (using readLine()).
                    System.out.println("Message from " + username + ": " + inputLine);
                    if (inputLine.equalsIgnoreCase("BYE")) { // If the client enters "bye", the server sends a "Goodbye" message and terminates the connection using closeEverything() method, then breaks the loop.
                        out.println("Goodbye i'm shutting down");
                        closeEverything();
                        break;
                    }
                    if (inputLine.startsWith("GET")){ // If the message starts with "GET", the handleGetRequest() method is called.
                        handleGetRequest(inputLine);
                        break;
                    } else if (inputLine.startsWith("POST")) { // If the message starts with "POST", the handlePostRequest() method is called.
                        handlePostRequest(inputLine,in);
                    }
                    //out.println("Echo:" + inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void closeEverything(){ // Makes sure the input and output streams and the client sockets are properly closed to free up resources after the connection is terminated.
            try {
                out.close();
                in.close();
                clientSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        public void handleGetRequest(String requestLine) {
            System.out.println("Received request: " + requestLine);  // Debugging

            String[] parts = requestLine.trim().split("\\s+");  // Split on spaces

            if (parts.length != 3 || !parts[2].startsWith("HTTP/")) {
                sendBadRequest();
                return;
            }

            String path = parts[1];  // Extract the path

            if (path.equals("/")) {
                sendResponse(200, "OK", "Welcome to SimpleJavaHTTPServer! Available paths: /hello, /time, /echo");
            } else if (path.equals("/hello")) {
                sendResponse(200, "OK", "Hello, welcome to the server!");
            } else if (path.equals("/time")) {
                sendResponse(200, "OK", "Current server time: " + java.time.LocalDateTime.now());
            } else if (path.equals("/echo")) {
                sendResponse(200, "OK", storedMessage.isEmpty() ? "No message stored yet." : storedMessage);
            } else {
                sendResponse(404, "Not Found", "404 Not Found: The requested resource does not exist");
            }
        }
        private String storedMessage = ""; // Store the message for GET /echo

        public void handlePostRequest(String requestLine, BufferedReader in) {
            try {
                // Read headers
                int contentLength = 0;
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                }

                // Read request body completely
                StringBuilder requestBody = new StringBuilder();
                int totalRead = 0;
                while (totalRead < contentLength) {
                    int readChar = in.read();
                    if (readChar == -1) break; // Stop if stream ends unexpectedly
                    requestBody.append((char) readChar);
                    totalRead++;
                }

                storedMessage = requestBody.toString(); // Store the full message

                // Send response
                sendResponse(200, "OK", "Message stored: " + storedMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*String httpVersion = parts[2]; // HTTP/1.1

                if (!httpVersion.equals("HTTP/1.1")) {
                    sendBadRequest();
                    return;
                }

                String userAgent = "Unknown";
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("User-Agent")) {
                        userAgent = line.substring(12).trim();
                    }
                }

                String responseBody;
                switch (path){
                    case "/hello":
                        responseBody = "Hello, welcome to the server!";
                        break;
                    case "/time":
                        responseBody = "Current server time: " + java.time.LocalDateTime.now();
                        break;
                    case "/echo":
                        responseBody = "Echoing back your request!";
                        break;
                    default:
                        sendResponse(400, "Not Found", "404 Not Found: The requested resource does not exist");
                        return;
                }
                responseBody += "\nYour User.Agent: " + userAgent;
                sendResponse(200, "OK", responseBody);


            }catch (IOException e){
                e.printStackTrace();
            }

                 */
        public void sendResponse(int statusCode, String statusMessage, String body){
            out.println("HTTP/1.1 " + statusCode + " " + statusMessage);
            out.println("Date: " + java.time.ZonedDateTime.now());
            out.println("Server: SimpleJavaHTTPServer");
            out.println("Content-Type: text/plain; charset=UTF-8");
            out.println("Content-Length: " + body.length());
            out.println(); // Empty line before response body
            out.println(body);
        }
        public void sendBadRequest(){
            sendResponse(400,"Bad Request","400 Bad Request: Invalid request format.");
        }
    }
}
