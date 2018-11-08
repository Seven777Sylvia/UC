package yooco.uchain.uchainwallet.view.page.me;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.adapter.EmptyAdapter;
import yooco.uchain.uchainwallet.view.adapter.TransactionRecordRecyclerViewAdapter;
import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.changelistener.OnTxStateUpdateListener;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.IGetNeoTransactionHistoryCallback;
import yooco.uchain.uchainwallet.utils.task.callback.ILoadTransactionRecordCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthTransactionHistoryCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.GetNeoTransactionHistory;
import yooco.uchain.uchainwallet.utils.task.runnable.LoadTransactionRecord;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.GetEthTransactionHistory;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.PhoneUtils;
import yooco.uchain.uchainwallet.utils.ToastUtils;
import yooco.uchain.uchainwallet.view.dialog.SwitchWallet2Dialog;

/**
 * Created by SteelCabbage on 2018/5/31 0031.
 */

public class MeTransactionRecordFragment extends BaseFragment implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, TransactionRecordRecyclerViewAdapter
                .OnItemClickListener, IGetNeoTransactionHistoryCallback,
        ILoadTransactionRecordCallback, OnTxStateUpdateListener, TextWatcher, SwitchWallet2Dialog
                .onSelectedWalletListener, IGetEthTransactionHistoryCallback {

    private static final String TAG = MeTransactionRecordFragment.class.getSimpleName();

    private TextView mTv_me_transaction_record_title;
    private TextView mTv_me_transaction_record_address;
    private ImageButton mIb_me_transaction_record_switch;
    private SwipeRefreshLayout mSl_transaction_record;
    private RecyclerView mRv_transaction_record;
    private EditText mEt_tx_records_search;
    private ImageButton mIb_tx_records_cancel;
    private EmptyAdapter mEmptyAdapter;
    private TransactionRecordRecyclerViewAdapter mTxRecyclerViewAdapter;

    private WalletBean mCurrentClickedWallet;
    private List<TransactionRecord> mTransactionRecords;
    private List<TransactionRecord> mSearchTxRecords;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me_transaction_record, container, false);
    }

    @Override
    protected void init(View view) {
        super.init(view);

        initView(view);
        initData();
        loadTxsFromDb();
        incrementalUpdateTxDbFromNet();
    }

    private void initView(View view) {
        mTv_me_transaction_record_title = view.findViewById(R.id.tv_me_transaction_record_title);
        mTv_me_transaction_record_address = view.findViewById(R.id.tv_me_transaction_record_address);
        mIb_me_transaction_record_switch = view.findViewById(R.id.ib_me_transaction_record_switch);
        mSl_transaction_record = view.findViewById(R.id.sl_transaction_record);
        mRv_transaction_record = view.findViewById(R.id.rv_transaction_record);

        // 搜索功能
        mEt_tx_records_search = view.findViewById(R.id.et_tx_records_search);
        mIb_tx_records_cancel = view.findViewById(R.id.ib_tx_records_cancel);

        mEt_tx_records_search.addTextChangedListener(this);
        mIb_tx_records_cancel.setOnClickListener(this);


        mRv_transaction_record.setLayoutManager(new LinearLayoutManager(UChainWalletApplication.getInstance(),
                LinearLayoutManager.VERTICAL, false));
        mTransactionRecords = new ArrayList<>();
        mSearchTxRecords = new ArrayList<>();
        mTxRecyclerViewAdapter = new TransactionRecordRecyclerViewAdapter(mTransactionRecords);
        mTxRecyclerViewAdapter.setOnItemClickListener(this);
        mEmptyAdapter = new EmptyAdapter(mTxRecyclerViewAdapter, UChainWalletApplication.getInstance(), R.layout
                .recyclerview_empty_tx);
        mRv_transaction_record.setAdapter(mEmptyAdapter);

        mIb_me_transaction_record_switch.setOnClickListener(this);
        mSl_transaction_record.setColorSchemeColors(this.getActivity().getResources().getColor(R.color.c_1253BF));
        mSl_transaction_record.setOnRefreshListener(this);

        // copy address
        mTv_me_transaction_record_address.setOnClickListener(this);
    }

    private void initData() {
        Me3Activity me3Activity = (Me3Activity) getActivity();
        mCurrentClickedWallet = me3Activity.getWalletBean();
        if (null == mCurrentClickedWallet) {
            UChainLog.e(TAG, "currentClickedWalletBean is null!");
            return;
        }

        mTv_me_transaction_record_title.setText(mCurrentClickedWallet.getName());
        mTv_me_transaction_record_address.setText(mCurrentClickedWallet.getAddress());
        UChainListeners.getInstance().addOnTxStateUpdateListener(this);
    }

    private void loadTxsFromDb() {
        String address = mTv_me_transaction_record_address.getText().toString().trim();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int preClearSize = mSearchTxRecords.size();
                mTransactionRecords.clear();
                mSearchTxRecords.clear();
                mTxRecyclerViewAdapter.notifyItemRangeRemoved(0, preClearSize);
                mEmptyAdapter.notifyDataSetChanged();
            }
        });

        TaskController.getInstance().submit(new LoadTransactionRecord(mCurrentClickedWallet.getWalletType(), address, this));
    }

    @Override
    public void loadTransactionRecord(final List<TransactionRecord> transactionRecords) {
        if (null == transactionRecords || transactionRecords.isEmpty()) {
            UChainLog.w(TAG, "loadTransactionRecord() -> transactionRecords is null or empty!");
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UChainLog.i(TAG, "loadTransactionRecord ok!");

                int preClearSize = mTransactionRecords.size();
                mTransactionRecords.clear();
                mSearchTxRecords.clear();
                mTxRecyclerViewAdapter.notifyItemRangeRemoved(0, preClearSize);
                mEmptyAdapter.notifyDataSetChanged();

                mTransactionRecords.addAll(transactionRecords);
                mSearchTxRecords.addAll(transactionRecords);
                mTxRecyclerViewAdapter.notifyItemRangeInserted(0, transactionRecords.size());
                mEmptyAdapter.notifyDataSetChanged();

                if (mSl_transaction_record.isRefreshing()) {
                    mSl_transaction_record.setRefreshing(false);
                }

                mEt_tx_records_search.getText().clear();
            }
        });
    }

    private void incrementalUpdateTxDbFromNet() {
        String address = mTv_me_transaction_record_address.getText().toString().trim();
        int walletType = mCurrentClickedWallet.getWalletType();
        switch (walletType) {
            case Constant.WALLET_TYPE_NEO:
                TaskController.getInstance().submit(new GetNeoTransactionHistory(address, this));
                break;
            case Constant.WALLET_TYPE_ETH:
                TaskController.getInstance().submit(new GetEthTransactionHistory(address, this));
                break;
            case Constant.WALLET_TYPE_CPX:

                break;
            default:
                UChainLog.e(TAG, "Illegal wallet type!");
                break;
        }
    }

    @Override
    public void getNeoTransactionHistory(List<TransactionRecord> transactionRecords) {
        if (null == transactionRecords || transactionRecords.isEmpty()) {
            UChainLog.w(TAG, "getNeoTransactionHistory() -> transactionRecords is null or empty!");
            if (mSl_transaction_record.isRefreshing()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSl_transaction_record.setRefreshing(false);
                    }
                });
            }
            return;
        }

        loadTxsFromDb();
    }

    @Override
    public void getEthTransactionHistory(List<TransactionRecord> transactionRecords) {
        if (null == transactionRecords || transactionRecords.isEmpty()) {
            UChainLog.w(TAG, "getEthTransactionHistory() -> transactionRecords is null or empty!");
            if (mSl_transaction_record.isRefreshing()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSl_transaction_record.setRefreshing(false);
                    }
                });
            }
            return;
        }

        loadTxsFromDb();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_me_transaction_record_switch:
                showDialog();
                break;
            case R.id.ib_tx_records_cancel:
                mEt_tx_records_search.getText().clear();
                break;
            case R.id.tv_me_transaction_record_address:
                String copyAddr = mTv_me_transaction_record_address.getText().toString().trim();
                PhoneUtils.copy2Clipboard(UChainWalletApplication.getInstance(), copyAddr);
                ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                        .getResources().getString(R.string.wallet_address_copied));
                break;
            default:
                break;
        }
    }

    private void showDialog() {
        SwitchWallet2Dialog switchWallet2Dialog = SwitchWallet2Dialog.newInstance();
        switchWallet2Dialog.setCurrentWallet(mCurrentClickedWallet);
        switchWallet2Dialog.setOnSelectedWalletListener(this);
        switchWallet2Dialog.show(getFragmentManager(), "SwitchWallet2Dialog");
    }

    @Override
    public void onSelectedWallet(WalletBean walletBean) {
        if (null == walletBean) {
            UChainLog.e(TAG, "neoWallet is null!");
            return;
        }

        mCurrentClickedWallet = walletBean;
        mTv_me_transaction_record_title.setText(walletBean.getName());
        mTv_me_transaction_record_address.setText(walletBean.getAddress());

        // update transactionRecord
        loadTxsFromDb();
        incrementalUpdateTxDbFromNet();
    }

    @Override
    public void onItemClick(int position) {
        if (null == mTransactionRecords || mTransactionRecords.isEmpty()) {
            UChainLog.e(TAG, "mTransactionRecords is null or is empty!");
            return;
        }

        TransactionRecord transactionRecord = mTransactionRecords.get(position);
        if (null == transactionRecord) {
            UChainLog.e(TAG, "transactionRecord is null!");
            return;
        }

        startActivityParcelable(TransactionDetailActivity.class, false, Constant
                .PARCELABLE_TRANSACTION_RECORD, transactionRecord);
    }

    @Override
    public void onRefresh() {
        incrementalUpdateTxDbFromNet();
    }

    @Override
    public void onTxStateUpdate(String txID, int state, long txTime) {
        if (TextUtils.isEmpty(txID)) {
            UChainLog.e(TAG, "txID is null!");
            return;
        }

        if (null == mTransactionRecords || mTransactionRecords.isEmpty()) {
            UChainLog.e(TAG, "mTransactionRecords is null or empty!");
            return;
        }

        for (TransactionRecord transactionRecord : mTransactionRecords) {
            if (null == transactionRecord) {
                continue;
            }

            if (txID.equals(transactionRecord.getTxID())) {
                transactionRecord.setTxState(state);
                if (txTime != Constant.NO_NEED_MODIFY_TX_TIME) {
                    transactionRecord.setTxTime(txTime);
                }
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTxRecyclerViewAdapter.notifyDataSetChanged();
                mEmptyAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UChainListeners.getInstance().removeOnTxStateUpdateListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mTransactionRecords.clear();
        mTransactionRecords.addAll(mSearchTxRecords);

        if (TextUtils.isEmpty(s)) {
            UChainLog.w(TAG, "onTextChanged() -> is empty!");
            mIb_tx_records_cancel.setVisibility(View.INVISIBLE);
            mTxRecyclerViewAdapter.notifyDataSetChanged();
            mEmptyAdapter.notifyDataSetChanged();
            return;
        }

        mIb_tx_records_cancel.setVisibility(View.VISIBLE);
        Iterator<TransactionRecord> iterator = mTransactionRecords.iterator();
        while (iterator.hasNext()) {
            TransactionRecord transactionRecord = iterator.next();
            if (null == transactionRecord) {
                UChainLog.e(TAG, "transactionRecord is null!");
                continue;
            }

            if (!transactionRecord.getTxID().contains(s)) {
                iterator.remove();
            }
        }

        mTxRecyclerViewAdapter.notifyDataSetChanged();
        mEmptyAdapter.notifyDataSetChanged();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
