package com.atguigu.p2pinvest0828.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.p2pinvest0828.R;
import com.atguigu.p2pinvest0828.bean.Image;
import com.atguigu.p2pinvest0828.bean.Index;
import com.atguigu.p2pinvest0828.bean.Product;
import com.atguigu.p2pinvest0828.common.AppNetConfig;
import com.atguigu.p2pinvest0828.util.UIUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by shkstart on 2016/11/30 0030.
 */
public class HomeFragment extends Fragment {


    @Bind(R.id.iv_title_back)
    ImageView ivTitleBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_title_setting)
    ImageView ivTitleSetting;
    @Bind(R.id.banner)
    Banner banner;
    @Bind(R.id.tv_home_product)
    TextView tvHomeProduct;
    @Bind(R.id.tv_home_yearrate)
    TextView tvHomeYearrate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = UIUtils.getView(R.layout.fragment_home);//context实例：application
//        View view = View.inflate(getActivity(), R.layout.fragment_home, null);//context实例：activity
        ButterKnife.bind(this, view);

        //初始化title
        initTitle();

        //初始化数据
        initData();

        return view;
    }

    private Index index;

    private void initData() {
        index = new Index();
        AsyncHttpClient client = new AsyncHttpClient();
        //访问的url
        String url = AppNetConfig.INDEX;
        client.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String content) {//200：响应成功
                //解析json数据：GSON / FASTJSON
                JSONObject jsonObject = JSON.parseObject(content);
                //解析json对象数据
                String proInfo = jsonObject.getString("proInfo");
                Product product = JSON.parseObject(proInfo, Product.class);
                //解析json数组数据
                String imageArr = jsonObject.getString("imageArr");
                List<Image> images = jsonObject.parseArray(imageArr, Image.class);
                index.product = product;
                index.images = images;

                //更新页面数据
                tvHomeProduct.setText(product.name);
                tvHomeYearrate.setText(product.yearRate + "%");

                //设置banner样式
                banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
                //设置图片加载器
                banner.setImageLoader(new GlideImageLoader());
                //设置图片地址构成的集合
                ArrayList<String> imagesUrl = new ArrayList<String>(index.images.size());
//                for(int i = 0; i < imagesUrl.size(); i++) {//imagesUrl.size():0
                for (int i = 0; i < index.images.size(); i++) {//index.images.size():4
                    imagesUrl.add(index.images.get(i).IMAURL);
                }
                banner.setImages(imagesUrl);
                //设置banner动画效果
                banner.setBannerAnimation(Transformer.DepthPage);
                //设置标题集合（当banner样式有显示title时）
                String[] titles = new String[]{"分享砍学费","人脉总动员","想不到你是这样的app","购物节，爱不单行"};
                banner.setBannerTitles(Arrays.asList(titles));
                //设置自动轮播，默认为true
                banner.isAutoPlay(true);
                //设置轮播时间
                banner.setDelayTime(1500);
                //设置指示器位置（当banner模式中有指示器时）
                banner.setIndicatorGravity(BannerConfig.CENTER);
                //banner设置方法全部调用完毕时最后调用
                banner.start();
            }

            @Override
            public void onFailure(Throwable error, String content) {//响应失败
                Toast.makeText(UIUtils.getContext(), "联网获取数据失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            /**
             常用的图片加载库：
             Universal Image Loader：一个强大的图片加载库，包含各种各样的配置，最老牌，使用也最广泛。
             Picasso: Square出品，必属精品。和OkHttp搭配起来更配呦！
             Volley ImageLoader：Google官方出品，可惜不能加载本地图片~
             Fresco：Facebook出的，天生骄傲！不是一般的强大。
             Glide：Google推荐的图片加载库，专注于流畅的滚动。
             */


            //Picasso 加载图片简单用法
            Picasso.with(context).load((String) path).into(imageView);

        }
    }

    private void initTitle() {
        ivTitleBack.setVisibility(View.GONE);
        tvTitle.setText("首页");
        ivTitleSetting.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
