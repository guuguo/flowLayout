package top.guuguo.flowlayout;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.MeasureSpec.EXACTLY;

/**
 * mimi 创造于 2016-12-22. 项目 diary
 */

public class FlowLayout extends ViewGroup {

  /**
   * 存储所有的View，按行记录
   */
  private List<List<View>> mAllViews = new ArrayList<List<View>>();

  /**
   * 记录设置单行显示的标志
   */
  private boolean mIsSingleLine;

  /**
   * 数据适配器
   */
  private RecyclerView.Adapter mAdapter;

  /**
   * View之间的间距
   */
  private int mDividerSpace;
  /**
   * 列数
   */
  private int mColumnNumbers;
  /**
   * 是否设置了网格布局
   */
  private boolean mIsGridMode;
  /**
   * 是否每行居中处理
   */
  private int mLineAlign = LineAlignLeft;
  private int mCheckType;
  /**
   * 行数
   */
  private int mRowNumbers;
  /**
   * Grid 模式，chain Style
   */
  private int mChainStyle = ChainStyleSpreadInside;

  public static final int ChainStyleSpread = 0;
  public static final int ChainStylePacked = 1;
  public static final int ChainStyleSpreadInside = 2;

  public static final int LineAlignLeft = 0;
  public static final int LineAlignCenter = 1;
  public static final int LineAlignRight = 2;
  /**
   * 分割区域颜色
   */
  private int mDividerColor;
  private HashMap<View, Rect> viewLayoutRectMap = new HashMap<>();
  private Paint mLinePaint = new Paint();

  /**
   * 网格布局子布局平均宽度
   */
  private int widthGridChildAv = 0;
  private int heightGridChildAv = 0;

  private ArrayList<RecyclerView.ViewHolder> viewHolders = new ArrayList();

  public FlowLayout(Context context) {
    this(context, null);
  }

