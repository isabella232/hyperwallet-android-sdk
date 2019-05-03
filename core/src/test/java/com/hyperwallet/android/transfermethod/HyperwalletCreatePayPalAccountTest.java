package com.hyperwallet.android.transfermethod;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.HyperwalletStatusTransition.StatusDefinition.ACTIVATED;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.CREATED_ON;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.EMAIL;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.STATUS;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.PayPalAccount;
import com.hyperwallet.android.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.rule.HyperwalletMockWebServer;
import com.hyperwallet.android.rule.HyperwalletSdkMock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
public class HyperwalletCreatePayPalAccountTest {
    @Rule
    public HyperwalletMockWebServer mServer = new HyperwalletMockWebServer();
    @Rule
    public HyperwalletSdkMock mSdkMock = new HyperwalletSdkMock(mServer);
    @Rule
    public HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private HyperwalletListener<PayPalAccount> mListener;

    @Captor
    private ArgumentCaptor<PayPalAccount> mPayPalAccountCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletException> mExceptionCaptor;

    private final CountDownLatch mAwait = new CountDownLatch(1);

    @Test
    public void testCreatePayPalAccount_withSuccess() throws InterruptedException {

        String responseBody = mExternalResourceManager.getResourceContent("paypal_account_response.json");
        mServer.mockResponse().withHttpResponseCode(HttpURLConnection.HTTP_CREATED).withBody(responseBody).mock();

        final PayPalAccount.Builder builder = new PayPalAccount
                .Builder("US", "USD", "jsmith@paypal.com")
                .token("trm-ac5727ac-8fe7-42fb-b69d-977ebdd7b48b");

        final PayPalAccount payPalAccount = builder.build();

        assertThat(payPalAccount.getField(TRANSFER_METHOD_COUNTRY), is("US"));
        assertThat(payPalAccount.getField(TRANSFER_METHOD_CURRENCY), is("USD"));
        assertThat(payPalAccount.getField(EMAIL), is("jsmith@paypal.com"));
        assertThat(payPalAccount.getField(TOKEN), is("trm-ac5727ac-8fe7-42fb-b69d-977ebdd7b48b"));


        Hyperwallet.getDefault().createPayPalAccount(payPalAccount, mListener);
        mAwait.await(50, TimeUnit.MILLISECONDS);

        RecordedRequest recordedRequest = mServer.getRequest();
        verify(mListener).onSuccess(mPayPalAccountCaptor.capture());
        verify(mListener, never()).onFailure(any(HyperwalletException.class));

        PayPalAccount paypalAccountResponse = mPayPalAccountCaptor.getValue();
        assertThat(paypalAccountResponse, is(notNullValue()));

        //paypal account info
        assertThat(recordedRequest.getPath(),
                is("/rest/v3/users/usr-fbfd5848-60d0-43c5-8462-099c959b49c7/paypal-accounts"));

        assertThat(paypalAccountResponse.getField(STATUS), is(ACTIVATED));
        assertThat(paypalAccountResponse.getField(TOKEN), is("trm-ac5727ac-8fe7-42fb-b69d-977ebdd7b48b"));
        assertThat(paypalAccountResponse.getField(TRANSFER_METHOD_COUNTRY), is("US"));
        assertThat(paypalAccountResponse.getField(TRANSFER_METHOD_CURRENCY), is("USD"));
        assertThat(paypalAccountResponse.getField(TYPE), is(PAYPAL_ACCOUNT));
        assertThat(paypalAccountResponse.getField(CREATED_ON), is(notNullValue()));
        assertThat(paypalAccountResponse.getField(EMAIL), is("jsmith@paypal.com"));
    }

    @Test
    public void testCreatePayPalAccount_withValidationError() throws InterruptedException {
        String responseBody = mExternalResourceManager.getResourceContentError("transfer_method_error_response.json");
        mServer.mockResponse().withHttpResponseCode(HttpURLConnection.HTTP_BAD_REQUEST).withBody(responseBody).mock();

        final PayPalAccount payPalAccount = new PayPalAccount
                .Builder("", "USD", "jsmith@paypal.com")
                .token("trm-ac5727ac-8fe7-42fb-b69d-977ebdd7b48b")
                .build();

        Hyperwallet.getDefault().createPayPalAccount(payPalAccount, mListener);
        mAwait.await(50, TimeUnit.MILLISECONDS);

        RecordedRequest recordedRequest = mServer.getRequest();
        verify(mListener, never()).onSuccess(any(PayPalAccount.class));
        verify(mListener).onFailure(mExceptionCaptor.capture());

        HyperwalletException hyperwalletException = mExceptionCaptor.getValue();
        assertThat(hyperwalletException, is(notNullValue()));

        assertThat(recordedRequest.getPath(),
                is("/rest/v3/users/usr-fbfd5848-60d0-43c5-8462-099c959b49c7/paypal-accounts"));

        HyperwalletErrors hyperwalletErrors = hyperwalletException.getHyperwalletErrors();
        assertThat(hyperwalletErrors.getErrors(), hasSize(1));

        HyperwalletError hyperwalletError = hyperwalletErrors.getErrors().get(0);
        assertThat(hyperwalletError.getCode(), is("CONSTRAINT_VIOLATIONS"));
        assertThat(hyperwalletError.getFieldName(), is("transferMethodCountry"));
        assertThat(hyperwalletError.getMessage(), is("You must provide a value for this field"));
    }
}