package ServerClient;

import Game.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents the game server, handling client connections, managing the game state,
 * and facilitating communication between players.
 */
public class Server implements Runnable {

    /** Map of all connected clients, where the key is the player's nickname and the value is the connection handler. */
    public ConcurrentHashMap<String, ConnectionHandler> connectionsMap = new ConcurrentHashMap<>();
    private ServerSocket serverSocket; //server socket listen for incoming connections
    private boolean done;
    private ExecutorService threadPool;
    public final Set<String> nicknamesSet = Collections.synchronizedSet(new HashSet<>());
    public Game game;
    public boolean isGameCreated = false;
    public boolean isGameRunning = false;
    private int clientCounter = 0;
    public int playerCounter = 0;
    public int dateCounter = 0;
    public int birthdayCounter = 0;
    public Map<String, Player> playerMap = Collections.synchronizedMap(new HashMap<>());
    public String guardName;

    /** Initializes a new Server instance. */
    public Server(){
        done = false;
    }

    /**
     * Runs the server, accepting client connections, and managing the client handlers.
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8080);
            threadPool = Executors.newCachedThreadPool();
            while(!done){
                System.out.println("client counter " + clientCounter);
                Socket client = serverSocket.accept();//socket object for communicating with this client
                clientCounter++;
                if(clientCounter > 4 || isGameRunning){
                    PrintWriter tempOut = new PrintWriter(client.getOutputStream(), true);
                    tempOut.println("Server full or a game is currently running. Please try again" +
                                    " later.");
                    tempOut.close();
                    client.close();
                    clientCounter--;
                }else{
                    ConnectionHandler handler = new ConnectionHandler(client, this);
                    threadPool.execute(handler);
                }
            }
        } catch (Exception e) {
            shutdownServer();
        }

    }

    /**
     * Shuts down the server, closing the socket and shutting down the thread pool.
     */
    public void shutdownServer(){
        try{
            done = true;
            threadPool.shutdown();
            if(!serverSocket.isClosed()){
                serverSocket.close(); //close serverSocket
            }
            //for(ConnectionHandler ch : connectionsMap){
            //ch.shutdownClient();
            //}
        } catch (IOException e) {}
        //cannot handle/ ignore it
    }

    /**
     * Checks if a nickname is available and not already taken by another player.
     *
     * @param nickname the nickname to check
     * @return true if the nickname is available, false otherwise
     */
    public boolean isNicknameAvailable(String nickname) {
        return nicknamesSet.add(nickname);
    }

    /**
     * Removes a nickname from the set of active nicknames.
     *
     * @param nickname the nickname to remove
     */
    public void removeNickname(String nickname) {
        nicknamesSet.remove(nickname);
    }

    /**
     * Handles individual client connections, managing messages and game actions.
     */
    public class ConnectionHandler implements Runnable {

        private Socket client;
        //get stream from socket/client
        private BufferedReader in;
        //write to client
        private PrintWriter out;
        public String nickname;
        public Server server;
        public Player player;
        public boolean hasJoinedGame = false;

        /**
         * Constructs a ConnectionHandler for a specific client.
         *
         * @param client the client socket
         * @param server the server instance
         */
        public ConnectionHandler(Socket client, Server server) {
            this.client = client;
            this.server = server;
        }

        /**
         * Sends a message to all connected clients.
         *
         * @param message the message to broadcast
         */
        public void broadcastToAll(String message) {
            connectionsMap.values().forEach(ch -> ch.sendMessage(message));
        }

        /**
         * Sends a message to all connected clients except the sender.
         *
         * @param message the message to broadcast
         * @param senderNickname the nickname of the sender to exclude
         */
        public void broadcastExceptSelf(String message, String senderNickname) {
            connectionsMap.forEach( (nickname, handler) -> {
                if (!nickname.equals(senderNickname)) {
                    handler.sendMessage(message);
                }
            });
        }

        /**
         * Extracts the username and message content from a private message and sends it to the specified user.
         *
         * @param message the full message string
         * @param senderNickname the nickname of the sender
         */
        public void extractUsernameAndMessage(String message, String senderNickname){
            try{
                String userPart = message.split(" ",3)[1];
                String msgPart = message.split(" ",3)[2];
                broadcastToSpecificUser(userPart, msgPart, senderNickname);
            } catch (ArrayIndexOutOfBoundsException e){
                out.println("wrong msg type");
            }
        }

