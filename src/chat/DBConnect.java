package chat;

import chat.Login.SHA256;
import chat.Login.userDTO;
import chat.chatroom.Room;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class DBConnect {
    String url ="sql주소";
    String user = "유저이름";
    String pw = "패스워드";
    SHA256 sha256 = new SHA256();
    private Connection conn = null;
    private PreparedStatement pstmt; // 쿼리문을 컴파일 해주는 객체
    // private Statement stmt;
    ResultSet rs = null; // 쿼리문 실행시, 결과를 저장하는 객체


    // DB연결 함수
    public Connection connect(){
        try{
            if(conn == null || conn.isClosed()){
                // JDBC 드라이버 설치 후 연결
                conn = DriverManager.getConnection(url,user,pw);
                System.out.println("DB연결 성공");
            }
        } catch(SQLException e){
            System.out.println("DB연결 실패");
        }
        return conn;
    }

    // DB연결 해제 함수
    public void disconnect(){
        if(conn != null){
            try{
                conn.close();
                System.out.println("DB연결 해제");
            } catch(SQLException e){
                System.out.println("DB연결 해제 실패");
            }
        }
    }

    // 유저 정보 삽입 함수
    // SHA256을 통해 암호화를 진행하므로, 해당 메소드에 NoSuchAlgorithmException을 throw해준다.
    public boolean InsertUserInfo(String id, String pw, String level) throws NoSuchAlgorithmException{
        String sql = "INSERT INTO usertable VALUES(?,?,?);";
        try{
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,id);
            pstmt.setString(2, sha256.encrypt(pw) );
            pstmt.setString(3,level);
            pstmt.executeUpdate();

//            System.out.println("정상적으로 실행");
            return true;
        } catch(SQLException e){
            System.out.println(e.getErrorCode());
            System.out.println(e);
//            System.out.println("데이터 삽입 실패");
            return false;
        }
    }

    /** 아이디 중복 확인 함수*/
    public Boolean Search_dupId(String id){
        String sql = "SELECT id FROM userTable WHERE id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,id);
            rs = pstmt.executeQuery();
            String idv = null;
            while(rs.next()){
                idv = rs.getString("id");
            }

            if(idv!=null&&idv.equals(id)){
                return true;
            }
            else{
                return false;
            }

        } catch(SQLException e){
            System.out.println("error");
            return false;
        }
    }
    /** 아이디 확인 함수*/
    public boolean checkuserId(String id){
        String sql = "SELECT id FROM userTable WHERE id = ?";
        try{
            String idv = null;
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,id);
            rs = pstmt.executeQuery();
            while(rs.next()){
                idv = rs.getString("id");
            }

            if(idv!=null&&idv.equals(id)){
                return true;
            }
            else{
                return false;
            }
        } catch (SQLException e){
            System.out.println("error");
            return false;
        }
    }
    // 비밀번호 확인 함수
    // 마찬가지로 NoSuchAlgorithmException 예외 처리.
    public boolean checkPassword(String id,String pw) throws NoSuchAlgorithmException {
        String sql = "SELECT pw FROM userTable WHERE id = ?";
        try{
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,id);
            rs = pstmt.executeQuery();
            String pwv = null;
            while(rs.next()){
                pwv = rs.getString("pw");
            }

            if(pwv.equals(sha256.encrypt(pw))){
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException e){
            System.out.println("error");
            return false;
        }
    }

    // 방을 소켓 포트 번호를 통해 찾아낸다
    public Room getRoomBySocketNum(int socketNum) {
        String sql = "SELECT * FROM room WHERE socket = ?";
        Room room = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, socketNum);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Room 객체를 생성하고, 결과에서 값을 설정
                room = new Room();
                room.setSocket(rs.getInt("socket"));
                room.setTitle(rs.getString("title"));
                room.setMessages(rs.getString("messages"));
                room.setHost(rs.getString("host"));
                room.setBest(rs.getInt("best"));
                // System.out.println(socketNum);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }

        return room;
    }

    public boolean insertRoom(Room room) {
        String sql = "INSERT INTO room (socket, title, messages, host, best) VALUES (?, ?, ?, ?,?)";
        try {
            pstmt = conn.prepareStatement(sql);

            // Room 객체의 값을 설정
            pstmt.setInt(1, room.getSocket());
            pstmt.setString(2, room.getTitle());
            pstmt.setString(3, room.getMessages());
            pstmt.setString(4, room.getHost());
            pstmt.setInt(5, room.getBest());

            // 삽입 실행
            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0; // 삽입 성공 여부 반환
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false; // 실패 시 false 반환
        }
    }

    public boolean updateRoomBySocketNum(int socketNum, String messages) {
        String sql = "UPDATE room SET messages = ? WHERE socket = ?";
        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, messages);
            pstmt.setInt(2, socketNum);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0; // 업데이트 성공 여부 반환
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false; // 실패 시 false 반환
        }
    }

    public boolean updateBestBySocketNum(int socketNum, int best) {
        String sql = "UPDATE room SET best = ? WHERE socket = ?";
        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, best);
            pstmt.setInt(2, socketNum);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0; // 업데이트 성공 여부 반환
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false; // 실패 시 false 반환
        }
    }



    // 서버의 사용자 static 리스트
    public ArrayList<userDTO> setUserInfo(String id){
        String sql = "SELECT * FROM userTable WHERE id = ?";
        ArrayList<userDTO> info = new ArrayList<userDTO>();
        try{
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,id);
            rs = pstmt.executeQuery();
            while(rs.next()){
                userDTO user = new userDTO();
                user.setId(rs.getString("id"));
                user.setLevel(rs.getString("level"));
                info.add(user);
            }
            return info;
        } catch(SQLException e){
            return null;
        }
    }
}