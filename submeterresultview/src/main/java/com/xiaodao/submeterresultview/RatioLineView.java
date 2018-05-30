package com.xiaodao.submeterresultview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 横向柱状图，带%
 * Created by shijunduan on 2018/5/17.
 */

public class RatioLineView extends View {
    private Context mContext;
    private int mWidth; // 控件宽度
    private int mHight; // 控件高度
    private int mLineWith; // 宽度
    private int mLineHight; // 高度
    private String mTitle; // 标题
    private String mUnit;//单位
    private float mSweepLengthPercent =1; // 绘制长度的百分比 0-100
    private int mTextColor; // 字体颜色
    private int mTextSize; // 字体大小
    private int mBgColor; // 控件容器颜色
    private int mLineBgColor; // 线条背景颜色
    private int mLineColor; //  线条颜色

    private Paint mPaintLine;// 线条的画笔
    private Paint mPaintLineBg;// 线条背景画笔
    private Paint mPaintFen;//百分比画笔
    private Paint mPaintDes;//标题画笔
    private ValueAnimator valueAnimator;

    public void setmSweepLengthPercent(float sweepPercent) {
        if(0<sweepPercent&&sweepPercent<=100){
            valueAnimator = ValueAnimator.ofFloat(1 , sweepPercent);
            valueAnimator.setDuration(400);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSweepLengthPercent = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            valueAnimator.start();

        }
    }

    public RatioLineView(Context context) {
        this(context,null);
    }

    public RatioLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray =context.obtainStyledAttributes(attrs,R.styleable.subratiolineview,defStyleAttr,0);
        mLineWith = typedArray.getDimensionPixelSize(R.styleable.subratiolineview_rl_lineWidth,dpToPx(10));
        mLineHight = typedArray.getDimensionPixelSize(R.styleable.subratiolineview_rl_lineHight,dpToPx(10));
        mTitle = typedArray.getString(R.styleable.subratiolineview_rl_title);
        mUnit = typedArray.getString(R.styleable.subratiolineview_rl_unit);

        mTextColor = typedArray.getColor(R.styleable.subratiolineview_rl_textColor, Color.BLACK);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.subratiolineview_rl_measureTextSize,spToPx(12));
        mBgColor = typedArray.getColor(R.styleable.subratiolineview_rl_bgColor,Color.GRAY);
        mLineColor = typedArray.getColor(R.styleable.subratiolineview_rl_linColor,Color.RED);
        mLineBgColor = typedArray.getColor(R.styleable.subratiolineview_rl_linColor_bg,Color.RED);
        typedArray.recycle();
        init();
    }

    private void init() {


        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(mLineColor);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeCap(Paint.Cap.ROUND);
        mPaintLine.setStrokeWidth(mLineHight);
        mPaintLineBg = new Paint();
        mPaintLineBg.setAntiAlias(true);
        mPaintLineBg.setColor(mLineBgColor);
        mPaintLineBg.setStyle(Paint.Style.STROKE);//stroke 描边，fill填充满
        mPaintLineBg.setStrokeCap(Paint.Cap.ROUND);//绘制线条开始和结束的圆角
        mPaintLineBg.setStrokeWidth(mLineHight);
        mPaintFen = new Paint();
        mPaintFen.setAntiAlias(true);
        mPaintFen.setColor(Color.parseColor("#9198b2"));
        mPaintFen.setTextSize(mTextSize);
        mPaintFen.setTextAlign(Paint.Align.CENTER);

        mPaintFen.setStyle(Paint.Style.STROKE);
        mPaintDes = new Paint();
        mPaintDes.setAntiAlias(true);
        mPaintDes.setColor(Color.parseColor("#9198b2"));
        mPaintDes.setTextSize(mTextSize);
        mPaintDes.setStyle(Paint.Style.STROKE);




    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth  = 0;
        int measureHight = 0;
        int widthMode  = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if(widthMode== MeasureSpec.EXACTLY){
            measureWidth = widthSize;
        }else if(widthMode == MeasureSpec.AT_MOST){
            mWidth = mTextSize*mTitle.length()+mTextSize*mUnit.length()+mTextSize*2+mLineWith+60;//10偏移量
            measureWidth = Math.min(mWidth, widthSize);
        }else if(widthMode == MeasureSpec.UNSPECIFIED){
            mWidth = mTextSize*mTitle.length()+mTextSize*mUnit.length()+mLineWith;
            measureWidth = Math.min(mWidth, widthSize);
        }

        int heightMode  = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(heightMode== MeasureSpec.EXACTLY){
            measureHight = heightSize;
        }else if(heightMode == MeasureSpec.AT_MOST){
            mHight = Math.max(mLineHight,mTextSize);
            measureHight = Math.min(mHight, heightSize);
        }else if(heightMode == MeasureSpec.UNSPECIFIED){
            mHight = Math.max(mLineHight,mTextSize);
            measureHight = mHight;
        }

        setMeasuredDimension(measureWidth,measureHight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制控件背景
        if(mBgColor!=0){
            canvas.drawColor(mBgColor);
        }
        int startLineY =mHight/2;
        int paddRight = dpToPx(8);
        int paddRight3 = dpToPx(16);
        int paddRight2 = dpToPx(30);
        int yPos = (int) ((canvas.getHeight() / 2) - ((mPaintDes.descent() + mPaintDes.ascent()) / 2)) ;
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

        //绘制 标题
        canvas.drawText(mTitle,0,yPos,mPaintDes);
        //绘制 背景 线
        int startLineX =mTitle.length()*mTextSize+paddRight;
        int stopLineX =startLineX+mLineWith;
        canvas.drawLine(startLineX,startLineY,stopLineX,startLineY,mPaintLineBg);
        //绘制线
        canvas.drawLine(startLineX,startLineY,startLineX+mLineWith*(mSweepLengthPercent /100),startLineY,mPaintLine);
        //绘制 “分”
        String fen = (int) mSweepLengthPercent +"";
        canvas.drawText(fen,stopLineX+paddRight3,yPos,mPaintFen);
        canvas.drawText(mUnit,stopLineX+paddRight2,yPos,mPaintDes);//4为100分时的偏移量


    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }


}
