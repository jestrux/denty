package com.akilsw.waky.denti.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.akilsw.waky.denti.MoiUtils;
import com.akilsw.waky.denti.R;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Waky on 12/29/2017.
 */

public class LinkPreviewFragment extends Fragment {
    private static final String ARG_TYPE = "page_type";
    public static final int PAGE_TYPE_OTHER = 0;
    public static final int PAGE_TYPE_YOUTUBE = 1;
    String entered_url;

    private static final String ARG_LINK = "page_link";

    @BindView(R.id.resource_title)
    TextView mTitle;

    @BindView(R.id.resource_description)
    EditText mLinkInput;

    @BindView(R.id.resource_image)
    ImageView mImage;

    @BindView(R.id.youtube_play)
    FrameLayout mYoutubePlay;

    private int page_type;


    private LinkChangedListener linkChangedListener;
    public interface LinkChangedListener{
        void onLinkChanged(String link);
        void onMetaChanged(String title, String image);
        void onSwitchToYoutube();
    }
    public void setLinkChangedListener(final LinkChangedListener linkChangedListener){
        this.linkChangedListener = linkChangedListener;
    }

    long delay = 1000; // 1 seconds after user stops typing
    long last_text_edit = 0;
    Handler handler = new Handler();

    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            Log.d("WOURA", "Input checker");
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                Log.d("WOURA", "Passed! fetching: " + entered_url);
                String yt_id = MoiUtils.extractYTId(entered_url);
                String yt_url = "https://i.ytimg.com/vi/"+ yt_id +"/maxresdefault.jpg";

                if(page_type == PAGE_TYPE_YOUTUBE){
                    Glide.with(getContext())
                            .load(yt_url)
                            .into(mImage);
                }
                else if(yt_id != null){
                    mYoutubePlay.setVisibility(View.VISIBLE);
                    mTitle.setText("Youtube video url");
                    mLinkInput.setHint("url goes here");
                    mImage.setBackgroundColor(Color.parseColor("#000000"));
                    linkChangedListener.onSwitchToYoutube();

                    Glide.with(getContext())
                            .load(yt_url)
                            .into(mImage);
                }else{
                    setPreview(entered_url);
                }
            }else{
                Log.d("WOURA", "Failed!");
            }
        }
    };

    public LinkPreviewFragment() {
    }

    public static LinkPreviewFragment newInstance(int page_type) {
        LinkPreviewFragment fragment = new LinkPreviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, page_type);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        page_type = getArguments().getInt(ARG_TYPE);
        View rootView = inflater.inflate(R.layout.fragment_link_preview, container, false);
        ButterKnife.bind(this,rootView);

        if (page_type == PAGE_TYPE_YOUTUBE){
            mYoutubePlay.setVisibility(View.VISIBLE);
            mTitle.setText("Youtube video url");
            mLinkInput.setHint("url goes here");
            mImage.setBackgroundColor(Color.parseColor("#000000"));
        }

        setLinkChangedListener((LinkChangedListener) getActivity());

        mLinkInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                entered_url = editable.toString();
                linkChangedListener.onLinkChanged(entered_url);

                last_text_edit = System.currentTimeMillis();
                handler.postDelayed(input_finish_checker, delay);
            }
        });

        return rootView;
    }

    private void setPreview(String url){
        TextCrawler textCrawler = new TextCrawler();

        LinkPreviewCallback linkPreviewCallback = new LinkPreviewCallback() {
            @Override
            public void onPre() {
                // Any work that needs to be done before generating the preview. Usually inflate
                // your custom preview layout here.
            }

            @Override
            public void onPos(SourceContent sourceContent, boolean b) {
                String title = sourceContent.getTitle();
                List<String> images = sourceContent.getImages();

                Log.d("WOURA", "URL RESULTS: " + title);

                if((title != null && title.length() > 0) && (images != null && images.size() > 0)){
                    linkChangedListener.onMetaChanged(title, images.get(0));
                }

                if(title != null && title.length() > 0){
                    Log.d("WOURA", "TITLE: " + title);
                    mTitle.setText(title);
                }
                if(images != null && images.size() > 0){
                    Log.d("WOURA", "IMAGE: " + sourceContent.getImages().get(0));

                    Glide.with(getContext())
                            .load(images.get(0))
                            .into(mImage);
                }
            }
        };

        textCrawler.makePreview( linkPreviewCallback, url);
    }
}
