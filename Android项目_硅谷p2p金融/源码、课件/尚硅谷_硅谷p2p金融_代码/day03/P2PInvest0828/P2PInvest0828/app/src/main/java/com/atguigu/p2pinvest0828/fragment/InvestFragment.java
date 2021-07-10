package com.atguigu.p2pinvest0828.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.atguigu.p2pinvest0828.R;
import com.atguigu.p2pinvest0828.common.BaseFragment;
import com.loopj.android.http.RequestParams;

import butterknife.Bind;

/**
 * Created by shkstart on 2016/11/30 0030.
 */
public class InvestFragment extends BaseFragment {

    @Bind(R.id.iv_title_back)
    ImageView ivTitleBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_title_setting)
    ImageView ivTitleSetting;



    @Override
    protected RequestParams getParams() {
        return null;
    }

    @Override
    protected String getUrl() {
        return null;
    }

    @Override
    protected void initData(String content) {

    }

    protected void initTitle() {
        ivTitleBack.setVisibility(View.GONE);
        tvTitle.setText("投资");
        ivTitleSetting.setVisibility(View.GONE);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_invest;
    }

}
