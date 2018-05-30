package com.xiaodao.submeterresultview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 角度转动是从3点钟方向为0°开始，顺时针转动为正向，如6点钟为90度，9点钟为180度
 * Created by shijunduan on 2018/5/17.
 */

public class MeterResultView extends View {
    private Context mContext;
    private int mWidth; // 控件宽度
    private int mHight; // 控件高度
    private float mCenterX;
    private float mCenterY;
    private int mRadius; // 内圆弧半径
    private int mLineWith; // 圆弧横向宽度
    private int mRadiusTotal; // 外圆弧半径 = 内圆弧半径 +  圆弧横向宽度
    private int mStartAngle; // 起始角度
    private int mSweepAngle; // 绘制bg的角度
    private int mCurrentSweepAngle; // 当前滑动的角度
    private float mSweepAnglePercent=1; // 绘制角度的百分比 0-100
    private int mTextColor; // 字体颜色
    private int mTextSize; // 字体大小
    private int mBgColor; // 控件容器颜色
    private int mCircleBgColor; // 圆弧背景颜色
    private int mCircleColor; //  圆弧颜色

    private Paint mPaintArc;// 圆弧的画笔
    private Paint mPaintArcBg;// 圆弧背景画笔
    private Paint mPaintFen;//画笔
    private Paint mPaintDes;//画笔
    private Paint mPaintLine;//分割
    private RectF mCircleRectF;//绘制圆弧的矩形边界
    private ValueAnimator valueAnimator;

    public void setmSweepAnglePercent(float sweepAnglePercent) {
        if(0<sweepAnglePercent&&sweepAnglePercent<100){
            this.mSweepAnglePercent = sweepAnglePercent;
            valueAnimator = ValueAnimator.ofFloat(0 ,mSweepAnglePercent);
            valueAnimator.setDuration(400);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSweepAnglePercent = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            valueAnimator.start();

//            invalidate();
        }
    }

    public MeterResultView(Context context) {
        this(context,null);
    }

