package com.revauc.revolutionbuy.ui.buy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.revauc.revolutionbuy.R;
import com.revauc.revolutionbuy.databinding.ActivitySellerOfferDetailBinding;
import com.revauc.revolutionbuy.network.BaseResponse;
import com.revauc.revolutionbuy.network.RequestController;
import com.revauc.revolutionbuy.network.request.auth.BuyerCompleteTransactionRequest;
import com.revauc.revolutionbuy.network.response.seller.SellerOfferDto;
import com.revauc.revolutionbuy.network.retrofit.AuthWebServices;
import com.revauc.revolutionbuy.network.retrofit.DefaultApiObserver;
import com.revauc.revolutionbuy.ui.BaseActivity;
import com.revauc.revolutionbuy.ui.buy.adapter.ProductImageAdapter;
import com.revauc.revolutionbuy.ui.sell.ReportItemActivity;
import com.revauc.revolutionbuy.util.Constants;
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class PurchasedItemDetailActivity extends BaseActivity implements View.OnClickListener {


    private ActivitySellerOfferDetailBinding mBinding;
    private SellerOfferDto mProductDetail;
    private static final int REQUEST_REPORT_ITEM=223;
    private final BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_seller_offer_detail);

        mProductDetail = getIntent().getParcelableExtra(Constants.EXTRA_PRODUCT_DETAIL);

        mBinding.textTitle.setText(mProductDetail.getBuyerProduct().getTitle());
        mBinding.textCategories.setText(mProductDetail.getBuyerProduct().getBuyerProductCategoriesString() + "");
        mBinding.textPriceOffered.setText((mProductDetail.getDescription().split("&&")[0])+" "+mProductDetail.getPrice());
        mBinding.textDescription.setText(mProductDetail.getDescription().split("&&")[1]);
        mBinding.textMobileNo.setText(mProductDetail.getUser().getMobile());

        if (mProductDetail.getSellerProductImages() != null && !mProductDetail.getSellerProductImages().isEmpty()) {
            if(mProductDetail.getSellerProductImages().size()>1)
            {
                mBinding.viewpagerImages.setAdapter(new ProductImageAdapter(this,mProductDetail.getSellerProductImages()));
                mBinding.indicatorImages.setViewPager(mBinding.viewpagerImages);
            }
            else
            {
                Picasso.with(this).load(mProductDetail.getSellerProductImages().get(0)
                        .getImageName()).placeholder(R.drawable.ic_placeholder_purchase_detail).into(mBinding.imageProduct);
            }
        } else {
            mBinding.imageProduct.setImageResource(R.drawable.ic_placeholder_purchase_detail);
        }

        if(mProductDetail.getUser()!=null)
        {
            mBinding.textSellerName.setText(mProductDetail.getUser().getName());
            mBinding.textSellerLocation.setText(mProductDetail.getUser().getCity().getName()+", "+mProductDetail.getUser().getCity().getState().getName());
            mBinding.textItemSold.setText(""+mProductDetail.getUser().getSoldCount());
            mBinding.textItemPurchased.setText(""+mProductDetail.getUser().getPurchasedCount());
        }

        mBinding.imageBack.setOnClickListener(this);
        mBinding.textUnlockContactDetails.setOnClickListener(this);
        mBinding.textPay.setOnClickListener(this);
        mBinding.textReportItem.setOnClickListener(this);
        mBinding.textMarkComplete.setOnClickListener(this);

//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this);
//        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mReciever, new IntentFilter(ItemPurchasedActivity.BROAD_OFFER_PURCHASED));
    }



    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.image_back:
                onBackPressed();
                break;
            case R.id.text_unlock_contact_details:
                mBinding.textUnlockContactDetails.setVisibility(View.GONE);
                mBinding.textMobile.setVisibility(View.VISIBLE);
                mBinding.textPay.setVisibility(View.VISIBLE);
                mBinding.textMarkComplete.setVisibility(View.VISIBLE);
                break;
            case R.id.text_mobile:
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mProductDetail.getUser().getMobile()));
                startActivity(intent);
                break;
            case R.id.text_pay:
//                Intent intent = new Intent(this,SellNowActivity.class);
//                intent.putExtra(Constants.EXTRA_CATEGORY,""+mProductDetail.getId());
//                startActivity(intent);
                showToast(getString(R.string.coming_soon));
                break;
            case R.id.text_mark_complete:
                markBuyerTransactionComplete();
                break;
            case R.id.text_report_item:
                Intent reoprtintent = new Intent(this,ReportItemActivity.class);
                reoprtintent.putExtra(Constants.EXTRA_PRODUCT_ID,mProductDetail.getId());
                startActivityForResult(reoprtintent,REQUEST_REPORT_ITEM);
                break;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_REPORT_ITEM)
        {
            if(resultCode==Activity.RESULT_OK)
            {
                showSnackBarFromBottom(getString(R.string.report_sent),mBinding.mainContainer,false);
            }
        }
    }



    private void markBuyerTransactionComplete() {
        showProgressBar();
        AuthWebServices apiService = RequestController.createRetrofitRequest(false);

        apiService.markBuyerTransactionComplete(new BuyerCompleteTransactionRequest("Paid in Cash",mProductDetail.getBuyerProductId(),mProductDetail.getId(),mProductDetail.getUser().getId())).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DefaultApiObserver<BaseResponse>(this) {

            @Override
            public void onResponse(BaseResponse response) {
                hideProgressBar();
                if (response.isSuccess()) {
                    startActivity(new Intent(PurchasedItemDetailActivity.this,ItemPurchasedActivity.class));
//                    showToast(response.getMessage());
                }
                else
                {
                    showSnackBarFromBottom(response.getMessage(),mBinding.mainContainer, true);
                }

            }

            @Override
            public void onError(Throwable call, BaseResponse baseResponse) {
                hideProgressBar();
                if (baseResponse != null) {
                    String errorMessage = baseResponse.getMessage();
                    showSnackBarFromBottom(errorMessage,mBinding.mainContainer, true);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReciever);
    }
}

