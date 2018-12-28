package com.akilsw.waky.denti.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akilsw.waky.denti.MoiUtils;
import com.bumptech.glide.Glide;
import com.akilsw.waky.denti.Constants;
import com.akilsw.waky.denti.ImageViewerActivity;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Resource;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fred on 07/08/2017.
 */

public class ResourcesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Resource> mValues;
    Context mContext;
    private String VIEW_REF_RESOURCE = "VIEW_REF_RESOURCES";

    public ResourcesAdapter(Context context, ArrayList<Resource> list){
        mContext = context;
        mValues = list;
    }

    private ItemClickCallback itemClickCallback;

    public interface ItemClickCallback{
        void onItemLongClick(int p);
        void onItemClick(ArrayList<String> files);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback){
        this.itemClickCallback = itemClickCallback;
    }

    @Override
    public int getItemViewType(int position) {
        return mValues.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == Constants.RESOURCE_TYPE_REFERENCE){
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.resource_item_ref,parent,false);

            return new RefViewHolder(view);
        }
        else if (viewType == Constants.RESOURCE_TYPE_IMAGE
                || viewType == Constants.RESOURCE_TYPE_VIDEO
                || viewType == Constants.RESOURCE_TYPE_FILE ){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.resource_item_image,parent,false);
            return new ImageViewHolder(view);
        }
        else if (viewType == Constants.RESOURCE_TYPE_LINK){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.resource_item_link,parent,false);
            return new LinkViewHolder(view);
        }else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.resource_item_ref,parent,false);

            return new RefViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();


        if (viewType == Constants.RESOURCE_TYPE_REFERENCE){
            RefViewHolder vh = (RefViewHolder) holder;
            vh.mItem = mValues.get(position);
            int type = vh.mItem.getType();
            vh.mTitle.setText(vh.mItem.getTitle());

            String desc_text = vh.mItem.getDescription();
            if(desc_text.length() > 50){
                desc_text = vh.mItem.getDescription().substring(0, 50);
                desc_text += "...";
            }else {
                desc_text = "Click to enter description";
                vh.mDescription.setAlpha(0.5f);
            }

            vh.mDescription.setText(desc_text);
        }
        else if (viewType == Constants.RESOURCE_TYPE_IMAGE
                || viewType == Constants.RESOURCE_TYPE_VIDEO
                || viewType == Constants.RESOURCE_TYPE_FILE ){
            ImageViewHolder vh = (ImageViewHolder) holder;
            vh.mItem = mValues.get(position);
            int type = vh.mItem.getType();
            vh.mTitle.setText(vh.mItem.getTitle());

            if(viewType == Constants.RESOURCE_TYPE_VIDEO){
                if(vh.mItem.getYoutubeId() == null)
                    return;

                vh.mYoutubePlay.setVisibility(View.VISIBLE);
                vh.mDescription.setText(vh.mItem.getDescription());
                String yt_url = "https://i.ytimg.com/vi/"+ vh.mItem.getYoutubeId() +"/maxresdefault.jpg";
                Glide.with(mContext)
                        .load(yt_url)
                        .into(vh.mImage);
            }
            else{
                int count = vh.mItem.getFiles().size();
                if(count > 0){
                    if(viewType == Constants.RESOURCE_TYPE_IMAGE)
                        Glide.with(mContext)
                            .load(vh.mItem.getFiles().get(0))
                            .into(vh.mImage);
                    else{
                        vh.mYoutubePlayWrapper.setVisibility(View.GONE);
                        vh.mPdfIcon.setVisibility(View.VISIBLE);
//                        vh.mYoutubePlay.setVisibility(View.VISIBLE);
//                        ImageView im = vh.mYoutubePlay.findViewById(R.id.type_icon);
//                        Glide.with(mContext)
//                                .load(R.drawable.ic_resource_file)
//                                .into(im);
//                        vh.mImage.setBackgroundColor(Color.parseColor("#000000"));
                    }
                }

                String ext = viewType == Constants.RESOURCE_TYPE_IMAGE ? " Image" : " File";

                if (count == 1)
                    vh.mDescription.setText("1" + ext);
                else if(count > 1)
                    vh.mDescription.setText(count + ext + "s");
                else
                    vh.mDescription.setText("Type " + vh.mItem.getType());
            }
        }

        else if (viewType == Constants.RESOURCE_TYPE_LINK){
            LinkViewHolder vh = (LinkViewHolder) holder;
            vh.mItem = mValues.get(position);
            int type = vh.mItem.getType();
            vh.mTitle.setText(vh.mItem.getTitle());

            String[]meta =  vh.mItem.getMeta();

            String url = vh.mItem.getDescription();
            vh.mPath.setText(url);

            if(meta != null && (meta[0].length() > 0 || meta[1].length() > 0)){
                vh.mLinkMeta.setVisibility(View.VISIBLE);
                vh.mLinkTitle.setText(meta[0]);

                Glide.with(mContext)
                        .load(meta[1])
                        .into(vh.mLinkImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class RefViewHolder extends RecyclerView.ViewHolder{
        public final LinearLayout mView;

        @BindView(R.id.resource_title)
        TextView mTitle;

        @BindView(R.id.resource_description)
        TextView mDescription;

        public Resource mItem;

        public RefViewHolder(View itemView) {
            super(itemView);

            mView = (LinearLayout) itemView;
            ButterKnife.bind(this,itemView);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int type = mItem.getType();
                    Toast.makeText(mContext, "Item Clicked", Toast.LENGTH_SHORT).show();
                }
            });

            mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(itemClickCallback != null) {
                        itemClickCallback.onItemLongClick(getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private static void playYoutubeVideo(Context context, String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
        public final LinearLayout mView;

        @BindView(R.id.resource_title)
        TextView mTitle;

        @BindView(R.id.resource_description)
        TextView mDescription;

        @BindView(R.id.resource_image)
        ImageView mImage;

        @BindView(R.id.pdf_icon)
        ImageView mPdfIcon;

        @BindView(R.id.youtube_play)
        FrameLayout mYoutubePlay;

        @BindView(R.id.youtube_play_wrapper)
        FrameLayout mYoutubePlayWrapper;

        public Resource mItem;

        public ImageViewHolder(View itemView) {
            super(itemView);

            mView = (LinearLayout) itemView;
            ButterKnife.bind(this,itemView);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int type = mItem.getType();

                    if(type == Constants.RESOURCE_TYPE_VIDEO){
                        playYoutubeVideo(mContext, mItem.getYoutubeId());
                    }else if(type == Constants.RESOURCE_TYPE_IMAGE){
                        Intent intent = new Intent(mContext, ImageViewerActivity.class);
                        intent.putExtra("title", mItem.getTitle());
                        intent.putStringArrayListExtra("images", mItem.getFiles());
                        mContext.startActivity(intent);
                    }else{
                        if(itemClickCallback != null) {
                            itemClickCallback.onItemClick(mValues.get(getAdapterPosition()).getFiles());
                        }
                    }
                }
            });

            mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(itemClickCallback != null) {
                        itemClickCallback.onItemLongClick(getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public class LinkViewHolder extends RecyclerView.ViewHolder{
        public final LinearLayout mView;

        @BindView(R.id.resource_title)
        TextView mTitle;

        @BindView(R.id.resource_path)
        TextView mPath;

        @BindView(R.id.resource_link_meta)
        LinearLayout mLinkMeta;

        @BindView(R.id.resource_link_image)
        ImageView mLinkImage;

        @BindView(R.id.resource_link_title)
        TextView mLinkTitle;

        public Resource mItem;

        public LinkViewHolder(View itemView) {
            super(itemView);

            mView = (LinearLayout) itemView;
            ButterKnife.bind(this,itemView);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mItem.getDescription()));
                    mContext.startActivity(i);
                }
            });

            mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(itemClickCallback != null) {
                        itemClickCallback.onItemLongClick(getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}
