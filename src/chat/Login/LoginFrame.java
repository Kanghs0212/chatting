package chat.Login;

import chat.DBConnect;
import chat.Lobby.LobbyClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class LoginFrame extends JFrame {

    public static ArrayList<userDTO> userInfo = new ArrayList<userDTO>();

    public static void main(String[] args) {
        new LoginFrame();
    }
    public LoginFrame() {
        setSize(300,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setTitle("환영합니다!");
        JPanel login_input = new JPanel();
        
        // 그리드백 레이아웃 사용
        login_input.setLayout(new GridBagLayout());
        
        //힌트 필드 클래스를 이용해 바로 생성
        HintTextField id = new HintTextField("아이디 입력",20);
        HintPwField pw = new HintPwField("비밀번호 입력",20);
        JButton loginbtn = new JButton("로그인");
        JButton signup = new JButton("가입하기");
        JLabel title = new JLabel("Talkify");
        title.setFont(new Font("", Font.BOLD, 20));
        JLabel welcome = new JLabel("환영합니다!");


        loginbtn.addActionListener(new ActionListener() { //로그인 클릭 이벤트
            @Override
            public void actionPerformed(ActionEvent e) {
                // 각 필드에 있는 값을 가져온다.
                String id_value = id.getText();
                String pw_value = pw.getText();
                DBConnect db = new DBConnect(); // db연결
                
                // 만약 둘다 공백이 아닐경우
                if(!(id_value.equals("")||pw_value.equals(""))){
                    db.connect(); // 에 접근후 해당 id 유저가 존재하는지 확인
                    boolean id_check = db.checkuserId(id_value);
                    
                    // 가능한 id일경우
                    if(id_check){
                        boolean pw_check = false;
                        // 비밀번호 확인
                        try {
                            pw_check = db.checkPassword(id_value,pw_value);
                        } catch (NoSuchAlgorithmException ex) {
                            throw new RuntimeException(ex);
                        }
                        
                        //만약 비밀번호도 가능할 경우
                        if (pw_check) {
                            userInfo = db.setUserInfo(id_value); // 서버의 유저 리스트에 저장

                            // 성공했다는 알람창을 띄우고 접속
                            JOptionPane.showMessageDialog(null, "로그인 성공", "로그인", JOptionPane.INFORMATION_MESSAGE);
                            setVisible(false);
                            new LobbyClient();

                        }
                        // 비밀번호가 틀린경우
                        else{
                            JOptionPane.showMessageDialog(null,"비밀번호가 틀렸습니다","비밀번호 오류",JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    // 아이디가 틀린경우
                    else{
                        JOptionPane.showMessageDialog(null,"아이디가 틀렸습니다","아이디 오류",JOptionPane.INFORMATION_MESSAGE);
                    }

                }
                // 공백이 있을경우
                else{
                    JOptionPane.showMessageDialog(null, "입력되지 않은 부분이 있습니다","입력오류",JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });
        
        signup.addActionListener(new ActionListener() { //가입하기 버튼 클릭 이벤트
            @Override
            public void actionPerformed(ActionEvent e) {
                new signup();
                setVisible(false);
            }
        });


        // 그리드백Constraints 를 생성하여 해당 gbc에 각 위치에 component들을 삽입
        GridBagConstraints gridBagConstraint = new GridBagConstraints();
        gridBagConstraint.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraint.insets = new Insets(5,10,5,10);

        Component[] components = {title, welcome, id, pw, loginbtn, signup};

        gridBagConstraint.gridx = 0;
        for (int i = 0; i < components.length; i++) {
            gridBagConstraint.gridy = i; // 행 설정
            login_input.add(components[i], gridBagConstraint); // 항상 GridBagConstraints 전달
        }

        add(login_input);
        setVisible(true);
    }

}