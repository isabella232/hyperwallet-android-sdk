package com.hyperwallet.android.model.graphql.keyed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static com.hyperwallet.android.model.graphql.Fee.FeeRate.FLAT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_ACCOUNT;

import com.hyperwallet.android.model.graphql.Fee;
import com.hyperwallet.android.rule.ExternalResourceManager;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class TransferMethodTypeTest {

    @Rule
    public final ExternalResourceManager mResourceManager = new ExternalResourceManager();
    @Rule
    public final ExpectedException mThrown = ExpectedException.none();

    @Test
    public void testTransferMethodType_convertJsonObject() throws Exception {
        String data = mResourceManager.getResourceContent("tm_type_item.json");
        JSONObject jsonObject = new JSONObject(data);
        TransferMethodType transferMethodType = new TransferMethodType(jsonObject);
        assertThat(transferMethodType.getCode(), is(BANK_ACCOUNT));
        assertThat(transferMethodType.getName(), is("Bank Account"));
        assertThat(transferMethodType.getProcessingTime().getValue(), is("1-3 Business days"));
        assertThat(transferMethodType.getProcessingTime().getCountry(), is("US"));
        assertThat(transferMethodType.getProcessingTime().getCurrency(), is("USD"));
        assertThat(transferMethodType.getFees(), hasSize(1));
        final Set<Fee> fees = transferMethodType.getFees();
        List<Fee> feeList = new ArrayList<>(fees);
        assertThat(feeList.get(0).getValue(), is("2.00"));
        assertThat(feeList.get(0).getFeeRateType(), is(FLAT));
    }

    @Test
    public void testTransferMethodType_convertJsonObjectWithoutConnection() throws Exception {
        String data = mResourceManager.getResourceContent("tm_type_without_fees_item.json");
        JSONObject jsonObject = new JSONObject(data);
        TransferMethodType transferMethodType = new TransferMethodType(jsonObject);
        assertThat(transferMethodType.getCode(), is(BANK_ACCOUNT));
        assertThat(transferMethodType.getName(), is("Bank Account"));
        assertThat(transferMethodType.getProcessingTime().getValue(), is("1-3 Business days"));
        assertThat(transferMethodType.getFees(), is(empty()));
    }


    @Test
    public void testTransferMethodType_isNotEqual() throws Exception {
        String data = mResourceManager.getResourceContent("tm_type_item.json");
        JSONObject jsonObjectData = new JSONObject(data);
        TransferMethodType transferMethodType = new TransferMethodType(jsonObjectData);


        String anotherData = mResourceManager.getResourceContent("tm_type_item_bank_card.json");
        JSONObject anotherJsonObjectData = new JSONObject(anotherData);
        TransferMethodType anotherTransferMethodType = new TransferMethodType(
                anotherJsonObjectData);

        assertThat(transferMethodType.equals(anotherTransferMethodType), is(false));
    }
}