package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private final Scanner scanner;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.scanner = new Scanner(System.in);
    }

    public void startClient() {
        try {
            // Handle server prompts
            while (true) {
                String serverMessage = bufferedReader.readLine();
                if (serverMessage == null) break;
                System.out.println(serverMessage);

                if (serverMessage.startsWith("Login successful") || serverMessage.startsWith("Sign-up successful")) {
                    break; // Exit authentication loop
                }

                // Send user input to the server
                String clientResponse = scanner.nextLine();
                bufferedWriter.write(clientResponse);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

            // Start listening for messages
            new Thread(this::listenForMessages).start();

            // Allow user to send messages
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void listenForMessages() {
        try {
            String messageFromServer;
            while (socket.isConnected()) {
                messageFromServer = bufferedReader.readLine();
                if (messageFromServer == null) break;
                System.out.println(messageFromServer);
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void closeEverything() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234)) {
            System.out.println("Connected to the server.");
            Client client = new Client(socket);
            client.startClient();
        } catch (IOException e) {
            System.out.println("Failed to connect to the server: " + e.getMessage());
        }
    }
}
