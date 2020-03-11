package ca.ualberta.boost;

/* This class class is used to sign in user into the app
 * //todo: ask alex for citation */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import ca.ualberta.boost.models.User;
import ca.ualberta.boost.models.Driver;
import ca.ualberta.boost.models.Rider;
import ca.ualberta.boost.stores.UserStore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import ca.ualberta.boost.models.Driver;

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Declare an instance of FirebaseAuth
    private FirebaseAuth auth;

    // Declare EditTexts for user's signUp info
    private EditText firstName;
    private EditText userName;
    private EditText email;
    private EditText phoneNumber;
    private EditText password;

    private Button signUpButton;

    // Declare spinner that enables a user to sign up as a Driver or a rider
    private Spinner spinner;
    private String userType;


    // Declare variables to store user's signUp info
    private String nameEntered;
    private String usernameEntered;
    private String passwordEntered;
    private String emailEntered;
    private String phoneNumberEntered;



    //fireStore reference to users
    CollectionReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        init();


    }

    /**
     * Initialize Firebase Auth
     * get fireStore reference to users
     * reference spinner
     * create and add user when signUpButton clicked
     */
    private void init(){
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // get reference to EditTexts
        firstName = findViewById(R.id.sign_up_first_name);
        userName = findViewById(R.id.sign_up_username);
        email = findViewById(R.id.sign_up_email);
        phoneNumber = findViewById(R.id.sign_up_phone_number);
        password = findViewById(R.id.sign_up_password);

        //fireStore reference to users
        ref = FirebaseFirestore.getInstance().collection("users");

        //Reference spinner
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this , R.array.userType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //Create and add user when button clicked
        signUpButton = findViewById(R.id.confirm_sign_up_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
//                uniqueUserName(userName.getText().toString());
            }
        });

    }

    /**
     *  adds user to database
     *  Authenticate user with Email and password
     *  call store user function
     *  call signInUser function
      */
    private void addUser() {
        if(authenticate()) {
            auth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(SignUpActivity.this, "user account created", Toast.LENGTH_SHORT).show();
                            finish();
                            storeUser();
                            signInUser();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUpActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    //launches the home activity
    private void launchHomeRider(){
        Intent intent = new Intent(this, RiderMainPage.class);
        startActivity(intent);
    }
    private void launchHomeDriver(){
        Intent intent = new Intent(this, DriverMainPage.class);
        startActivity(intent);
    }


    /**
     *  stores user into database users
     *  stores user as Driver or Rider based on the userType
     */
    //todo: call create driver and ride using if statement , condition= userType

    private void storeUser() {

        //get sign up details
        nameEntered = firstName.getText().toString();
        usernameEntered = userName.getText().toString();
        passwordEntered = password.getText().toString();
        emailEntered = email.getText().toString();
        phoneNumberEntered =  phoneNumber.getText().toString();

        if (userType.equals("Driver")){
            createDriver();
        }

        else{
            createRider();
        }

        Map<String, String> map = new HashMap<>();
        map.put("Name", firstName.getText().toString());
        map.put("Username", userName.getText().toString());
        map.put("Email", email.getText().toString());
        map.put("Phone", firstName.getText().toString());
        map.put("Password", password.getText().toString());
        map.put("id", auth.getUid());
        map.put("role",spinner.getSelectedItem().toString());
        Log.i("value",spinner.getSelectedItem().toString());
        ref.document(auth.getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(SignUpActivity.this, "into the db", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Creates a new Driver object
     * Stores Driver using UserStore class
     */
    public void createDriver(){
        User user = new Driver(nameEntered, usernameEntered, passwordEntered, emailEntered, phoneNumberEntered, userType);
        UserStore.saveUser(user);
    }

    /**
     * Creates a new Rider object
     * Stores Rider using UserStore class
     */
    public void createRider(){
        User user = new Rider(nameEntered, usernameEntered, passwordEntered, emailEntered, phoneNumberEntered, userType);
        UserStore.saveUser(user);
    }

    /**
     * Signs in user and launches the home activity
     */
    private void signInUser () {
        //uniqueUserName(userName.getText().toString());
        if(authenticate()) {
            auth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            launchHome(userType);

                            if(spinner.getSelectedItem().toString().matches("Rider")) {
                                launchHomeRider();
                            } else {
                                launchHomeDriver();
                            }
                        }
                    });
        }
    }

    /**
     *  launches the home activity for Rider or Driver
     *  open DriverMainPage if userType is Driver
     *  open RiderMainPage if userType is Rider
     * @param userType
     */
    private void launchHome(String userType) {

        if (userType.equals("Driver")) {
            Intent intent = new Intent(this, DriverMainPage.class);
            startActivity(intent);

        } else {
            Intent intent = new Intent(this, RiderMainPage.class);
            startActivity(intent);

        }
    }


    /**
     * Authenticate user
     * Return true if fields have values and password is longer than 6 characters
     */
    private boolean authenticate(){
        if(!uniqueUserName(userName.getText().toString())){
            Toast.makeText(this, "Username taken", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(email.getText().toString().matches("")){
            Toast.makeText(this, "Enter a Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(password.getText().toString().matches("")){
            Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(email.getText().toString().length() < 6){
            Toast.makeText(this, "password too short lol", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     *
     * Spinner contains userType
     * onItem selected gets userType selected from spinner list
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
       userType = adapterView.getItemAtPosition(i).toString();
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }



    public boolean uniqueUserName(final String username){
        //boolean object
      //  flag = true;
//        boolean test = true;
        //final SignUpActivity self = this;
        final boolean[] flag = new boolean[1];
        flag[0] = false;
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document: task.getResult()){
                                if(username.matches(document.get("Username").toString())){
                                    Toast.makeText(SignUpActivity.this, "Username is already in use lol", Toast.LENGTH_SHORT).show();
                                    Log.i("value",username);
                                    Log.i("value",document.get("Username").toString());
                                  //  flag = false;
                                    //outer.setflag
                                   // SignUpActivity.this.setFlag(true);
                                    flag[0] = true;
                                }
                            }
                        }
                    }
                });
        Log.i("value",String.valueOf(flag[0]));
        return flag[0];
    }

}