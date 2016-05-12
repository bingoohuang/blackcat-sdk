package com.github.bingoohuang.blackcat.sdk.utils;

public class StrBuilder {
    private StringBuilder sb;

    public StrBuilder() {
        sb = new StringBuilder();
    }

    public StrBuilder(StringBuilder sb) {
        this.sb = sb;
    }

    public StrBuilder(String str) {
        sb = new StringBuilder(str);
    }

    public StrBuilder p(String str) {
        sb.append(str);
        return this;
    }

    public StrBuilder p(long l) {
        sb.append(l);
        return this;
    }

    public StrBuilder p(int l) {
        sb.append(l);
        return this;
    }

    public StrBuilder p(float f) {
        sb.append(f);
        return this;
    }

    public StrBuilder p(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public StrBuilder deleteLast() {
        sb.deleteCharAt(sb.length() - 1);
        return this;
    }

    public StrBuilder replaceLast(char c) {
        return deleteLast().p(c);
    }

    public StrBuilder p(Object obj) {
        sb.append(obj);
        return this;
    }

    public int len() {
        return sb.length();
    }
}
