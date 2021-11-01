package com.dimeno.adapter;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.dimeno.adapter.base.RecyclerViewHolder;
import com.dimeno.adapter.callback.OnHolderChildClickCallback;
import com.dimeno.adapter.callback.OnHolderChildLongClickCallback;
import com.dimeno.adapter.callback.OnHolderClickCallback;
import com.dimeno.adapter.callback.OnHolderLongClickCallback;
import com.dimeno.adapter.callback.OnItemChildClickCallback;
import com.dimeno.adapter.callback.OnItemChildLongClickCallback;
import com.dimeno.adapter.callback.OnItemClickCallback;
import com.dimeno.adapter.callback.OnItemLongClickCallback;
import com.dimeno.adapter.holder.HeaderFooterViewHolder;

import java.util.List;

/**
 * recycler basic adapter
 * Created by wangzhen on 2020/6/9.
 */
public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = -20000; // [-20000, 0)
    private static final int VIEW_TYPE_FOOTER = -40000; // [-40001, -20000)
    protected static final int VIEW_TYPE_EMPTY = VIEW_TYPE_FOOTER - 1; // -40001

    protected List<T> mDatas;
    private SparseArray<View> mHeaders = new SparseArray<>();
    protected SparseArray<View> mFooters = new SparseArray<>();

    private OnItemClickCallback mItemClickCallback;
    private OnItemChildClickCallback mItemChildClickCallback;
    private OnItemLongClickCallback mItemLongClickCallback;
    private OnItemChildLongClickCallback mItemChildLongClickCallback;

    private int mHeaderIndex;
    private int mFooterIndex;
    private View mEmptyView;

    public RecyclerAdapter(List<T> list) {
        this.mDatas = list;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return mHeaders.keyAt(position);
        }
        if (isFooterPosition(position)) {
            return mFooters.keyAt(getFootersCount() - (getItemCount() - position));
        }
        return getAbsItemViewType(compatPosition(position));
    }

    protected int getAbsItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mHeaders.get(viewType) != null) {
            return new HeaderFooterViewHolder(mHeaders.get(viewType));
        }
        if (mFooters.get(viewType) != null) {
            return new HeaderFooterViewHolder(mFooters.get(viewType));
        }
        return onAbsCreateViewHolder(parent, viewType);
    }

    public abstract RecyclerView.ViewHolder onAbsCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isInnerPosition(position)) return;
        setItemEvent(holder);
        if (holder instanceof RecyclerViewHolder) {
            ((RecyclerViewHolder<T>) holder).setData(mDatas.get(compatPosition(position)));
        }
    }

    /**
     * add a new header
     *
     * @param view header
     */
    public void addHeader(View view) {
        mHeaders.put(VIEW_TYPE_HEADER + mHeaderIndex++, view);
    }

    /**
     * add a new footer
     *
     * @param view footer
     */
    public void addFooter(View view) {
        mFooters.put(VIEW_TYPE_FOOTER + mFooterIndex++, view);
    }

    /**
     * set empty view
     *
     * @param view view
     */
    public void setEmpty(View view) {
        mEmptyView = view;
    }

    /**
     * whether the position is in region of both headers and footers
     *
     * @param position position
     * @return is inner position
     */
    public final boolean isInnerPosition(int position) {
        return isHeaderPosition(position) || isFooterPosition(position);
    }

    /**
     * is header position
     *
     * @param position position
     * @return is header position
     */
    private boolean isHeaderPosition(int position) {
        return position < getHeadersCount();
    }

    /**
     * is footer position
     *
     * @param position position
     * @return is footer position
     */
    private boolean isFooterPosition(int position) {
        return position >= getItemCount() - getFootersCount() && position < getItemCount();
    }

    /**
     * item count including headers and footers
     *
     * @return item count
     */
    @Override
    public int getItemCount() {
        int count = mDatas == null ? 0 : mDatas.size();
        if (count > 0) {
            if (mFooters.get(VIEW_TYPE_EMPTY) != null) {
                mFooters.remove(VIEW_TYPE_EMPTY);
            }
        } else {
            if (mEmptyView != null && mFooters.get(VIEW_TYPE_EMPTY) == null) {
                mFooters.put(VIEW_TYPE_EMPTY, mEmptyView);
            }
        }
        return count + getHeadersCount() + getFootersCount();
    }

    @Override
    public long getItemId(int position) {
        if (isHeaderPosition(position)) {
            return mHeaders.keyAt(position);
        }
        if (isFooterPosition(position)) {
            return mFooters.keyAt(getFootersCount() - (getItemCount() - position));
        }
        return super.getItemId(position);
    }

    /**
     * headers count
     *
     * @return count
     */
    public int getHeadersCount() {
        return mHeaders.size();
    }

    /**
     * footers count
     *
     * @return count
     */
    public int getFootersCount() {
        return mFooters.size();
    }

    /**
     * data list
     *
     * @return list
     */
    public List<T> getDatas() {
        return mDatas;
    }

    /**
     * replace current data source
     *
     * @param list list
     */
    public void setData(List<T> list) {
        this.mDatas = list;
        notifyDataSetChanged();
    }

    /**
     * append data to source
     *
     * @param list list
     */
    public void addData(List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        int position = getItemCount() - getFootersCount();
        if (mDatas == null || mDatas.isEmpty()) {
            mDatas = list;
            notifyDataSetChanged();
        } else {
            mDatas.addAll(list);
            notifyItemRangeInserted(position, list.size());
        }
    }

    /**
     * resolve the correct position for data excluding headers and footers
     *
     * @param position position
     * @return position
     */
    private int compatPosition(int position) {
        return position - getHeadersCount();
    }

    /**
     * set event for view holder
     *
     * @param holder view holder
     */
    private void setItemEvent(final RecyclerView.ViewHolder holder) {
        if (mItemClickCallback != null || holder instanceof OnHolderClickCallback) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickCallback != null)
                        mItemClickCallback.onItemClick(v, compatPosition(holder.getLayoutPosition()));
                    if (holder instanceof OnHolderClickCallback)
                        ((OnHolderClickCallback) holder).onItemClick(v, compatPosition(holder.getLayoutPosition()));
                }
            });
        }
        if (mItemLongClickCallback != null || holder instanceof OnHolderLongClickCallback) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mItemLongClickCallback != null)
                        mItemLongClickCallback.onItemLongClick(v, compatPosition(holder.getLayoutPosition()));
                    if (holder instanceof OnHolderLongClickCallback)
                        ((OnHolderLongClickCallback) holder).onItemLongClick(v, compatPosition(holder.getLayoutPosition()));
                    return false;
                }
            });
        }
        if(holder.itemView instanceof ViewGroup){
            ViewGroup parent = (ViewGroup) holder.itemView;
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = parent.getChildAt(i);
                if(childAt != null) {
                    if (!childAt.isClickable()) {
                        childAt.setClickable(true);
                    }
                    if (childAt.getId() != View.NO_ID) {
                        childAt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mItemChildClickCallback != null) {
                                    mItemChildClickCallback.onItemChildClick(view, compatPosition(holder.getLayoutPosition()));
                                }
                                if (holder instanceof OnHolderChildClickCallback) {
                                    ((OnHolderChildClickCallback) holder).onItemChildClick(view, compatPosition(holder.getLayoutPosition()));
                                }
                            }
                        });
                    }
                }
            }
        }
        if(holder.itemView instanceof ViewGroup){
            ViewGroup parent = (ViewGroup) holder.itemView;
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = parent.getChildAt(i);
                if(childAt != null) {
                    if (!childAt.isLongClickable()) {
                        childAt.setLongClickable(true);
                    }
                    if (childAt.getId() != View.NO_ID) {
                        childAt.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                if (mItemChildLongClickCallback != null) {
                                    mItemChildLongClickCallback.onItemChildLongClick(view, compatPosition(holder.getLayoutPosition()));
                                }
                                if (holder instanceof OnHolderChildLongClickCallback) {
                                    ((OnHolderChildLongClickCallback) holder).onItemChildLongClick(view, compatPosition(holder.getLayoutPosition()));
                                }
                                return true;
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * set click callback
     *
     * @param callback callback
     */
    public void setOnClickCallback(OnItemClickCallback callback) {
        this.mItemClickCallback = callback;
    }

    /**
     * set click callback
     *
     * @param callback callback
     */
    public void setOnChildClickCallback(OnItemChildClickCallback callback) {
        this.mItemChildClickCallback = callback;
    }

    /**
     * set long click callback
     *
     * @param callback callback
     */
    public void setOnLongClickCallback(OnItemLongClickCallback callback) {
        this.mItemLongClickCallback = callback;
    }

    /**
     * set childl ong click callback
     *
     * @param callback callback
     */
    public void setOnChildLongClickCallback(OnItemChildLongClickCallback callback) {
        this.mItemChildLongClickCallback = callback;
    }

    /**
     * let header or footer views have full width for grid layout manager
     *
     * @param recyclerView recyclerView
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
            manager.setSpanSizeLookup(new SpanSizeLookupWrapper(manager.getSpanCount(), manager.getSpanSizeLookup()));
        }
    }

    /**
     * wrapper class to handle span size
     */
    private class SpanSizeLookupWrapper extends GridLayoutManager.SpanSizeLookup {
        private int spanCount;
        private GridLayoutManager.SpanSizeLookup spanSizeLookup;

        public SpanSizeLookupWrapper(int spanCount, GridLayoutManager.SpanSizeLookup spanSizeLookup) {
            this.spanCount = spanCount;
            this.spanSizeLookup = spanSizeLookup;
        }

        @Override
        public int getSpanSize(int position) {
            if (isInnerPosition(position)) {
                return spanCount;
            }
            if (spanSizeLookup != null) {
                return spanSizeLookup.getSpanSize(position);
            }
            return 1;
        }
    }

    /**
     * let header or footer views have full width for staggered grid layout manager
     *
     * @param holder holder
     */
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (isInnerPosition(holder.getLayoutPosition())) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
            }
        }
    }
}
