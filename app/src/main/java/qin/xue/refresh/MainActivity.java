package qin.xue.refresh;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;

import qin.xue.refreshlibrary.RefreshLayout;

import static qin.xue.refresh.Module.NORMAL_TYPE;


/**
 * Created by qinxue on 2018/8/25.
 */

public class MainActivity extends Activity implements RefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private RefreshLayout mRefreshLayout;
    private ArrayList<Module> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        initData();
        mAdapter = new RecyclerViewAdapter(mData);
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.addRefreshListener(this);
    }

    private void initData() {
        mData = new ArrayList<>();
        mData.add(new Module(0, Module.HEAD_TYPE));
        for (int i = 100; i < 120; i++) {
            mData.add(new Module(i, NORMAL_TYPE));
        }
    }

    @Override
    protected void onDestroy() {
        mRefreshLayout.removeRefreshListener(this);
        super.onDestroy();
    }

    @Override
    public void onStartPull(int type, int y) {
        Log.i(TAG, "onStartPull type = " + type + "y = " + y);
    }

    @Override
    public void onPulling(int type, int y) {
        Log.i(TAG, "onPulling type = " + type + "y = " + y);

    }

    @Override
    public void onPullRelease(int type, int y) {
        Log.i(TAG, "onPullRelease type = " + type + "y = " + y);
        //模拟1s网络请求
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDate();
            }
        }, 1000);
    }

    /**
     * 下拉触发
     *
     * @param type
     * @param y
     */
    @Override
    public void onTrigger(int type, int y) {
        Log.i(TAG, "onTrigger type = " + type + "y = " + y);

    }

    @Override
    public void onScrollingToHoldPosition(int type, int y) {
        Log.i(TAG, "onScrollingToHoldPosition type = " + type + "y = " + y);
    }

    @Override
    public void onHoldPosition(int type, int y) {
        Log.i(TAG, "onHoldPosition type = " + type + "y = " + y);

    }

    @Override
    public void onScrollingToIdle(int type, int y) {
        Log.i(TAG, "onScrollingToIdle type = " + type + "y = " + y);

    }

    @Override
    public void onScrollingIdleStop(int type, int y) {
        Log.i(TAG, "onScrollingIdleStop type = " + type + "y = " + y);
    }

    private void updateDate() {
        mData.add(1, new Module(mData.get(1).val - 1, NORMAL_TYPE));
        mAdapter.notifyDataSetChanged();
    }
}