    public MeterResultView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeterResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray =context.obtainStyledAttributes(attrs,R.styleable.submeterresultview,defStyleAttr,0);
        mRadius =  typedArray.getDimensionPixelSize(R.styleable.submeterresultview_mr_radius,dpToPx(80));
        mStartAngle = typedArray.getInt(R.styleable.submeterresultview_mr_startAngle,180);
        mSweepAngle = typedArray.getInt(R.styleable.submeterresultview_mr_sweepAngle,180);
        mLineWith = typedArray.getDimensionPixelSize(R.styleable.submeterresultview_mr_stripeWidth,dpToPx(10));
        mTextColor = typedArray.getColor(R.styleable.submeterresultview_mr_textColor, Color.BLACK);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.submeterresultview_mr_measureTextSize,spToPx(12));
        mBgColor = typedArray.getColor(R.styleable.submeterresultview_mr_bgColor,Color.GRAY);
        mCircleColor = typedArray.getColor(R.styleable.submeterresultview_mr_arcColor,Color.RED);
        mCircleBgColor = typedArray.getColor(R.styleable.submeterresultview_mr_arcColor_bg,Color.RED);
        typedArray.recycle();
        init();
    }

    private void init() {
        mRadiusTotal = mRadius+mLineWith;
        mCenterX = mCenterY = 0.0f;
        if (mStartAngle <= 180 && mStartAngle + mSweepAngle >= 180) {
            mWidth = mRadiusTotal * 2 ;// + getPaddingLeft() + getPaddingRight()+ dpToPx(2) * 2;
        } else {
            float[] point1 = getCoordinatePoint(mRadiusTotal, mStartAngle);
            float[] point2 = getCoordinatePoint(mRadiusTotal, mStartAngle + mSweepAngle);
            float max = Math.max(Math.abs(point1[0]), Math.abs(point2[0]));
            mWidth = (int) (max * 2 );//+ getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2;
        }
        if ((mStartAngle <= 90 && mStartAngle + mSweepAngle >= 90)
                || (mStartAngle <= 270 && mStartAngle + mSweepAngle >= 270)) {
            mHight = mRadiusTotal * 2 + getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2;
        } else {
            float[] point1 = getCoordinatePoint(mRadiusTotal, mStartAngle);
            float[] point2 = getCoordinatePoint(mRadiusTotal, mStartAngle + mSweepAngle);
            float max = Math.max(Math.abs(point1[1]), Math.abs(point2[1]));
            mHight = (int) (max * 2 + getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2);
        }
        mCenterX = mWidth / 2.0f;
        mCenterY = mHight / 2.0f;

        mCircleRectF = new RectF(mLineWith,mLineWith,mWidth-mLineWith,mHight-mLineWith);
        mPaintArc = new Paint();
        mPaintArc.setAntiAlias(true);
        mPaintArc.setColor(mCircleColor);
        mPaintArc.setStyle(Paint.Style.STROKE);
        mPaintArc.setStrokeCap(Paint.Cap.ROUND);
        mPaintArc.setStrokeWidth(mLineWith);
        mPaintArcBg = new Paint();
        mPaintArcBg.setAntiAlias(true);
        mPaintArcBg.setColor(mCircleBgColor);
        mPaintArcBg.setStyle(Paint.Style.STROKE);//stroke 描边，fill填充满
        mPaintArcBg.setStrokeCap(Paint.Cap.ROUND);//绘制线条开始和结束的圆角
        mPaintArcBg.setStrokeWidth(mLineWith);
        mPaintFen = new Paint();
        mPaintFen.setAntiAlias(true);
        mPaintFen.setColor(mTextColor);
        mPaintFen.setTextSize(mTextSize);
        mPaintFen.setStyle(Paint.Style.STROKE);
        mPaintDes = new Paint();
        mPaintDes.setAntiAlias(true);
        mPaintDes.setColor(Color.parseColor("#9198b2"));
        mPaintDes.setTextSize(mTextSize/2);
        mPaintDes.setStyle(Paint.Style.STROKE);

        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(Color.parseColor("#f88166"));
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeCap(Paint.Cap.ROUND);
        mPaintLine.setStrokeWidth(3);



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
            measureWidth = Math.min(mWidth, widthSize);
        }else if(widthMode == MeasureSpec.UNSPECIFIED){
            measureWidth = Math.min(mWidth, widthSize);
        }

        int heightMode  = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(heightMode== MeasureSpec.EXACTLY){
            measureHight = heightSize;
        }else if(heightMode == MeasureSpec.AT_MOST){
            measureHight = Math.min(mHight, heightSize);
        }else if(heightMode == MeasureSpec.UNSPECIFIED){
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
        //绘制圆弧背景
        canvas.drawArc(mCircleRectF,mStartAngle,mSweepAngle,false,mPaintArcBg);
        //绘制圆弧进度,根据背景的弧长*80%。在算出结束点的xy坐标
        canvas.drawArc(mCircleRectF,mStartAngle,mSweepAngle*(mSweepAnglePercent/100),false,mPaintArc);
        //绘制中心数组和“分”
        String fen = (int)mSweepAnglePercent+"";
        float txtX = mCenterX-(fen.length()+1)*mTextSize/4;//从圆心x位置向左移动，（fen的长度+“分”的长度）*字体的大小/4
        float txtY =mCenterY+dpToPx(10);//从圆心详细移动20dp
        canvas.drawText(fen,txtX,txtY,mPaintFen);
        canvas.drawText(" 分",txtX+fen.length()*mTextSize/2,txtY,mPaintDes);

        //绘制圆角分隔线
        int lineLength = mTextSize*3/2;
        int lineHight = dpToPx(3);
        float startLineX = mCenterX-lineLength/2;
        float startLineY = txtY+mTextSize/3;
        float stopLineX = startLineX+lineLength;
        mPaintLine.setStrokeWidth(lineHight);
        canvas.drawLine(startLineX,startLineY,stopLineX,startLineY,mPaintLine);
        //绘制“综合评价”
        String desTxt = "综合评价";
        float desTxtX = mCenterX-desTxt.length()/2*mTextSize/2;
        canvas.drawText(desTxt,desTxtX,startLineY+lineHight+mTextSize/2,mPaintDes);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    /**
     * 依圆心坐标，半径，扇形角度，计算出扇形终射线与圆弧交叉点的xy坐标
     */
    public float[] getCoordinatePoint(int radius, float cirAngle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(cirAngle); // 将角度转换为弧度
        if (cirAngle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (cirAngle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (cirAngle > 90 && cirAngle < 180) {
            arcAngle = Math.PI * (180 - cirAngle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (cirAngle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (cirAngle > 180 && cirAngle < 270) {
            arcAngle = Math.PI * (cirAngle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (cirAngle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - cirAngle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }
}
