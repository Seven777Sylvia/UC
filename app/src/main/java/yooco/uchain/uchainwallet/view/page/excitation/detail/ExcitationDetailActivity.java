package yooco.uchain.uchainwallet.view.page.excitation.detail;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Locale;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.data.bean.AddressResultCode;
import yooco.uchain.uchainwallet.data.bean.request.RequestSubmitExcitation;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.IGetLocalCpxSumCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.GetLocalCpxSum;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.PhoneUtils;
import yooco.uchain.uchainwallet.utils.ToastUtils;
import yooco.uchain.uchainwallet.view.dialog.ExcitationDialog;

public class ExcitationDetailActivity extends BaseActivity implements View.OnClickListener,
        IDetailView, IGetLocalCpxSumCallback {

    private static final String TAG = ExcitationDetailActivity.class.getSimpleName();

    private GetDetailCodePresenter mGetAddressResultPresenter;

    private EditText mCpxAddressInput;
    //private EditText mEthAddressInput;
    private TextView mWrongAddressNote;
    private Button mExcitationCommit;
    private ImageButton mCpxAddressInputCancel;
   // private ImageButton mEthAddressInputCancel;

    private int mGasLimit;
    private int mExcitationId;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_excitation_detail);
    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    private void initView() {
        Intent intent = getIntent();
        if (null == intent) {
            UChainLog.e(TAG, "intent is null!");
            return;
        }

        mGasLimit = intent.getIntExtra(Constant.EXCITATION_GAS_LIMIT, 0);
        mExcitationId = intent.getIntExtra(Constant.EXCITATION_ACTIVITY_ID, 0);

        mCpxAddressInput = findViewById(R.id.cpx_address_input);
        //mEthAddressInput = findViewById(R.id.eth_address_input);
        mWrongAddressNote = findViewById(R.id.tv_excitation_detail_wrong_address_note);
        mCpxAddressInputCancel = findViewById(R.id.cpx_address_input_cancel);
        //mEthAddressInputCancel = findViewById(R.id.eth_address_input_cancel);
        mWrongAddressNote = findViewById(R.id.tv_excitation_detail_wrong_address_note);
        mExcitationCommit = findViewById(R.id.btn_excitation_submit);

        TextView sumNote = findViewById(R.id.excitation_note_text);
        String sumText = getResources().getString(R.string.excitation_detail_sum_dialog_note);
        if (PhoneUtils.getAppLanguage().contains(Locale.CHINA.toString())) {
            String chineseTip = sumText + mGasLimit + "(≥" + mGasLimit + ")";
            sumNote.setText(chineseTip);
            UChainLog.i(TAG, "chineseTip: " + chineseTip);
        } else {
            String englishTip = sumText + " " + mGasLimit + "(≥" + mGasLimit + ")";
            sumNote.setText(englishTip);
            UChainLog.i(TAG, "EnglishTip: " + englishTip);
        }


        mCpxAddressInput.addTextChangedListener(new DetailTextWatcher(mCpxAddressInput));
     //   mEthAddressInput.addTextChangedListener(new DetailTextWatcher(mEthAddressInput));

        mExcitationCommit.setOnClickListener(this);
        mCpxAddressInputCancel.setOnClickListener(this);
        //mEthAddressInputCancel.setOnClickListener(this);
    }

    private void initData() {
        cpxCondition();
        mGetAddressResultPresenter = new GetDetailCodePresenter(this);
    }

    private void cpxCondition() {
        TaskController.getInstance().submit(new GetLocalCpxSum(this));
    }

    @Override
    public void getLocalCpxSum(String gasLimit) {
        UChainLog.i(TAG, "gasLimit:" + gasLimit);
        UChainLog.i(TAG, "mGasLimit:" + mGasLimit);

        BigDecimal cpxCondition = new BigDecimal(mGasLimit);
        BigDecimal localSum = new BigDecimal(gasLimit);
        int i = localSum.compareTo(cpxCondition);
        if (i == 0 || i == 1) {
            UChainLog.i(TAG, "more than");
        } else {
            UChainLog.i(TAG, "less than");
            showExcitationDialog();
        }
    }

    private void showExcitationDialog() {
        ExcitationDialog excitationDialog = ExcitationDialog.newInstance(mGasLimit);
        excitationDialog.show(getFragmentManager(), "ExcitationDialog");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_excitation_submit:
                submitExcitation();
                break;
            case R.id.cpx_address_input_cancel:
                clearCpxInput();
                break;
           /* case R.id.eth_address_input_cancel:
                clearEthInput();
                break;*/
            default:
                break;
        }
    }

    private void clearCpxInput() {
        mCpxAddressInput.getText().clear();
    }

    private void clearEthInput() {
        //mEthAddressInput.getText().clear();
    }

    private void submitExcitation() {
        String cpxAddress = mCpxAddressInput.getText().toString().trim();
     /*   String ethAddress = mEthAddressInput.getText().toString().trim();*/

        if (TextUtils.isEmpty(cpxAddress)) {
            UChainLog.e(TAG, "the CPX address isEmpty!");
            ToastUtils.getInstance().showToast(getResources().getString(R.string.excitation_cpx_address_empty_toast));
            return;
        }

      /*  if (TextUtils.isEmpty(ethAddress)) {
            UChainLog.e(TAG, "the ETH address isEmpty!");
            ToastUtils.getInstance().showToast(getResources().getString(R.string.excitation_eth_address_empty_toast));
            return;
        }*/

       /* if (!ethAddress.startsWith(Constant.ETH_ADDRESS_START_WITH) || ethAddress.length() != 42) {
            UChainLog.e(TAG, "the address is not Eth type!");
            ToastUtils.getInstance().showToast(getResources().getString(R.string.excitation_eth_address_wrong_format_toast));
            return;
        }*/

        if (!cpxAddress.startsWith(Constant.NEO_ADDRESS_START_WITH) || cpxAddress.length() != 34) {
            UChainLog.e(TAG, "the address is not CPX type!");
            ToastUtils.getInstance().showToast(getResources().getString(R.string.excitation_cpx_address_wrong_format_toast));
            return;
        }

        RequestSubmitExcitation requestSubmitExcitation = new RequestSubmitExcitation();
        requestSubmitExcitation.setCPX(cpxAddress);
        requestSubmitExcitation.setETH(null);
        requestSubmitExcitation.setId(mExcitationId);

        mGetAddressResultPresenter.getDetailCode(requestSubmitExcitation);
    }

    @Override
    public void getDetailCode(final AddressResultCode addressResultCode) {
        if (null == addressResultCode) {
            UChainLog.e(TAG, "addressResultCode is null!");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance().showToast(getString(R.string.server_request_failed));
                }
            });
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int cpxCode = addressResultCode.getCpxCode();

                if (cpxCode == 5200) {
                    ToastUtils.getInstance().showToast(getString(R.string.excitation_save_ok));
                    finish();
                    return;
                }

                if (cpxCode == 5000) {
                    ToastUtils.getInstance().showToast(getString(R.string.excitation_save_repeat));
                    return;
                }

                if (cpxCode == 5003) {
                    ToastUtils.getInstance().showToast(getString(R.string.excitation_format_err));
                    return;
                }

                if (cpxCode == 5001) {
                    ToastUtils.getInstance().showToast(getString(R.string.excitation_save_fail));
                }
            }
        });

    }

    private class DetailTextWatcher implements TextWatcher {
        private EditText mEditText;

        public DetailTextWatcher(EditText editText) {
            mEditText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int textId = mEditText.getId();
            switch (textId) {
                case R.id.cpx_address_input:
                    if (TextUtils.isEmpty(mEditText.getText())) {
                        mCpxAddressInputCancel.setVisibility(View.GONE);
                    } else {
                        mCpxAddressInputCancel.setVisibility(View.VISIBLE);
                    }
                    break;
              /*  case R.id.eth_address_input:
                    if (TextUtils.isEmpty(mEditText.getText())) {
                        mEthAddressInputCancel.setVisibility(View.GONE);
                    } else {
                        mEthAddressInputCancel.setVisibility(View.VISIBLE);
                    }
                    break;*/
                default:
                    break;
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

}
