/*
 * Copyright 2018 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.hyperwallet.android.model.paging;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.exception.HyperwalletException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for the the paged list response
 */
public class HyperwalletPageList<T> {
    private static final String COUNT = "count";
    private static final String DATA = "data";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";
    private static final String PAGE_LINKS = "links";


    private int mCount;
    private List<T> mDataList;
    private List<HyperwalletPageLink> mHyperwalletPageLinks;
    private int mLimit;
    private int mOffset;

    public HyperwalletPageList(@NonNull final JSONObject page, @NonNull final Class<T> clazz)
            throws HyperwalletException {
        try {
            JSONArray jsonArray = page.getJSONArray(DATA);
            if (jsonArray != null) {
                mDataList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    Constructor<T> constructor = clazz.getConstructor(JSONObject.class);
                    mDataList.add(constructor.newInstance(jsonArray.getJSONObject(i)));
                }
            }
        } catch (Exception e) {
            throw new HyperwalletException(e);
        }

        try {
            JSONArray jsonArray = page.getJSONArray(PAGE_LINKS);
            if (jsonArray != null) {
                mHyperwalletPageLinks = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    mHyperwalletPageLinks.add(new HyperwalletPageLink(jsonArray.optJSONObject(i)));
                }
            }
        } catch (Exception e) {
            throw new HyperwalletException(e);
        }

        mCount = page.optInt(COUNT, 0);
        mLimit = page.optInt(LIMIT, 0);
        mOffset = page.optInt(OFFSET, 0);
    }

    @VisibleForTesting
    public HyperwalletPageList(List<T> dataList) {
        mDataList = dataList;
    }

    public int getCount() {
        return mCount;
    }

    public List<T> getDataList() {
        return mDataList;
    }

    public int getLimit() {
        return mLimit;
    }

    public int getOffset() {
        return mOffset;
    }

    public List<HyperwalletPageLink> getPageLinks() {
        return mHyperwalletPageLinks;
    }


    public HyperwalletPageLink getNextPageLink() {
        return getPageLink("next");
    }

    public HyperwalletPageLink getPreviousageLink() {
        return getPageLink("previous");
    }


    private HyperwalletPageLink getPageLink(String name) {
        if (mHyperwalletPageLinks != null) {
            for (HyperwalletPageLink pageLink : mHyperwalletPageLinks) {
                if (pageLink.getPageParameter().getRel().equals(name)) {
                    return pageLink;
                }
            }
        }
        return null;
    }
}