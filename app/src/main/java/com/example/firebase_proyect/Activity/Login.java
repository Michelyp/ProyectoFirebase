package com.example.firebase_proyect.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firebase_proyect.R;
import com.example.firebase_proyect.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class Login extends AppCompatActivity {
    private Button login;
    private TextView botonreset;
    private Button registro;
    private EditText emailInicial;
    private EditText passwordInicial;
    private FirebaseAuth mAuth;
    private Intent MainActivity;
    private ImageView loginPhoto;
    private SharedPreferences mSharedPreferences;
    private Switch recuerdame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        References();
        //conexión con la base de datos en firebase
        mAuth = FirebaseAuth.getInstance();
        MainActivity = new Intent(this, NavigationActivity.class);
        //inicia el shared Preference
        //al seleccionar la foto te manda a registrar
        loginPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent registerActivity = new Intent(getApplicationContext(), RegistrarActivity.class);
                startActivity(registerActivity);
                finish();


            }
        });
        //comprueba los datos introducidos para que se puedan loguearse
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mail = emailInicial.getText().toString();
                String password = passwordInicial.getText().toString();

                if (mail.isEmpty() || password.isEmpty()) {
                    showMessage("Porfavor. Verifique los campos");
                }
                else
                {
                    signIn(mail,password);
                }
            }
        });
        //se auto-rellenan el email y contraseña en caso de haberse guardado
        mSharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        setCredentialsIfExist();

        //Si el switch está activado guarda los valores introducidos en los campos de email y password
        recuerdame.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showMessage("Se activo el recuerdame");
                saveOnPreferences(emailInicial.getText().toString().trim(), passwordInicial.getText().toString().trim());
            }
        });

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loggy = new Intent(Login.this, RegistrarActivity.class);
                startActivity(loggy);

            }
        });
        botonreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,RecuperarcontraActivity.class);
                startActivity(intent);
            }
        });


    }


    //declaración variables
    private void References() {
        botonreset = (TextView) findViewById(R.id.RecuperarContraseña);
        login = (Button) findViewById(R.id.botonLogin);
        recuerdame= (Switch) findViewById(R.id.remember_me_switch);
        registro = (Button) findViewById(R.id.botonRegistro);
        emailInicial = (EditText) findViewById(R.id.MailInicial);
        passwordInicial = (EditText) findViewById(R.id.PasswordInicial);
        loginPhoto = (ImageView) findViewById(R.id.login_photo);


    }
    //comprueba los datos
    private boolean isValidData() {
        if (emailInicial.getText().toString().length() > 0 &&
                passwordInicial.getText().toString().length() > 0
                ){
            return true;
        } else{
            return false;
        }
    }
    private  boolean validar(){
        String correo=emailInicial.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    //método que fija el email y contraseña que se hayan guardado
    private void setCredentialsIfExist() {
        String email = Utils.getUserMailPrefs(mSharedPreferences);
        String password = Utils.getUserPassPrefs(mSharedPreferences);
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            emailInicial.setText(email);
            passwordInicial.setText(password);
            recuerdame.setChecked(true);
        }
    }
    //método para corroborar que el usuario se encuentre en la base de datos
    private void signIn(String mail, String password) {


        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    updateUI();

                }
                else {
                    showMessage(task.getException().getMessage());

                }


            }
        });
    }
    //método que guarda el email y contraseña introducidos
    private void saveOnPreferences(String email, String password) {
        if (recuerdame.isChecked()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("email", email);
            editor.putString("pass", password);
            editor.apply();
        } else {
            Utils.removeSharedPreferences(mSharedPreferences);
        }
    }
    //Termina el activity
    private void updateUI() {
        startActivity(MainActivity);
        finish();
    }

    private void showMessage(String text) {

        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

    //mantiene la sesión del usuario abierta una vez registrado
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            //el usuario ya está conectado, por lo que debemos redirigirlo a la página de inicio
            updateUI();

        }
    }

}
