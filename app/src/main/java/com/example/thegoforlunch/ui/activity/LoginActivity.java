package com.example.thegoforlunch.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.thegoforlunch.R;
import com.example.thegoforlunch.databinding.ActivityLoginBinding;

import com.example.thegoforlunch.model.User;
import com.example.thegoforlunch.viewmodel.AppViewModel;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {


    // private static
    private static final String TAG = LoginActivity.class.getSimpleName();
    public static final int RC_GOOGLE_SIGN_IN = 4567;
    public static final int RC_EMAIL_SIGN_IN = 5678;


    // variables
    private ActivityLoginBinding mBinding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser mCurrentUser;
    private CallbackManager mCallbackManager;
    private AuthCredential mUpdatedAuthCredential;
    private LoginManager mLoginManager;
    private AppViewModel mAppViewModel;


    // inherited methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        init();
        setContentView(mBinding.getRoot());
        configureGoogleSignIn();
        configureFacebookSignIn();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (mCurrentUser != null) {
            navigateToMainActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        handleGoogleSignInRequest(requestCode, data);

    }


    // methods
    private void init() {
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mAppViewModel = new ViewModelProvider(this).get(AppViewModel.class);
    }

    public void onClick(View view) {
        Log.d(TAG, "onClick");

        if (view.getId() == R.id.login_facebook_login_button) {
            signInWithFacebook();
        } else if (view.getId() == R.id.login_google_login_button) {
            signInWithGoogle();
        } else if (view.getId() == R.id.login_email_login_text_view) {
            signInWithEmailAndPassword();
        }
    }

    private void configureGoogleSignIn() {
        Log.d(TAG, "configureGoogleSignIn");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
    }

    private void signInWithGoogle() {
        Log.d(TAG, "signInWithGoogle");

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void handleGoogleSignInRequest(int requestCode, @Nullable Intent data) {
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                firebaseAuthWithCredential(credential);
            } catch (ApiException e) {
                Log.e(TAG, "onActivityResult: Google sign in failed.", e);
            }
        }
    }

    private void configureFacebookSignIn() {
        Log.d(TAG, "configureFacebookSignIn");

        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();
        mLoginManager.logOut();
        mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook sign in: onSuccess");

                firebaseAuthWithCredential(FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken()));
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook sign in: onCancel");

                Toast.makeText(LoginActivity.this, R.string.sign_in_canceled, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "facebook sign in: onError", error);

                // should check connection status to send a "no connection"
                // message to user if not available
                Toast.makeText(LoginActivity.this, R.string.unknown_sign_in_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithFacebook() {
        Log.d(TAG, "signInWithFacebook");

        mLoginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList(
                "email",
                "public_profile"));
    }





    private void firebaseAuthWithCredential(AuthCredential credential) {
        Log.d(TAG, "firebaseAuthWithCredential");

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "firebaseAuthWithCredential: success.");

                        mCurrentUser = task.getResult().getUser();
                        if (mCurrentUser != null) {
                            if (mUpdatedAuthCredential != null) {
                                mCurrentUser.linkWithCredential(mUpdatedAuthCredential);
                            }
                            createUserInFirestore(mCurrentUser);
                            navigateToMainActivity();
                        }
                    } else {
                        Log.e(TAG, "firebaseAuthWithCredential: failure.", task.getException());

                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            FirebaseAuthUserCollisionException e = (FirebaseAuthUserCollisionException) task.getException();
                            if (e.getErrorCode().equals("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") && e.getUpdatedCredential() != null) {
                                makeAlertDialogExistingSignIn(e.getEmail(), e.getUpdatedCredential());
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.unknown_sign_in_error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.unknown_sign_in_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void makeAlertDialogExistingSignIn(String email, AuthCredential credential) {
        Log.d(TAG, "makeAlertDialogExistingSignIn");

        String providerName = "";
        switch (credential.getProvider()) {
            case TwitterAuthProvider.PROVIDER_ID:
                providerName = "Twitter";
                break;
            case FacebookAuthProvider.PROVIDER_ID:
                providerName = "Facebook";
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.email_already_linked_title)
                .setMessage(email + getString(R.string.email_already_linked_message, providerName))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    mUpdatedAuthCredential = credential;
                    signInWithGoogle();
                })
                .setNegativeButton(R.string.no, null)
                .create()
                .show();
    }

    private void signInWithEmailAndPassword() {
        Log.d(TAG, "signInWithEmailAndPassword");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);/*

        AltertDialogLoginBinding altertDialogLoginBinding = AltertDialogLoginBinding.inflate(getLayoutInflater());
        alert.setView(altertDialogLoginBinding.getRoot());
        alert.setCancelable(false);
        AlertDialog dialog = alert.create();

        final String[] name = new String[1];
        final String[] email = new String[1];
        final String[] password = new String[1];

        altertDialogLoginBinding.loginAlertDialogLoginButton.setOnClickListener(v -> {
            name[0] = altertDialogLoginBinding.loginAlertDialogNameEditText.getText() != null
                    ? altertDialogLoginBinding.loginAlertDialogNameEditText.getText().toString().trim()
                    : "";
            email[0] = altertDialogLoginBinding.loginAlertDialogEmailEditText.getText() != null
                    ? altertDialogLoginBinding.loginAlertDialogEmailEditText.getText().toString().trim()
                    : "";
            password[0] = altertDialogLoginBinding.loginAlertDialogPasswordEditText.getText() != null
                    ? altertDialogLoginBinding.loginAlertDialogPasswordEditText.getText().toString()
                    : "";

            if (email[0].isEmpty() || password[0].isEmpty()) {
                Toast.makeText(LoginActivity.this, getString(R.string.provide_email_password), Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        altertDialogLoginBinding.loginAlertDialogCloseButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        dialog.setOnDismissListener(dialog1 -> {
            if (email[0] != null) {
                mAuth.createUserWithEmailAndPassword(email[0], password[0])
                        .addOnCompleteListener(LoginActivity.this, createUserWithEmailTask -> {
                            if (createUserWithEmailTask.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail: success");

                                mCurrentUser = mAuth.getCurrentUser();

                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name[0].isEmpty()
                                                ? email[0].split("@")[0]
                                                : name[0])
                                        .build();

                                mCurrentUser.updateProfile(userProfileChangeRequest)
                                        .addOnCompleteListener(task12 -> {
                                            if (task12.isSuccessful()) {
                                                createUserInFirestore(mCurrentUser);
                                                navigateToMainActivity();
                                            }
                                        });
                            } else {
                                Log.w(TAG, "createUserWithEmail: failure", createUserWithEmailTask.getException());

                                Exception exception = createUserWithEmailTask.getException();
                                mAuth.signInWithEmailAndPassword(email[0], password[0])
                                        .addOnCompleteListener(LoginActivity.this, signInWithEmailAndPasswordTask -> {
                                            if (signInWithEmailAndPasswordTask.isSuccessful()) {
                                                Log.d(TAG, "signInWithEmail: success");
                                                Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                                                intent.putExtra("email", email[0]);
                                                startActivityForResult(intent, RC_EMAIL_SIGN_IN);
                                            } else {
                                                Log.w(TAG, "signInWithEmail: failure", signInWithEmailAndPasswordTask.getException());
                                                if (signInWithEmailAndPasswordTask.getException() != null
                                                        && signInWithEmailAndPasswordTask.getException().getMessage() != null
                                                        && signInWithEmailAndPasswordTask.getException().getMessage().equals("The password is invalid or the user does not have a password.")) {
                                                    Toast.makeText(LoginActivity.this, signInWithEmailAndPasswordTask.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (exception != null) {
                                                        Toast.makeText(LoginActivity.this, exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });
                            }
                        });
            }
        });*/
    }

    private void createUserInFirestore(FirebaseUser user) {
        Log.d(TAG, "createUserInFirestore");

        String uid = user.getUid();
        String name = user.getDisplayName();
        String email = user.getEmail();
        String urlPicture = user.getPhotoUrl() != null
                ? user.getPhotoUrl().toString()
                : "https://cdn.pixabay.com/photo/2016/08/08/09/17/avatar-1577909_1280.png";

        mAppViewModel.createOrUpdateUser(new User(uid, name, email, urlPicture))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createOrUpdateUser: onSuccess");
                    } else {
                        Log.e(TAG, "createOrUpdateUser: onFailure", task.getException());
                    }
                });
    }

    private void navigateToMainActivity() {
        Log.d(TAG, "navigateToMainActivity");

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
