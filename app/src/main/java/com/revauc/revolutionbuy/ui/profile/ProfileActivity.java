package com.revauc.revolutionbuy.ui.profile;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.revauc.revolutionbuy.R;
import com.revauc.revolutionbuy.databinding.ActivityForgotPasswordBinding;
import com.revauc.revolutionbuy.databinding.ActivityProfileBinding;
import com.revauc.revolutionbuy.network.BaseResponse;
import com.revauc.revolutionbuy.network.RequestController;
import com.revauc.revolutionbuy.network.request.auth.ForgotPasswordRequest;
import com.revauc.revolutionbuy.network.response.UserDto;
import com.revauc.revolutionbuy.network.retrofit.AuthWebServices;
import com.revauc.revolutionbuy.network.retrofit.DefaultApiObserver;
import com.revauc.revolutionbuy.ui.BaseActivity;
import com.revauc.revolutionbuy.ui.auth.CreateProfileActivity;
import com.revauc.revolutionbuy.ui.auth.ForgotPasswordActivity;
import com.revauc.revolutionbuy.ui.auth.ForgotPasswordConfirmActivity;
import com.revauc.revolutionbuy.util.Constants;
import com.revauc.revolutionbuy.util.PreferenceUtil;
import com.revauc.revolutionbuy.util.StringUtils;
import com.revauc.revolutionbuy.util.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by nishant on 24/09/17.
 */

public class ProfileActivity extends BaseActivity implements View.OnClickListener {


    private ActivityProfileBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        mBinding.imageBack.setOnClickListener(this);
        mBinding.textEdit.setOnClickListener(this);

        UserDto userDto = PreferenceUtil.getUserProfile();
        //setting values
        mBinding.textName.setText(userDto.getName());
        mBinding.textAge.setText(userDto.getAge()+" Years Old");
        mBinding.textLocation.setText(userDto.getCity().getName()+", "+userDto.getCity().getState().getName()+", "+userDto.getCity().getName());

        if(!StringUtils.isNullOrEmpty(userDto.getImageName()))
        {
            showProgressBar();
            Picasso.with(ProfileActivity.this).load(userDto.getImageName()).into(mBinding.imageProfile,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            hideProgressBar();

                        }

                        @Override
                        public void onError() {
                            hideProgressBar();
                        }
                    });

        }

    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.image_back:
                onBackPressed();

                break;
            case R.id.text_edit:
                Intent intent = new Intent(ProfileActivity.this, CreateProfileActivity.class);
                intent.putExtra(Constants.EXTRA_FROM_SETTINGS,true);
                startActivity(intent);
                break;
        }

    }

}

