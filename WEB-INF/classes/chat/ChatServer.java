package chat;

import java.io.IOException;
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
import util.JSONParse;
import util.HTMLFilter;

@ServerEndpoint(value = "/chat")
public class ChatServer {

    // private static final Log log = LogFactory.getLog(ChatServer.class);

    private static final String GUEST_PREFIX = "user_";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<ChatServer> connections =
            new CopyOnWriteArraySet<>();

    private final String nickname;
    private Session session;

    public ChatServer() {
        nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
    }


    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        String message = String.format("* %s %s", nickname, "has joined.");
        broadcast(message);
    }


    @OnClose
    public void end() {
        connections.remove(this);
        String message = String.format("* %s %s",
                nickname, "has disconnected.");
        broadcast(message);
    }


    @OnMessage
    public void incoming(String message) {
        // Never trust the client
        JSONParse a = new JSONParse();
        String filteredMessage = String.format("%s: %s",
                nickname, HTMLFilter.filter(message.toString()));
        Map<String, Object> new_msg = a.parse(HTMLFilter.filter(message.toString()));
        System.out.println("[chat server] new object from msg : "+ new_msg);
        broadcast(filteredMessage);
    }




    @OnError
    public void onError(Throwable t) throws Throwable {
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
}