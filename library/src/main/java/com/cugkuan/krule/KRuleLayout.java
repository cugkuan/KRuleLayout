package com.cugkuan.krule;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * KRuleLayout 布局，源于在实际开发中的一个需求，有这样的需求：
 * ，布局分为 left ,right ,bottom;有时候，left 很高，或者 right 很高。
 * 这个时候，right 或者 left部分会有大量的空闲区域，我们希望 其它的View 适当的去填充这些区域。
 * <p>
 * 支持 left,right 的 权重布局
 * 支持 部分的 layout_gravity 的部分属性，更多的属性，在后续版本中添加
 */
public class KRuleLayout extends ViewGroup {


    public static final int RULE_LEFT = 1;
    public static final int RULE_RIGHT = 2;
    public static final int RULE_BOTTOM = 3;

    /**
     * 以左边的为基准
     */
    public static final int DIRECTION_LEFT = 0;

    /**
     * 如果未指定方向，那么以左边的为准
     */
    public static final int UNSPECIFIED_DIRECTION = DIRECTION_LEFT;

    private View mLeftView;
    private View mRightView;

    private List<View> mBottomViews = new ArrayList<>();

    private int mDirection = UNSPECIFIED_DIRECTION;
    /**
     * 忽略的高度
     */
    private int ignoreHeight = 0;

    /**
     * 跟随左边或者右边的View
     */
    private List<View> mFollowView = new ArrayList<>();


    public KRuleLayout(Context context) {
        this(context, null);
    }

