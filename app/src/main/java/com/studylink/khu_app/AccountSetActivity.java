package com.studylink.khu_app;

import android.accounts.Account;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static android.os.Build.ID;

public class AccountSetActivity extends AppCompatActivity {
    private FirebaseDatabase FirebaseDatabase;
    private FirebaseAuth auth;
    private TextView account_set_next;
    private EditText account_username;
    private EditText account_userbirth;
    private TextView account_useraddress;
    private String sex_check = "false";


    TextView gender_male, gender_female;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_set);
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase = FirebaseDatabase.getInstance();
        account_username = (EditText) findViewById(R.id.account_username);
        account_userbirth = (EditText) findViewById(R.id.account_userbirth);
        account_useraddress = (TextView) findViewById(R.id.account_useraddress);

        gender_male = findViewById(R.id.gender_male);
        gender_female = findViewById(R.id.gender_female);
        account_set_next = findViewById(R.id.account_set_next);

        gender_male.setClickable(true);
        gender_female.setClickable(true);
        account_set_next.setClickable(true);

        gender_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChange(gender_male, gender_female);
                sex_check = "male";
            }
        });
        gender_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChange(gender_female, gender_male);
                sex_check = "female";
            }
        });

        account_set_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseAccountset();
                Intent intent = new Intent (AccountSetActivity.this, AccountFinActivity.class);
                startActivity(intent);
            }
        });

    }

    public void colorChange(TextView change_blue, TextView change_gray){
        change_blue.setBackground(getResources().getDrawable(R.drawable.blue_border_register));
        change_gray.setBackground(getResources().getDrawable(R.drawable.gray_border_register));
        change_blue.setTextColor(Color.parseColor("#2f78db"));
        change_gray.setTextColor(Color.parseColor("#e4e4e4"));
    }

    public void databaseAccountset(){                                                               //데이터를 파이어베이스에 올림
        AccountDTO Accountset = new AccountDTO();

        String uid = auth.getCurrentUser().getUid();
        Accountset.username = account_username.getText().toString();
        Accountset.userbirth = account_userbirth.getText().toString();
        Accountset.usersex = sex_check;

        FirebaseDatabase.getReference().child("users").child(uid).setValue(Accountset);
        Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show();
    }

    /*public void postFirebaseDatabase(boolean add){
        FirebaseDatabase = FirebaseDatabase.getInstance();
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> accountValues = null;
        if(add){
            AccountDTO accountDTO = new AccountDTO(account_username, account_userbirth, sex_check, );
            accountValues = accountDTO.toMap();
        }
        childUpdates.put("/users/" + auth.getCurrentUser().getUid(), accountValues);
        FirebaseDatabase.getReference().updateChildren(childUpdates);
    }*/
}
