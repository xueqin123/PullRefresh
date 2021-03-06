package qin.xue.refreshlibrary;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by qinxue on 2017/12/5.
 */

public class HeaderView extends FrameLayout implements RefreshLayout.OnRefreshListener {

    public HeaderView(Context context) {
        super(context);
        initView(context);
    }

    public HeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public HeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);

    }

    private void initView(Context context) {
        TextView textView = new TextView(context);
        textView.setText("Header");
        addView(textView);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onStartPull(int type, int y) {

    }

    @Override
    public void onPulling(int type, int y) {
    }

    @Override
    public void onPullRelease(int type, int y) {

    }

    @Override
    public void onTrigger(int type, int y) {

    }

    @Override
    public void onScrollingToHoldPosition(int type, int y) {

    }

    @Override
    public void onHoldPosition(int type, int y) {


    }

    @Override
    public void onScrollingToIdle(int type, int y) {

    }

    @Override
    public void onScrollingIdleStop(int type, int y) {
    }
}
