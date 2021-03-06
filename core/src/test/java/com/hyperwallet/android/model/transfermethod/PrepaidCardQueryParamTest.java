package com.hyperwallet.android.model.transfermethod;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static com.hyperwallet.android.model.StatusTransition.StatusDefinition.ACTIVATED;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.model.transfermethod.TransferMethodQueryParam.TransferMethodSortable.ASCENDANT_CREATE_ON;
import static com.hyperwallet.android.model.transfermethod.TransferMethodQueryParam.TransferMethodSortable.ASCENDANT_STATUS;
import static com.hyperwallet.android.model.transfermethod.TransferMethodQueryParam.TransferMethodSortable.DESCENDANT_CREATE_ON;

import com.hyperwallet.android.model.QueryParam;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Map;

public class PrepaidCardQueryParamTest {
    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";
    private static final String CREATE_BEFORE = "createdBefore";
    private static final String CREATE_AFTER = "createdAfter";
    private static final String CREATE_ON = "createdOn";
    private static final String TRANSFER_METHOD_TYPE = "type";
    private static final String STATUS = "status";
    private static final String SORT_BY = "sortBy";


    @Test
    public void testPrepaidCardQueryParam_withUrlQueryMap() {
        Calendar createdBefore = Calendar.getInstance();
        createdBefore.set(2019, 0, 1, 10, 12, 22);
        Calendar createdAfter = Calendar.getInstance();
        createdAfter.set(2019, 0, 1, 0, 0, 0);
        Calendar createdOn = Calendar.getInstance();
        createdOn.set(2019, 0, 1, 10, 10, 0);

        PrepaidCardQueryParam queryParam = new PrepaidCardQueryParam.Builder()
                .offset(100)
                .limit(200)
                .createdBefore(createdBefore.getTime())
                .createdAfter(createdAfter.getTime())
                .createdOn(createdOn.getTime())
                .status(ACTIVATED)
                .sortByStatusAsc()
                .build();

        assertThat(queryParam.getLimit(), is(200));
        assertThat(queryParam.getOffset(), is(100));
        assertThat(queryParam.getType(), is(PREPAID_CARD));
        assertThat(queryParam.getStatus(), is(ACTIVATED));
        assertThat(queryParam.getSortBy(), is(ASCENDANT_STATUS));

        createdBefore = Calendar.getInstance();
        createdBefore.setTime(queryParam.getCreatedBefore());
        assertThat(createdBefore.get(Calendar.YEAR), is(2019));
        assertThat(createdBefore.get(Calendar.MONTH), is(Calendar.JANUARY));
        assertThat(createdBefore.get(Calendar.DAY_OF_MONTH), is(1));
        assertThat(createdBefore.get(Calendar.HOUR), is(10));
        assertThat(createdBefore.get(Calendar.MINUTE), is(12));
        assertThat(createdBefore.get(Calendar.SECOND), is(22));

        createdAfter = Calendar.getInstance();
        createdAfter.setTime(queryParam.getCreatedAfter());
        assertThat(createdAfter.get(Calendar.YEAR), is(2019));
        assertThat(createdAfter.get(Calendar.MONTH), is(Calendar.JANUARY));
        assertThat(createdAfter.get(Calendar.DAY_OF_MONTH), is(1));
        assertThat(createdAfter.get(Calendar.HOUR), is(0));
        assertThat(createdAfter.get(Calendar.MINUTE), is(0));
        assertThat(createdAfter.get(Calendar.SECOND), is(0));

        createdOn = Calendar.getInstance();
        createdOn.setTime(queryParam.getCreatedOn());
        assertThat(createdOn.get(Calendar.YEAR), is(2019));
        assertThat(createdOn.get(Calendar.MONTH), is(Calendar.JANUARY));
        assertThat(createdOn.get(Calendar.DAY_OF_MONTH), is(1));
        assertThat(createdOn.get(Calendar.HOUR), is(10));
        assertThat(createdOn.get(Calendar.MINUTE), is(10));
        assertThat(createdOn.get(Calendar.SECOND), is(0));
    }

    @Test
    public void testPrepaidCardQueryParam_verifyDefaultValues() {
        PrepaidCardQueryParam queryParam = new PrepaidCardQueryParam.Builder().build();
        assertThat(queryParam.getLimit(), is(10));
        assertThat(queryParam.getOffset(), is(0));
        assertThat(queryParam.getType(), is(PREPAID_CARD));
        assertThat(queryParam.getStatus(), is(nullValue()));
        assertThat(queryParam.getSortBy(), is(nullValue()));
        assertThat(queryParam.getCreatedBefore(), is(nullValue()));
        assertThat(queryParam.getCreatedAfter(), is(nullValue()));
    }

