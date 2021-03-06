package com.hyperwallet.android.transfermethod;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.CARD_BRAND;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.CARD_NUMBER;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.CARD_TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.CREATED_ON;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.DATE_OF_EXPIRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.STATUS;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.util.HttpMethod.POST;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.exception.HyperwalletRestException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.BankCard;
import com.hyperwallet.android.rule.ExternalResourceManager;
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
public class CreateBankCardTest {

    @Rule
    public ExternalResourceManager mExternalResourceManager = new ExternalResourceManager();
    @Rule
    public HyperwalletMockWebServer mServer = new HyperwalletMockWebServer();
    @Rule
    public HyperwalletSdkMock mHyperwalletSdkMock = new HyperwalletSdkMock(mServer);
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Mock
    private HyperwalletListener<BankCard> mListener;
    @Captor
    private ArgumentCaptor<BankCard> mListTransferMethodCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletException> mExceptionCaptor;


    private CountDownLatch mAwait = new CountDownLatch(1);

    @Test
    public void testCreateBankCard_withSuccess() throws InterruptedException {

        final BankCard bankCard = new BankCard.Builder()
                .cardBrand("VISA")
                .cardNumber("4216701111111114")
                .cardType("DEBIT")
                .dateOfExpiry("2019-11")
                .token("trm-fake-token")
                .transferMethodCountry("US")
                .transferMethodCurrency("USD")
                .build();

        String responseBody = mExternalResourceManager.getResourceContent("add_card_response.json");
        mServer.mockResponse().withHttpResponseCode(HttpURLConnection.HTTP_CREATED).withBody(responseBody).mock();


        Hyperwallet.getDefault().createBankCard(bankCard, mListener);
        mAwait.await(500, TimeUnit.MILLISECONDS);

        verify(mListener).onSuccess(mListTransferMethodCaptor.capture());
        verify(mListener, never()).onFailure(any(HyperwalletException.class));

        BankCard bankCardResponse = mListTransferMethodCaptor.getValue();
        RecordedRequest recordedRequest = mServer.getRequest();
        assertThat(recordedRequest.getPath(),
                is("/rest/v3/users/test-user-token/bank-cards"));
        assertThat(recordedRequest.getMethod(), is(POST.name()));

        assertThat(bankCardResponse.getField(CARD_BRAND), is("VISA"));
        assertThat(bankCardResponse.getField(CARD_NUMBER), is(equalTo("************0114")));
        assertThat(bankCardResponse.getField(CARD_TYPE), is("DEBIT"));
        assertThat(bankCardResponse.getField(DATE_OF_EXPIRY), is("2019-11"));
        assertThat(bankCardResponse.getField(TOKEN), is("trm-fake-token"));
        assertThat(bankCardResponse.getField(TRANSFER_METHOD_COUNTRY), is(equalTo("US")));
        assertThat(bankCardResponse.getField(TRANSFER_METHOD_CURRENCY), is(equalTo("USD")));
        assertThat(bankCardResponse.getField(TYPE), is("BANK_CARD"));
        assertThat(bankCardResponse.getField(CREATED_ON), is(equalTo("2019-01-08T00:56:15")));
        assertThat(bankCardResponse.getField(STATUS), is(equalTo("ACTIVATED")));
    }

    @Test
    public void testCreateBankCard_withValidationError() throws InterruptedException {

        final BankCard bankCard = new BankCard.Builder()
                .cardBrand("AMEX")
                .cardNumber("1216701111111114")
                .cardType("DEBIT")
                .dateOfExpiry("2018-11")
                .token("trm-fake-token")
                .transferMethodCountry("US")
                .transferMethodCurrency("USD")
                .build();

        String responseBody = mExternalResourceManager.getResourceContentError("add_card_error_response.json");
        mServer.mockResponse().withHttpResponseCode(HttpURLConnection.HTTP_BAD_REQUEST).withBody(responseBody).mock();

        Hyperwallet.getDefault().createBankCard(bankCard, mListener);
        mAwait.await(500, TimeUnit.MILLISECONDS);

        verify(mListener, never()).onSuccess(any(BankCard.class));
        verify(mListener).onFailure(mExceptionCaptor.capture());
        RecordedRequest recordedRequest = mServer.getRequest();
        assertThat(recordedRequest.getPath(),
                is("/rest/v3/users/test-user-token/bank-cards"));
        assertThat(recordedRequest.getMethod(), is(POST.name()));

        HyperwalletException hyperwalletException = mExceptionCaptor.getValue();
        assertThat(hyperwalletException, is(notNullValue()));
        assertThat(((HyperwalletRestException) hyperwalletException).getHttpCode(),
                is(HttpURLConnection.HTTP_BAD_REQUEST));
        Errors errors = hyperwalletException.getErrors();
        assertThat(errors, is(notNullValue()));
        assertThat(errors.getErrors(), is(notNullValue()));
        assertThat(errors.getErrors().size(), is(2));

        Error error1 = errors.getErrors().get(0);
        assertThat(error1.getCode(), is("CARD_NOT_SUPPORTED"));
        assertThat(error1.getMessage(), is("The card account supplied is not currently supported."));
        assertThat(error1.getFieldName(), is("cardNumber"));

        Error error2 = errors.getErrors().get(1);
        assertThat(error2.getCode(), is("CARD_EXPIRATION_DATE"));
        assertThat(error2.getMessage(), is("Expiration date"));
        assertThat(error2.getFieldName(), is("dateOfExpiry"));


    }
}

