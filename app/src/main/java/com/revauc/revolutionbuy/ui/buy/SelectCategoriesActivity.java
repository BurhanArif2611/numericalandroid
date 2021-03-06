package com.revauc.revolutionbuy.ui.buy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.revauc.revolutionbuy.R;
import com.revauc.revolutionbuy.databinding.ActivityProfileBinding;
import com.revauc.revolutionbuy.databinding.ActivitySelectCategoriesBinding;
import com.revauc.revolutionbuy.listeners.OnCategorySelectListener;
import com.revauc.revolutionbuy.network.BaseResponse;
import com.revauc.revolutionbuy.network.RequestController;
import com.revauc.revolutionbuy.network.response.UserDto;
import com.revauc.revolutionbuy.network.response.buyer.BuyerProductCategoryDto;
import com.revauc.revolutionbuy.network.response.buyer.BuyerProductDto;
import com.revauc.revolutionbuy.network.response.buyer.CategoriesResponse;
import com.revauc.revolutionbuy.network.response.buyer.CategoryDto;
import com.revauc.revolutionbuy.network.response.profile.StateResponse;
import com.revauc.revolutionbuy.network.retrofit.AuthWebServices;
import com.revauc.revolutionbuy.network.retrofit.DefaultApiObserver;
import com.revauc.revolutionbuy.ui.BaseActivity;
import com.revauc.revolutionbuy.ui.auth.CreateProfileActivity;
import com.revauc.revolutionbuy.ui.buy.adapter.CategoriesAdapter;
import com.revauc.revolutionbuy.ui.buy.adapter.ItemListedActivity;
import com.revauc.revolutionbuy.util.Constants;
import com.revauc.revolutionbuy.util.PreferenceUtil;
import com.revauc.revolutionbuy.util.StringUtils;
import com.revauc.revolutionbuy.widget.SingleActionBottomSheetAlert;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 *  24/09/17.
 */

public class SelectCategoriesActivity extends BaseActivity implements View.OnClickListener, OnCategorySelectListener {


    private ActivitySelectCategoriesBinding mBinding;
    private List<CategoryDto> mCategories;
    private CategoriesAdapter mAdapter;
    private final BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private String mSelectedCategory="";
    private BuyerProductDto mProductDetail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_categories);
        mProductDetail = getIntent().getParcelableExtra(Constants.EXTRA_PRODUCT_DETAIL);
        mBinding.toolbarBuyer.tvToolbarGeneralLeft.setText(R.string.back);
        mBinding.toolbarBuyer.tvToolbarGeneralLeft.setVisibility(View.VISIBLE);
        mBinding.toolbarBuyer.tvToolbarGeneralLeft.setOnClickListener(this);
        mBinding.toolbarBuyer.txvToolbarGeneralCenter.setText(getString(mProductDetail==null?R.string.select_categories:R.string.edit_category));
        mBinding.toolbarBuyer.tvToolbarGeneralRight.setText(R.string.next);
        mBinding.toolbarBuyer.tvToolbarGeneralRight.setVisibility(View.VISIBLE);
        mBinding.toolbarBuyer.tvToolbarGeneralRight.setOnClickListener(this);
        mBinding.layoutBuyerFooter.textStepOne.setEnabled(true);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchCategories();
            }
        }, 250);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReciever, new IntentFilter(ItemListedActivity.BROAD_BUY_LISTED_COMPLETE));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReciever);
    }

    private void fetchCategories() {
        showProgressBar();
        AuthWebServices apiService = RequestController.createRetrofitRequest(false);

        apiService.getCategories().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DefaultApiObserver<CategoriesResponse>(this) {

            @Override
            public void onResponse(CategoriesResponse response) {
                hideProgressBar();
                if (response != null && response.isSuccess()) {
                    mCategories = response.getResult().getCategory();
                    mAdapter = new CategoriesAdapter(SelectCategoriesActivity.this, mCategories, SelectCategoriesActivity.this);
                    RecyclerView.LayoutManager lay = new LinearLayoutManager(SelectCategoriesActivity.this);
                    mBinding.recyclerViewCategories.setLayoutManager(lay);
                    mBinding.recyclerViewCategories.setAdapter(mAdapter);
                    checkForSelectedIds();
                } else {
                    showSnackBarFromBottom(response.getMessage(), mBinding.mainContainer, true);
                }

            }

            @Override
            public void onError(Throwable call, BaseResponse baseResponse) {
                if (baseResponse != null) {
                    hideProgressBar();
                    String errorMessage = baseResponse.getMessage();
                    showSnackBarFromBottom(errorMessage, mBinding.mainContainer, true);
                }
            }
        });
    }

    private void checkForSelectedIds() {
        if(mProductDetail!=null)
        {
            for(BuyerProductCategoryDto buyerProductCategoryDto:mProductDetail.getBuyerProductCategories())
            {
                for(CategoryDto categoryDto:mCategories)
                {
                    if(buyerProductCategoryDto.getCategory().getId()==categoryDto.getId())
                    {
                        categoryDto.setSelected(true);
                        break;
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_general_left:
                onBackPressed();

                break;
            case R.id.tv_toolbar_general_right:
                if(getSelectedCount()>0)
                {
                    Intent intent = new Intent(SelectCategoriesActivity.this, AddTitleActivity.class);
                    intent.putExtra(Constants.EXTRA_CATEGORY,mSelectedCategory.substring(0,mSelectedCategory.length()-1));
                    intent.putExtra(Constants.EXTRA_PRODUCT_DETAIL,mProductDetail);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                }
                else
                {
                    SingleActionBottomSheetAlert.getInstance(SelectCategoriesActivity.this,getString(R.string.select_at_least_one_category),getString(R.string.got_it)).show();
                }

                break;
        }

    }

    @Override
    public void onCategorySelected(int position) {
        if (mCategories.get(position).isSelected()) {
            mCategories.get(position).setSelected(false);
            mAdapter.notifyItemChanged(position);
        } else {
            if (getSelectedCount()<5) {
                mCategories.get(position).setSelected(true);
                mAdapter.notifyItemChanged(position);
            } else {
                SingleActionBottomSheetAlert.getInstance(SelectCategoriesActivity.this,getString(R.string.five_at_max),getString(R.string.got_it)).show();
            }
        }

    }

    private int getSelectedCount() {
        int selectedCount = 0;
        mSelectedCategory = "";
        for (CategoryDto categoryDto : mCategories) {
            if (categoryDto.isSelected()) {
                mSelectedCategory = mSelectedCategory + categoryDto.getId()+",";
                selectedCount++;
            }
        }
       return selectedCount;
    }
}

