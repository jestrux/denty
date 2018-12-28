package com.akilsw.waky.denti;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoView;

public class ImageViewerActivity extends AppCompatActivity {
    @BindView(R.id.wrapper)
    FrameLayout wrapper;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    ArrayList<String> imageList = new ArrayList<String>();
    CustomPagerAdapter adapter;

    boolean isFullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);

        Intent i = getIntent();
        String title = i.getStringExtra("title");
        if(title != null)
            toolbar.setTitle(title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ArrayList<String> list = i.getStringArrayListExtra("images");
        if(list != null && list.size() > 0){
            imageList.addAll(list);
            adapter = new CustomPagerAdapter(getBaseContext(), imageList);
            viewPager.setAdapter(adapter);

            Log.d("WOURA", "Found image: " + imageList.size());
        }else{
            Log.d("WOURA", "Image list is null.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class CustomPagerAdapter extends PagerAdapter{
        private Context mContext;
        private ArrayList<String> paths;
        private LayoutInflater inflater;

        CustomPagerAdapter(Context c, ArrayList<String> paths){
            this.mContext = c;
            this.paths = paths;

            Log.d("WOURA", "Adapter class called with: " + paths.size());
        }

        @Override
        public int getCount() {
            return this.paths.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (ImageView) object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View itemView = inflater.inflate(R.layout.image_item, container, false);
//            ImageView iv = (ImageView) itemView.findViewById(R.id.item_image);

            PhotoView iv = new PhotoView(mContext);
            iv.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setBackgroundColor(Color.parseColor("#000000"));

            Glide.with(mContext)
                    .load(paths.get(position))
                    .into(iv);

            iv.setClickable(true);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("WOURA", "Swiper clicked");

                    if(isFullScreen){
                        ObjectAnimator animation = ObjectAnimator.ofFloat(toolbar, "translationY", 0);
                        animation.setDuration(150);
                        animation.start();
                    }
                    else{
                        ObjectAnimator animation = ObjectAnimator
                                .ofFloat(toolbar, "translationY", -toolbar.getHeight());
                        animation.setDuration(250);
                        animation.start();
                    }

                    isFullScreen = !isFullScreen;
                    setFullscreen(isFullScreen);
                }
            });

            container.addView(iv);
            return iv;
        }

        private void setFullscreen(boolean fullscreen)
        {
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            if (fullscreen)
            {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            }
            else
            {
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            }
            getWindow().setAttributes(attrs);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }
}
