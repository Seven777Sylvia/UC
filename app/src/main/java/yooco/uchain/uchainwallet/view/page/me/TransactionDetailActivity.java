package yooco.uchain.uchainwallet.view.page.me;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.math.BigDecimal;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.PhoneUtils;
import yooco.uchain.uchainwallet.utils.ToastUtils;

public class TransactionDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = TransactionDetailActivity.class.getSimpleName();

    private TextView mTv_transaction_detail_amount;
    private TextView mTv_transaction_detail_unit;
    private TextView mTv_transaction_detail_from;
    private TextView mTv_transaction_detail_to;
    private TextView mTv_transaction_detail_time;
    private TextView mTv_transaction_detail_tx_id;

    private TransactionRecord mTransactionRecord;
    private TextView mTv_transaction_detail_gas_consumed;
    private TextView mTv_transaction_detail_gas_price;
    private TextView mTv_transaction_detail_gas_total_fee;
    private TextView mTv_transaction_detail_gas_consumed_title;
    private TextView mTv_transaction_detail_gas_price_title;
    private TextView mTv_transaction_detail_gas_total_fee_title;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_transaction_detail);
    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setBlackStatusBar(true);
        super.onCreate(savedInstanceState);
    }

    private void initView() {
        mTv_transaction_detail_amount = (TextView) findViewById(R.id.tv_transaction_detail_amount);
        mTv_transaction_detail_unit = (TextView) findViewById(R.id.tv_transaction_detail_unit);
        mTv_transaction_detail_from = (TextView) findViewById(R.id.tv_transaction_detail_from);
        mTv_transaction_detail_to = (TextView) findViewById(R.id.tv_transaction_detail_to);
        mTv_transaction_detail_time = (TextView) findViewById(R.id.tv_transaction_detail_time);
        mTv_transaction_detail_tx_id = (TextView) findViewById(R.id.tv_transaction_detail_tx_id);
        // eth
        mTv_transaction_detail_gas_consumed = (TextView) findViewById(R.id.tv_transaction_detail_gas_consumed);
        mTv_transaction_detail_gas_price = (TextView) findViewById(R.id.tv_transaction_detail_gas_price);
        mTv_transaction_detail_gas_total_fee = (TextView) findViewById(R.id.tv_transaction_detail_gas_total_fee);

        mTv_transaction_detail_gas_consumed_title = (TextView) findViewById(R.id.tv_transaction_detail_gas_consumed_title);
        mTv_transaction_detail_gas_price_title = (TextView) findViewById(R.id.tv_transaction_detail_gas_price_title);
        mTv_transaction_detail_gas_total_fee_title = (TextView) findViewById(R.id.tv_transaction_detail_gas_total_fee_title);

        mTv_transaction_detail_from.setOnClickListener(this);
        mTv_transaction_detail_to.setOnClickListener(this);
        mTv_transaction_detail_tx_id.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (null == intent) {
            UChainLog.e(TAG, "intent is null!");
            return;
        }

        mTransactionRecord = intent.getParcelableExtra(Constant.PARCELABLE_TRANSACTION_RECORD);
        if (null == mTransactionRecord) {
            UChainLog.e(TAG, "transactionRecord is null!");
            return;
        }

        mTv_transaction_detail_amount.setText(mTransactionRecord.getTxAmount());
        mTv_transaction_detail_unit.setText(String.valueOf(UChainWalletApplication.getInstance()
                .getResources().getString(R.string.transfer_amount) + " (" + mTransactionRecord.getAssetSymbol() + ")"));
        mTv_transaction_detail_from.setText(mTransactionRecord.getTxFrom());
        mTv_transaction_detail_to.setText(mTransactionRecord.getTxTo());
        mTv_transaction_detail_time.setText(PhoneUtils.getFormatTime(mTransactionRecord.getTxTime()));
        mTv_transaction_detail_tx_id.setText(mTransactionRecord.getTxID());

        //eth
        String txType = mTransactionRecord.getTxType();
        if (TextUtils.isEmpty(txType)) {
            UChainLog.e(TAG, "txType is null!");
            return;
        }

        switch (txType) {
            case Constant.ASSET_TYPE_ETH:
            case Constant.ASSET_TYPE_ERC20:
                mTv_transaction_detail_gas_consumed.setVisibility(View.VISIBLE);
                mTv_transaction_detail_gas_price.setVisibility(View.VISIBLE);
                mTv_transaction_detail_gas_total_fee.setVisibility(View.VISIBLE);

                mTv_transaction_detail_gas_consumed.setText(TextUtils.isEmpty(mTransactionRecord.getGasConsumed()) ? "" :
                        mTransactionRecord.getGasConsumed());

                String gasPrice = mTransactionRecord.getGasPrice();
                if (TextUtils.isEmpty(gasPrice)) {
                    mTv_transaction_detail_gas_price.setText("");
                } else {
                    try {
                        String gasPriceDec = new BigDecimal(gasPrice).divide(new BigDecimal(10).pow(9)).toPlainString();
                        mTv_transaction_detail_gas_price.setText(gasPriceDec);
                    } catch (Exception e) {
                        UChainLog.e(TAG, "TransactionDetailActivity exception:" + e.getMessage());
                        mTv_transaction_detail_gas_price.setText("");
                    }
                }

                mTv_transaction_detail_gas_total_fee.setText(TextUtils.isEmpty(mTransactionRecord.getGasFee()) ? "" :
                        mTransactionRecord.getGasFee());
                break;
            default:
                mTv_transaction_detail_gas_consumed.setVisibility(View.INVISIBLE);
                mTv_transaction_detail_gas_price.setVisibility(View.INVISIBLE);
                mTv_transaction_detail_gas_total_fee.setVisibility(View.INVISIBLE);
                mTv_transaction_detail_gas_consumed_title.setVisibility(View.INVISIBLE);
                mTv_transaction_detail_gas_price_title.setVisibility(View.INVISIBLE);
                mTv_transaction_detail_gas_total_fee_title.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_transaction_detail_from:
                String from = mTv_transaction_detail_from.getText().toString().trim();
                copy2Clipboard(from, UChainWalletApplication.getInstance().getResources().getString
                        (R.string.payment_address_copied));
                break;
            case R.id.tv_transaction_detail_to:
                String to = mTv_transaction_detail_to.getText().toString().trim();
                copy2Clipboard(to, UChainWalletApplication.getInstance().getResources().getString
                        (R.string.payee_address_copied));
                break;
            case R.id.tv_transaction_detail_tx_id:
                String txId = mTv_transaction_detail_tx_id.getText().toString().trim();
                copy2Clipboard(txId, UChainWalletApplication.getInstance().getResources().getString
                        (R.string.tx_id_copied));
                break;
            default:
                break;
        }
    }

    private void copy2Clipboard(String content, String toastMsg) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(toastMsg)) {
            UChainLog.e(TAG, "content or toastMsg is null!");
            return;
        }

        PhoneUtils.copy2Clipboard(UChainWalletApplication.getInstance(), content);
        ToastUtils.getInstance().showToast(toastMsg);
    }
}
