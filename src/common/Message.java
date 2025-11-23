package common;
import java.io.Serializable;


public class Message implements Serializable {
    public enum Mode {
        CONNECT,
        DISCONNECT,
        CHAT,
        PRIVATE_CHAT,
        GROUP_CHAT,
        ORDER,
        TIME_UPDATE,
        EMOJI
    }


    private Mode mode;
    private String sender;
    private String receiver;
    private String content;
    private Object data;


    public Message(Mode mode, String sender, String receiver, String content, Object data) {
        this.mode = mode;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.data = data;
    }


    public Mode getMode() { return mode; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public Object getData() { return data; }

    @Override
    public String toString() {
        return "[" + mode + "] " + sender + " -> " + receiver + " : " + content;
    }
}