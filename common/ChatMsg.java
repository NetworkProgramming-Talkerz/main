package common;

import java.io.Serializable;
import javax.swing.ImageIcon;

// [중요] 직렬화(Serializable) 인터페이스 필수: 객체를 네트워크로 전송하기 위함
public class ChatMsg implements Serializable {
    private static final long serialVersionUID = 1L; // 클래스 버전 관리 ID

    // -------------------------------------------------------
    // 1. 프로토콜(통신 규칙) 상수 정의
    // -------------------------------------------------------

    // [로그인/접속 관련]
    public final static int MODE_LOGIN = 0x1;        // 일반 손님 로그인
    public final static int MODE_LOGOUT = 0x2;       // 로그아웃/퇴장
    public final static int MODE_ADMIN_LOGIN = 0x3;  // [NEW] 관리자(직원) 로그인

    // [채팅 관련]
    public final static int MODE_TX_STRING = 0x10;   // 전체 채팅 (관리자->전체 공지, 손님->관리자)
    public final static int MODE_PRIVATE_CHAT = 0x11;// [NEW] 1:1 귓속말 (특정 대상에게만)
    public final static int MODE_TX_IMAGE = 0x40;    // 이미지/이모티콘 전송

    // [PC방 특수 기능]
    public final static int MODE_TX_ORDER = 0x80;    // 상품 주문 (손님 -> 관리자)
    public final static int MODE_TX_TIME = 0x100;    // 시간 충전/갱신 알림

    // -------------------------------------------------------
    // 2. 데이터 멤버 변수
    // -------------------------------------------------------
    public String userID;   // 보내는 사람 ID (예: PC01, Manager)
    public String receiver; // [중요] 받는 사람 ID (예: PC02) - 1:1 대화 및 특정 전송용
    public int mode;        // 메시지 종류 (위의 상수 사용)
    public String message;  // 대화 내용, 주문 내역, 시스템 메시지 등
    public ImageIcon image; // 이모티콘 이미지 객체
    public long size;       // 파일 크기 또는 주문 금액 데이터 등

    // -------------------------------------------------------
    // 3. 생성자 (상황에 따라 골라 쓰세요)
    // -------------------------------------------------------

    // (1) 풀 생성자: 모든 필드를 초기화할 때 사용
    public ChatMsg(String userID, String receiver, int mode, String message, ImageIcon image, long size) {
        this.userID = userID;
        this.receiver = receiver;
        this.mode = mode;
        this.message = message;
        this.image = image;
        this.size = size;
    }

    // (2) 귓속말/주문용 생성자: (보내는사람, 받는사람, 모드, 내용)
    // 예: new ChatMsg("Manager", "PC01", MODE_PRIVATE_CHAT, "조용히 해주세요");
    public ChatMsg(String userID, String receiver, int mode, String message) {
        this(userID, receiver, mode, message, null, 0);
    }

    // (3) 전체 채팅용 생성자: (보내는사람, 모드, 내용) - 받는사람은 필요 없음(null)
    // 예: new ChatMsg("PC01", MODE_TX_STRING, "안녕하세요");
    public ChatMsg(String userID, int mode, String message) {
        this(userID, null, mode, message, null, 0);
    }

    // (4) 로그인/로그아웃용 생성자: (보내는사람, 모드) - [아까 오류 났던 부분 해결!]
    // 예: new ChatMsg("PC01", MODE_LOGIN);
    public ChatMsg(String userID, int mode) {
        this(userID, null, mode, null, null, 0);
    }
}