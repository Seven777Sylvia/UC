package yooco.uchain.uchainwallet.utils.task.callback.eth;

import ethmobile.Wallet;

/**
 * Created by SteelCabbage on 2018/6/11 0011.
 */

public interface IFromMnemonicToEthWalletCallback {
    void fromMnemonicToEthWallet(Wallet wallet);
}
