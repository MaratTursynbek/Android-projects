package com.marat.apps.android.pro3.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.marat.apps.android.pro3.Databases.StoreToDatabaseHelper;
import com.marat.apps.android.pro3.Dialogs.CarTypePickerDialog;
import com.marat.apps.android.pro3.Dialogs.CityPickerDialog;
import com.marat.apps.android.pro3.Interfaces.DialogCarTypeChosenListener;
import com.marat.apps.android.pro3.Interfaces.DialogCityChosenListener;
import com.marat.apps.android.pro3.Models.CarType;
import com.marat.apps.android.pro3.Models.City;
import com.marat.apps.android.pro3.Models.PhoneNumberEditText;
import com.marat.apps.android.pro3.Interfaces.RequestResponseListener;
import com.marat.apps.android.pro3.Internet.PostRequest;
import com.marat.apps.android.pro3.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

public class CreateAccountActivity extends AppCompatActivity implements RequestResponseListener, DialogCityChosenListener, DialogCarTypeChosenListener, TextView.OnEditorActionListener, View.OnClickListener {

    private static final String TAG = "logtag";

    private String userRegistrationURL = "https://propropro.herokuapp.com/api/v1/users";
    private String formattedPhoneNumber;

    private EditText userNameEditText, passwordEditText, confirmPasswordEditText, cityEditText, carTypeEditText;
    private PhoneNumberEditText phoneNumberEditText;
    private ProgressDialog dialog;
    private CityPickerDialog dialogChooseCity;
    private CarTypePickerDialog dialogChooseCarType;

    private City chosenCity;
    private int chosenCityPosition;
    private CarType chosenCarType;
    private int chosenCarTypePosition;

    private boolean isSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userNameEditText = (EditText) findViewById(R.id.createAccountUserNameEditText);
        phoneNumberEditText = (PhoneNumberEditText) findViewById(R.id.createAccountPhoneNumberEditText);
        passwordEditText = (EditText) findViewById(R.id.createAccountPasswordEditText);
        confirmPasswordEditText = (EditText) findViewById(R.id.createAccountPasswordEditText2);
        cityEditText = (EditText) findViewById(R.id.createAccountCityEditText);
        carTypeEditText = (EditText) findViewById(R.id.createAccountCarTypeEditText);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        phoneNumberEditText.setText(extras.getString("phone_number"));
        phoneNumberEditText.setFocusable(false);

        userNameEditText.setOnEditorActionListener(this);
        passwordEditText.setOnEditorActionListener(this);
        confirmPasswordEditText.setOnEditorActionListener(this);

