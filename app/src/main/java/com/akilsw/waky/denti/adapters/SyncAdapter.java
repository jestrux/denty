package com.akilsw.waky.denti.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.akilsw.waky.denti.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.ChatMessage;

/**
 * Created by fred on 07/08/2017.
 */

public class SyncAdapter extends RecyclerView.Adapter<SyncAdapter.ViewHolder>{

    ArrayList<ChatMessage> mValues;
    Context mContext;
    private int lastAnimatedPosition = -1;

    public SyncAdapter(Context context, ArrayList<ChatMessage> list){
        mContext = context;
        mValues = list;
    }

    private ItemClickCallback itemClickCallback;

    public interface ItemClickCallback{
        void onItemComplete(int p);
//        void onItemRemove(int p);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback){
        this.itemClickCallback = itemClickCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mSender.setText(holder.mItem.getSender());

        if(holder.mItem.getBody() != null){
            holder.mBody.setVisibility(View.VISIBLE);
            holder.mBody.setText(holder.mItem.getBody());
        }else{
            holder.mBody.setVisibility(View.GONE);
        }

        if(holder.mItem.getImg() != null){
            holder.mImage.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(holder.mItem.getImg()).into(holder.mImage);
        }else{
            holder.mImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final LinearLayout mView;

        @BindView(R.id.message_body)
        TextView mBody;

        @BindView(R.id.message_image)
        ImageView mImage;

        @BindView(R.id.message_sender)
        TextView mSender;

        public ChatMessage mItem;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = (LinearLayout)itemView;
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void onClick(View view) {
            itemClickCallback.onItemComplete(getAdapterPosition());
        }
    }
}

