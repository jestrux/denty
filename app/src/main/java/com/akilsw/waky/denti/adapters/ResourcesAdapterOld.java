package com.akilsw.waky.denti.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akilsw.waky.denti.Constants;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Resource;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;

/**
 * Created by fred on 07/08/2017.
 */

public class ResourcesAdapterOld extends RecyclerView.Adapter<ResourcesAdapterOld.ViewHolder> {

    ArrayList<Resource> mValues;
    Context mContext;
    private String VIEW_REF_RESOURCE = "VIEW_REF_RESOURCES";

    public ResourcesAdapterOld(Context context, ArrayList<Resource> list){
        mContext = context;
        mValues = list;
    }

    private ItemClickCallback itemClickCallback;

    public interface ItemClickCallback{
        void onItemRemove(int p);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback){
        this.itemClickCallback = itemClickCallback;
    }

    @Override
    public int getItemViewType(int position) {
        return mValues.get(position).getType();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == Constants.RESOURCE_TYPE_REFERENCE)
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.resource_item_ref,parent,false);
        else if (viewType == Constants.RESOURCE_TYPE_IMAGE || viewType == Constants.RESOURCE_TYPE_VIDEO)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.resource_item_image,parent,false);
        else if (viewType == Constants.RESOURCE_TYPE_FILE || viewType == Constants.RESOURCE_TYPE_LINK)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.resource_item_file,parent,false);
        else
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.resource_item_ref,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        if (viewType == Constants.RESOURCE_TYPE_REFERENCE){

        }
        else if (viewType == Constants.RESOURCE_TYPE_IMAGE || viewType == Constants.RESOURCE_TYPE_VIDEO){

        }

        else if (viewType == Constants.RESOURCE_TYPE_FILE || viewType == Constants.RESOURCE_TYPE_LINK){

        }

        holder.mItem = mValues.get(position);
        int type = holder.mItem.getType();

        int[] type_icons = {R.drawable.ic_resource_ref, R.drawable.ic_resource_image, R.drawable.ic_resource_file, R.drawable.ic_resource_link, R.drawable.ic_resource_yt};

        if(position == mValues.size() - 1)
            holder.mRefSeparator.setVisibility(View.GONE);

        GradientDrawable tvBackground = (GradientDrawable) holder.mTypeIcon.getBackground();
        getIconColor(holder.mItem.getType());

        tvBackground.setColor(getIconColor(type));

        holder.mTitle.setText(holder.mItem.getTitle());
        holder.mTypeIcon.setImageResource(type_icons[type]);
    }

    private int getIconColor(int type) {
        int c = Color.parseColor("#ffffff");

        switch (type){
            case Constants.RESOURCE_TYPE_REFERENCE:
                c = Color.parseColor("#33bddf");
                break;
            case Constants.RESOURCE_TYPE_IMAGE:
                c = Color.parseColor("#ffaa39");
                break;
            case Constants.RESOURCE_TYPE_FILE:
                c = Color.parseColor("#e23ce2");
                break;
            case Constants.RESOURCE_TYPE_LINK:
                c = Color.parseColor("#000000");
            case Constants.RESOURCE_TYPE_VIDEO:
                c = Color.parseColor("#ff2c2c");
        }

        return c;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final FrameLayout mView;
        public RelativeLayout viewBackground;
        public LinearLayout viewForeground;

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.type_icon)
        ImageView mTypeIcon;

        @BindView(R.id.ref_separator)
        View mRefSeparator;

        public Resource mItem;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = (FrameLayout) itemView;
            ButterKnife.bind(this,itemView);

            viewBackground = itemView.findViewById(R.id.reference_background);
            viewForeground = itemView.findViewById(R.id.reference_foreground);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int type = mItem.getType();
                    String title = mItem.getTitle();
                    String content = mItem.getDescription();
                    Log.d("WOURA", "Content: " + content);

                    switch (type){
                        case Constants.RESOURCE_TYPE_REFERENCE : {
                            SimpleDialog.build()
                                    .title(title)
                                    .msg(content)
                                    .show((FragmentActivity) mContext, VIEW_REF_RESOURCE);
                        }
                        break;
                        case Constants.RESOURCE_TYPE_VIDEO :
                            playYoutubeVideo(mContext, mItem.getYoutubeId());
                            break;
                        case Constants.RESOURCE_TYPE_LINK:{
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(content));
                            mContext.startActivity(i);
                        }
                        break;
                        case Constants.RESOURCE_TYPE_IMAGE : {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse("file://" + content), "image/*");
//                            intent.setData(Uri.parse("file://" + content));
                            mContext.startActivity(intent);
                        }
                        break;
                        case Constants.RESOURCE_TYPE_FILE : {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("file://" + content));
                            mContext.startActivity(intent);
                        }
                    }
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

    public void removeItem(int position) {
        mValues.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(Resource resource, int position) {
        mValues.add(position, resource);
        // notify item added by position
        notifyItemInserted(position);
    }
}
