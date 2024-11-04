package ServerClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Represents a client that connects to a server and handles sending and receiving messages.
 * The Client class manages its connection to the server, processes input from the user,
 * and displays server messages.
 */
public class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean done;
    private Thread inputThread;

    /**
     * Runs the client, establishing a connection to the server, starting the InputHandler
     * for user input, and listening for messages from the server.
     */
    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 8080);
            out = new PrintWriter(client.getOutputStream(), true); // ToServerWriter
            in = new BufferedReader(new InputStreamReader(client.getInputStream())); // FromServerReader

            InputHandler inHandler = new InputHandler(); // User Input in separate thread
            inputThread = new Thread(inHandler);
            inputThread.setDaemon(true); // Set as daemon thread
            inputThread.start(); // Run InputHandler

            // Receive and print messages from server
            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
                if (inMessage.startsWith("You left the server")) {
                    shutdown();
                    break;
                } else if (inMessage.startsWith("Server full or a game is currently running.")) {
                    shutdown();
                    break;
                }
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    /**
     * Shuts down the client by closing the input and output streams, the socket,
     * and setting the done flag to true to stop the InputHandler thread.
     */
    public void shutdown() {
        done = true;
        try {
            // Close resources
            if (in != null) in.close();
            if (out != null) out.close();
            if (!client.isClosed()) client.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Handles user input in a separate thread, allowing the client to send messages to the server
     * asynchronously while still receiving messages.
     */
    class InputHandler implements Runnable {

        /**
         * Continuously reads user input and sends it to the server. Stops if the done flag is set
         * or an IOException occurs.
         */
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message == null) {
                        break; // Stream closed
                    }
                    out.println(message); // Send message to server
                }
            } catch (IOException e) {
                // InputHandler exits when IOException occurs
            }
        }
    }

    /**
     * The main method to start the client, creating a Client instance and running it.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}

