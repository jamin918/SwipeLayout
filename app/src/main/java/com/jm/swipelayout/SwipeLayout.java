package com.jm.swipelayout;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * @author jamin
 * @date 2017/4/6
 * @desc 侧滑布局
 */
public class SwipeLayout extends FrameLayout {

    private ViewDragHelper mViewDragHelper;
    private int mTouchSlop;
    private View contentView;
    private View deleteView;
    private int contentViewWidth;
    private int deleteViewWidth;
    private int STATE_OPEN = 0;
    public int STATE_CLOSE = 1;
    public int mState = STATE_CLOSE;
    private float downX, downY;

    public SwipeLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {

        mViewDragHelper = ViewDragHelper.create(this, mCallback);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!SwipeLayoutManager.getInstance().couldSwipe(this)) {
            // 侧滑不可用，拦截touch事件，交给父容器处理，父容器响应onTouchEvent事件
            return true;
        }
        // 否则交给ViewDragHelper处理
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!SwipeLayoutManager.getInstance().couldSwipe(SwipeLayout.this)) {
            SwipeLayoutManager.getInstance().closeOpenInstance();
            return false;  // 侧滑不可用时，不消费touch事件，交给父容器处理
        }

        // 处理多指滑动的情况，只消费第一个手指触摸事件
        if (event.getPointerId(0) == 0) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    float moveY = event.getY();

                    // 水平滑动
                    if (Math.abs(moveY - downY) < Math.abs(moveX - downX)) {
                        // 请求父View不拦截事件
                        requestDisallowInterceptTouchEvent(true);
                    }

                    // 垂直方向滑动，关闭已经打开的item
                    if (Math.abs(moveY - downY) > Math.abs(moveX - downX)){
                        SwipeLayoutManager.getInstance().closeOpenInstance();
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    // 计算抬起的坐标
                    float upX = event.getX();
                    float upY = event.getY();
                    // 计算按下点和抬起点的距离
                    float touchD = getDistanceBetween2Points(new PointF(downX, downY), new PointF(upX, upY));
                    // 模拟点击事件
                    if (touchD < mTouchSlop) {
                        // 打开状态则关闭，否则执行点击事件
                        if (SwipeLayoutManager.getInstance().isOpenInstance(SwipeLayout.this)) {
                            SwipeLayoutManager.getInstance().closeOpenInstance();
                        } else {
                            if (listener != null) {
                                listener.onItemClick();
                            }
                        }

                    }
                    break;
            }
            mViewDragHelper.processTouchEvent(event);
            return true;
        }

        // 不是第一个手指触发的事件，不消费
        return false;
    }

    /**
     * 获得两点之间的距离
     *
     * @param p0
     * @param p1
     * @return
     */
    public static float getDistanceBetween2Points(PointF p0, PointF p1) {
        return (float) Math.sqrt(Math.pow(p0.y - p1.y, 2) + Math.pow(p0.x - p1.x, 2));
    }

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        /**需要拖拽的View */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == contentView || child == deleteView;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return deleteViewWidth;
        }

        /**
         * 固定view在水平方向移动的位置
         * @param child 被拖拽view
         * @param left  手指滑动时被拖拽view的left位置
         * @param dx    view在水平方向移动的距离
         * @return 被拖拽view最终left位置
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            if (child == contentView) {
                // 右边界
                left = left > 0 ? 0 : left;
                // 左边界
                left = left < -deleteViewWidth ? -deleteViewWidth : left;
            }

            if (child == deleteView) {
                // 左边界
                left = left < contentViewWidth - deleteViewWidth ? contentViewWidth - deleteViewWidth : left;
                // 右边界
                left = left > contentViewWidth ? contentViewWidth : left;
            }

            return left;
        }

        /**
         * 被拖拽view位置改变时调用，一般用来做伴随移动和判断状态执行相应的操作
         * @param changedView 被拖拽view
         * @param left view当前的left
         * @param top  view当前的top
         * @param dx   view的水平移动距离
         * @param dy   view的竖直移动距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == contentView) {
                // 对deleteView重新布局
                int newLeft = deleteView.getLeft() + dx;
                deleteView.layout(newLeft, deleteView.getTop(), newLeft + deleteViewWidth, deleteView.getBottom());
            }

            if (changedView == deleteView) {
                // 对contentView重新布局
                contentView.layout(left - contentViewWidth, contentView.getTop(), left, contentView.getBottom());
            }

            // 处理打开与关闭的逻辑
            if (contentView.getLeft() == -deleteViewWidth && mState == STATE_CLOSE) {
                mState = STATE_OPEN;
                // 记录打开的SwipeLayout
                SwipeLayoutManager.getInstance().setOpenInstance(SwipeLayout.this);
            }

            if (contentView.getLeft() == 0 && mState == STATE_OPEN) {
                mState = STATE_CLOSE;
                SwipeLayoutManager.getInstance().closeOpenInstance();
            }

        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (contentView.getRight() < contentViewWidth - deleteViewWidth / 2) {
                openDeleteMenu();
            } else {
                closeDeleteMenu();
            }
        }
    };

    public void closeDeleteMenu() {
        mViewDragHelper.smoothSlideViewTo(contentView, 0, contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void openDeleteMenu() {
        mViewDragHelper.smoothSlideViewTo(contentView, -deleteViewWidth, contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(this);
    }


    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        contentView.layout(left, top, right, bottom);
        deleteView.layout(right, top, right + deleteViewWidth, bottom);
    }

    /**
     * 获取contentView和deleteView
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 做简单的异常处理
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("SwipeLayout must have two children!");
        }
        contentView = getChildAt(0);
        deleteView = getChildAt(1);
    }

    /**
     * 获取contentView和deleteView的测量大小
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        contentViewWidth = contentView.getMeasuredWidth();
        deleteViewWidth = deleteView.getMeasuredWidth();
    }

    /**
     * 获取删除区域
     */
    public View getDeleteView() {
        return deleteView;
    }

    /**
     * 获取内容区域
     */
    public View getContentView() {
        return contentView;
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 点击事件回调接口
     */
    public interface OnItemClickListener {
        void onItemClick();
    }

}
