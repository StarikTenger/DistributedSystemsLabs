package chat;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Serializable  {
    Integer sender; // TODO: Client from ClientList
    String text;
    Date date;

    public Message(Integer s, String t) {
        sender = s;
        text = t;
        date = new Date();
    }
    public Message(Integer s, String t, Date d) {
        sender = s;
        text = t;
        date = d;
    }
}