  public FlowLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }


  public void setDividerColor(int mDividerColor) {
    this.mDividerColor = mDividerColor;
    if (mDividerColor != Integer.MAX_VALUE) {
      mLinePaint.setColor(mDividerColor);
    }
  }

  public void setDividerSpace(int mDividerSpace) {
    this.mDividerSpace = mDividerSpace;
    mLinePaint.setStrokeWidth(mDividerSpace);
  }

  private void init(Context context, AttributeSet attrs) {
    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
    setDividerColor(ta.getColor(R.styleable.FlowLayout_dividerColor, Integer.MAX_VALUE));
    setDividerSpace((int) ta.getDimension(R.styleable.FlowLayout_divideSpace, 0));
    mColumnNumbers = ta.getInteger(R.styleable.FlowLayout_columnNumbers, 0);
    mRowNumbers = ta.getInteger(R.styleable.FlowLayout_rowNumbers, mLineAlign);
    mLineAlign = ta.getInteger(R.styleable.FlowLayout_lineAlign, 0);
    mCheckType = ta.getInteger(R.styleable.FlowLayout_checkType, FlowAdapter.CHECK_TYPE_NONE);
    mChainStyle = ta.getInteger(R.styleable.FlowLayout_chainStyle, mChainStyle);
    if (mColumnNumbers != 0) {
      mIsGridMode = true;
    }
    ta.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (mIsGridMode) {
      setGridMeasure(widthMeasureSpec, heightMeasureSpec);
    } else {
      setFlowMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  /**
   * gridMode高度是否平均分布(如果layout高度确定，则平均分布，不确定则根据子布局高度自适应)
   */
  private boolean isHeightAvg = false;

  private void setGridMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // 获得它的父容器为它设置的测量模式和大小
    int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
    int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
    int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
    int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
    //获取viewgroup的padding
    int paddingLeft = getPaddingLeft();
    int paddingRight = getPaddingRight();
    int paddingTop = getPaddingTop();
    int paddingBottom = getPaddingBottom();
    //最终的宽高值
    mAllViews.clear();
    //推测行数
    mRowNumbers = getChildCount() % mColumnNumbers == 0 ? getChildCount() / mColumnNumbers
        : (getChildCount() / mColumnNumbers + 1);
    if (mRowNumbers == 0) {
      mRowNumbers++;
    }
    int maxChildHeight = 0;
    int maxHeight = 0;
    widthGridChildAv =
        (sizeWidth - (mColumnNumbers - 1) * mDividerSpace - paddingLeft - paddingRight)
            / mColumnNumbers;
    heightGridChildAv =
        (sizeHeight - (mRowNumbers - 1) * mDividerSpace - paddingTop - paddingBottom) / mRowNumbers;
    if (modeHeight == MeasureSpec.EXACTLY) {
      isHeightAvg = true;
    } else {
      isHeightAvg = false;
    }
    //统计最大高度/最大宽度
    int line = 0;
    mAllViews.add(new ArrayList<View>());
    for (int i = 0; i < getChildCount(); i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        if (mAllViews.get(line).size() >= mColumnNumbers) { //如果上一行已满  开始下一行
          maxHeight += maxChildHeight;
          maxChildHeight = 0;
          mAllViews.add(new ArrayList<View>());
          line++;
        }
        mAllViews.get(line).add(child);

        int widthSpec;
        widthSpec = getChildMeasureSpec(widthMeasureSpec, sizeWidth - widthGridChildAv, lp.width);
        int heightSpec;
        if (modeHeight != MeasureSpec.EXACTLY) {
          heightSpec = getChildMeasureSpec(heightMeasureSpec, paddingTop + paddingBottom,
              lp.height);
          child.measure(widthSpec, heightSpec);
          heightGridChildAv = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        } else {
          heightSpec = getChildMeasureSpec(heightMeasureSpec, sizeHeight - heightGridChildAv,
              lp.height);
          child.measure(widthSpec, heightSpec);
        }
        // 得到child的lp
        maxChildHeight = Math
            .max(maxChildHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
      }
    }
    maxHeight += maxChildHeight;
    int heightResult = maxHeight + mDividerSpace * (line) + paddingBottom + paddingTop;
    setMeasuredDimension(sizeWidth, (modeHeight == EXACTLY) ? sizeHeight : heightResult);
  }

  private void setGridLayout() {
    if (getChildCount() > 0) {
      int lastLineHeight = getPaddingTop();
      for (int i = 0; i < mAllViews.size(); i++) {
        int maxChildLineHeight = 0;
        for (int j = 0; j < mAllViews.get(i).size(); j++) {
          final View child = mAllViews.get(i).get(j);
          if (child != null) {
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //子元素最大可能占位宽度
            int stayMaxWidth = widthGridChildAv - lp.leftMargin - lp.rightMargin;
            int childLeft = 0;

            if (stayMaxWidth > child.getMeasuredWidth()) {
              if (mChainStyle == ChainStyleSpread) {
                int extent =
                    (stayMaxWidth - child.getMeasuredWidth()) * mColumnNumbers / (mColumnNumbers
                        + 1);
                childLeft =
                    getPaddingLeft() + j * (extent + child.getMeasuredWidth() + mDividerSpace)
                        + lp.leftMargin + extent;
              } else if (mChainStyle == ChainStylePacked) {
                int extent = (stayMaxWidth - child.getMeasuredWidth()) * mColumnNumbers / 2;
                childLeft = getPaddingLeft() + j * (child.getMeasuredWidth() + mDividerSpace)
                    + lp.leftMargin + extent;
              } else if (mChainStyle == ChainStyleSpreadInside) {
                int extent =
                    (stayMaxWidth - child.getMeasuredWidth()) * mColumnNumbers / (mColumnNumbers
                        - 1);
                childLeft =
                    getPaddingLeft() + j * (extent + child.getMeasuredWidth() + mDividerSpace)
                        + lp.leftMargin;
              }
            } else {
              childLeft = getPaddingLeft() + j * (widthGridChildAv + mDividerSpace) + lp.leftMargin;
            }
            if (isHeightAvg) {
              int stayMaxHeight = heightGridChildAv - lp.topMargin - lp.bottomMargin;
              int centerVerticalSpace = 0;
              if (stayMaxHeight > child.getMeasuredHeight()) {
                centerVerticalSpace = (stayMaxHeight - child.getHeight()) / 2;
              }
              int childTop = lastLineHeight + lp.topMargin
                  + centerVerticalSpace; //(int) (getPaddingTop() + i * (heightGridChildAv + mDividerSpace) + lp.topMargin + centerVerticalSpace);
              child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(),
                  child.getMeasuredHeight() + childTop);
              //child占用的地方(包含margin)
              int childHeight = heightGridChildAv + mDividerSpace;
              maxChildLineHeight = Math.max(maxChildLineHeight, childHeight);
            } else {
              int childTop = lastLineHeight
                  + lp.topMargin; //(int) (getPaddingTop() + i * (heightGridChildAv + mDividerSpace) + lp.topMargin + centerVerticalSpace);
              child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(),
                  child.getMeasuredHeight() + childTop);
              int childHeight =
                  child.getMeasuredHeight() + lp.bottomMargin + lp.topMargin + mDividerSpace;
              maxChildLineHeight = Math.max(maxChildLineHeight, childHeight);
            }
          }
        }
        lastLineHeight += maxChildLineHeight;
      }
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (mIsGridMode) {
      setGridLayout();
    } else {
      setFlowLayout();
    }
  }


  /**
   * 流式布局的测量模式
   */
  private void setFlowMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    viewLayoutRectMap.clear();
    // 获得它的父容器为它设置的测量模式和大小
    int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
    int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
    int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
    int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

    int lastLineHeight = getPaddingTop();
    int currentLineLayoutWidth = getPaddingLeft();
    //该行最大高度
    int maxChildLineHeight = 0;

    int currentLine = 0;
    mAllViews.clear();
    mAllViews.add(new ArrayList<View>());
    // 遍历所有的孩子
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        int childPlaceHeight = childHeight + lp.topMargin + lp.bottomMargin;
        int childPlaceWidth = childWidth + lp.leftMargin + lp.rightMargin;
        // 如果已经需要换行
        if (currentLineLayoutWidth + childPlaceWidth + getPaddingRight() + mDividerSpace
            > sizeWidth) {
          offsetLineView(sizeWidth, currentLine, currentLineLayoutWidth);
          //换行后当前行高
          currentLineLayoutWidth = getPaddingLeft();
          //已布局行高加上该行最大行高
          lastLineHeight += (maxChildLineHeight + mDividerSpace);
          //最大行高归零
          maxChildLineHeight = 0;

          currentLine++;
          mAllViews.add(new ArrayList<View>());

        }
        /**
         * 布局该child
         */
        if (currentLineLayoutWidth != getPaddingLeft()) {
          currentLineLayoutWidth += mDividerSpace;
        }
        int childLeft = currentLineLayoutWidth + lp.leftMargin;
        int childTop = lastLineHeight + lp.topMargin;
        currentLineLayoutWidth += childPlaceWidth;
        maxChildLineHeight = Math.max(maxChildLineHeight, childPlaceHeight);

        viewLayoutRectMap.put(child,
            new Rect(childLeft, childTop, childLeft + child.getMeasuredWidth(),
                child.getMeasuredHeight() + childTop));
        mAllViews.get(currentLine).add(child);
      }
      if (i == getChildCount() - 1) {
        offsetLineView(sizeWidth, currentLine, currentLineLayoutWidth);
      }
    }
    setMeasuredDimension(sizeWidth, lastLineHeight + maxChildLineHeight + getPaddingBottom());
  }

  private void offsetLineView(int sizeWidth, int currentLine, int currentLineLayoutWidth) {
    switch (mLineAlign) {
      case LineAlignLeft:
        break;
      case LineAlignCenter:
        int centerOffset = (sizeWidth - currentLineLayoutWidth - getPaddingRight()) / 2;
        for (View childView : mAllViews.get(currentLine)) {
          Rect rect = viewLayoutRectMap.get(childView);
          rect.left += centerOffset;
          rect.right += centerOffset;
        }
        break;
      case LineAlignRight:
        int rightOffset = (sizeWidth - currentLineLayoutWidth - getPaddingRight());
        for (View childView : mAllViews.get(currentLine)) {
          Rect rect = viewLayoutRectMap.get(childView);
          rect.left += rightOffset;
          rect.right += rightOffset;
        }
        break;
    }
  }

  /**
   * 流式布局的布局模式
   */
  private void setFlowLayout() {
    // 遍历所有的孩子
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        Rect rect = viewLayoutRectMap.get(child);
        child.layout(rect.left, rect.top, rect.right, rect.bottom);
      }
    }
  }

  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (mDividerColor != Integer.MAX_VALUE && mIsGridMode && mDividerSpace > 0) {
      for (int i = 1; i < mColumnNumbers; i++) {
        canvas.drawLine(getPaddingLeft() + widthGridChildAv * i + mDividerSpace / 2, getPaddingTop()
            , getPaddingLeft() + widthGridChildAv * i + mDividerSpace / 2,
            getMeasuredHeight() - getPaddingBottom(), mLinePaint);
      }
      for (int j = 1; j < mRowNumbers; j++) {
        canvas
            .drawLine(getPaddingLeft(), getPaddingTop() + heightGridChildAv * j + mDividerSpace / 2,
                getMeasuredWidth() - getPaddingRight()
                , getPaddingTop() + heightGridChildAv * j + mDividerSpace / 2, mLinePaint);
      }
    }

  }

  public void setAllViews(List<View> views) {
    removeAllViews();
    if (views == null || views.size() == 0) {
      return;
    }
    for (int i = 0; i < views.size(); i++) {
      View view = views.get(i);
      addView(view);
    }
  }

  /**
   * 删除所有view
   */
  public boolean clearViews() {
    if (getChildCount() > 0) {
      removeAllViews();
      return true;
    }
    return false;
  }

  /**
   * 是否只显示单行
   */
  public void setSingleLine(boolean isSingle) {
    mIsSingleLine = isSingle;
    requestLayout();
  }

  /**
   * 是否单行显示
   *
   * @return true 单行显示 false 多行显示
   */
  public boolean isSingleLine() {
    return mIsSingleLine;
  }

  /**
   * 设置数据适配器
   */
  public void setAdapter(RecyclerView.Adapter adapter) {
    mAdapter = adapter;
    if (mAdapter instanceof FlowAdapter) {
      ((FlowAdapter) mAdapter).setCheckLimit(mCheckType);
    }
    if (adapter != null && !adapter.hasObservers()) {
      adapter.registerAdapterDataObserver(observer);
    }
    notifyChange();
  }

  /**
   * 返回选中的map ,key=position value=item 数据
   */
  public HashMap getCheckedItemsMap() {
    if (mAdapter instanceof FlowAdapter) {
      return ((FlowAdapter) mAdapter).getCheckedMap();
    } else {
      return new HashMap<>();
    }
  }

  private void notifyChange() {
    viewHolders.clear();
    for (int i = 0; i < mAdapter.getItemCount(); i++) {
      RecyclerView.ViewHolder holder;

      holder = mAdapter.createViewHolder(this, mAdapter.getItemViewType(i));
      viewHolders.add(holder);
      this.addView(holder.itemView);
      mAdapter.bindViewHolder(holder, i);
    }
  }

  /**
   * 返回网格布局的间隔距离
   */
  public float getDividerSpace() {
    return mDividerSpace;
  }


  /**
   * 设置列数
   */
  public void setColumnNumbers(int columnNumbers) {
    mColumnNumbers = columnNumbers;
    if (mColumnNumbers != 0) {
      mIsGridMode = true;
    }
    requestLayout();
  }

  /**
   * 获得列数
   */
  public int getColumnNumbers() {
    return mColumnNumbers;
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new MarginLayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateLayoutParams(LayoutParams p) {
    return new MarginLayoutParams(p);
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new MarginLayoutParams(super.generateDefaultLayoutParams());
  }

  @NonNull
  private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
    @Override
    public void onChanged() {
      super.onChanged();
      notifyChange();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
      super.onItemRangeChanged(positionStart, itemCount);
      for (int i = positionStart; i < positionStart + itemCount; i++) {
        mAdapter.bindViewHolder(viewHolders.get(i), i);
        mAdapter.getItemViewType(i);
      }
    }
  };

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (mAdapter != null && mAdapter.hasObservers()) {
      mAdapter.unregisterAdapterDataObserver(observer);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (mAdapter != null && !mAdapter.hasObservers())
      mAdapter.registerAdapterDataObserver(observer);
  }
}
