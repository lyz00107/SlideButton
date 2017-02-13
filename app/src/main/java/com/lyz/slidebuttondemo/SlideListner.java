package com.lyz.slidebuttondemo;

/**
 * 对滑动按钮的监听事件
 * Created by user on 2017/2/8.
 */
public interface SlideListner {
    /**
     * 完成滑动的执行方法
     */
    public void slideOver();

    /**
     * 未完成滑动的执行方法
     */
    public void slideRestart();
}
