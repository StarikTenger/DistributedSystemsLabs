package chat;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

public class ChatHistory {
    LinkedList<Message> messages;

    public ChatHistory() {
        messages = new LinkedList<Message>();
    }
    public ChatHistory(LinkedList<Message> m) {
        messages = m;
    }

    public LinkedList<Message> getMessages() {
        return messages;
    }

    public void loadChatHistory(Server_itf s) throws RemoteException {
        System.out.println("Load chat history...");
        messages = s.getChat();
        System.out.println("Chat:");
        // show all messages after loading
        for (Message message: messages) {
            showMessage(message);
        }
    }

    public void showChatHistory() {
        System.out.println("Chat:");
        // show all messages after loading
        for (Message message: messages) {
            showMessage(message);
        }
    }

    public void loadNewMessage(Message m) {
        messages.add(m);
        showMessage(messages.getLast());
    }

    private void showMessage(Message m) {
        System.out.println(m.sender + ": " + m.text + "(" + m.date + ")");
    }

    public void sendMessage(ClientList l, Message m) {
        System.out.println("Sending message...");
        HashMap<Integer, Client_itf> list = l.getClients();
        if (list != null) {
            list.forEach((id, client) -> {
                try {
                    client.resMessage(m);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        System.out.println("Add it to local history");
        loadNewMessage(m);
    }
}
