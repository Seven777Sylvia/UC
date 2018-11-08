package yooco.uchain.uchainwallet.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import yooco.uchain.uchainwallet.global.Constant;

/**
 * Created by SteelCabbage on 2018/3/28 0028.
 */

public class UChainWalletDbHelper extends SQLiteOpenHelper {

    private static final String TAG = UChainWalletDbHelper.class.getSimpleName();

    private static final int DB_VERSION = 1;

    private static final String DB_NAME = "uchainData";

    public UChainWalletDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                                int version) {
        super(context, name, factory, version);
//        SQLiteDatabase.loadLibs(context);
    }

    public UChainWalletDbHelper(Context context) {
        this(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // wallet
        db.execSQL(Constant.SQL_CREATE_NEO_WALLET);
        db.execSQL(Constant.SQL_CREATE_ETH_WALLET);
        db.execSQL(Constant.SQL_CREATE_CPX_WALLET);

        // tx
        db.execSQL(Constant.SQL_CREATE_NEO_TRANSACTION_RECORD);
        db.execSQL(Constant.SQL_CREATE_ETH_TRANSACTION_RECORD);
        db.execSQL(Constant.SQL_CREATE_NEO_TX_CACHE);
        db.execSQL(Constant.SQL_CREATE_ETH_TX_CACHE);

        // assets
        db.execSQL(Constant.SQL_CREATE_NEO_ASSETS);
        db.execSQL(Constant.SQL_CREATE_ETH_ASSETS);
        db.execSQL(Constant.SQL_CREATE_CPX_ASSETS);

        // portrait
        db.execSQL(Constant.SQL_CREATE_PORTRAIT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
