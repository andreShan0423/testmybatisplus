package com.example.mybatisplus.Until;

@FunctionalInterface
public interface BeanCopyUtilCallBack<S, T> {
    /**
     * 定义默认回调方法
     *
     * @param t 来源
     * @param s 目标
     */
    void callBack(S t, T s);
}