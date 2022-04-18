package com.yalantis.ucrop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.model.AspectRatio;

import java.util.ArrayList;

public class RatioAdapter extends RecyclerView.Adapter<RatioAdapter.ViewHolder> {
    private ArrayList<AspectRatio> aspectRatioList;
    private OnClickRatio onClickRatio;
    private Context context;
    private int id = 0;

    @SuppressLint("NotifyDataSetChanged")
    public void setId(int id) {
        this.id = id;
        notifyDataSetChanged();
    }

    public RatioAdapter(ArrayList<AspectRatio> aspectRatioList, Context context, OnClickRatio onClickRatio) {
        this.onClickRatio = onClickRatio;
        this.context = context;
        this.aspectRatioList = aspectRatioList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ucrop_aspect_ratio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
//        final UCrop.Options options = new UCrop.Options();

        holder.tvRatio.setText(aspectRatioList.get(holder.getAdapterPosition()).getAspectRatioTitle());
        holder.ivRatio.setImageResource(aspectRatioList.get(holder.getAdapterPosition()).getImage());
        holder.cslRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition() == 0) {
//                    UCrop.Options options = new UCrop.Options();
//                    options.setFreeStyleCropEnabled(true);
                    onClickRatio.onChangeRatio(1.0f, holder.getAdapterPosition());
                }
                else onClickRatio.onChangeRatio(aspectRatioList.get(holder.getAdapterPosition()).getAspectRatioX() / aspectRatioList.get(holder.getAdapterPosition()).getAspectRatioY(), holder.getAdapterPosition());
                if (id >= 0) notifyItemChanged(id);
                if (id != holder.getAdapterPosition()) {
                    id = holder.getAdapterPosition();
                    notifyItemChanged(id);
                }
            }
        });
        if (id == holder.getAdapterPosition()) {
            holder.tvRatio.setTextColor(context.getResources().getColor(R.color.ucrop_color_light_blue));
            holder.ivRatio.setImageResource(aspectRatioList.get(holder.getAdapterPosition()).getImageSelect());
        } else {
            holder.tvRatio.setTextColor(context.getResources().getColor(R.color.ucrop_color_white));
            holder.ivRatio.setImageResource(aspectRatioList.get(holder.getAdapterPosition()).getImage());
       }
    }

    @Override
    public int getItemCount() {
        return aspectRatioList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRatio;
        ImageView ivRatio;
        ConstraintLayout cslRatio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRatio = itemView.findViewById(R.id.tv_ratio);
            ivRatio = itemView.findViewById(R.id.iv_type_ratio);
            cslRatio = itemView.findViewById(R.id.csl_ratio);
        }
    }

    public interface OnClickRatio {
        void onChangeRatio(float ratio, int id);
    }
}
