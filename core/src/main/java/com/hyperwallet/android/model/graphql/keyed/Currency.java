/*
 *  The MIT License (MIT)
 *  Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  associated documentation files (the "Software"), to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge, publish, distribute,
 *  sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 *  NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.model.graphql.keyed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.transfermethod.TransferMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the transfer method currency that are available in a given country
 */
public class Currency implements KeyedNode {

    private static final String CURRENCY_CODE = NODE_CODE;
    private static final String CURRENCY_NAME = NODE_NAME;
    private static final String TRANSFER_METHOD_TYPES = "transferMethodTypes";

    private final Set<TransferMethodType> mTransferMethodTypes;
    private final String mCode;
    private final MappedConnection<TransferMethodType> mTransferMethodTypeMappedConnection;
    private final String mName;

    /**
     * Constructor to build Currency based on {@link JSONObject} representation
     *
     * @param currency JSON object that represents currency data
     */
    public Currency(@NonNull final JSONObject currency) throws JSONException, NoSuchMethodException,
            IllegalAccessException, InstantiationException, InvocationTargetException {
        mCode = currency.optString(CURRENCY_CODE);
        mName = currency.optString(CURRENCY_NAME);
        mTransferMethodTypes = new LinkedHashSet<>(1);
        JSONObject transferMethodTypes = currency.optJSONObject(TRANSFER_METHOD_TYPES);
        if (transferMethodTypes != null && transferMethodTypes.length() != 0) {
            mTransferMethodTypeMappedConnection =
                    new MappedConnection<>(transferMethodTypes, TransferMethodType.class);
        } else {
            mTransferMethodTypeMappedConnection = null;
        }
    }

    /**
     * @return Currency code represented in ISO 4217 three-letter code format
     */
    @NonNull
    @Override
    public String getCode() {
        return mCode;
    }

    /**
     * @return Currency name in ISO 4217 currency names format
     */
    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    /**
     * @return set of {@code TransferMethodType} represented by this {@link Currency} instance
     */
    @NonNull
    public Set<TransferMethodType> getTransferMethodTypes() {
        if (mTransferMethodTypeMappedConnection != null && mTransferMethodTypes.isEmpty()) {
            mTransferMethodTypes.addAll(mTransferMethodTypeMappedConnection.getNodes());
            return mTransferMethodTypes;
        }
        return mTransferMethodTypes;
    }

    /**
     * Get specific Transfer Method Type
     *
     * @param transferMethodType transfer method type {@link TransferMethod.TransferMethodTypes}
     * @return Transfer method type representation based from parameter passed, if exists
     */
    @Nullable
    public TransferMethodType getTransferMethodType(@NonNull final String transferMethodType) {
        if (mTransferMethodTypeMappedConnection != null) {
            return mTransferMethodTypeMappedConnection.getNode(transferMethodType);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        Currency currency = (Currency) o;
        return Objects.equals(getCode(), currency.getCode()) &&
                Objects.equals(getName(), currency.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getName());
    }
}
