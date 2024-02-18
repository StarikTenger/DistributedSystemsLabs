package chat;

import java.sql.Timestamp;
import java.util.Date;

public class Message {
    Integer sender; // TODO: Client from ClientList
    String text;
    Date date;

    public Message(Integer s, String t) {
        sender = s;
        text = t;
        date = new Date();
    }
}
