package yooco.uchain.uchainwallet.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;

public class AssetsRecyclerViewAdapter extends RecyclerView.Adapter<AssetsRecyclerViewAdapter
        .AssetsAdapterHolder> implements
        View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = AssetsRecyclerViewAdapter.class.getSimpleName();

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private List<WalletBean> mWalletBeans;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public AssetsRecyclerViewAdapter(List<WalletBean> walletBeans) {
        mWalletBeans = walletBeans;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public void onClick(View v) {
        if (null == mOnItemClickListener) {
            UChainLog.e(TAG, "mOnItemClickListener is null!");
            return;
        }
        mOnItemClickListener.onItemClick((Integer) v.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        if (null == mOnItemLongClickListener) {
            UChainLog.e(TAG, "mOnItemLongClickListener is null!");
            return false;
        }
        mOnItemLongClickListener.onItemLongClick((Integer) v.getTag());
        return true;
    }

    @NonNull
    @Override
    public AssetsAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_assets_item, parent, false);
        AssetsAdapterHolder holder = new AssetsAdapterHolder(view);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AssetsAdapterHolder holder, int position) {
        WalletBean walletBean = mWalletBeans.get(position);
        if (null == walletBean) {
            UChainLog.e(TAG, "walletBean is null!");
            return;
        }

        switch (position%5) {
            case Constant.WALLET_ICON_COLOR_SKY:
                holder.walletType.setImageDrawable(UChainWalletApplication.getInstance().getResources().getDrawable(R.drawable.icon_assets_item_1));
                break;
            case Constant.WALLET_ICON_COLOR_ORANGE:
                holder.walletType.setImageDrawable(UChainWalletApplication.getInstance().getResources().getDrawable(R.drawable.icon_assets_item_2));
                break;
            case Constant.WALLET_ICON_COLOR_PURPLE:
                holder.walletType.setImageDrawable(UChainWalletApplication.getInstance().getResources().getDrawable(R.drawable.icon_assets_item_3));
                break;
            case Constant.WALLET_ICON_COLOR_OCEAN:
                holder.walletType.setImageDrawable(UChainWalletApplication.getInstance().getResources().getDrawable(R.drawable.icon_assets_item_4));
                break;
            case Constant.WALLET_ICON_COLOR_YELLWO:
                holder.walletType.setImageDrawable(UChainWalletApplication.getInstance().getResources().getDrawable(R.drawable.icon_assets_item_5));
                break;
            default:
                break;
        }

        holder.walletName.setText(walletBean.getName());
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return null == mWalletBeans ? 0 : mWalletBeans.size();
    }

    class AssetsAdapterHolder extends RecyclerView.ViewHolder {
        ImageView walletType;
        TextView walletName;

        AssetsAdapterHolder(View itemView) {
            super(itemView);
            walletType = itemView.findViewById(R.id.iv_assets_rv_item_wallet_type);
            walletName = itemView.findViewById(R.id.tv_assets_rv_item_wallet_name);
        }
    }
}
