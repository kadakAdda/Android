package com.example.kadakadda;

import android.content.Context;
import android.util.AttributeSet;

import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;

public class SpecialFbLoginButton extends LoginButton {

    LoginListener listener;

    public SpecialFbLoginButton(Context context) {
        super(context);
        listener = (LoginListener) getNewLoginListener();
    }

    public SpecialFbLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        listener = (LoginListener) getNewLoginListener();
    }

    public SpecialFbLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        listener = (LoginListener) getNewLoginListener();
    }

    public LoginManager getTheLoginManager(){
        return listener.getLoginManager();
    }

    public class LoginListener extends LoginClickListener{
        @Override
        protected LoginManager getLoginManager() {
            return super.getLoginManager();
        }
    }

    public LoginListener getNewLoginListener(){
        return new LoginListener();
    }
}