    @Test
    public void testBuildQuery_verifyDefaultValues() {
        QueryParam queryParam = new PrepaidCardQueryParam.Builder().build();

        Map<String, String> query = queryParam.buildQuery();

        Assert.assertNotNull(query);
        assertThat(query.size(), is(3));
        assertThat(query.get(OFFSET), is("0"));
        assertThat(query.get(LIMIT), is("10"));
        assertThat(query.get(TRANSFER_METHOD_TYPE), is(PREPAID_CARD));
        assertThat(query.get(STATUS), is(nullValue()));
        assertThat(query.get(SORT_BY), is(nullValue()));
        assertThat(query.get(CREATE_BEFORE), is(nullValue()));
        assertThat(query.get(CREATE_AFTER), is(nullValue()));
        assertThat(query.get(CREATE_ON), is(nullValue()));
    }

    @Test
    public void testBuildQuery_returnsQueryParameters() {
        Calendar createdBefore = Calendar.getInstance();
        createdBefore.set(2019, 0, 1, 10, 12, 22);
        Calendar createdAfter = Calendar.getInstance();
        createdAfter.set(2019, 0, 1, 0, 0, 0);
        Calendar createdOn = Calendar.getInstance();
        createdOn.set(2019, 0, 1, 10, 10, 0);

        PrepaidCardQueryParam queryParam = new PrepaidCardQueryParam.Builder()
                .offset(100)
                .limit(200)
                .createdBefore(createdBefore.getTime())
                .createdAfter(createdAfter.getTime())
                .createdOn(createdOn.getTime())
                .status(ACTIVATED)
                .sortByCreatedOnAsc()
                .build();
        Map<String, String> resultQuery = queryParam.buildQuery();

        assertThat(resultQuery.containsKey(STATUS), is(true));
        assertThat(resultQuery.containsKey(SORT_BY), is(true));
        assertThat(resultQuery.containsKey(OFFSET), is(true));
        assertThat(resultQuery.containsKey(LIMIT), is(true));
        assertThat(resultQuery.containsKey(CREATE_BEFORE), is(true));
        assertThat(resultQuery.containsKey(CREATE_AFTER), is(true));
        assertThat(resultQuery.containsKey(TRANSFER_METHOD_TYPE), is(true));

        assertThat(resultQuery.get(LIMIT), is("200"));
        assertThat(resultQuery.get(OFFSET), is("100"));
        assertThat(resultQuery.get(STATUS), is(ACTIVATED));
        assertThat(resultQuery.get(SORT_BY), is(ASCENDANT_CREATE_ON));
        assertThat(resultQuery.get(CREATE_BEFORE), is("2019-01-01T10:12:22"));
        assertThat(resultQuery.get(CREATE_AFTER), is("2019-01-01T00:00:00"));
        assertThat(resultQuery.get(CREATE_ON), is("2019-01-01T10:10:00"));
        assertThat(resultQuery.get(TRANSFER_METHOD_TYPE), is(PREPAID_CARD));
    }

    @Test
    public void testBuilder_verifyValues() {
        Calendar dateAfter = Calendar.getInstance();
        dateAfter.set(2019, 6, 21, 12, 45);
        Calendar dateBefore = Calendar.getInstance();
        dateBefore.set(2019, 6, 20, 9, 10);
        Calendar dateOn = Calendar.getInstance();
        dateOn.set(2019, 6, 20, 10, 21);
        PrepaidCardQueryParam queryParam = new PrepaidCardQueryParam.Builder()
                .createdAfter(dateAfter.getTime())
                .createdBefore(dateBefore.getTime())
                .createdOn(dateOn.getTime())
                .offset(100)
                .limit(20)
                .sortByCreatedOnDesc()
                .status(ACTIVATED)
                .build();

        assertThat(queryParam.getOffset(), is(100));
        assertThat(queryParam.getLimit(), is(20));
        assertThat(queryParam.getSortBy(), is(DESCENDANT_CREATE_ON));
        assertThat(queryParam.getStatus(), is(ACTIVATED));
        assertThat(queryParam.getType(), is(PREPAID_CARD));
        assertThat(queryParam.getCreatedAfter().getTime(), is(dateAfter.getTimeInMillis()));
        assertThat(queryParam.getCreatedBefore().getTime(), is(dateBefore.getTimeInMillis()));
        assertThat(queryParam.getCreatedOn().getTime(), is(dateOn.getTimeInMillis()));
    }
}