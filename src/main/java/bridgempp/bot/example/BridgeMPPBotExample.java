package bridgempp.bot.example;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vincent Bode
 */
public class BridgeMPPBotExample {
    static Socket socket;
    static Scanner bufferedReader;
    static PrintStream printStream;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        while (true) {
            try {
                socket = new Socket("vinpasso.org", 1234);
                printStream = new PrintStream(socket.getOutputStream(), true);
                bufferedReader = new Scanner(socket.getInputStream());
                botInitialize();
            } catch (IOException ex) {
                Logger.getLogger(BridgeMPPBotExample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static Message parseMessage(BufferedReader reader) throws IOException
    {
        return new Message(reader.readLine(), reader.readLine());
    }
    
    public static void printMessage(PrintStream printStream, Message message)
    {
        printStream.println(message.target);
        printStream.println(message.message);
    }
    
    public static void printCommand(String command)
    {
        printStream.println(command);
    }

    private static void botInitialize() {
        printCommand("!usekey <bridgempp-bot-key>");
        printCommand("!subscribegroup <bridgempp-bot-group>");
    }
    
    public interface Bot
    {
        public Message messageRecieved(Message message);
    }

    public static class Message {
        public String target;
        public String message;
        public Message(String sender, String message) {
            this.target = sender;
            this.message = message;
        }
    }

}
