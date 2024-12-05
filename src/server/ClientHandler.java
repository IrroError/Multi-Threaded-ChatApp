package server;

import database.UserDAO;
import services.AuthService;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private final AuthService authService;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.authService = new AuthService(new UserDAO());
        BufferedReader tempBufferedReader = null;
        BufferedWriter tempBufferedWriter = null;
        try {
            tempBufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            tempBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Prompt user to log in or sign up
            tempBufferedWriter.write("Enter 1 to Login or 2 to Sign-up:");
            tempBufferedWriter.newLine();
            tempBufferedWriter.flush();
            String option = tempBufferedReader.readLine();

            if ("1".equals(option)) {
                if (!performLogin(tempBufferedReader, tempBufferedWriter)) {
                    closeEverything(socket, tempBufferedReader, tempBufferedWriter);
                }
            } else if ("2".equals(option)) {
                if (!performSignup(tempBufferedReader, tempBufferedWriter)) {
                    closeEverything(socket, tempBufferedReader, tempBufferedWriter);
                }
            } else {
                tempBufferedWriter.write("Invalid option. Disconnecting...");
                tempBufferedWriter.newLine();
                tempBufferedWriter.flush();
                closeEverything(socket, tempBufferedReader, tempBufferedWriter);
            }

            // If authenticated, initialize communication
            this.bufferedReader = tempBufferedReader;
            this.bufferedWriter = tempBufferedWriter;
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has joined the chat!");

        } catch (IOException e) {
            closeEverything(socket, tempBufferedReader, tempBufferedWriter);
            throw new RuntimeException("ClientHandler initialization failed.", e);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean performLogin(BufferedReader reader, BufferedWriter writer) throws IOException {
        writer.write("Enter username:");
        writer.newLine();
        writer.flush();
        String username = reader.readLine();

        writer.write("Enter password:");
        writer.newLine();
        writer.flush();
        String password = reader.readLine();

        if (authService.loginUser(username, password)) {
            this.clientUsername = username;
            writer.write("Login successful! Welcome to the chat.");
            writer.newLine();
            writer.flush();
            return true;
        } else {
            writer.write("Login failed. Disconnecting...");
            writer.newLine();
            writer.flush();
            return false;
        }
    }

    private boolean performSignup(BufferedReader reader, BufferedWriter writer) throws IOException {
        writer.write("Enter a username:");
        writer.newLine();
        writer.flush();
        String username = reader.readLine();

        writer.write("Enter a password:");
        writer.newLine();
        writer.flush();
        String password = reader.readLine();

        if (authService.registerUser(username, password)) {
            writer.write("Sign-up successful. You can now log in.");
            writer.newLine();
            writer.flush();
            return true;
        } else {
            writer.write("Sign-up failed. Disconnecting...");
            writer.newLine();
            writer.flush();
            return false;
        }
    }
}
