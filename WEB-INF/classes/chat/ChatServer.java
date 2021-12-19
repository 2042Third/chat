package chat;

import java.io.IOException;
import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

// import org.apache.juli.logging.Log;
// import org.apache.juli.logging.LogFactory;
import java.util.*;
import javaserver.chat.*;
import util.JSONParse;
import util.HTMLFilter;

@ServerEndpoint(value = "/chat")
public class ChatServer {

    // private static final Log log = LogFactory.getLog(ChatServer.class);
    private static final String SAVE_DIR = "marshes";
    // private static final String GUEST_PREFIX = "user_";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<ChatServer> connections =
            new CopyOnWriteArraySet<>();

    public static String nickname;
    public static String nickID;
    private Session session;
    private JSONParse a_parse = new JSONParse();



    public ChatServer() {
        Integer i = connectionIds.getAndIncrement();
    }


    private void read_incoming (String a){
        File fileSaveDir = new File("/usr/local/web_notes_dir/chat");
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }

        Map<String, Object> msg = a_parse.parse(HTMLFilter.filter(a));
        System.out.println("[chat server] new object from msg : "+ msg);
        switch((String) msg.get("type")){
            case "register":
                System.out.println("[chat server] registration for "+msg.get("sender")+".");
                this.nickname = (String)msg.get("sender");
                this.nickID = this.nickname+this.connectionIds.get();
                String regi_ack = a_parse.json_request("regi_ack","serv", this.nickname, this.nickID,"","");
                try {
                    this.session.getBasicRemote().sendText(regi_ack);
                }
                catch (IOException e){
                    System.out.println("[chat server] registration send-back failure");
                }
                break;
            
            default:
                System.out.println("[chat server] Unkown type: "+msg.get("type"));
        }

    }

    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);

        // String message = String.format("%s", nickname, "has joined.");
        // broadcast(message);
    }


    @OnClose
    public void end() {
        connections.remove(this);
        // String message = String.format("* %s %s",
        //         nickname, "has disconnected.");
        // broadcast(message);
    }


    @OnMessage
    public void incoming(String message) {
        // Never trust the client
        String filteredMessage = String.format("%s",
                nickname, HTMLFilter.filter(message.toString()));
        read_incoming(filteredMessage);
        // broadcast(filteredMessage);
    }




    @OnError
    public void onError(Throwable t) throws Throwable {
        System.out.println("[chat server] ERROR: "+ t.toString());
        // log.error("Chat Error: " + t.toString(), t);
    }


    private static void broadcast(String msg) {
        for (ChatServer client : connections) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {
                // log.debug("Chat Error: Failed to send message to client", e);
                connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    // Ignore
                }
                String message = String.format("* %s %s",
                        client.nickname, "has been disconnected.");
                broadcast(message);
            }
        }
    }
    private static void send_to(String userid, String msg) {
        for (ChatServer client : connections) {
            if (userid.equals(client.nickID)){
                try {
                    synchronized (client) {
                        client.session.getBasicRemote().sendText(msg);
                    }
                } catch (IOException e) {
                    // log.debug("Chat Error: Failed to send message to client", e);
                    connections.remove(client);
                    try {
                        client.session.close();
                    } catch (IOException e1) {
                        // Ignore
                    }
                    String message = String.format("* %s %s",
                            client.nickname, "has been disconnected.");
                    broadcast(message);
                }
                return ;
            }

        }
    }
}