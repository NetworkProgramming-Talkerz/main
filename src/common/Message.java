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
        EMOJI,
        CONNECT_NOTICE,   // ✨ 추가: 접속/퇴장 공지 전용
        DUPLICATE_PC,  // 🔹 PC번호 중복 접속 거절
        ADD_TIME,          // STAFF가 시간 추가할 때
        FORCE_LOGOUT       // STAFF가 강제 종료할 때
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