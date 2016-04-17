[![Project BridgeMPP](https://github.com/Vinpasso/BridgeMPP/raw/master/src/main/resources/BridgeMPP-Logo-No-Background.png )](https://github.com/Vinpasso/BridgeMPP/)

Project Status
=========
BridgeMPP: 

[![Build Status](https://travis-ci.org/Vinpasso/BridgeMPP.svg?branch=master)](https://travis-ci.org/Vinpasso/BridgeMPP)

BridgeMPP-Bots: 

[![Build Status](https://travis-ci.org/Vinpasso/BridgeMPP-Bot.svg?branch=master)](https://travis-ci.org/Vinpasso/BridgeMPP-Bot)


About BridgeMPP:
=========
BridgeMPP is an instant messaging bridge enabling users to send messages across the walls of each single instant messenger.

Look below for a list of currently supported Protocols.

Building
=========
This project is a Maven project. Use `mvn install` in the project's root directory to build BridgeMPP. This requires a JDK and Apache Maven. A fully functioning Jar will be created in the target/ directory with the name `BridgeMPP-Maven-<version>-[SNAPSHOT]-jar-with-dependencies.jar`.

Launching
=========
On first launch BridgeMPP will create an embedded Apache Derby database, along with some additional configuration all stored in the current directory.

Use `java -jar BridgeMPP-Maven-<version>-[SNAPSHOT]-jar-with-dependencies.jar` to launch BridgeMPP. Optionally, a stop time can be passed in milliseconds using the -stopTime argument. This is useful in cooperation with backup scripts that can back-up the database in a specified period.

First Run
========
One the first run BridgeMPP will setup the environment. Once this has finished a log message with the first-run access key will appear. Type `!usekey [key]` to use this key to gain full configuration rights. This key may only be used once, but further keys can now be generated. A message confirming the rights granted should appear shortly.

You now should have full access rights to the BridgeMPP server. If this is not the case shutdown BridgeMPP, delete the Database in the bridgedb/ directory, restart BridgeMPP and wait for a new key to be generated.

Essentials
========
BridgeMPP message routing is based on four entities.

The user table represents individual users of BridgeMPP, storing an identifier such as email, phone number, account name, along with an optional alias as well as the user's permissions.
Users will typically communicate with BridgeMPP in two ways. Either by chatting with the BridgeMPP account directly, or by chatting in a channel/group/multi-user-chat in which the BridgeMPP account participates in. This is referred to as the Endpoint, an identifier to the Chat/Group where BridgeMPP will be sending messages to.
A Service is the container of multiple endpoints that takes care of sending and receiving messages. Typical services are XMPP, WhatsApp, Telegram or the Network.
A Group is a BridgeMPP internal message routing construct. Endpoints may be added to a group in order to automatically exchange messages between all the endpoints in a group.

Setup Message Routing
========
All BridgeMPP commands can be issued from an endpoint with the necessary rights. See Section Keys on how to generate additional access keys

To setup message exchange, a group needs to be created where endpoints can exchange the messages. Use `!creategroup "name"` to create a group with an arbitrary name.

Now that a group has been created endpoints need to subscribe to this group to send and receive messages. The simplest way to achieve this is to use the command `!subscribegroup "name"` to add the current endpoint over which the command has been sent to the specified group. As soon as multiple endpoints have been subscribed to a group, BridgeMPP will start distributing messages between the endpoints. The inverse command is `!unsubscribegroup "name"`

At this point messages can already be exchanged, however exchanging messages isn't useful if they don't cross the Instant Messenger's border. To add new services to BridgeMPP the service has to be loaded. Different services require different conditions, check below on the how-to to loading individual services.

Help!
========
A list of all BridgeMPP commands can be obtained by typing `!help`.
Command syntax can be obtained by typing `!command !<commandname>`.
All other problems and feature-requests can hopefully be solved by opening a GitHub issue. 

Develop
========
Be sure to fork BridgeMPP to improve it, don't be afraid to send a pull request my way.

License
========
Gnu GPL v3: http://www.gnu.org/licenses/gpl-3.0.html

Services
========

## Console

Messages can be sent straight from and to the Server Console (Text only). Any non-command message will be routed to their destinations.
This service is activated by default. Loading this service should not be necessary, however `!loadconsoleservice` can be used to manually load this service.

## E-Mail

Provided an email account (imap/smtp), BridgeMPP can send and receive HTML/Text Emails.
Can be loaded by typing `!loadmailservice <imap-host> <imap-port> <imap-smtp-username> <imap-smtp-password> <smtp-host> <smtp-port>`. The outgoing smtp username and password should be identical to the incomming imap username and password. BridgeMPP will move received messages from the inbox into the BridgeMPP-Processed folder.

## Skype

Requires a Desktop Skype client running, BridgeMPP will send and receive Text Messages over the Skype Desktop API. Since Microsoft no longer supports the Skype API this is unstable at best. Messages are known to be received in duplicates.
Can be loaded by typing `!loadskypeservice`. This will attempt to connect to the locally running Skype desktop client (a Popup will appear to ask for permission). Skype needs to be running for BridgeMPP to send and receive messages.

## Asynchronous Network Sockets

BridgeMPP can send and receive all supported message types over asynchronous network sockets. Uses the Google ProtoBuf included in the resources folder. The official BridgeMPP Bots use this interface to communicate with BridgeMPP.
Asynchronous sockets scale better than their synchronous counterparts to more clients.
Can be loaded with `!loadasyncsocketservice <listenAddress> <port> <numberOfServerThreads> <numberOfClientThreads>`. This listen address refers to the local network interface that should be used to listen for connections. Use 0.0.0.0 to listen on all interfaces. The Port refers to the port used to listen for incomming connections. The Number of Server Threads refers to the number of threads used to accept incomming connections and build client threads. This should be set to a value between (1-3). The Number of Client Threads refers to the number of threads used to communicate with the network. 3-10 threads should suffice.

## Synchronous Network Sockets

BridgeMPP can send and receive messages in 3 different styles with synchronous sockets. Mode 0 uses plain text, interpreting every newline as a seperate message. Mode 1 uses XML parsing with the <message></message> tags to support multi-line messages. Mode 3 uses the same Google ProtoBuf as described in Asynchronous Network Sockets.

## Facebook

Sending and receiving messages from Facebook users and groups is still work in progress.

## League of Legends
BridgeMPP uses the League of Legends XMPP Server to send and receive text messages from the League of Legends Chat Service. BridgeMPP can also monitor game statuses, such as main menu, hosting game or in game, which can be found in the XMPP_Presence endpoint.

## Teamspeak

BridgeMPP can use the Teamspeak Server Query API to communicate text messages to clients in the teamspeak channel. Currently only the default channel is supported.

## Telegram

BridgeMPP can use the Telegram Bot API to send and receive messages from Telegram groups and chats. Currently only text messages are supported.

## Whatsapp

BridgeMPP uses Yowsup to communicate with WhatsApp. Acquire a fully working Yowsup installation before attempting to launch the BridgeMPP WhatsApp service. Text messages can be sent/received. Media messages can be received while they are unencrypted.

## XMPP

XMPP is BridgeMPP most supported protocol. Text and media can be sent and received.
