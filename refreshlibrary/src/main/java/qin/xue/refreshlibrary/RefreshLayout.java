package qin.xue.refreshlibrary;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * Created by qinxue on 2017/12/5.
 */

public class RefreshLayout extends ViewGroup {
    private static final String TAG = RefreshLayout.class.getSimpleName();
    private static final int STATE_IDLE = 0;
    private static final int STATE_DRAGGING = 1;
    private static final int STATE_SCROLLING_TO_HOLD_POSITION = 2;
    private static final int STATE_HOLDING_POSITION = 3;
    private static final int STATE_SCROLLING_TO_IDLE = 4;

    public static final int REFRESH_TYPE_DEFAULT = 0;
    public static final int REFRESH_TYPE_TOP = 1;
    public static final int REFRESH_TYPE_BOTTOM = 2;

    private HeaderView mHeaderView;
    private FooterView mFooterView;
    private RecyclerView mContentView;
    private Scroller mScroller;
    private int mHeaderTop;
    private int mHeaderBottom;
    private int mContentTop;     // 布局到屏幕顶部的高度
    private int mContentRealTop; // 布局顶部到 RecyclerView 的高度
    private int mContentRealBottom;
    private int mFooterTop;
    private int mHeaderTriggerPosition;
    private int mFooterTriggerPosition;
    private int mHeaderHoldPosition;  //head show height
    private int mFooterHoldPosition;  //foot show height
    private float mScale;
    private boolean isFirstTrigger = true;

    private int mCurrentState = STATE_IDLE;
    private int mCurrentType = REFRESH_TYPE_DEFAULT;
    private int mBeyondTopDistance;

    private ArrayList<LastY> mLastYs = new ArrayList<>();
    private HashSet<OnRefreshListener> mListeners = new HashSet<>();

