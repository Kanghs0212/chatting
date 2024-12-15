package chat.chatroom;


import chat.DBConnect;
import chat.Lobby.LobbyClient;
import chat.Protocol;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ChatClient extends JFrame {
    JTextPane textPane; // JTextArea 대신 JTextPane 사용, 채팅 내역
    StyledDocument doc; // document 스타일
    JList P_list; // 현재 입장한 사용자 리스트
    DBConnect db = new DBConnect();
    JTextField MsgField; // 메시지 입력  필드
    JButton sendbtn; // 전송 버튼
    JButton gamebtn; // 미니게임 버튼
    JButton closeDrawingBtn; // 창 띄우기 버튼
    Socket socket; // 소켓
    DataInputStream dis;
    DataOutputStream dos;
    DefaultListModel model = new DefaultListModel(); // 리스트를 저장하는 모델
    ArrayList<String> nameList = new ArrayList<String>(); // 사용자 이름 리스트
    String inputId; // 현재 사용자 이름
    String level; // 권한
    String host; // 현재 방 호스트 이름
    String messages; // 메시지 기록
    String IP; // 아이피

    public ChatClient(int port, String IP, String inputId, String level, String host, String messages) {
        this.inputId = inputId;
        this.level = level;
        this.host = host;
        this.messages = messages;
        this.IP = IP;


        setTitle("Chat_room");
        setForeground(Color.black);
        setBounds(650, 400, 500, 350);
        setLayout(new BorderLayout());

        // 상단 버튼 패널
        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new GridLayout(1, 2, 10, 0)); // 버튼 2개를 가로로 정렬
        gamebtn = new JButton("미니게임");
        closeDrawingBtn = new JButton("그림판 표시");
        topButtonPanel.add(gamebtn);
        topButtonPanel.add(closeDrawingBtn);
        add(topButtonPanel, BorderLayout.NORTH);

        // 텍스트 출력 영역
        textPane = new JTextPane(); // JTextPane 생성
        textPane.setEditable(false); // 텍스트 수정 불가
        doc = textPane.getStyledDocument(); // StyledDocument 가져오기

        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 메시지 입력 및 전송 버튼
        JPanel sendpanel = new JPanel();
        sendpanel.setLayout(new BorderLayout());
        MsgField = new JTextField();
        sendbtn = new JButton("전송");
        sendpanel.add(MsgField, BorderLayout.CENTER);
        sendpanel.add(sendbtn, BorderLayout.EAST);
        add(sendpanel, BorderLayout.SOUTH);

        // 참가자 리스트
        P_list = new JList(model);
        JScrollPane participantpane = new JScrollPane(P_list);
        add(participantpane, BorderLayout.EAST);
        participantpane.setPreferredSize(new Dimension(100, participantpane.getPreferredSize().height));

        // 이벤트 리스너 설정
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    if (dos != null) {
                        dos.writeUTF("&@EXIT"); // 나가는 메시지
                        dos.flush();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        if (socket != null) socket.close(); // 소켓 닫기
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        sendbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = MsgField.getText().trim();
                if (!msg.isEmpty()) {
                    sendMessage();
                }
            }
        });

        MsgField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                String msg = MsgField.getText().trim();
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER && !msg.isEmpty()) {
                    sendMessage();
                }
            }
        });

        gamebtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame gameFrame = new JFrame("공 피하기 게임");
                SwingAvoidGame game = new SwingAvoidGame(port);
                gameFrame.add(game);
                gameFrame.setSize(400, 400);
                gameFrame.setVisible(true);
                gameFrame.setLocationRelativeTo(null);
                gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        game.stopGame(); // 게임 종료
                    }
                });
                // 게임 스레드 실행
                Thread gameThread = new Thread(game);
                gameThread.start();
            }
        });

        // 그림판 닫기 버튼(기능 없음, 클릭 시 아무 작업 안 함)
        closeDrawingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LobbyClient.client.setVisible(true);
            }
        });

        ClientThread clientThread = new ClientThread(port, IP);
        clientThread.setDaemon(true);
        clientThread.start();

        setVisible(true);
    }

    class ClientThread extends Thread{

        int port;
        String IP;
        public ClientThread(int port, String IP){
            this.port=port;
            this.IP = IP;
        }
        @Override
        public void run(){
            try{
                String[] message = messages.split("\u001E");

                String id = inputId;
                socket = new Socket(this.IP,this.port);
                appendMessage("채팅방에 접속되었습니다", Color.blue);
                appendMessage("채팅방의 호스트 : " + host, Color.blue);
                for (int i = 0; i < message.length; i++) {
                    appendMessage(message[i], Color.black);
                }
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                dis = new DataInputStream(is);
                dos = new DataOutputStream(os);
                dos.writeUTF(id);
                while(true){
                    String msg = dis.readUTF();
                    if(msg.substring(0,10).equals(Protocol.LOBBY_USER_INFO)){//유저 이름 리스트
                        String username = msg.substring(Protocol.LOBBY_USER_INFO.length());
                        if(!nameList.contains(username)){
                            synchronized (nameList) {
                                nameList.add(username);
                                model.addElement(username);
                            }
                        }
                    }
                    else if(msg.substring(0,10).equals(Protocol.USER_JOIN)){ // 유저 입장했을때
                        appendMessage(msg.substring(10), Color.green);
                    }
                    else if(msg.substring(0,10).equals(Protocol.USER_LEAVE)){ // 유저 나갔을때
                        nameList.clear();
                        model.clear();

                        appendMessage(msg.substring(10), Color.green);
                    }else if(msg.substring(0,10).equals(Protocol.USER_NOT_FOUND)){
                        appendMessage(msg.substring(10), Color.lightGray);
                    }
                    else{
                        messages +=msg+"\u001E";
                        db.connect();
                        db.updateRoomBySocketNum(port, messages);
                        appendMessage(msg, Color.black);
                    }
                }
            }catch(UnknownHostException e){
                appendMessage("서버 주소가 이상합니다", Color.black);
            }catch(IOException e){
                appendMessage("서버와 연결이 끊겼습니다", Color.black);
            }

        }
    }
    void appendMessage(String message, Color color) {
        try {
            Style style = textPane.addStyle("Style", null); // 스타일 생성
            StyleConstants.setForeground(style, color);    // 텍스트 색상 설정
            doc.insertString(doc.getLength(), message + "\n", style); // 메시지 추가
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void sendMessage(){
        String msg = MsgField.getText();
        MsgField.setText("");

        Thread t = new Thread(){
            @Override
            public void run(){
                try{
                    dos.writeUTF(msg);
                    dos.flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
}
