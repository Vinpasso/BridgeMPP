/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class ConsoleService implements BridgeService {

    Scanner scanner;
    ConsoleReader reader;
    Thread consoleThread;
    private ArrayList<Endpoint> endpoints;

    @Override
    public void connect(String args) {
        ShadowManager.log(Level.INFO, "Console Service is being loaded...");
        endpoints = new ArrayList<>();
        scanner = new Scanner(System.in);
        reader = new ConsoleReader();
        consoleThread = new Thread(reader, "Console Reader");
        consoleThread.start();
        ShadowManager.log(Level.INFO, "Console Service has been loaded...");
    }

    @Override
    public void disconnect() {
        ShadowManager.log(Level.WARNING, "Console service has been disconnected...");
        scanner.close();
    }

    @Override
    public void returnToSender(String target, String response) {
        sendMessage(target, response);
    }

    @Override
    public void sendMessage(String target, String response) {
        System.out.println(response);
    }

    @Override
    public String getName() {
        return "Console";
    }

    //Only one Endpoint. Adding a second does nothing
    @Override
    public void addEndpoint(Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    class ConsoleReader implements Runnable {

        public ConsoleReader() {
            if (endpoints.isEmpty()) {
                endpoints.add(new Endpoint(ConsoleService.this, "Server"));
            }
        }

        @Override
        public void run() {
            ShadowManager.log(Level.FINE, "Console reader is running...");
            while (scanner.hasNext()) {
                CommandInterpreter.processMessage(scanner.nextLine(), endpoints.get(0));
            }
            ShadowManager.log(Level.FINE, "Console reader has closed");
        }

    }
}
