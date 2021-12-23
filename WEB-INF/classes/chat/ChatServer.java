package chat;

import java.io.IOException;
import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

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

    private static HashMap<String, String> users = new HashMap<>();
    public static String nickname;
    public static String nickID;
    private Session session;
    private JSONParse a_parse = new JSONParse();



    public ChatServer() {
        Integer i = connectionIds.getAndIncrement();
    }

    private void msg_resolve (Map<String, Object> msg){
        String msg_str = a_parse
            .json_request("msg", this.nickname, (String)msg.get("receiver"),
                (String)msg.get("p2phash"), (String)msg.get("msghash"),(String)msg.get("msg"), "");

        int sent_ = send_to(users.get((String)msg.get("receiver")), msg_str);
        msg_str = a_parse
            .json_request("msg", (String)msg.get("sender"), (String)msg.get("receiver"),
                (String)msg.get("p2phash"), (String)msg.get("msghash"),(String)msg.get("msg"), ""+sent_);
        try {
            this.session.getBasicRemote().sendText(msg_str);
        }
        catch (IOException e){
            System.out.println("[chat server] registration send-back failure");
        }
    }

    private void read_incoming (String a){
        File fileSaveDir = new File("/usr/local/web_notes_dir/chat");
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }

        Map<String, Object> msg = a_parse.parse(HTMLFilter.filter(a));
        // System.out.println("[chat server] new object from msg : "+ msg);
        // System.out.println("[chat server] new string from msg : "+ a);
        switch((String) msg.get("type")){
            case "register":
                System.out.println("[chat server] registration for "+msg.get("sender")+".");
                nickname = (String)msg.get("sender");
                users.put(nickname,session.getId() );
                this.nickID = this.nickname+this.connectionIds.get();
                String regi_ack = a_parse.json_request("regi_ack","serv", this.nickname, this.nickID,"","");
                try {
                    this.session.getBasicRemote().sendText(regi_ack);
                }
                catch (IOException e){
                    System.out.println("[chat server] registration send-back failure");
                }
                break;
            case "msg":
                System.out.println("[chat server] new message "+msg.get("sender"));
                this.msg_resolve(msg);
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
        // String filteredMessage = String.format("%s",
        //         nickname, HTMLFilter.filter(message.toString()));
        read_incoming(message.toString());
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
    private static int send_to(String userid, String msg) {
        int sent_=0;
        for (ChatServer client : connections) {
            System.out.println("\t[chat server] compare:\n\t"+userid+"\n\t"+client.session.getId()+"\n");
            if (true){
                try {
                    synchronized (client) {
                        if (userid.equals(client.session.getId()) ){
                            client.session.getBasicRemote().sendText(msg);
                            sent_++;
                            return sent_;
                        }
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
                // return sent_;
            }

        }
        return 0;
    }
}