    public KRuleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.KRuleLayout);
            if (array.hasValue(R.styleable.KRuleLayout_direction)) {
                mDirection = array.getInt(R.styleable.KRuleLayout_direction, UNSPECIFIED_DIRECTION);
            }
            if (array.hasValue(R.styleable.KRuleLayout_ignoreHeight)) {
                ignoreHeight = array.getLayoutDimension(R.styleable.KRuleLayout_ignoreHeight, 0);
            }
            array.recycle();
        }
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new KRuleLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new KRuleLayout.LayoutParams(KRuleLayout.LayoutParams.WRAP_CONTENT, KRuleLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof KRuleLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {

        if (lp instanceof KRuleLayout.LayoutParams) {
            return new KRuleLayout.LayoutParams(lp);
        } else if (lp instanceof MarginLayoutParams) {
            return new KRuleLayout.LayoutParams(lp);
        }
        return new KRuleLayout.LayoutParams(lp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mBottomViews.clear();
        mFollowView.clear();
        mLeftView = null;
        mRightView = null;
        int leftHeight = 0;
        int rightHeight = 0;
        prepareChildView();
        if (mLeftView == null || mRightView == null) {
            allBottomMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            LayoutParams paramsRight = (LayoutParams) mRightView.getLayoutParams();
            LayoutParams paramsLeft = (LayoutParams) mLeftView.getLayoutParams();
            if (paramsLeft.layout_weight > 0 && paramsRight.layout_weight > 0) {
                //表明这个Layout 是按照比例进行分配的
                measureWeight(widthMeasureSpec, heightMeasureSpec);
            } else {
                if (mDirection == DIRECTION_LEFT) {
                    measureLeftToRight(widthMeasureSpec, heightMeasureSpec);
                } else {
                    measureRightToLeft(widthMeasureSpec, heightMeasureSpec);
                }
            }
            /**
             * 左边可支配的宽度
             */
            int leftUsedWidth = mLeftView.getMeasuredWidth() + paramsLeft.leftMargin + paramsLeft.rightMargin;
            /**
             * 右边可支配的宽度
             */
            int rightUsedWidth = mRightView.getMeasuredWidth() + paramsRight.rightMargin + paramsRight.leftMargin;
            //布局其它的元素
            leftHeight = mLeftView.getMeasuredHeight() + paramsLeft.topMargin + paramsLeft.bottomMargin;
            rightHeight = mRightView.getMeasuredHeight() + paramsRight.topMargin + paramsRight.bottomMargin;
            if (mDirection == DIRECTION_LEFT) {

                leftHeight = leftHeight + ignoreHeight;
                Iterator<View> iterator = mBottomViews.iterator();

                while (iterator.hasNext()) {
                    View view = iterator.next();
                    LayoutParams params = (LayoutParams) view.getLayoutParams();

                    leftHeight = leftHeight + params.topMargin;

                    if (leftHeight >= rightHeight) {
                        break;
                    } else {
                        int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, params.height);
                        int ableUserWidthSpece = MeasureSpec.makeMeasureSpec(leftUsedWidth - params.leftMargin - params.rightMargin,
                                MeasureSpec.EXACTLY);
                        int chileWidthSpec = getChildMeasureSpec(ableUserWidthSpece,params.leftMargin +params.rightMargin,
                                params.width);
                        view.measure(chileWidthSpec, childHeightSpec);
                        if (view.getMeasuredWidth() + params.leftMargin +params.rightMargin > leftUsedWidth){
                            break;
                        }

                        leftHeight = leftHeight + view.getMeasuredHeight() + params.bottomMargin;
                        mFollowView.add(view);
                        iterator.remove();
                    }
                }
                leftHeight = leftHeight - ignoreHeight;

            } else {
                rightHeight = rightHeight + ignoreHeight;
                Iterator<View> iterator = mBottomViews.iterator();
                while (iterator.hasNext()) {
                    View view = iterator.next();
                    LayoutParams params = (LayoutParams) view.getLayoutParams();
                    rightHeight = rightHeight + params.topMargin;
                    if (rightHeight >= leftHeight) {
                        break;
                    } else {
                        int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, params.height);
                        int ableUserWidthSpece = MeasureSpec.makeMeasureSpec(rightUsedWidth - params.leftMargin - params.rightMargin,
                                MeasureSpec.EXACTLY);
                        int chileWidthSpec = getChildMeasureSpec(ableUserWidthSpece,params.leftMargin +params.rightMargin,
                                params.width);
                        view.measure(chileWidthSpec, childHeightSpec);
                        if (view.getMeasuredWidth() + params.leftMargin + params.rightMargin > rightUsedWidth){
                            break;
                        }
                        rightHeight = rightHeight + view.getMeasuredHeight() + params.bottomMargin;
                        mFollowView.add(view);
                        iterator.remove();
                    }
                }
                rightHeight = rightHeight - ignoreHeight;
            }
            int totalHeight = Math.max(rightHeight, leftHeight);
            for (View view : mBottomViews) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, 0);
                totalHeight = totalHeight + view.getMeasuredHeight() + params.topMargin + params.bottomMargin;
            }
            totalHeight = totalHeight + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), totalHeight);
        }
    }

    /**
     * 按照权重进行分配，left和wight的宽度
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void measureWeight(int widthMeasureSpec, int heightMeasureSpec) {

        LayoutParams paramsLeft = (LayoutParams) mLeftView.getLayoutParams();
        LayoutParams paramsRight = (LayoutParams) mRightView.getLayoutParams();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        //表明这个Layout 是按照比例进行分配的
        float wightSum = paramsLeft.layout_weight + paramsRight.layout_weight;
        int remain = widthSize - getPaddingRight() - getPaddingLeft() - paramsLeft.leftMargin
                - paramsLeft.rightMargin - paramsRight.rightMargin - paramsRight.leftMargin;
        int share = (int) (remain / wightSum);
        int leftUsedWidth = (int) (paramsLeft.layout_weight * share);
        int rightUsedWidth = (int) (paramsRight.layout_weight * share);

        int leftWidthMeasureSpec = MeasureSpec.makeMeasureSpec(leftUsedWidth, MeasureSpec.EXACTLY);
        int rightWidthMeasureSpec = MeasureSpec.makeMeasureSpec(rightUsedWidth, MeasureSpec.EXACTLY);

        mLeftView.measure(leftWidthMeasureSpec,
                getChildMeasureSpec(heightMeasureSpec, paramsLeft.leftMargin + paramsLeft.rightMargin,
                        paramsLeft.height));
        mRightView.measure(rightWidthMeasureSpec,
                getChildMeasureSpec(heightMeasureSpec, paramsRight.leftMargin + paramsRight.rightMargin,
                        paramsRight.height));

    }

    /**
     * 以 left 为基准,从左到有的测量
     */
    private void measureLeftToRight(int widthMeasureSpec, int heightMeasureSpec) {

        LayoutParams paramsL = (LayoutParams) mLeftView.getLayoutParams();
        LayoutParams paramsR = (LayoutParams) mRightView.getLayoutParams();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int leftUsedWidth = paramsL.width;
        int rightUsedWidth = paramsR.width;
        if (leftUsedWidth >= 0) {
            measureChildWithMargins(mLeftView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if (rightUsedWidth >= 0) {
                measureChildWithMargins(mRightView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(mRightView, widthMeasureSpec,
                        mLeftView.getMeasuredWidth() + paramsL.leftMargin + paramsL.rightMargin,
                        heightMeasureSpec, 0);
            }
        } else {
            if (rightUsedWidth >= 0) {
                measureChildWithMargins(mRightView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                measureChildWithMargins(mLeftView, widthMeasureSpec,
                        mRightView.getMeasuredWidth() + paramsR.leftMargin + paramsR.rightMargin,
                        heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(mLeftView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int leftUsed = mLeftView.getMeasuredWidth() + getPaddingLeft() + getPaddingRight()
                        + paramsL.leftMargin + paramsL.rightMargin;
                if (leftUsed >= widthSize) {
                    mRightView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
                            , MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
                } else {
                    measureChildWithMargins(mRightView, widthMeasureSpec,
                            mLeftView.getMeasuredWidth()
                                    + paramsL.leftMargin + paramsL.rightMargin,
                            heightMeasureSpec, 0);
                }
            }
        }
    }


    /**
     * 以right 为基准，从右边到左边的测量
     */
    private void measureRightToLeft(int widthMeasureSpec, int heightMeasureSpec) {

        LayoutParams paramsL = (LayoutParams) mLeftView.getLayoutParams();
        LayoutParams paramsR = (LayoutParams) mRightView.getLayoutParams();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int leftUsedWidth = paramsL.width;
        int rightUsedWidth = paramsR.width;
        if (rightUsedWidth >= 0) {
            measureChildWithMargins(mRightView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            if (leftUsedWidth >= 0) {
                measureChildWithMargins(mLeftView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(mLeftView, widthMeasureSpec,
                        mRightView.getMeasuredWidth() + paramsR.leftMargin + paramsR.rightMargin,
                        heightMeasureSpec,
                        0);
            }
        } else {
            if (leftUsedWidth >= 0) {
                measureChildWithMargins(mLeftView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                measureChildWithMargins(mRightView, widthMeasureSpec,
                        mLeftView.getMeasuredWidth() + paramsL.leftMargin + paramsL.rightMargin,
                        heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(mRightView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int rightUsed = mRightView.getMeasuredWidth() + paramsR.leftMargin + paramsR.rightMargin
                        + getPaddingLeft() + getPaddingRight();
                if (rightUsed >= widthSize) {
                    mLeftView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
                            , MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
                } else {
                    measureChildWithMargins(mLeftView, widthMeasureSpec,
                            mRightView.getMeasuredWidth() + paramsR.leftMargin + paramsR.rightMargin,
                            heightMeasureSpec, 0);
                }
            }
        }
    }

    /**
     * mLeftView 和RightView 有缺失的情况
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void allBottomMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mBottomViews == null) {
            mBottomViews = new ArrayList<>();
        }
        if (mLeftView != null) {
            LayoutParams params = (LayoutParams) mLeftView.getLayoutParams();
            mBottomViews.add(0, mLeftView);
        }
        if (mRightView != null) {
            LayoutParams params = (LayoutParams) mRightView.getLayoutParams();
            mBottomViews.add(0, mRightView);
        }
        int mTotalHeight = 0;
        for (View view : mBottomViews) {
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mTotalHeight = mTotalHeight + view.getMeasuredHeight() + params.topMargin + params.bottomMargin;
        }
        mTotalHeight = mTotalHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mTotalHeight);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //注意,对l,t,r,b要进行处理，否则出现意外的的错误
        l = 0;
        t = 0;
        r = getMeasuredWidth();
        if (mLeftView == null || mRightView == null) {
            layoutVertical(l, t, r, b);
        } else {
            layoutDirection(l, t, r, b);
        }
    }


    private void layoutDirection(int l, int t, int r, int b) {

        int leftTop = t + getPaddingTop();
        int rightTop = t + getPaddingTop();
        int left = l + getPaddingLeft();

        LayoutParams leftParams = (LayoutParams) mLeftView.getLayoutParams();
        LayoutParams rightParams = (LayoutParams) mRightView.getLayoutParams();
        int rightHeight = rightParams.topMargin + rightParams.bottomMargin + mRightView.getMeasuredHeight();
        int leftHeight = leftParams.topMargin + leftParams.bottomMargin + mLeftView.getMeasuredHeight();

        int followHeight = 0;
        for (View view : mFollowView) {
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            followHeight = followHeight + params.topMargin + params.bottomMargin + view.getMeasuredHeight();
        }
        if (mDirection == DIRECTION_LEFT) {
            leftHeight = leftHeight + followHeight;
        } else {
            rightHeight = rightHeight + followHeight;
        }
        int leftGravity = leftParams.gravity;
        if (rightHeight > leftHeight) {
            //未来支持更多的样式
            switch (leftGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.CENTER_VERTICAL:
                    leftTop = leftTop + (rightHeight - leftHeight) / 2;
                    break;
                default:
                    break;
            }
        }
        int rightGravity = rightParams.gravity;
        if (leftHeight > rightHeight) {
            switch (rightGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.CENTER_VERTICAL:
                    rightTop = rightTop + (leftHeight - rightHeight) / 2;
                    break;
                default:
            }
        }

        //从上到下
        leftTop = leftTop + leftParams.topMargin;
        rightTop = rightTop + rightParams.topMargin;

        if (mDirection == DIRECTION_LEFT) {
            int lR = left + leftParams.leftMargin + mLeftView.getMeasuredWidth();
            mLeftView.layout(left + leftParams.leftMargin, leftTop,
                    lR,
                    leftTop + mLeftView.getMeasuredHeight());
            //右边的View进行布局
            lR = lR + leftParams.rightMargin + rightParams.leftMargin;

            mRightView.layout(lR,
                    rightTop,
                    lR + mRightView.getMeasuredWidth(),
                    rightTop + mRightView.getMeasuredHeight());
        } else {
            int rL = r - getPaddingRight() - rightParams.rightMargin - mRightView.getMeasuredWidth();
            mRightView.layout(rL, rightTop, rL + mRightView.getMeasuredWidth(),
                    rightTop + mRightView.getMeasuredHeight());
            int lR = rL - rightParams.leftMargin - leftParams.rightMargin;
            int lL = lR - mLeftView.getMeasuredWidth();
            mLeftView.layout(lL,
                    leftTop, lR,
                    leftTop + mLeftView.getMeasuredHeight());
        }

        leftTop = leftTop + mLeftView.getMeasuredHeight() + leftParams.topMargin + leftParams.bottomMargin;
        rightTop = rightTop + rightParams.bottomMargin + mRightView.getMeasuredHeight() + rightParams.bottomMargin;

        /**
         * 根据方向不同，进行不同的Layout
         */
        if (mDirection == DIRECTION_LEFT) {
            for (View view : mFollowView) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                leftTop = leftTop + params.topMargin;
                view.layout(left+params.leftMargin, leftTop, left + params.leftMargin + view.getMeasuredWidth(),
                        leftTop + view.getMeasuredHeight());
                leftTop = leftTop + params.bottomMargin + view.getMeasuredHeight();
            }
        } else {
            int rightViewL = r - getPaddingRight() - rightParams.rightMargin
                    - mRightView.getMeasuredWidth() - rightParams.leftMargin;
            for (View view : mFollowView) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                int vL = rightViewL + params.leftMargin;
                int vR = vL + view.getMeasuredWidth();
                rightTop = rightTop + params.topMargin;
                view.layout(vL, rightTop, vR, rightTop + view.getMeasuredHeight() + params.bottomMargin);
                rightTop = rightTop + view.getMeasuredHeight() + params.bottomMargin;
            }
        }
        //剩余的BottomView 进行Layout
        layoutVertical(l, Math.max(leftTop, rightTop) - getPaddingTop(),
                r, b);

    }

    private void layoutVertical(int l, int t, int r, int b) {

        int left = l + getPaddingLeft();
        int top = t + getPaddingTop();
        int right = r - getPaddingRight();

        for (View view : mBottomViews) {
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            int viewLeft = 0;
            int viewRight = 0;
            top = top + params.topMargin;
            int gravity = params.gravity;
            switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.RIGHT:
                    viewRight = right - params.rightMargin;
                    viewLeft = viewRight - view.getMeasuredWidth();
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    viewLeft = ((right - left - view.getMeasuredWidth()) / 2) + params.leftMargin + left;
                    viewRight = viewLeft + view.getMeasuredWidth();
                    break;
                case Gravity.LEFT:
                default:
                    viewLeft = left + params.leftMargin;
                    viewRight = viewLeft + view.getMeasuredWidth();
                    break;

            }
            view.layout(viewLeft, top, viewRight, top + view.getMeasuredHeight());
            top = top + view.getMeasuredHeight() + params.bottomMargin;
        }

    }


    /**
     *整理
     */
    private void prepareChildView() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != GONE) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                if (params.rule == LayoutParams.RULE_LEFT){
                    if (mLeftView == null){
                        mLeftView = view;
                    }else {
                        mBottomViews.add(view);
                    }
                }else if (params.rule == LayoutParams.RULE_RIGHT){
                    if (mRightView == null){
                        mRightView = view;
                    }else {
                        mBottomViews.add(view);
                    }
                }else {
                    mBottomViews.add(view);
                }
            }
        }
    }

    public static class LayoutParams extends MarginLayoutParams {


        public static final int RULE_LEFT = 1;

        public static final int RULE_RIGHT = 2;

        public static final int RULE_BOTTOM = 3;

        /**
         * 规则 left = 1 ;right = 2；其余的 都是3
         * 如果没有指定，那么系统会进行默认的处理
         */
        public int rule = RULE_BOTTOM;
        /**
         * Value for {@link #gravity} indicating that a gravity has not been
         * explicitly specified.
         */
        public static final int UNSPECIFIED_GRAVITY = -1;

        /**
         * The gravity to apply with the View to which these layout parameters
         * are associated.
         * <p>
         * The default value is {@link #UNSPECIFIED_GRAVITY}, which is treated
         * by FrameLayout as {@code Gravity.TOP | Gravity.START}.
         *
         * @attr ref android.R.styleable#FrameLayout_Layout_layout_gravity
         * @see android.view.Gravity
         */
        public int gravity = UNSPECIFIED_GRAVITY;
        /**
         * 权重
         */
        public float layout_weight = -1.0f;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.KRuleLayout);
            if (array.hasValue(R.styleable.KRuleLayout_rule)) {
                rule = array.getInt(R.styleable.KRuleLayout_rule, RULE_BOTTOM);
            }
            if (array.hasValue(R.styleable.KRuleLayout_android_layout_gravity)) {
                gravity = array.getInt(R.styleable.KRuleLayout_android_layout_gravity, UNSPECIFIED_GRAVITY);
            }
            if (array.hasValue(R.styleable.KRuleLayout_android_layout_weight)) {
                layout_weight = array.getFloat(R.styleable.KRuleLayout_android_layout_weight, -1);
            }
            array.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams params) {
            super(params);
        }
    }


}
