package yooco.uchain.uchainwallet.utils;

import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.view.page.assets.AssetsFragment;
import yooco.uchain.uchainwallet.view.page.excitation.ExcitationFragment;
import yooco.uchain.uchainwallet.view.page.me.MeFragment;
import yooco.uchain.uchainwallet.view.page.me.MeManageDetailFragment;
import yooco.uchain.uchainwallet.view.page.me.MeTransactionRecordFragment;
import yooco.uchain.uchainwallet.view.page.me.portrait.MeCommonPortraitFragment;
import yooco.uchain.uchainwallet.view.page.me.portrait.MeEnterpriseKeyFragment;
import yooco.uchain.uchainwallet.view.page.me.portrait.MeEnterprisePortraitFragment;
import yooco.uchain.uchainwallet.view.page.wallet.BackupFragment;
import yooco.uchain.uchainwallet.view.page.wallet.ConfirmMnemonicFragment;
import yooco.uchain.uchainwallet.view.page.wallet.CopyMnemonicFragment;
import yooco.uchain.uchainwallet.view.page.wallet.ImportKeystoreFragment;
import yooco.uchain.uchainwallet.view.page.wallet.ImportMnemonicFragment;

public class FragmentFactory {

    private static AssetsFragment sAssetsFragment; //0
    private static MeFragment sMeFragment; //1
    private static ExcitationFragment sExcitationFragment; //2

    private static BackupFragment sBackupFragment;
    private static CopyMnemonicFragment sCopyMnemonicFragment;
    private static ConfirmMnemonicFragment sConfirmMnemonicFragment;

    private static MeManageDetailFragment sMeManageDetailFragment;
    private static MeTransactionRecordFragment sMeTransactionRecordFragment;
    private static MeCommonPortraitFragment sMeCommonPortraitFragment;
    private static MeEnterprisePortraitFragment sMeEnterprisePortraitFragment;
    private static MeEnterpriseKeyFragment sMeEnterpriseKeyFragment;

    private static ImportMnemonicFragment sImportMnemonicFragment;
    private static ImportKeystoreFragment sImportKeystoreFragment;

    public static BaseFragment getFragment(int position) {
        BaseFragment baseFragment = null;
        switch (position) {
            case 0:
                if (null == sAssetsFragment) {
                    sAssetsFragment = new AssetsFragment();
                }
                baseFragment = sAssetsFragment;
                break;
            case 1:
                if (null == sMeFragment) {
                    sMeFragment = new MeFragment();
                }
                baseFragment = sMeFragment;
                break;
            case 2:
                if (null == sExcitationFragment) {
                    sExcitationFragment = new ExcitationFragment();
                }
                baseFragment = sExcitationFragment;
                break;
            default:
                break;
        }
        return baseFragment;
    }

    public static BaseFragment getFragment(String fragmentTag) {
        BaseFragment baseFragment = null;
        switch (fragmentTag) {
            case Constant.FRAGMENT_TAG_BACKUP:
                if (null == sBackupFragment) {
                    sBackupFragment = new BackupFragment();
                }
                baseFragment = sBackupFragment;
                break;
            case Constant.FRAGMENT_TAG_COPY_MNEMONIC:
                if (null == sCopyMnemonicFragment) {
                    sCopyMnemonicFragment = new CopyMnemonicFragment();
                }
                baseFragment = sCopyMnemonicFragment;
                break;
            case Constant.FRAGMENT_TAG_CONFIRM_MNEMONIC:
                if (null == sConfirmMnemonicFragment) {
                    sConfirmMnemonicFragment = new ConfirmMnemonicFragment();
                }
                baseFragment = sConfirmMnemonicFragment;
                break;
            case Constant.FRAGMENT_TAG_ME_MANAGE_DETAIL:
                if (null == sMeManageDetailFragment) {
                    sMeManageDetailFragment = new MeManageDetailFragment();
                }
                baseFragment = sMeManageDetailFragment;
                break;
            case Constant.FRAGMENT_TAG_ME_TRANSACTION_RECORD:
                if (null == sMeTransactionRecordFragment) {
                    sMeTransactionRecordFragment = new MeTransactionRecordFragment();
                }
                baseFragment = sMeTransactionRecordFragment;
                break;
            case Constant.FRAGMENT_TAG_IMPORT_MNEMONIC:
                if (null == sImportMnemonicFragment) {
                    sImportMnemonicFragment = new ImportMnemonicFragment();
                }
                baseFragment = sImportMnemonicFragment;
                break;
            case Constant.FRAGMENT_TAG_IMPORT_KEYSTORE:
                if (null == sImportKeystoreFragment) {
                    sImportKeystoreFragment = new ImportKeystoreFragment();
                }
                baseFragment = sImportKeystoreFragment;
                break;
            case Constant.FRAGMENT_TAG_ME_COMMON_PORTRAIT:
                if (null == sMeCommonPortraitFragment) {
                    sMeCommonPortraitFragment = new MeCommonPortraitFragment();
                }
                baseFragment = sMeCommonPortraitFragment;
                break;
            case Constant.FRAGMENT_TAG_ME_ENTERPRISE_PORTRAIT:
                if (null == sMeEnterprisePortraitFragment) {
                    sMeEnterprisePortraitFragment = new MeEnterprisePortraitFragment();
                }
                baseFragment = sMeEnterprisePortraitFragment;
                break;
            case Constant.FRAGMENT_TAG_ME_ENTERPRISE_KEY:
                if (null == sMeEnterpriseKeyFragment) {
                    sMeEnterpriseKeyFragment = new MeEnterpriseKeyFragment();
                }
                baseFragment = sMeEnterpriseKeyFragment;
                break;
            default:
                break;
        }
        return baseFragment;
    }
}
