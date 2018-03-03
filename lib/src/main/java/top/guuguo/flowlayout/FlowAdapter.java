package top.guuguo.flowlayout;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

/**
 * mimi 创造于 2017/9/11.
 * 项目 androidLib
 */

public abstract class FlowAdapter<T> extends RecyclerView.Adapter<FlowAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
    private List<T> items;
    private HashMap<Integer, ViewHolder> viewsMap = new HashMap<>();

    public HashMap<Integer, T> getCheckedMap() {
        return checkedMap;
    }

    private HashMap<Integer, T> checkedMap = new HashMap<>();
    public static final int CHECK_TYPE_NONE = -1;
    public static final int CHECK_TYPE_MULTI = -1;
    public static final int CHECK_TYPE_SINGLE = 1;

    public void setCheckLimit(int limitNum) {
        this.checkType = limitNum;
    }

    /**
     * -1是没有选择模式，0是多选模式,没有限制个数，1是单选模式，大于1是多选模式但是有限制个数
     */
    private int checkType = -1;

    public void setNewData(List<T> list) {
        items = list;
        viewsMap.clear();
        checkedMap.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(onCreateView());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final T item = items.get(position);
        onBindView(holder.itemView, item, checkedMap.containsKey(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (checkType) {
                    case -1:
                        break;
                    case 1:
                        if (checkedMap.containsKey(position)) {
                            checkedMap.clear();
                            notifyItemChanged(position);
                        } else {
                            checkedMap.clear();
                            checkedMap.put(position, item);
                            notifyDataSetChanged();
                        }
                        break;
                    case 0:
                        if (checkedMap.containsKey(position)) {
                            checkedMap.remove(position);
                        } else {
                            checkedMap.put(position, item);
                        }
                        notifyItemChanged(position);
                        break;
                    default:
                        if (checkedMap.containsKey(position)) {
                            checkedMap.remove(position);
                            notifyItemChanged(position);
                        } else if (checkedMap.size() >= checkType) {
                            isMaxChecked(checkType);
                        } else {
                            checkedMap.put(position, item);
                            notifyItemChanged(position);
                        }
                }
            }
        });
        viewsMap.put(position, holder);
    }

    public int getItemCount() {
        return items.size();
    }

    protected abstract View onCreateView();

    protected abstract void onBindView(View view, T item, boolean isChecked);

    protected void isMaxChecked(int limitedMaxNum) {
    }

    public View getItemView(int position) {
        return viewsMap.get(position).itemView;
    }
}
