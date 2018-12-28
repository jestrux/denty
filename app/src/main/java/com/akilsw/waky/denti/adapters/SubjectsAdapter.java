package com.akilsw.waky.denti.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Subject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fred on 07/08/2017.
 */

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.ViewHolder> {

    ArrayList<Subject> mValues;
    Context mContext;
    private int lastAnimatedPosition = -1;

    public SubjectsAdapter(Context context, ArrayList<Subject> list){
        mContext = context;
        mValues = list;
    }

    private ItemClickCallback itemClickCallback;

    public interface ItemClickCallback{
        void onItemClick(int p);
//        void onItemRemove(int p);
    }

    public void setItemClickCallback(final ItemClickCallback itemClickCallback){
        this.itemClickCallback = itemClickCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subject_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mTitle.setText(holder.mItem.getName());

        if(position == mValues.size() - 1)
            holder.mSeparator.setVisibility(View.GONE);

        animateView(holder.mView,position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final FrameLayout mView;
        public RelativeLayout viewBackground;
        public LinearLayout viewForeground;

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.deleteSubject)
        ImageView mRemover;

        @BindView(R.id.subject_separator)
        View mSeparator;

        public Subject mItem;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = (FrameLayout)itemView;
            ButterKnife.bind(this,itemView);

            viewBackground = itemView.findViewById(R.id.subject_background);
            viewForeground = itemView.findViewById(R.id.subject_foreground);

//            mView.setOnClickListener(this);
//            mRemover.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickCallback.onItemClick(getAdapterPosition());
        }
    }

    //Animate view after its attached to window
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

    public void filterList(ArrayList<Subject> filteredTunes) {
        this.mValues = filteredTunes;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mValues.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(Subject subject, int position) {
        mValues.add(position, subject);
        // notify item added by position
        notifyItemInserted(position);
    }
}
