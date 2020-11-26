package com.dimeno.adapter.sample;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dimeno.adapter.base.RecyclerItem;
import com.dimeno.adapter.callback.OnItemChildClickCallback;
import com.dimeno.adapter.callback.OnItemChildLongClickCallback;
import com.dimeno.adapter.callback.OnItemClickCallback;
import com.dimeno.adapter.sample.adapter.UserAdapter;
import com.dimeno.adapter.sample.entity.UserEntity;
import com.dimeno.adapter.sample.holder.EmptyHolder;
import com.dimeno.adapter.sample.holder.FooterHolder;
import com.dimeno.adapter.sample.holder.HeaderHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecycler;
    private UserAdapter mAdapter;
    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecycler = findViewById(R.id.recycler);

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#33888888"));
        mRecycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = parent.getChildAt(i);
                    if (childAt != null && i < childCount - 1) {
                        int left = childAt.getLeft();
                        int right = childAt.getRight();
                        int top = childAt.getBottom();
                        int bottom = top + 1;
                        c.drawRect(new Rect(left, top, right, bottom), mPaint);
                    }
                }
            }

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                if (position == parent.getAdapter().getItemCount() - 1) {
                    outRect.set(0, 0, 0, 0);
                } else {
                    outRect.set(0, 0, 0, 1);
                }
            }
        });
        init();
    }

    public void init() {
        final List<UserEntity> list = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            list.add(new UserEntity("张三", i + 1));
        }
        mAdapter = new UserAdapter(list, mRecycler);
        mAdapter.setOnClickCallback(new OnItemClickCallback() {
            @Override
            public void onItemClick(View itemView, int position) {
                mAdapter.getDatas().remove(position);
                mAdapter.notifyItemRemoved(position + mAdapter.getHeadersCount());
            }
        });
        mAdapter.setOnChildClickCallback(new OnItemChildClickCallback() {
            @Override
            public void onItemChildClick(View itemView, int position) {
                switch (itemView.getId()){
                    case R.id.tv_name:
                        Toast.makeText(itemView.getContext(),list.get(position).name,Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        mAdapter.setOnChildLongClickCallback(new OnItemChildLongClickCallback() {
            @Override
            public void onItemChildLongClick(View itemView, int position) {
                switch (itemView.getId()){
                    case R.id.tv_age:
                        Toast.makeText(itemView.getContext(),list.get(position).name,Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        mAdapter.setEmpty(new EmptyHolder().onCreateView(mRecycler));

        mAdapter.addHeader(new HeaderHolder().onCreateView(mRecycler));
        mAdapter.addHeader(new FooterHolder().onCreateView(mRecycler));
        mAdapter.addHeader(new HeaderHolder().onCreateView(mRecycler));

        mAdapter.addFooter(new RecyclerItem() {
            @Override
            protected int layout() {
                return R.layout.item_footer_red_layout;
            }
        }.onCreateView(mRecycler));
        mAdapter.addFooter(new RecyclerItem() {
            @Override
            protected int layout() {
                return R.layout.item_footer_green_layout;
            }
        }.onCreateView(mRecycler));
        mAdapter.addFooter(new FooterHolder().onCreateView(mRecycler));

        mRecycler.setAdapter(mAdapter);
    }
}