    public RefreshLayout(Context context) {
        super(context);
        initView(context);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    private void initView(Context context) {
        mScroller = new Scroller(context);
        mScale = getResources().getDisplayMetrics().density;
        mBeyondTopDistance = (int) getResources().getDimension(R.dimen.top_trigger_size);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int totalHeight = 0;
        for (int i = 0; i < count; ++i) {
            View childView = getChildAt(i);
            if (childView instanceof RecyclerView) {
                measureChild(childView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) + mBeyondTopDistance, MeasureSpec.getMode(heightMeasureSpec)));
            } else {
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            }
            totalHeight = totalHeight + childView.getMeasuredHeight();
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.getMode(heightMeasureSpec)));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            View childView = getChildAt(i);
            if (childView instanceof HeaderView) {
                mHeaderTop = t;
                mHeaderBottom = mHeaderTop + childView.getMeasuredHeight();
                childView.layout(l, t, r, mHeaderBottom);
                mHeaderView = (HeaderView) childView;
                mContentRealTop = mHeaderBottom;
                mContentTop = mHeaderBottom + mBeyondTopDistance;
            } else if (childView instanceof RecyclerView) {
                mContentRealBottom = mContentRealTop + childView.getMeasuredHeight();
                childView.layout(l, mContentRealTop, r, mContentRealBottom);
                mContentView = (RecyclerView) childView;
                mFooterTop = mContentRealBottom;
            } else if (childView instanceof FooterView) {
                childView.layout(l, mFooterTop, r, mFooterTop + childView.getMeasuredHeight());
                mFooterView = (FooterView) childView;
            }
        }
        scrollTo(0, mContentTop);

        addRefreshListener(mHeaderView);
        addRefreshListener(mFooterView);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastYs.add(new LastY((int) ev.getY(ev.getActionIndex()), ev.getPointerId(ev.getActionIndex())));
                break;
            case MotionEvent.ACTION_MOVE:       //包含多个手指的move

                int dyMax = 0;                       //多手指滑动时使用最大的滑动距离计算

                for (int i = 0; i < ev.getPointerCount(); i++) {
                    int id = ev.getPointerId(i);
                    int y = (int) ev.getY(i);
                    LastY lastY = getLastYById(id);
                    int dy = y - lastY.getLastY();
                    lastY.setLastY(y);
                    if (Math.abs(dyMax) < Math.abs(dy)) {
                        dyMax = dy;
                    }
                }
                //屏蔽掉上拉功能，如需要替换判断代码， if(isRecyclerViewTop() && dyMax > 0 || isRecyclerViewBottom() && dyMax < 0) && mCurrentState != STATE_DRAGGING）
                if ((isRecyclerViewTop() && dyMax > 0) && mCurrentState != STATE_DRAGGING) {

                    //切换进入可以拖动状态
                    mCurrentState = STATE_DRAGGING;
                    if (isRecyclerViewTop()) {
                        mCurrentType = REFRESH_TYPE_TOP;
                    } else {
                        mCurrentType = REFRESH_TYPE_BOTTOM;
                    }
                    dispatchOnStartPull();
                }
                if (mCurrentState == STATE_DRAGGING) {
                    scrollBy(0, (int) (-dyMax / 2));
                    //在拖动状态可以随意拖动
                    //屏蔽掉上拉功能，如需要替换判断代码， if ((isRecyclerViewTop() && getScrollY() > mContentTop || isRecyclerViewBottom() && getScrollY() < mContentTop)&& !(isRecyclerViewTop() && isRecyclerViewBottom()))
                    if (isRecyclerViewTop() && getScrollY() > mContentTop) {
                        scrollTo(0, mContentTop);
                        mCurrentState = STATE_IDLE;
                        return super.dispatchTouchEvent(ev);
                    }
                    postInvalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCurrentState == STATE_DRAGGING) {
                    removeMLastById(ev.getPointerId(ev.getActionIndex()));
                    dispatchOnPullRelease();

                    if (Math.abs(getScrollY() - mContentTop) >= mHeaderHoldPosition || Math.abs(getScrollY() - mContentTop) >= mFooterHoldPosition) {
                        scrollToHoldPosition();
                    } else {
                        scrollToIdle();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mLastYs.add(new LastY((int) ev.getY(ev.getActionIndex()), ev.getPointerId(ev.getActionIndex())));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                removeMLastById(ev.getPointerId(ev.getActionIndex()));
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void removeMLastById(int id) {
        int removeIndex = 0;
        for (int i = 0; i < mLastYs.size(); i++) {
            if (mLastYs.get(i).getId() == id) {
                removeIndex = i;
            }
        }
        mLastYs.remove(removeIndex);
    }

    private LastY getLastYById(int id) {
        int getIndex = 0;
        for (int i = 0; i < mLastYs.size(); i++) {
            if (mLastYs.get(i).getId() == id) {
                getIndex = i;
            }
        }
        return mLastYs.get(getIndex);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        switch (mCurrentState) {
            case STATE_IDLE:
                //原始位置

                break;
            case STATE_DRAGGING:
                //拉动过程中
                dispatchOnPulling();

                if (getScrollY() <= mContentTop && (Math.abs(getScrollY() - mContentTop)) >= mHeaderTriggerPosition && isFirstTrigger) {
                    for (OnRefreshListener listener : mListeners) {
                        listener.onTrigger(mCurrentType, Math.abs(getScrollY() - mContentTop));
                    }
                    isFirstTrigger = false;
                } else if (getScrollY() > mContentTop && (Math.abs(getScrollY() - mContentTop)) >= mFooterTriggerPosition && isFirstTrigger) {
                    dispatchOnTrigger();
                    isFirstTrigger = false;
                }
                break;
            case STATE_SCROLLING_TO_HOLD_POSITION:
                //滑向加载停留位置
                dispatchOnScrollingToHoldPosition();


                //判断Hold位置临界，切换状态
                if (getScrollY() <= mContentTop && Math.abs(getScrollY() - mContentTop) == mHeaderHoldPosition) {
                    dispatchOnHoldPosition();
                    mCurrentState = STATE_HOLDING_POSITION;
                } else if (getScrollY() > mContentTop && Math.abs(getScrollY() - mContentTop) == mFooterHoldPosition) {
                    dispatchOnHoldPosition();
                    mCurrentState = STATE_HOLDING_POSITION;
                }

                break;
            case STATE_HOLDING_POSITION:
                if (getScrollY() <= mContentTop && 0 == mHeaderHoldPosition || getScrollY() > mContentTop && 0 == mFooterHoldPosition || isFirstTrigger) {
                    //设置初始值为0时，直接release
                    scrollToIdle();
                }
                //加载停留时,等待release
                break;
            case STATE_SCROLLING_TO_IDLE:
                //滑向原始位置
                dispatchOnScrollingToIdle();

                //判断原位置临界，切换状态
                if (getScrollY() == mContentTop) {
                    mCurrentType = REFRESH_TYPE_DEFAULT;
                    dispatchOnScrollingIdleStop();
                    mCurrentState = STATE_IDLE;
                    isFirstTrigger = true;
                }

                break;
            default:
                break;

        }

        //计算滑动位置
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    private void scrollToHoldPosition() {
        mCurrentState = STATE_SCROLLING_TO_HOLD_POSITION;
        if (getScrollY() <= mContentTop) {
            mScroller.startScroll(0, getScrollY(), 0, mContentTop - getScrollY() - mHeaderHoldPosition);
        } else if (getScrollY() >= mContentTop) {
            mScroller.startScroll(0, getScrollY(), 0, mContentTop - getScrollY() + mFooterHoldPosition);
        }
        postInvalidate();
    }

    private void scrollToIdle() {
        mCurrentState = STATE_SCROLLING_TO_IDLE;
        mScroller.startScroll(0, getScrollY(), 0, mContentTop - getScrollY());
        postInvalidate();
    }


    private boolean isRecyclerViewTop() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mContentView.getLayoutManager();
        int firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int visibleItemCount = layoutManager.getChildCount();
        return (visibleItemCount > 0 && firstVisibleItemPosition == 0);
    }

    public boolean isRecyclerViewBottom() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mContentView.getLayoutManager();
        int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        return (visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1);
    }

    private void dispatchOnStartPull() {
        for (OnRefreshListener listener : mListeners) {
            listener.onStartPull(mCurrentType, getScrollY() - mContentTop);
        }
    }

    private void dispatchOnPulling() {
        for (OnRefreshListener listener : mListeners) {
            listener.onPulling(mCurrentType, Math.abs(getScrollY() - mContentTop));
        }
    }

    private void dispatchOnPullRelease() {
        for (OnRefreshListener listener : mListeners) {
            listener.onPullRelease(mCurrentType, Math.abs(getScrollY() - mContentTop));
        }
    }

    private void dispatchOnTrigger() {
        for (OnRefreshListener listener : mListeners) {
            listener.onTrigger(mCurrentType, Math.abs(getScrollY() - mContentTop));
        }
    }


    private void dispatchOnScrollingToHoldPosition() {
        for (OnRefreshListener listener : mListeners) {
            listener.onScrollingToHoldPosition(mCurrentType, Math.abs(getScrollY() - mContentTop));
        }
    }

    private void dispatchOnHoldPosition() {
        for (OnRefreshListener listener : mListeners) {
            listener.onHoldPosition(mCurrentType, mHeaderHoldPosition);
        }
    }

    private void dispatchOnScrollingToIdle() {
        for (OnRefreshListener listener : mListeners) {
            listener.onScrollingToIdle(mCurrentType, Math.abs(getScrollY() - mContentTop));
        }
    }

    private void dispatchOnScrollingIdleStop() {
        for (OnRefreshListener listener : mListeners) {
            listener.onScrollingIdleStop(mCurrentType, Math.abs(getScrollY() - mContentTop));
        }
    }


    public void addRefreshListener(OnRefreshListener listener) {
        mListeners.add(listener);
    }

    public void removeRefreshListener(OnRefreshListener listener) {
        mListeners.remove(listener);
    }

    public void clearRefreshListener() {
        mListeners.clear();
    }


    private static class LastY {
        int lastY;
        int id;

        public LastY(int y, int actionId) {
            lastY = y;
            id = actionId;
        }

        public int getId() {
            return id;
        }

        public int getLastY() {
            return lastY;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setLastY(int lastY) {
            this.lastY = lastY;
        }

    }


    public interface OnRefreshListener {

        void onStartPull(int type, int y);

        void onPulling(int type, int y);

        void onPullRelease(int type, int y);

        void onTrigger(int type, int y);

        void onScrollingToHoldPosition(int type, int y);

        void onHoldPosition(int type, int y);

        void onScrollingToIdle(int type, int y);

        void onScrollingIdleStop(int type, int y);
    }

    //设置刷新过程中，headerView停留的位置
    public void setHeaderHoldPosition(int y) {
        mHeaderHoldPosition = y;
    }

    //设置刷新过程中，footView停留的位置
    public void setFooterHoldPosition(int y) {
        mFooterHoldPosition = y;
    }

    public void setHeaderTriggerPosition(int y) {
        mHeaderTriggerPosition = y;
    }

    public void setFooterTriggerPosition(int y) {
        mFooterTriggerPosition = y;
    }

    public int getBeyondTopDistance() {
        return mBeyondTopDistance;
    }

    public void setBeyondTopDistance(int mBeyondTopDistance) {
        this.mBeyondTopDistance = mBeyondTopDistance;
    }

    //刷新完成后调用，隐藏Header和FooterView。
    public void autoScrollToIdle() {
        scrollToIdle();
    }

}
