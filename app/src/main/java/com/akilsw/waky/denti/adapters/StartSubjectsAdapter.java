package com.akilsw.waky.denti.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Subject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fred on 07/08/2017.
 */

public class StartSubjectsAdapter extends RecyclerView.Adapter<StartSubjectsAdapter.ViewHolder> {

    ArrayList<Subject> mValues;
    Context mContext;
    private int lastAnimatedPosition = -1;

    public StartSubjectsAdapter(Context context, ArrayList<Subject> list){
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.start_subject_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mTitle.setText(holder.mItem.getName());

        animateView(holder.mView,position);

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public final LinearLayout mView;

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.deleteSubject)
        ImageView mRemover;

        public Subject mItem;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = (LinearLayout)itemView;
            ButterKnife.bind(this,itemView);

            mRemover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickCallback.onItemRemove(getAdapterPosition());
                }
            });
        }
    }

    private void animateView(View view, int position){
        if(position < lastAnimatedPosition){
            lastAnimatedPosition = position;
            view.setTranslationY(300);
            view.animate()
                    .translationY(0.0f)
                    .setInterpolator(new DecelerateInterpolator(3.0f))
                    .setDuration(700)
                    .start();
        }
    }
}
