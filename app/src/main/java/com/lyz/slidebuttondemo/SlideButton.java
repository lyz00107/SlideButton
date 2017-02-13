package com.lyz.slidebuttondemo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 滑动完成的Button
 * Created by lyz on 2017/2/7.
 */
public class SlideButton extends ViewGroup {

    /**
     * 初始化组件用到的各种属性
     */
    //viewgroup背景的颜色
    private int bgColor;
    //viewgroup的字体大小
    private int bgTextsize;
    //viewgroup的文字内容
    private String bgText;
    //viewgroup的文字颜色
    private int bgTextColor;
    //当移动到哪个位置时结束
    private float offSetFinish;
    //SlideCover的背景颜色
    private Drawable coverBackground;
    //SlideCover的字体大小
    private int coverTextsize;
    //SlideCover的字体内容
    private String coverText;
    //SlideCover的字体颜色
    private int coverTextColor;
    //SlideCover的字体Padding
    private int coverPadding=0;
    /**
     * 绘制paint
     */
    private Paint bgPaint;
    /**
     * viewgourp的字体测量参数
     */
    private Paint.FontMetrics bgFontMetrics;
    /**
     * 子控件
     */
    private SlideCover slideCover;

    /**
     * slideCover点下去的坐标
     */
    private float xTouch=0;
    /**
     * slideCover滑动时的偏移量
     */
    private float xSlideOffset=0;
    /**
     * 滑动监听
     */
    private SlideListner slideListner;
    public SlideButton(Context context) {
        super(context);
    }

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray=context.getTheme().obtainStyledAttributes(attrs,R.styleable.SlideButton,0,0);
        bgColor=typedArray.getColor(R.styleable.SlideButton_bg_color, Color.BLACK);
        coverBackground=typedArray.getDrawable(R.styleable.SlideButton_cover_drawable);
        bgTextsize=typedArray.getDimensionPixelSize(R.styleable.SlideButton_inner_text_size, 20);
        coverTextsize=typedArray.getDimensionPixelSize(R.styleable.SlideButton_cover_text_size, 20);
        bgTextColor=typedArray.getColor(R.styleable.SlideButton_inner_text_color, Color.WHITE);
        coverTextColor=typedArray.getColor(R.styleable.SlideButton_cover_text_color, Color.BLACK);
        bgText=typedArray.getString(R.styleable.SlideButton_inner_text);
        coverText=typedArray.getString(R.styleable.SlideButton_cover_text);
        offSetFinish=typedArray.getFloat(R.styleable.SlideButton_offset_finish, 0.7f);
        coverPadding=typedArray.getDimensionPixelSize(R.styleable.SlideButton_cover_padding, 0);
        //判断是否在0到1之间
        if(offSetFinish>1f){
            offSetFinish=1f;
        }else if(offSetFinish<0f){
            offSetFinish=0f;
        }
        bgInit();
    }

    /**
     * 添加监听事件
     * @param slideListner
     */
    public void setOnSlideListner(SlideListner slideListner){
        this.slideListner=slideListner;
    }

    /**
     * 重新开始
     */
    public void reset(){
        xTouch=0;
        xSlideOffset=0;
        update();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bgPaint.setColor(bgColor);
        //绘制矩形
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), bgPaint);
        bgPaint.setColor(bgTextColor);
        //绘制文字
        float height=  getMeasuredHeight() / 2-bgFontMetrics.top/2-bgFontMetrics.bottom/2;
        if(bgText!=null){
            canvas.drawText(bgText, getMeasuredWidth() / 2,height, bgPaint);
        }

    }

    /**
     * 初始化方法
     */
    private void bgInit(){
        //可以调用onDraw方法
        setWillNotDraw(false);
        //初始化子控件
        slideCover=new SlideCover(getContext());
        bgPaint = new Paint();
        bgPaint=new Paint();
        //文字大小
        bgPaint.setTextSize(bgTextsize);
        //居中
        bgPaint.setTextAlign(Paint.Align.CENTER);
        bgFontMetrics=bgPaint.getFontMetrics();
        //添加子控件
        addView(slideCover);
    }

    /**
     * 重新绘制子控件位置
     */
    private void update(){
        requestLayout();
    }

    /**
     * 自控件手指松开后调用
     */
    private void finish(){
        /**
         * 判断是否到达临界点
         */
        if(xSlideOffset>offSetFinish*getMeasuredWidth()){
            xSlideOffset=getMeasuredWidth();
            xTouch=0;
            slideListner.slideOver();
            update();
        }else {
            xTouch=0;
            xSlideOffset=0;
            slideListner.slideRestart();
            update();
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //判断偏移量是否小于零，小于零偏移量设为0，否则会向左移动
        if(xSlideOffset<0){
            xSlideOffset=0;
        }else  if(xSlideOffset>getMeasuredWidth()){
            xSlideOffset=getMeasuredWidth();
        }
        //子控件位置
        slideCover.layout((int)(0+xSlideOffset),0,(int)(getMeasuredWidth()+xSlideOffset),getMeasuredHeight());
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量子控件
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //获取子控件slideCover
        SlideCover slideCover= (SlideCover) getChildAt(0);
        setMeasuredDimension(slideCover.getMeasuredWidth(),slideCover.getMeasuredHeight());
    }

    /**
     * 覆盖在上面的可滑动的组件
     */
    private class SlideCover extends View {
        private Paint paint;
        private Paint.FontMetrics fontMetrics;
        //箭头距离控件左边的距离
        private  float  left_margin=0;

        private SlideCover(Context context) {
            super(context);
            init();
        }

        /**
         * 初始化方法
         */
        private void init(){
            if(null!=coverBackground){
                setBackgroundDrawable(coverBackground);
            }else {
                setBackgroundColor(Color.WHITE);
            }

            paint=new Paint();
            paint.setColor(coverTextColor);
            paint.setTextSize(coverTextsize);
            paint.setTextAlign(Paint.Align.CENTER);
            fontMetrics=paint.getFontMetrics();
            initAnimal();
        }

        /**
         * 箭头动画，实现平移效果
         */
        private void initAnimal(){
             //变化范围
            ValueAnimator objectAnimator= ObjectAnimator.ofFloat(0.8f, 0.9f);
            //变化周期
            objectAnimator.setDuration(1000);
            //重复类型
            objectAnimator.setRepeatMode(ValueAnimator.RESTART);
            //始终重复
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            //开始动画
            objectAnimator.start();
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    left_margin = (float) animation.getAnimatedValue();

                    postInvalidate();
                }
            });
        }

        @Override
        protected void onDraw(final Canvas canvas) {
            super.onDraw(canvas);
            //计算文字中心点高度
            float height=  getMeasuredHeight() / 2-fontMetrics.top/2-fontMetrics.bottom/2;
            //绘制箭头
            canvas.drawText("＞＞＞＞", getMeasuredWidth() * left_margin, height, paint);
            if(null!=coverText){
                canvas.drawText(coverText, getMeasuredWidth() / 2,height, paint);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize= MeasureSpec.getSize(widthMeasureSpec);
            int heightSize= MeasureSpec.getSize(heightMeasureSpec);
            int widthMode= MeasureSpec.getMode(widthMeasureSpec);
            int heightMode= MeasureSpec.getMode(heightMeasureSpec);
            if(heightMode== MeasureSpec.AT_MOST){//如果是wrap_content布局以字体的高度作为控件高度
                setMeasuredDimension(MeasureSpec.makeMeasureSpec(widthSize, widthMode), MeasureSpec.makeMeasureSpec((int) (fontMetrics.bottom - fontMetrics.top) + coverPadding * 2, heightMode));
            }else {
                setMeasuredDimension(MeasureSpec.makeMeasureSpec(widthSize, widthMode), MeasureSpec.makeMeasureSpec(heightSize + coverPadding * 2, heightMode));
            }

        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(event.getAction()== MotionEvent.ACTION_DOWN){//手指按下后的点击事件
                xTouch=event.getRawX();
            }else if(event.getAction()== MotionEvent.ACTION_MOVE){//移动中的点击事件
                xSlideOffset=event.getRawX()-xTouch;
                update();
            }else if(event.getAction()== MotionEvent.ACTION_UP){
                finish();
            }
            return true;
        }


    }
}
