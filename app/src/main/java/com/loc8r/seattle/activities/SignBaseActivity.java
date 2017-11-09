package com.loc8r.seattle.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.QueryListener;
import com.loc8r.seattle.mongodb.MongoDBManager;
import com.loc8r.seattle.utils.ProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 *  Abtract Base class for Sign_In and Sign_Up
 */
public abstract class SignBaseActivity extends FontsActivity
{
    private static final String LOG_TAG = SignBaseActivity.class.getSimpleName();
    private CallbackManager mCallbackManager;

    private Dialog mLoginProgressDialog;
    private AccessTokenTracker mAccessTokenTracker;

    private EditText mEmail;
    private EditText mPassword;

    private String mUsersFirstName;
    private String mUsersLastName;
    private String mUsersID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());

        //transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        findViewById(R.id.sign_in).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //sign in
                Log.d(LOG_TAG, "onClick: sign in");
                onSignIn();
            }
        });

        findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //sign up
                Log.d(LOG_TAG, "onClick: sign up");
                onSignUp();
            }
        });

        findViewById(R.id.skip).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //skip
                Log.d(LOG_TAG, "onClick: skip");
                anonymousLogin();
            }
        });

        //user is already logged in, continue to MainActivity
        if (MongoDBManager.getInstance(getApplicationContext()).isConnected())
        {
            //fetchProfile();
            login();
        }


        //Facebook login
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                Log.d(LOG_TAG, "Facebook login onSuccess: ");
                showLoginProgressDialog();

                //we are logged in to Facebook SDK, login with the same token to MongoDB
                MongoDBManager.getInstance(getApplicationContext()).doFacebookAuthentication(loginResult.getAccessToken().getToken(), new QueryListener<Void>()
                {
                    @Override
                    public void onSuccess(Void result)
                    {
                        Log.d(LOG_TAG, "onSuccess: login with facebook successful");
                        dismissLoginProgressBar();

                        //User is logged in to MongoDB via Facebook
                        login();
                    }

                    @Override
                    public void onError(Exception e)
                    {
                        Log.d(LOG_TAG, "onError: ");
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismissLoginProgressBar();
                    }
                });
            }

            @Override
            public void onCancel()
            {
                Log.d(LOG_TAG, "Facebook login onCancel: ");
                dismissLoginProgressBar();
            }

            @Override
            public void onError(FacebookException error)
            {
                Log.e(LOG_TAG, "Facebook login onError: ", error);
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                dismissLoginProgressBar();
            }
        });

        // Click listener for Facebook Button
        findViewById(R.id.btn_fb_login).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showLoginProgressDialog();
                loginToMyFbApp();
            }
        });

    } // End of OnCreate

    private void facebookLogin()
    {
        // TODO determine what exact permissions I need for Facebook
        LoginManager.getInstance().logInWithReadPermissions(SignBaseActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
    }

    private void loginToMyFbApp()
    {
        if (AccessToken.getCurrentAccessToken() != null)
        {
            mAccessTokenTracker = new AccessTokenTracker()
            {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
                {
                    mAccessTokenTracker.stopTracking();
                    if (currentAccessToken == null)
                    {
                        //(the user has revoked your permissions -
                        //by going to his settings and deleted your app)
                        //do the simple login to FaceBook
                        //case 1
                        facebookLogin();
                    }
                    else
                    {
                        //you've got the new access token now.
                        //AccessToken.getToken() could be same for both
                        //parameters but you should only use "currentAccessToken"
                        //case 2
                        fetchProfile();
                    }
                }
            };
            mAccessTokenTracker.startTracking();
            AccessToken.refreshCurrentAccessTokenAsync();
        }
        else
        {
            //do the simple login to FaceBook
            //case 1
            facebookLogin();
        }
    }

    // TODO Determine if FetchProfile is even needed.
    private void fetchProfile()
    {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback()
                {
                    @Override
                    public void onCompleted(JSONObject profileData, GraphResponse response)
                    {
                        Log.v("fetched info", profileData.toString());
                        try{
                            mUsersFirstName = profileData.getString("first_name");
                            mUsersLastName = profileData.getString("last_name");
                            mUsersID = profileData.getString("id");
                        } catch(JSONException e){
                            Log.e(LOG_TAG, "JSONException: ", e);
                        }

                        facebookLogin();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name"); //write the fields you need
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onPause()
    {
        dismissLoginProgressBar();
        super.onPause();
    }

    private void anonymousLogin()
    {
        showLoginProgressDialog();

        //login anonymously to MongoDB
        MongoDBManager.getInstance(getApplicationContext()).doAnonymousAuthentication(new QueryListener<Void>()
        {
            @Override
            public void onSuccess(Void result)
            {
                dismissLoginProgressBar();

                //user logged in anonymously
                login();
            }

            @Override
            public void onError(Exception e)
            {
                dismissLoginProgressBar();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login()
    {
        //go to MainActivity
        Log.d(LOG_TAG, "login: logging in with user: " + MongoDBManager.getInstance(getApplicationContext()).getUserId());

        Intent intent = new Intent(SignBaseActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoginProgressDialog()
    {
        if (mLoginProgressDialog != null)
        {
            mLoginProgressDialog.dismiss();
            mLoginProgressDialog = null;
        }

        mLoginProgressDialog = ProgressDialog.getDialog(this, false);
        mLoginProgressDialog.show();
    }

    private void dismissLoginProgressBar()
    {
        if (mLoginProgressDialog != null)
        {
            mLoginProgressDialog.dismiss();
            mLoginProgressDialog = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    protected abstract
    @LayoutRes
    int getContentView();

    protected abstract void onSignUp();

    protected abstract void onSignIn();

}