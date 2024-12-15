package chat.chatroom;

import chat.DBConnect;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatFrame extends JFrame {
    private boolean completed = false; // 작업 완료 상태
    private boolean flag = false; // 방만들기 성공 여부
    private final Object lock = new Object(); // 동기화 객체

    private int port=0;
    private String messages="";

    public ChatFrame(String host){
        setSize(500, 250);
        setLayout(null);
        setTitle("방 참가 및 생성");

        // 방 참가
        JLabel title = new JLabel("방 식별 번호 입력");
        title.setSize(200, 30);
        title.setLocation(20, 20);

        JTextField roomNum = new JTextField(10);
        roomNum.setSize(180, 30);
        roomNum.setLocation(20, 60);

        JButton login = new JButton("입장");
        login.setSize(100, 30);
        login.setLocation(220, 60);

        // 새로운 방 만들기
        JLabel title2 = new JLabel("새로운 방 만들기");
        title2.setSize(200, 30);
        title2.setLocation(20, 110);


        JLabel roomNum2Label = new JLabel("방 번호(10000~65535 사이)");
        roomNum2Label.setSize(150, 20);
        roomNum2Label.setLocation(20, 135);

        JTextField roomNum2 = new JTextField(10);
        roomNum2.setSize(120, 30);
        roomNum2.setLocation(20, 160);

        JLabel roomTitleLabel = new JLabel("채팅방 이름");
        roomTitleLabel.setSize(100, 20);
        roomTitleLabel.setLocation(200, 135);

        JTextField roomTitle = new JTextField(10);
        roomTitle.setSize(120, 30);
        roomTitle.setLocation(200, 160);

        JButton create = new JButton("만들기");
        create.setSize(120, 30);
        create.setLocation(340, 160);

        // 창을 그냥 닫아버리는 상황 처리
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                synchronized (lock) {
                    completed = true;
                    lock.notify();
                }
            }
        });


        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String socketNum = roomNum.getText();
                DBConnect db = new DBConnect();
                if(!socketNum.equals("")){
                    db.connect();
                    Room room = db.getRoomBySocketNum(Integer.parseInt(socketNum));

                    synchronized (lock) { // 동기화 블록으로 감싸기
                        if (room != null && room.getHost().equals(host)) {
                            new ChatServer(room.getSocket(), room.getHost());
                            port = room.getSocket();
                            messages = room.getMessages();
                            flag = true;
                            completed = true;
                            lock.notify(); // 대기 중인 스레드 깨우기
                            setVisible(false);
                        } else {
                            JOptionPane.showMessageDialog(null, "없는 방 식별 번호", "존재하지 않는 방입니다.", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String socketNum = roomNum2.getText();
                String title = roomTitle.getText();

                try {
                    long parsedNum = Long.parseLong(socketNum); // Long 타입으로 변환
                    if (parsedNum < 2 || parsedNum >= 65535) {
                        JOptionPane.showMessageDialog(null, "방 식별번호는 10000~65535 내에서 선택해주세요!", "오류", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "숫자 형식의 값을 입력해주세요!", "입력 오류", JOptionPane.ERROR_MESSAGE);
                }

                DBConnect db = new DBConnect();

                if(socketNum.equals("") || title.equals("")){
                    JOptionPane.showMessageDialog(null,"방 번호와 제목을 입력하세요!","방 번호와 제목을 입력하세요!",JOptionPane.INFORMATION_MESSAGE);
                }else{
                    db.connect();
                    Room room = db.getRoomBySocketNum(Integer.parseInt(socketNum));
                    Room room2 = db.getRoomBySocketNum(Integer.parseInt(socketNum+1));
                    synchronized (lock) { // 동기화 블록으로 감싸기
                        if (room != null || room2 != null) {
                            JOptionPane.showMessageDialog(null, "방 번호가 이미 존재합니다!", "방 번호를 다시 입력하세요!", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            Room newRoom = new Room();
                            newRoom.setSocket(Integer.parseInt(socketNum));
                            newRoom.setTitle(title);
                            newRoom.setMessages("");
                            newRoom.setHost(host);
                            newRoom.setBest(0);

                            db.insertRoom(newRoom);

                            new ChatServer(newRoom.getSocket(), newRoom.getHost());
                            port = newRoom.getSocket();
                            messages = newRoom.getMessages();
                            flag = true;
                            completed = true;
                            lock.notify(); // 대기 중인 스레드 깨우기
                            setVisible(false);
                        }
                    }
                }
            }
        });

        add(title);
        add(roomNum);
        add(login);
        add(title2);
        add(roomNum2Label);
        add(roomNum2);
        add(roomTitleLabel);
        add(roomTitle);
        add(create);
        setVisible(true);
    }
    public boolean waitForCompletion() {
        synchronized (lock) { // 동기화 블록으로 진입
            while (!completed) { // 작업 완료 상태가 될 때까지 대기
                try {
                    lock.wait(); // 다른 스레드에서 notify가 호출될 때까지 대기
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false; // 중단된 경우 false 반환
                }
            }
        }

        boolean returnFlag = flag;
        completed = false; // 작업 완료 상태 초기화
        flag = false;      // 작업 결과 초기화
        return returnFlag; // 완료 여부 반환
    }

    public int getPort(){
        return port;
    }
    public String getMessages() {return messages;}
}
