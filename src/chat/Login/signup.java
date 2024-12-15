package chat.Login;

import chat.DBConnect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;

public class signup extends JFrame {

    boolean testValue = false;

    public signup() {
        setSize(300,500);
        setTitle("회원가입");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15,5,0,0);

        HintTextField id = new HintTextField("아이디",20);
        HintPwField pw = new HintPwField("비밀번호",20);
        HintPwField repw = new HintPwField("비밀번호 재입력",20);
        JLabel title = new JLabel("회원가입");
        JButton duptest = new JButton("중복체크");
        JButton signupBtn = new JButton("가입하기");


        duptest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConnect db = new DBConnect();
                String id_value = id.getText();
                if(id_value.equals("")){
                    JOptionPane.showMessageDialog(null,"아이디 입력해주세요","아이디 입력",JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    db.connect();
                    Boolean dupcode = db.Search_dupId(id_value);
                    db.disconnect();
                    if (!dupcode) {
                        testValue = true;
                        JOptionPane.showMessageDialog(null, "아이디 사용가능", "중복확인", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "중복된 아이디가 있습니다", "중복확인", JOptionPane.INFORMATION_MESSAGE);
                        testValue = false;
                    }
                }
            }
        });

        signupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id_value = id.getText();
                String pw_value = pw.getText();
                String repw_value = repw.getText();
                DBConnect db = new DBConnect();
                if(!(id_value.equals("")||pw_value.equals("")||repw_value.equals(""))){
                    if(testValue == true){
                        if(pw_value.equals(repw_value)){
                            db.connect();
                            boolean insert_code = false;

                            try {
                                insert_code = db.InsertUserInfo(id_value,pw_value,"사용자");
                            } catch (NoSuchAlgorithmException ex) {
                                throw new RuntimeException(ex);
                            }

                            if(insert_code == true){
                                JOptionPane.showMessageDialog(null,"회원가입 성공","회원가입 성공",JOptionPane.INFORMATION_MESSAGE);
                                setVisible(false);
                                new LoginFrame();
                            }
                            else{
                                JOptionPane.showMessageDialog(null,"회원가입 실패","회원가입 실패",JOptionPane.INFORMATION_MESSAGE);
                            }
                            db.disconnect();
                        }
                        else{
                            JOptionPane.showMessageDialog(null,"비밀번호 재입력 오류","비밀번호 매칭",JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    else{
                        JOptionPane.showMessageDialog(null,"아이디 중복확인을 해주세요","중복확인",JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(null,"입력하지 않은 부분이 있습니다","입력 오류",JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });


        gbc.gridx = 0;
        gbc.gridy = 0;
        add(title,gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 2;
        add(id,gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        add(duptest,gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 2;
        add(pw,gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 2;
        add(repw,gbc);


        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth =5;
        add(signupBtn,gbc);

        setVisible(true);
    }
}