package com.example.alex.fua;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CustomerSignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = CustomerSignupActivity.class.getSimpleName();

    @Bind(R.id.email_sign_up_button) Button mButtomSignUp;
    @Bind(R.id.signup_login) TextView msignIn;

    @Bind(R.id.userName) EditText mUname;
    @Bind(R.id.email) EditText mEmail;
    @Bind(R.id.password) EditText mPassword;
    @Bind(R.id.connfirm_password) EditText mPassword_confirm;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mAuthProgressDialog;

    private String mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signup);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        createAuthStateListener();

        createAuthProgressDialog();

        mButtomSignUp.setOnClickListener(this);
        msignIn.setOnClickListener(this);
    }

    private void createAuthProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Creating Account...");
        mAuthProgressDialog.setMessage("Authenticating....");
        mAuthProgressDialog.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        if (v == mButtomSignUp){
            createAccount();
        }
        if (v == msignIn){
            Intent intent = new Intent(CustomerSignupActivity.this, CustomerLoginActivity.class);
            startActivity(intent);
        }
    }

    private void createAccount() {
        mUser = mUname.getText().toString().trim();
        final String email  = mEmail.getText().toString().trim();
        String password     = mPassword.getText().toString().trim();
        String confirmPassword = mPassword_confirm.getText().toString().trim();

        boolean validName = isValidName(mUser);
        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password, confirmPassword);

        if (!validName || !validEmail || !validPassword) return;

        mAuthProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mAuthProgressDialog.dismiss();

                if (task.isSuccessful()) {
                    Log.d(TAG, "Authentication successful");
                    createFirebaseUserProfile(task.getResult().getUser());
                } else {
                    Toast.makeText(CustomerSignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if (password.length() < 6){
            mPassword.setError("Your password must be greater than 6 characters");
            return false;
        }else if (!password.equals(confirmPassword)){
            mPassword.setError("Your Passwords do not match");
            return false;
        }
        return true;
    }

    public boolean isValidEmail(String email) {
        boolean isGoodEmail = (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mEmail.setError("Please enter a valid email address");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mUname.setError("Please enter your name");
            return false;
        }
        return true;
    }

    private void createAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(CustomerSignupActivity.this, CustomerMapActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

            }
        };
    }

    private void createFirebaseUserProfile(final FirebaseUser user){
        UserProfileChangeRequest addProfile = new UserProfileChangeRequest.Builder().setDisplayName(mUser).build();
        user.updateProfile(addProfile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, user.getDisplayName());
                        }
                    }

                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