        /**
         * Sends a private message to a specific user.
         *
         * @param user the target username
         * @param message the message content
         * @param senderNickname the nickname of the sender
         */
        public void broadcastToSpecificUser(String user, String message, String senderNickname){
            ConnectionHandler userHandler = connectionsMap.get(user);
            if(userHandler != null){
                sendMessage("sent private Message to: " + user + " : " + message);
                userHandler.sendMessage("received private Message from " + senderNickname + ": " + message);
            } else {
                sendMessage("User '" + user + "' not found.");
            }
        }

        @Override
        public void run() {
            try{
                out = new PrintWriter(client.getOutputStream(), true);//toClientWriter
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));//fromClientReader

                //send to client
                out.println("Please enter a name: ");//sends msg to client

                //get message from client
                nickname = in.readLine();
                while (nickname == null || nickname.isEmpty() || !server.isNicknameAvailable(nickname)) {
                    out.println("Name already taken or invalid, choose another one:");
                    nickname = in.readLine();
                }
                connectionsMap.put(nickname, this);
                System.out.println("connectionMap" + connectionsMap);
                for (var entry : connectionsMap.entrySet()) {
                    System.out.println(entry.getKey() + "/" + entry.getValue());
                }
                out.println("Welcome " + nickname); //to joiner
                out.println("Use /help for a list of commands");
                out.println("Use /create to create a new game");
                if(isGameCreated){
                    out.println("A game has been created. Do you want to join? Type /join or /noJoin");
                }
                System.out.println(nickname + " connected");//message for the server
                System.out.println("-----");
                System.out.println(connectionsMap);
                System.out.println(connectionsMap.get(nickname));
                //send to everyone except new joiner
                broadcastExceptSelf(nickname + " joined the server", nickname);

