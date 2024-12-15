package chat;

public class Protocol {
    public static final String USER_JOIN = "!$#@$%&(%^";        // 유저 입장 메시지
    public static final String USER_NOT_FOUND = "!%#^&@$%!#";  // 존재하지 않는 유저 알림
    public static final String USER_VALID = "##########";      // 유저 초대 성공 메시지
    public static final String CHAT_FRAME_CREATION = "/CreateChatFrame "; // 채팅방 생성 명령
    public static final String INVITE_PLAYERS = "/playersInvite";          // 플레이어 초대 메시지 전달
    public static final String CLEAR_INVITES = "%%%%%%%%%%";    // 초대 리스트 초기화
    public static final String LOBBY_USER_INFO = "!!@//456,@"; // 로비 유저 정보
    public static final String USER_LEAVE = "&@%&^#$!&*"; // 유저가 나갈때 정보
    public static final String SERVER_IP = "192.168.0.201"; // 서버 아이피

    public static final int SEVER_LOBBY_PORT = 1024;
}