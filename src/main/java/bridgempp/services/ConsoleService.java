/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;

import bridgempp.BridgeService;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;

/**
 *
 * @author Vinpasso
 */
public class ConsoleService implements BridgeService {

	Scanner scanner;
	ConsoleReader reader;
	Thread consoleThread;
	private ArrayList<Endpoint> endpoints;

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.PLAIN_TEXT };

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
	public void sendMessage(Message message) {
		System.out.println(message.toComplexString(getSupportedMessageFormats()));
	}

	@Override
	public String getName() {
		return "Console";
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	// Only one Endpoint. Adding a second does nothing
	@Override
	public void addEndpoint(Endpoint endpoint) {
		endpoints.add(endpoint);
	}

	class ConsoleReader implements Runnable {

		@Override
		public void run() {
			ShadowManager.log(Level.FINE, "Console reader is running...");
			try {
				while (true) {
					if (System.in.available() > 0) {
						if (endpoints.size() == 0) {
							endpoints.add(new Endpoint(ConsoleService.this, "Server"));
						}
						Message message = new Message(endpoints.get(0), scanner.nextLine(),
								getSupportedMessageFormats()[0]);
						CommandInterpreter.processMessage(message);
					} else {
						Thread.sleep(1000);
					}
				}
			} catch (IllegalStateException e) {
				ShadowManager.log(Level.WARNING, "System_IN was closed");
			} catch (InterruptedException e) {
				ShadowManager.log(Level.WARNING, "Shutting down Console Reader due to interrupt");
			} catch (IOException e) {
				ShadowManager.log(Level.WARNING, "System_IN was closed");
			}
			ShadowManager.log(Level.FINE, "Console reader has closed");
		}

	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}

	@Override
	public void interpretCommand(Message message) {
		message.getSender().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
	}
}
