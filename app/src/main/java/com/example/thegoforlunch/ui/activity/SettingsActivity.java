package com.example.thegoforlunch.ui.activity;


import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.thegoforlunch.R;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import javax.annotation.Nullable;

import com.example.thegoforlunch.databinding.ActivitySettingsBinding;
import com.example.thegoforlunch.util.PermissionsUtils;
import com.example.thegoforlunch.viewmodel.AppViewModel;

import java.text.DateFormat;
import java.util.Calendar;

import static com.example.thegoforlunch.util.AppConstants.NOTIFICATIONS_PREFERENCES_NAME;
import static com.example.thegoforlunch.util.AppConstants.RC_READ_EXTERNAL_STORAGE_PERMISSION;
import static com.example.thegoforlunch.util.AppConstants.SHARED_PREFERENCES_NAME;

public class SettingsActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{


    private TextView textView;

    // private static
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final int RC_CHOOSE_IMAGE = 1234;


    // variables
    private ActivitySettingsBinding mBinding;
    private SharedPreferences mSharedPreferences;
    private boolean mIsNotificationsActivated;
    private AppViewModel mViewModel;
    private Uri mUriImageSelected;


    // inherited methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        init();
        getNotificationsActivatedFromSharedPreferences();
        buildNotificationCheckBox();
        buildUserInformationEditText();
        buildUserSettingsPictureButton();
        buildConfirmButton();
        setContentView(mBinding.getRoot());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");

        handleReadExternalStoragePermissionRequest(requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        handleImageChosenRequest(requestCode, resultCode, data);
    }


    // methods
    private void init() {
        Log.d(TAG, "init");

        mBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
    }

    private void getNotificationsActivatedFromSharedPreferences() {
        Log.d(TAG, "getNotificationsActivatedFromSharedPreferences");

        mSharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mIsNotificationsActivated = mSharedPreferences.getBoolean(NOTIFICATIONS_PREFERENCES_NAME, true);
    }

    private void buildNotificationCheckBox() {
        Log.d(TAG, "buildNotificationCheckBox");

        mBinding.notificationCheckBox.setChecked(mIsNotificationsActivated);
        mBinding.notificationCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mIsNotificationsActivated = isChecked;
            mSharedPreferences
                    .edit()
                    .putBoolean(NOTIFICATIONS_PREFERENCES_NAME, mIsNotificationsActivated)
                    .apply();
        });
    }

    private void buildUserInformationEditText() {
        Log.d(TAG, "buildUserInformationEditText");

        mBinding.userSettingsNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mUriImageSelected != null) {
                    mBinding.confirmButton.setEnabled(true);
                } else if (mBinding.userSettingsNameEditText.getText() != null && mUriImageSelected == null) {
                    boolean isReady = mBinding.userSettingsNameEditText.getText().length() > 3;
                    mBinding.confirmButton.setEnabled(isReady);
                    mBinding.confirmButton.setTextColor(isReady
                            ? ContextCompat.getColor(SettingsActivity.this, R.color.colorDrawer)
                            : ContextCompat.getColor(SettingsActivity.this, R.color.colorLightGrey));
                }
            }
        });
    }

    private void buildUserSettingsPictureButton() {
        Log.d(TAG, "buildUserSettingsPictureButton");

        mBinding.userSettingsPictureImageButton.setOnClickListener(v -> {
            if (PermissionsUtils.checkExternalStoragePermission(SettingsActivity.this)) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RC_CHOOSE_IMAGE);
            }
        });
    }

    private void handleImageChosenRequest(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "handleImageChosenRequest");

        if (requestCode == RC_CHOOSE_IMAGE) {
            if (resultCode == RESULT_OK) {
                this.mUriImageSelected = data.getData();
                Glide.with(this)
                        .load(this.mUriImageSelected)
                        .circleCrop()
                        .into(mBinding.userSettingsPictureImageButton);
                mBinding.confirmButton.setEnabled(true);
                mBinding.confirmButton.setTextColor(ContextCompat.getColor(SettingsActivity.this, R.color.colorDrawer));
            }
        }
    }

    private void buildConfirmButton() {
        Log.d(TAG, "buildConfirmButton");

        mBinding.confirmButton.setOnClickListener(v -> {
            String name = "";
            if (mBinding.userSettingsNameEditText.getText() != null
                    && mBinding.userSettingsNameEditText.getText().length() > 3) {
                name = mBinding.userSettingsNameEditText.getText().toString().trim();
            }

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                if (!name.isEmpty()) {
                    mViewModel.updateUserName(currentUser.getUid(), name)
                            .addOnCompleteListener(SettingsActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, R.string.information_updated, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SettingsActivity.this, R.string.error_information_not_updated, Toast.LENGTH_SHORT).show();
                                }

                                uploadImageInFirebaseAndUpdateUserProfile(currentUser, mUriImageSelected);
                            });
                } else {
                    uploadImageInFirebaseAndUpdateUserProfile(currentUser, mUriImageSelected);
                }
            }
        });
    }

    private void uploadImageInFirebaseAndUpdateUserProfile(FirebaseUser user, Uri uriImageSelected) {
        Log.d(TAG, "uploadImageInFirebaseAndUpdateUserProfile");

        if (uriImageSelected != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference(user.getUid());
            imageRef.putFile(uriImageSelected)
                    .addOnCompleteListener(this, task -> {
                        if (task.getResult().getMetadata() != null && task.getResult().getMetadata().getReference() != null) {
                            task.getResult().getMetadata().getReference().getDownloadUrl()
                                    .addOnCompleteListener(SettingsActivity.this, task12 -> {
                                        String photoUrl = task12.getResult().toString();
                                        mViewModel.updateUserPicture(user.getUid(), photoUrl)
                                                .addOnCompleteListener(SettingsActivity.this, task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(SettingsActivity.this, R.string.information_updated, Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(SettingsActivity.this, R.string.error_information_not_updated, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    });
                        }
                    });
        } else {
            finish();
        }
    }








    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        updateTimeText(c);
        startAlarm(c);
    }

    private void updateTimeText(Calendar c){
        String timeText = "Alarm set for: ";
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        textView.setText(timeText);
    }

    private void startAlarm(Calendar c){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //Intent intent = new Intent(this, AlertReceiver.class);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        if(c.before(Calendar.getInstance())){
            c.add(Calendar.DATE,1);
        }
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    private void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //Intent intent = new Intent(this, AlertReceiver.class);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        // alarmManager.cancel(pendingIntent);
        textView.setText("Alarm Canceled");
    }


    private void handleReadExternalStoragePermissionRequest(int requestCode) {
        Log.d(TAG, "handleReadExternalStoragePermissionRequest");

        if (requestCode == RC_READ_EXTERNAL_STORAGE_PERMISSION) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RC_CHOOSE_IMAGE);
            }
        }
    }
}