                //for this particular client, loop and wait for messages/nachrichtenempfangen
                String message;
                while( (message = in.readLine()) != null){
                    String[] messageParts = message.split(" ", 2);
                    String command = messageParts[0];
                    String parameter = messageParts.length > 1 ? messageParts[1] : "";

                    switch(command){
                        case "/help":
                            commandHelp();
                            break;
                        case "/private":
                            commandPrivate(message);
                            break;
                        case "/bye":
                            commandBye();
                            break;
                        case "/create":
                            commandCreate();
                            break;
                        case "/join" :
                            commandJoin();
                            break;
                        case "/noJoin" :
                            commandNoJoin();
                            break;
                        case "/start" :
                            commandStart();
                            break;
                        case "/date" :
                            commandDate(message);
                            break;
                        case "/cardsInfo" :
                            commandCardsInfo();
                            break;
                        case "/bd" :
                            commandBirthday(message);
                            break;
                        case "/players" :
                            commandPlayers();
                            break;
                        case "/play" :
                            commandPlay(message);
                            break;
                        case "/target" :
                            commandTarget(message);
                            break;
                        case "/number" :
                            commandNumber(message);
                            break;
                        case "/3cards" :
                            command3Cards();
                            break;
                        case "/score" :
                            commandScore();
                            break;
                        default:
                            if (command.startsWith("/")) {
                                sendMessage("Command not found");
                            } else {
                                broadcastToAll(nickname + ": " + message);
                            }
                    }
                }
            } catch (IOException e){
                shutdownClient();
            }
        }

        public synchronized void startGame(){
            server.game = new Game(server);
            broadcastToAll("The game has started with " + playerMap.size() + " players");
            broadcastToAll("-----------------------------------------------------------");
            Thread gameThread = new Thread(server.game);
            gameThread.start();
        }

        /**
         * Sends a message to the client connected to this handler.
         *
         * @param message the message to send
         */
        public void sendMessage(String message) {
            out.println(message);
            out.flush();
        }
        /**
         * Provides a list of available commands to the client.
         */
        public void commandHelp(){
            out.println("/help - List of commands\n"
                        + "/private - Use '/private <playerName> <message>' to send a private message\n"
                        + "/create - Create a game\n"
                        + "/players - List of players\n"
                        + "/score - Shows the score\n"
                        + "/cardsInfo - short Information for the cards\n"
                        + "/3cards  - If available, shows you the 3 discarded cards\n"
                        + "/bye - Leave the server"); //ToDo
        }

        /**
         * Handles the /private command to send a private message.
         *
         * @param message the message content, including recipient and message
         */
        public void commandPrivate(String message){
            extractUsernameAndMessage(message, this.nickname);
        }

        /**
         * Handles the /create command to create a new game.
         */
        public void commandCreate(){
            // TODO
            if(!isGameCreated){
                sendMessage("You created a game");
                sendMessage("you joined the game");
                hasJoinedGame = true;
                this.player = new Player(this.nickname, this);
                player.isPlaying = true;
                server.playerMap.put(nickname, this.player);
                broadcastExceptSelf(nickname + " created a game. Do u want to join?\n"
                                    + "Type /join or /noJoin", nickname);
                isGameCreated = true;
                playerCounter++;
                //TODO
            }else{
                sendMessage("A game has already been created. Type /join to join " +
                            "it");
            }
        }

        /**
         * Handles the /join command to join an existing game.
         */
        public void commandJoin(){
            if(!isGameRunning && hasJoinedGame == false){
                broadcastToAll(nickname + " joined the game");
                hasJoinedGame = true;
                this.player = new Player(this.nickname, this);
                server.playerMap.put(nickname, this.player);
                playerCounter++;
                broadcastToAll(playerCounter + "/4" + " players joined the game");
                broadcastToAll("To start the game type /start");
                //Todo
            }else if(hasJoinedGame){
                sendMessage("You have already joined the game.");
            }else{
                sendMessage("Cannot join, game is in process.");
                commandNoJoin();
            }
        }

        /**
         * Handles the /noJoin command to leave the server without joining the game.
         */
        public void commandNoJoin(){
            // TODO
            broadcastExceptSelf(nickname + " left the server", nickname);
            out.println("You left the server ");
            System.out.println(nickname + " left the room");//message for the server
            server.removeNickname(nickname);
            server.connectionsMap.remove(nickname);
            shutdownClient();
            server.clientCounter--;
            System.out.println(server.clientCounter);
        }

        /**
         * Processes the /date command, allowing the player to input the number of days since their last date.
         *
         * @param message the command input from the player, expected to contain the number of days
         */
        public void commandDate(String message){
            // Split the input to extract the number of days
            String[] parts = message.split(" ");
            if (parts.length == 2) {
                try {
                    int days = Integer.parseInt(parts[1]);  // Extract the number of days since the last date
                    System.out.println("days: " + days);
                    //Todo
                    player.lastDate = days;
                    dateCounter++;
                    sendMessage("Thank you for your response.");
                    //TODO
                    //System.out.println(player.lastDate);
                    System.out.println("---");
                    sendMessage("Please type in your birthday. In this format: /bd yyyy/mm/dd");

                } catch (NumberFormatException e) {
                    sendMessage("Invalid format. Please use the format '/date <number of days>'. For example, '/date 60'.");
                }
            } else {
                sendMessage("Invalid command. Please use the format '/date <number of days>'.");
            }
        }

        /**
         * Processes the /bd (birthday) command, allowing the player to input their birthdate.
         *
         * @param message the command input from the player, expected to contain the birthday in the format yyyy/MM/dd
         */
        public void commandBirthday(String message){
            String[] parts = message.split(" ");
            if (parts.length == 2) {
                try {
                    String birthday = parts[1];
                    System.out.println("birthday: " + birthday);
                    String datePattern = "\\d{4}/\\d{2}/\\d{2}";
                    if (!birthday.matches(datePattern)) {
                        sendMessage("Invalid date format. Please use the format '/birthday <yyyy/MM/dd>'. For " +
                                    "example, '/birthday 1999/04/23'.");
                        return;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                    Date date = sdf.parse(birthday);
                    player.age = date.getTime();
                    System.out.println(player.age);
                    birthdayCounter++;
                    sendMessage("Thank you for your response.");
                    System.out.println("---");
                    if(dateCounter == playerCounter && birthdayCounter == playerCounter){
                        broadcastToAll("The game is starting now");
                        startGame();
                    }
                } catch (NumberFormatException e) {
                    sendMessage("Invalid format. Please use the format '/date <number of days>'. For example, '/date 60'.");
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                sendMessage("Invalid command. Please use the format '/date <number of days>'.");
            }
        }
        /**
         * Processes the /bye command, allowing the player to leave the server.
         */
        public void commandBye(){
            if(server.isGameRunning){
                out.println("You cannot leave while the game is being played");
                return;
            }
            broadcastExceptSelf(nickname + " left the room", nickname);
            out.println("You left the server ");
            System.out.println(nickname + " left the room");//message for the server
            server.removeNickname(nickname);
            shutdownClient();
            server.clientCounter--;
            System.out.println("clientcounter" + server.clientCounter);
        }

        /**
         * Processes the /start command, initiating the game if enough players have joined.
         */
        public void commandStart(){
            if(!hasJoinedGame){
                sendMessage("You must join the game before you can start it.");
                return;
            }
            if(!server.isGameCreated){
                sendMessage("Game has not been created.");
                return;
            }
            if(!isGameRunning) {
                if(playerCounter > 1){
                    broadcastToAll("Game is starting soon.");
                    isGameRunning = true;
                    server.connectionsMap.forEach( (key, val) -> {
                        if(val.hasJoinedGame == false){
                            val.commandNoJoin();
                        }
                    });
                    System.out.println("server player Mapppo");
                    System.out.println(server.playerMap);
                    broadcastToAll("The game will start once all players have entered the time of " +
                                   "their last date and their birthday.");
                    broadcastToAll("Please enter the number of days since your last date in this format: /date 60 " +
                                   "(for example, /date 60 means your last date was 60 days ago).");
                }else{
                    broadcastToAll("Game cannot start. Not enough players");
                }
            }else{
                sendMessage("Game is already running");
            }
        }

        /**
         * Processes the /cardsInfo command, sends information about the different card types and their effects
         * to the player.
         */
        public void commandCardsInfo(){
            out.println("8-Princess(1): Lose if discarded.\n"
                        + "7-Countess(1): Must be played if you have King or Prince in hand.\n"
                        + "6-King(1): Trade hands with another player.\n"
                        + "5-Prince(2): Choose a player. They discard their hand and draw a new card.\n"
                        + "4-Handmaid(2): You cannot be chosen until your next turn.\n"
                        + "3-Baron(2): Compare hands with another player, lower number is out.\n"
                        + "2-Priest(2): Look at a player's hand.\n"
                        + "1-Guard(5): Guess a player's hand; if correct the player is out.");
        }

        /**
         * Processes the /number command, allowing the player to choose a number for the Guard card's effect.
         *
         * @param message the command input from the player, expected to contain a number between 2 and 8
         */
        public void commandNumber(String message){
            String[] parts = message.split(" ");
            if (parts.length < 2) {
                sendMessage("Please choose a number between 2 - 8. Usage: '/number number'. For example /number 3 ");
                return;
            }
            String numberString = parts[1];

            int number = Integer.parseInt(numberString);
            System.out.println(number);
            if(number < 2 || number > 8){
                sendMessage("Please choose a number between 2 - 8. Usage: '/number <number>'. For example /number 7 ");
                return;
            }
            player.playCardGuard(number, playerMap.get(guardName));
            server.game.playerFinishedTurn();
        }

        /**
         * Processes the /target command, allowing the player to select a target for their current card action.
         *
         * @param message the command input from the player, expected to contain a target player's name
         */
        public void commandTarget(String message){
            if(!player.isMyTurn){
                sendMessage("Not your turn to target");
                return;
            }
            String[] parts = message.split(" ");
            if (parts.length < 2) {
                sendMessage("Please specify a target name to attack. Use: /target <TargetName>");
                return;
            }
            String targetName = parts[1];

            if(!playerMap.containsKey(targetName)){
                sendMessage("Player not found. Please try again.");
                return;
            }
            Player pl = playerMap.get(targetName);
            if(pl.isImmune){
                sendMessage("Target is immune. Choose another target.");
                return;
            }
            if(game.knockedOutPlayersSet.contains(pl)){
                sendMessage("Target is already knocked out. Choose another one. Usage: /target <TargetName");
                return;
            }
            switch (player.cardToPlay){
                case "King":
                    player.playCardKing(playerMap.get(targetName));
                    server.game.playerFinishedTurn();
                    break;
                case "Prince":
                    player.playCardPrince(playerMap.get(targetName), game.deck);
                    server.game.playerFinishedTurn();
                    break;
                case "Baron":
                    player.playCardBaron(playerMap.get(targetName));
                    server.game.playerFinishedTurn();
                    break;
                case "Priest":
                    player.playCardPriest(playerMap.get(targetName));
                    server.game.playerFinishedTurn();
                    break;
                case "Guard":
                    guardName = targetName;
                    sendMessage("Number from 2-8 is required. Type /number <number> \n"
                                + "For example /number 8");
                    break;
            }
        }

        /**
         * Processes the /play command, allowing the player to play a specified card from their hand.
         *
         * @param message the command input from the player, expected to contain the card name
         */
        public void commandPlay(String message){
            if(!player.isMyTurn){
                sendMessage("Not your turn to play");
                return;
            }
            if (player.isKnockedOutOfRound) {
                sendMessage("You have been knocked out of the round and cannot play.");
                return;
            }
            String[] parts = message.split(" ");
            if (parts.length < 2) {
                sendMessage("Please specify a card name to play. Usage: /play <CardName>");
                return;
            }
            String cardName = parts[1];
            player.cardToPlay = cardName;
            System.out.println(cardName + " = cardName");

            if(!cardName.equals(player.hand.get(0).getName()) && !cardName.equals(player.hand.get(1).getName())){
                System.out.println(player.hand.get(0).getName());
                System.out.println(player.hand.get(1).getName());
                System.out.println(player.hand.get(0).getName().equals(cardName));
                sendMessage("Wrong card name. Please try again in this format: /play <Cardname>. for example /play " +
                            "Princess");
                return;
            }
            switch(cardName){
                case "Princess":
                    player.playCardPrincess();
                    server.game.playerFinishedTurn();
                    return;
                case "Countess":
                    player.playCardCountess();
                    server.game.playerFinishedTurn();
                    return;
                case "Handmaid":
                    player.playCardHandmaid();
                    server.game.playerFinishedTurn();
                    return;

            }

            boolean onlyNotImmune =
                    game.turnOrderMap.size() - game.knockedOutPlayersSet.size() - game.immunePlayersList.size() == 1;
            System.out.println("onlyImmue " + onlyNotImmune);
            System.out.println(game.turnOrderMap.size() - game.knockedOutPlayersSet.size() - game.immunePlayersList.size());
            if(onlyNotImmune){
                if(player.cardToPlay.equals("Prince")){
                    sendMessage("You are the only player who is not immune. You automatically target yourself!");
                    player.playCardPrince(playerMap.get(player.nickname), game.deck);
                    server.game.playerFinishedTurn();
                    return;
                }else{
                    broadcastToAll(player.nickname + " is the only player who is not immune. The card is discarded " +
                                   "without an effect");
                    player.playCardAllImmune(player.cardToPlay);
                    server.game.playerFinishedTurn();
                    return;
                }
            }
            switch(cardName){
                case "King", "Prince", "Baron", "Priest", "Guard":
                    sendMessage("Target is required. Type /target \n"
                                + "For example /target Peter");
                    break;
            }

        }

        /**
         * Processes the /3cards command, revealing the three discarded cards if only two players are in the game.
         */
        public void command3Cards(){
            if(playerMap.size() > 2){
                sendMessage("there are no revealed 3 cards because of the player size");
                return;
            }
            sendMessage(game.deck.showThreeRemovedCards());

        }

        /**
         * Processes the /players command, providing a list of all players currently in the game.
         */
        public void commandPlayers(){
            StringBuilder playerList = new StringBuilder(5);
            server.playerMap.forEach( (key,val) -> {
                System.out.println(key);
                playerList.append(key).append(" : ");
            });
            sendMessage("The players are: " + playerList.toString());
        }

        /**
         * Processes the /score command, providing the current score for each player in the game.
         */
        public void commandScore(){
            server.game.tokenInfo();
        }

        /**
         * Shuts down the client connection and releases resources.
         */
        public void shutdownClient(){
            try{
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            } catch (IOException e) {
                //or ignore
                e.printStackTrace();
            }

        }
    }

    /**
     * The main method to start the server.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}



