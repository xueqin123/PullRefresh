package qin.xue.refresh;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by qinxue on 2018/8/25.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Module> data;

    public RecyclerViewAdapter(List<Module> data) {
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case Module.HEAD_TYPE:
                View v = inflater.inflate(R.layout.recycler_view_header, parent, false);
                HeaderViewHolder headerViewHolder = new HeaderViewHolder(v);
                holder = headerViewHolder;
                break;
            case Module.NORMAL_TYPE:
                View v1 = inflater.inflate(R.layout.recycler_view_item, parent, false);
                ViewHolder viewHolder = new ViewHolder(v1);
                viewHolder.textView = v1.findViewById(R.id.text);
                holder = viewHolder;
                break;
            default:
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (data.get(position).type) {
            case Module.HEAD_TYPE:
                break;
            case Module.NORMAL_TYPE:
                ViewHolder h = (ViewHolder) holder;
                Module module = data.get(position);
                h.textView.setText(String.valueOf(module.val));
                h.itemView.setBackgroundColor(getColor(module.val));
                break;
            default:
                break;
        }
    }

    private int getColor(int i) {
        int color = 0;
        switch (i % 3) {
            case 0:
                color = Color.RED;
                break;
            case 1:
                color = Color.GREEN;
                break;
            case 2:
                color = Color.BLUE;
                break;
            default:
                color = Color.WHITE;
                break;

        }
        return color;
    }


    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
