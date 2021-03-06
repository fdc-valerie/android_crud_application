package com.example.crudapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import org.w3c.dom.Text;

public class RegistrationActivity extends AppCompatActivity {
    private EditText regEmail, regPassword, regConfirmPass;
    private Button btnReg;
    private ProgressBar loadingPB;
    private TextView redirectLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        regEmail = (EditText) findViewById(R.id.regEmail);
        regPassword = (EditText) findViewById(R.id.regPassword);
        regConfirmPass = (EditText) findViewById(R.id.regConfirmPass);
        btnReg = (Button) findViewById(R.id.btnReg);
        loadingPB = (ProgressBar)findViewById(R.id.progressBar);
        redirectLogin = (TextView) findViewById(R.id.txtLogin);
        mAuth = FirebaseAuth.getInstance();

        redirectLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingPB.setVisibility(View.VISIBLE);
                String email = regEmail.getText().toString();
                String password = regPassword.getText().toString();
                String confirmPass =  regConfirmPass.getText().toString();

                if (!password.equals(confirmPass)) {
                    Toast.makeText(RegistrationActivity.this, "Password and Confirm Password didn't match", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPass)) {
                    regEmail.setError("Please enter email address");
                    regPassword.setError("Please enter password");
                    regConfirmPass.setError("Please enter confirm password");
                } else if (TextUtils.isEmpty(email)) {
                    regEmail.setError("Please enter email address");
                } else if (TextUtils.isEmpty(password)) {
                    regPassword.setError("Please enter password");
                } else if (TextUtils.isEmpty(confirmPass)) {
                    regConfirmPass.setError("Please enter confirm password");
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loadingPB.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                try
                                {
                                    throw task.getException();
                                }
                                catch(FirebaseAuthWeakPasswordException e) {
                                     regPassword.setError("The given password is invalid.");
                                }
                                catch(FirebaseAuthInvalidCredentialsException e){
                                    regEmail.setError("The email address is badly formatted");
                                }
                                catch (FirebaseAuthUserCollisionException existEmail) {
                                    regEmail.setError("Email already exists. Please try another email.");
                                }
                                catch (Exception e) {
                                    Toast.makeText(RegistrationActivity.this, "Fail to register. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(RegistrationActivity.this, "Successfully registered", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                finish();
                            }
                        }
                    });
                }
                loadingPB.setVisibility(View.GONE);
            }
        });

    }
}