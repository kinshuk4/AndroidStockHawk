package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.DetailsActivity;

public class StockWidgetService extends RemoteViewsService {
    private static String LOG_TAG = RemoteViewsService.class.getSimpleName();
    private static final String[] STOCK_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };

    private final int INDEX_ID = 0;
    private final int INDEX_SYMBOL = 1;
    private final int INDEX_BIDPRICE = 2;
    private final int INDEX_PERCENT_CHANGE = 3;
    private final int INDEX_ISUP = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(LOG_TAG, "onGetViewFactory()");
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                Log.d(LOG_TAG, "onDataSetChanged()");
                if (data != null) {
                    data.close();
                }
                Log.i("wid","here3");
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, STOCK_COLUMNS, QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                Log.d(LOG_TAG, "onDestroy()");
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                Log.d(LOG_TAG, "getCount()");
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.d(LOG_TAG, "getViewAt()");
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                Log.i("wid","here4");
                String symbols, bidPrice;
                symbols = data.getString(INDEX_SYMBOL);
                bidPrice = data.getString(INDEX_BIDPRICE);
                int isUp = data.getInt(INDEX_ISUP);
                views.setTextViewText(R.id.stock_symbol, symbols);
                views.setTextViewText(R.id.bid_price, bidPrice);
                if (isUp == 1) {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                views.setTextViewText(R.id.change, data.getString(INDEX_PERCENT_CHANGE));
                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);

                intent.putExtra(getString(R.string.symbol_intent_keyword), symbols);
                views.setOnClickFillInIntent(R.id.quote_root, intent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                Log.d(LOG_TAG, "getLoadingView()");
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                Log.d(LOG_TAG, "getViewTypeCount()");
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.d(LOG_TAG, "getItemId()");
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                Log.d(LOG_TAG, "hasStableIds()");
                return true;
            }
        };
    }
}
