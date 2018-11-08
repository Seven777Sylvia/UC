package yooco.uchain.uchainwallet.view.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;

/**
 * Created by SteelCabbage on 2018/5/24 0024.
 */

public class SwitchTransactionRecyclerViewAdapter extends RecyclerView
        .Adapter<SwitchTransactionRecyclerViewAdapter.SwitchTransactionAdapterHolder>
        implements View.OnClickListener {

    private static final String TAG = SwitchTransactionRecyclerViewAdapter.class.getSimpleName();

    private OnItemClickListener mOnItemClickListener;
    private List<WalletBean> mWalletBeans;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SwitchTransactionRecyclerViewAdapter(List<WalletBean> walletBeans) {
        mWalletBeans = walletBeans;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (null == mOnItemClickListener) {
            UChainLog.e(TAG, "mOnItemClickListener is null!");
            return;
        }
        mOnItemClickListener.onItemClick((Integer) v.getTag());
    }

    @NonNull
    @Override
    public SwitchTransactionAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int
            viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .recyclerview_me_switch_transaction,
                parent, false);
        SwitchTransactionAdapterHolder holder = new SwitchTransactionAdapterHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SwitchTransactionAdapterHolder holder, int position) {
        WalletBean walletBean = mWalletBeans.get(position);
        if (null == walletBean) {
            UChainLog.e(TAG, "walletBean is null!");
            return;
        }

        holder.walletName.setText(walletBean.getName());

        if (walletBean.isSelected()) {
            holder.walletName.setBackgroundColor(UChainWalletApplication.getInstance().getResources
                    ().getColor(R.color.c_1253BF));
            holder.walletName.setTextColor(Color.WHITE);
        } else {
            holder.walletName.setBackgroundResource(0);
            holder.walletName.setTextColor(UChainWalletApplication.getInstance().getResources
                    ().getColor(R.color.c_333333));
        }

        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return null == mWalletBeans ? 0 : mWalletBeans.size();
    }

    class SwitchTransactionAdapterHolder extends RecyclerView.ViewHolder {
        TextView walletName;

        SwitchTransactionAdapterHolder(View itemView) {
            super(itemView);
            walletName = itemView.findViewById(R.id.tv_me_switch_transaction);

        }
    }
}
