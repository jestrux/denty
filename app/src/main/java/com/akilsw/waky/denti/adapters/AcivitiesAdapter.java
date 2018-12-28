package com.akilsw.waky.denti.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Activity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fred on 07/08/2017.
 */

public class AcivitiesAdapter extends RecyclerView.Adapter<AcivitiesAdapter.ViewHolder> {

    ArrayList<Activity> mValues;
    Context mContext;
    private int lastAnimatedPosition = -1;

    public AcivitiesAdapter(Context context, ArrayList<Activity> list){
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
                .inflate(R.layout.activity_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mTitle.setText(holder.mItem.getTitle());

        if(holder.mItem.getDeadline() != -1)
            holder.mDeadline.setText(holder.mItem.getDeadlineStr());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final FrameLayout mView;
        public RelativeLayout viewDeleter, viewCompleter;
        public LinearLayout viewForeground;

        @BindView(R.id.activity_title)
        TextView mTitle;

        @BindView(R.id.completeButton)
        ImageView mCompleter;

        @BindView(R.id.activity_deadline)
        TextView mDeadline;

        public Activity mItem;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = (FrameLayout)itemView;
            ButterKnife.bind(this,itemView);

            viewForeground = itemView.findViewById(R.id.activity_foreground);
            viewCompleter = itemView.findViewById(R.id.activity_completer);

            mCompleter.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickCallback.onItemComplete(getAdapterPosition());
        }
    }

    public void removeItem(int position) {
        mValues.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(Activity activity, int position) {
        mValues.add(position, activity);
        // notify item added by position
        notifyItemInserted(position);
    }
}
