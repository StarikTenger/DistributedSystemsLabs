package chat;

import java.sql.Timestamp;
import java.util.Date;

public class Message {
    String sender; // TODO: Client from ClientList
    String text;
    Date date;

    public Message(String s, String t) {
        sender = s;
        text = t;
        date = new Date();
    }
}