        cityEditText.setOnClickListener(this);
        carTypeEditText.setOnClickListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean isHandled = false;
        switch (v.getId()) {
            case R.id.createAccountUserNameEditText:
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    userNameEditText.clearFocus();
                    passwordEditText.requestFocus();
                    isHandled = true;
                }
                break;
            case R.id.createAccountPasswordEditText:
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    passwordEditText.clearFocus();
                    confirmPasswordEditText.requestFocus();
                    isHandled = true;
                }
                break;
            case R.id.createAccountPasswordEditText2:
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    isHandled = true;
                }
                break;
        }
        return isHandled;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createAccountCityEditText:
                dialogChooseCity = CityPickerDialog.newInstance(chosenCityPosition);
                dialogChooseCity.show(getSupportFragmentManager(), "dialog");
                break;
            case R.id.createAccountCarTypeEditText:
                dialogChooseCarType = CarTypePickerDialog.newInstance(chosenCarTypePosition);
                dialogChooseCarType.show(getSupportFragmentManager(), "dialog");
                break;
        }
    }

    @Override
    public void cityChosen(City city, int position) {
        chosenCity = city;
        chosenCityPosition = position;
        cityEditText.setText(city.getCityName());
    }

    @Override
    public void carTypeChosen(CarType carType, int position) {
        chosenCarType = carType;
        chosenCarTypePosition = position;
        carTypeEditText.setText(carType.getCarTypeName());
    }

    public void registerUser(View v) {
        PostRequest postRequest = new PostRequest(this);
        postRequest.delegate = this;
        if (postRequest.isNetworkAvailable()) {
            if (userNameIsEntered()) {
                if (passwordIsEntered()) {
                    if (passwordIsConfirmed()) {
                        if (cityIsChosen()) {
                            if (carTypeIsChosen()) {
                                showProgressDialog();
                                postRequest.post(userRegistrationURL, createNewUserDataInJson());
                            } else {
                                showSnackErrorMessage(getString(R.string.error_choose_car), v);
                            }
                        } else {
                            showSnackErrorMessage(getString(R.string.error_choose_city), v);
                        }
                    } else {
                        showSnackErrorMessage(getString(R.string.error_passwords_do_not_match), v);
                    }
                } else {
                    showSnackErrorMessage(getString(R.string.error_enter_password), v);
                }
            } else {
                showSnackErrorMessage(getString(R.string.error_enter_name), v);
            }
        } else {
            hideKeyboard();
            showSnackErrorMessage(getString(R.string.error_no_internet_connection), v);
        }
    }

    private String createNewUserDataInJson() {
        String phone = phoneNumberEditText.getText().toString();
        formattedPhoneNumber = phone.substring(1, 4) + phone.substring(6, 9) + phone.substring(10, 12) + phone.substring(13);

        return "{\"user\":{"
                + "\"phone_number\":"             +   "\""   +     formattedPhoneNumber                             + "\""   +   ","
                + "\"name\":"                     +   "\""   +     userNameEditText.getText().toString()            + "\""   +   ","
                + "\"password\":"                 +   "\""   +     passwordEditText.getText().toString()            + "\""   +   ","
                + "\"password_confirmation\":"    +   "\""   +     confirmPasswordEditText.getText().toString()     + "\""   +   ","
                + "\"city_id\":"                  +   "\""   +     chosenCity.getCityID()                           + "\""   +   ","
                + "\"car_type_id\":"              +   "\""   +     chosenCarType.getCarTypeID()                     + "\""
                + "}}";
    }

    @Override
    public void onFailure(IOException e) {
        e.printStackTrace();
        showErrorToast(getString(R.string.error_could_not_load_data));
    }

    @Override
    public void onResponse(Response response) {
        dialog.dismiss();
        String responseMessage = response.message();
        Log.d(TAG, "CreateAccountActivity: " + "response message - " + responseMessage);

        if (getString(R.string.server_response_created).equals(responseMessage)) {
            try {
                String res = response.body().string();
                Log.d(TAG, "CreateAccountActivity: " + "response body - " + res);
                JSONObject responseJSON = new JSONObject(res);
                saveUserData(responseJSON);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            if (isSuccessful) {
                Intent finishIntent = new Intent("finish_register_activity");
                LocalBroadcastManager.getInstance(CreateAccountActivity.this).sendBroadcast(finishIntent);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("startPage", "AllCarWashers");
                startActivity(intent);
                finish();
            }
        } else {
            showErrorToast(getString(R.string.error_could_not_create_user));
        }
    }

    private boolean userNameIsEntered() {
        return (userNameEditText.length() >= 3 && userNameEditText.length() <= 50);
    }

    private boolean passwordIsEntered() {
        return (passwordEditText.length() >= 6);
    }

    private boolean passwordIsConfirmed() {
        return (passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString()));
    }

    private boolean cityIsChosen() {
        return (cityEditText.length() != 0);
    }

    private boolean carTypeIsChosen() {
        return (carTypeEditText.length() != 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        userNameEditText.clearFocus();
        passwordEditText.clearFocus();
        confirmPasswordEditText.clearFocus();
        cityEditText.clearFocus();
        carTypeEditText.clearFocus();
    }

    private void showProgressDialog() {
        hideKeyboard();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Регистрация");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void saveUserData(JSONObject userObject) throws JSONException {
        StoreToDatabaseHelper helper = new StoreToDatabaseHelper(this);
        isSuccessful = helper.saveUserLogInData(userObject, passwordEditText.getText().toString());
    }

    private void showSnackErrorMessage(String message, View v) {
        Snackbar snackbar = Snackbar.make(v, message, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(CreateAccountActivity.this, android.R.color.holo_red_dark));
        snackbar.show();
    }

    private void showErrorToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
