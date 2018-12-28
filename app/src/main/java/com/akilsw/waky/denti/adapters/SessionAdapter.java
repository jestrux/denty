package com.akilsw.waky.denti.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akilsw.waky.denti.Constants;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Session;
import com.akilsw.waky.denti.ui.ScheduleFragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fred on 07/08/2017.
 */

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    ArrayList<Session> mValues;
    Context mContext;
    int mType;

    public SessionAdapter(Context context, ArrayList<Session> list, int type){
        mContext = context;
        mValues = list;
        mType = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        int type = holder.mItem.getType();

        if(type == Constants.SESSION_TYPE_LECTURE)
            holder.mBg.setBackgroundColor(Color.parseColor("#00BCD4"));
        if(type == Constants.SESSION_TYPE_TUTORIAL)
            holder.mBg.setBackgroundColor(Color.parseColor("#FFC107"));

        holder.mTitle.setText(holder.mItem.getSubjectName());
        holder.mVenue.setText(holder.mItem.getVenue());
        holder.mFullTime.setText(holder.mItem.getFullTimeStr());
        holder.mDay.setText(holder.mItem.getShortDayName());

//        holder.mFullTime.setText(holder.mItem.getDayName() + "  " + holder.mItem.getFullTimeStr());
        if((mType == ScheduleFragment.SCHEDULE_TYPE_FULL)){
            if(position == 0 || (position != 0 && (mValues.get(position - 1).getDay() != holder.mItem.getDay()))
            )
                holder.mDay.setVisibility(View.VISIBLE);
            else
                holder.mDay.setVisibility(View.INVISIBLE);
        }
        else
            holder.mDay.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public final LinearLayout mView;

        @BindView(R.id.session_day)
        TextView mDay;

        @BindView(R.id.session_background)
        LinearLayout mBg;

        @BindView(R.id.session_title)
        TextView mTitle;

        @BindView(R.id.session_venue)
        TextView mVenue;

        @BindView(R.id.session_time)
        TextView mFullTime;

        public Session mItem;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = (LinearLayout)itemView;
            ButterKnife.bind(this,itemView);
        }
    }

    public void updateItems(ArrayList<Session> sessions) {
        this.mValues = sessions;
        notifyDataSetChanged();
    }
}
