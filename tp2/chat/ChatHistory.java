package chat;

import java.util.LinkedList;

public class ChatHistory {
    LinkedList<Message> messages;

    public void ChatHistory() {
        messages = new LinkedList<Message>();
    }

    public void loadChatHistory(Server_itf s) {
        //messages = s.getChatHistory();
    }

    public void loadNewMessage(Message m) {
        messages.add(m);
        showMessage(messages.getLast());
    }

    private void showMessage(Message m) {
        System.out.println(m.sender + ": " + m.text + "(" + m.date + ")");
    }

    public void sendMessage(ClientList l, Message m) {
        l.getClients().forEach((id, client) -> {
            //client.resMessage(m);
        });
        loadNewMessage(m);
    }
}
