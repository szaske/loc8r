package com.loc8r.seattle.activities;

import com.loc8r.seattle.R;

public class SignUpActivity extends SignBaseActivity
{

    @Override
    protected int getContentView()
    {
        return R.layout.activity_sign_up;
    }

    @Override
    protected void onSignUp()
    {
        // TODO: 12/03/17 sign up MongoDB
    }

    @Override
    protected void onSignIn()
    {
        startActivity(SignInActivity.newIntent(this));
        finish();
    }
}
