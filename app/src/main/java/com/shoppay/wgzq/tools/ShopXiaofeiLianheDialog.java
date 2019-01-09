package com.shoppay.wgzq.tools;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.wgzq.MyApplication;
import com.shoppay.wgzq.R;
import com.shoppay.wgzq.bean.BalanceHandle;
import com.shoppay.wgzq.bean.ShopCar;
import com.shoppay.wgzq.bean.SystemQuanxian;
import com.shoppay.wgzq.db.DBAdapter;
import com.shoppay.wgzq.http.InterfaceBack;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Administrator on 2018/1/21 0021.
 */

public class ShopXiaofeiLianheDialog {
    public static Dialog dialog;
    public static EditText et_jifenmoney;
    public static EditText et_yuemoney;
    public static EditText et_xjmoney;
    public static EditText et_yinlianmoney;
    public static EditText et_qitamongy;
    public static EditText et_wxmoney;
    public static EditText et_alimoney;

    public static double paymoney = 0.0;
    public static boolean isAli = false, isWx = false;
    public static SystemQuanxian sysquanxian;

    public static Dialog jiesuanDialog(MyApplication app, final boolean isVip, final Dialog loading, final Context context,
                                       int showingLocation, final String type, final double yfmoney, final int objifen, final String jfmsg, final InterfaceBack handler) {
        final Dialog dialog;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_shoppaylianhe, null);
        final TextView et_zfmoney = (TextView) view.findViewById(R.id.shoppay_et_money);
        final TextView tv_yfmoney = (TextView) view.findViewById(R.id.shoppay_tv_yfmoney);
        final TextView tv_jiesuan = (TextView) view.findViewById(R.id.tv_jiesuan);
        final EditText et_password = (EditText) view.findViewById(R.id.vip_et_password);
        final RelativeLayout rl_jiesuan = (RelativeLayout) view.findViewById(R.id.shoppay_rl_jiesuan);
        final RelativeLayout rl_password = (RelativeLayout) view.findViewById(R.id.vip_rl_password);
        sysquanxian = app.getSysquanxian();
        et_jifenmoney = view.findViewById(R.id.vip_et_jfmoney);
        et_yuemoney = view.findViewById(R.id.vip_et_yuemoney);
        et_xjmoney = view.findViewById(R.id.vip_et_xjmoney);
        et_yinlianmoney = view.findViewById(R.id.vip_et_yinlianmoney);
        et_qitamongy = view.findViewById(R.id.vip_et_qitamoney);
        et_wxmoney = view.findViewById(R.id.vip_et_wxmoney);
        et_alimoney = view.findViewById(R.id.vip_et_alimoney);
        final TextView tv_jfbl = view.findViewById(R.id.vip_tv_jfkj);
        final TextView tv_jfmsg = view.findViewById(R.id.vip_tv_jfsm);
        tv_jfmsg.setText(jfmsg);

        final RelativeLayout rl_wxpaysao = view.findViewById(R.id.rl_wxpaysao);
        final RelativeLayout rl_alipaysao = view.findViewById(R.id.rl_alipaysao);

        final RelativeLayout rb_isYinlian = view.findViewById(R.id.rl_yinlianpay);
        final RelativeLayout rb_money = view.findViewById(R.id.rl_xjpay);
        final RelativeLayout rb_zhifubao = view.findViewById(R.id.rl_alipay);
        final RelativeLayout rb_wx = view.findViewById(R.id.rl_wxpay);
        final RelativeLayout rb_yue = view.findViewById(R.id.rl_yuepay);
        final RelativeLayout rb_qita = view.findViewById(R.id.rl_qitapay);
        if (sysquanxian.isweixin == 0) {
            rb_wx.setVisibility(View.GONE);
        }
        if (sysquanxian.iszhifubao == 0) {
            rb_zhifubao.setVisibility(View.GONE);
        }
        if (sysquanxian.isyinlian == 0) {
            rb_isYinlian.setVisibility(View.GONE);
        }
        if (sysquanxian.isxianjin == 0) {
            rb_money.setVisibility(View.GONE);
        }
        if (sysquanxian.isqita == 0) {
            rb_qita.setVisibility(View.GONE);
        }
        if (sysquanxian.isyue == 0) {
            rb_yue.setVisibility(View.GONE);
        }
        dialog = new Dialog(context, R.style.DialogNotitle1);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        int screenWidth = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getWidth();
        dialog.setContentView(view, new LinearLayout.LayoutParams(
                screenWidth - 10, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialog.show();
        tv_yfmoney.setText(StringUtil.twoNum(yfmoney + ""));
        et_zfmoney.setText(objifen + "");
        et_wxmoney.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    if (sysquanxian.iswxpay == 0) {
                        double jfh = CommonUtils.del(yfmoney, et_jifenmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_jifenmoney.getText().toString()));
                        double yueh = CommonUtils.del(jfh, et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString()));
                        double xjh = CommonUtils.del(yueh, et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString()));
                        double yinlianh = CommonUtils.del(xjh, et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString()));
                        double paymoney = CommonUtils.del(yinlianh, et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString()));
                        if (paymoney > 0) {
                            et_alimoney.setText("");
                            et_wxmoney.setText(paymoney + "");
                        } else {
                            et_wxmoney.setText("");
                            ToastUtils.showToast(context, "大于折后金额，请检查输入信息");
                        }
                    } else {
//                        ToastUtils.showToast(getActivity(), "微信支付权限为标记模式");
                    }
                } else {
                    // 此处为失去焦点时的处理内容
                }
            }
        });

        et_alimoney.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (sysquanxian.iszfbpay == 0) {
                        double jfh = CommonUtils.del(yfmoney, et_jifenmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_jifenmoney.getText().toString()));
                        double yueh = CommonUtils.del(jfh, et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString()));
                        double xjh = CommonUtils.del(yueh, et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString()));
                        double yinlianh = CommonUtils.del(xjh, et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString()));
                        double paymoney = CommonUtils.del(yinlianh, et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString()));
                        if (paymoney > 0) {
                            et_wxmoney.setText("");
                            et_alimoney.setText(paymoney + "");
                        } else {
                            et_alimoney.setText("");
                            ToastUtils.showToast(context, "大于折后金额，请检查输入信息");
                        }
                    } else {
//                        ToastUtils.showToast(getActivity(), "支付宝支付权限为标记模式");
                    }
                } else {

                }
            }
        });

        et_jifenmoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                    tv_jfbl.setText("");
                } else {
                    et_wxmoney.setText("");
                    et_alimoney.setText("");
                    tv_jfbl.setText(editable.toString() + "*" + NullUtils.noNullHandle(sysquanxian.jifenbili).toString());
                }

            }
        });

        et_xjmoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                } else {
                    et_wxmoney.setText("");
                    et_alimoney.setText("");
                }
            }
        });
        et_yuemoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                } else {
                    et_wxmoney.setText("");
                    et_alimoney.setText("");
                }
            }
        });
        et_yinlianmoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                } else {
                    et_wxmoney.setText("");
                    et_alimoney.setText("");
                }
            }
        });
        et_qitamongy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                } else {
                    et_wxmoney.setText("");
                    et_alimoney.setText("");
                }
            }
        });
        paymoney = 0.0;
        isAli = false;
        isWx = false;
        rl_wxpaysao.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                if (sysquanxian.iswxpay == 0) {
                    if (!et_jifenmoney.getText().toString().equals("")) {
                        String jifen = CommonUtils.multiply(et_jifenmoney.getText().toString(), sysquanxian.jifenbili + "");
                        if (Double.parseDouble(jifen) > Double.parseDouble(PreferenceHelper.readString(context, "shoppay", "jifenall", "0"))) {
                            ToastUtils.showToast(context, "积分不足");
                            return;
                        }
                    }
                    if (!et_yuemoney.getText().toString().equals("")) {
                        if (Double.parseDouble(et_yuemoney.getText().toString()) > Double.parseDouble(PreferenceHelper.readString(context, "shoppay", "MemMoney", "0"))) {
                            ToastUtils.showToast(context, "余额不足");
                            return;
                        }
                    }
                    double jfh = CommonUtils.del(yfmoney, et_jifenmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_jifenmoney.getText().toString()));
                    double yueh = CommonUtils.del(jfh, et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString()));
                    double xjh = CommonUtils.del(yueh, et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString()));
                    double yinlianh = CommonUtils.del(xjh, et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString()));
                    paymoney = CommonUtils.del(yinlianh, et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString()));
                    if (paymoney > 0) {
                        isAli = false;
                        isWx = true;
                        BalanceHandle balanceHandle = new BalanceHandle();
                        balanceHandle.isAli = false;
                        balanceHandle.isWx = true;
                        balanceHandle.paymoney = paymoney;
                        balanceHandle.type = "wxpay";
                        balanceHandle.jifenmoney = et_jifenmoney.getText().toString().equals("") ? 0.0 : Double.parseDouble(et_jifenmoney.getText().toString());
                        balanceHandle.yuenmoney = et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString());
                        balanceHandle.xjmoney = et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString());
                        balanceHandle.yinlianmoney = et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString());
                        balanceHandle.qitamoney = et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString());
                        et_alimoney.setText("");
                        et_wxmoney.setText(paymoney + "");
                        if (!et_yuemoney.getText().toString().equals("") && sysquanxian.ispassword == 1) {
                            balanceHandle.ispassword = true;
                            handler.onResponse(balanceHandle);
                            dialog.dismiss();

                        } else {
                            balanceHandle.ispassword = false;
                            handler.onResponse(balanceHandle);
                            dialog.dismiss();
                        }
                    } else {
                        ToastUtils.showToast(context, "大于折后金额，请检查输入信息");
                    }
                } else {
                    ToastUtils.showToast(context, "微信支付权限为标记模式");
                }
            }
        });

        rl_alipaysao.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                if (sysquanxian.iszfbpay == 0) {
                    if (!et_jifenmoney.getText().toString().equals("")) {
                        String jifen = CommonUtils.multiply(et_jifenmoney.getText().toString(), sysquanxian.jifenbili + "");
                        if (Double.parseDouble(jifen) > Double.parseDouble(PreferenceHelper.readString(context, "shoppay", "jifenall", "0"))) {
                            ToastUtils.showToast(context, "积分不足");
                            return;
                        }
                    }
                    if (!et_yuemoney.getText().toString().equals("")) {
                        if (Double.parseDouble(et_yuemoney.getText().toString()) > Double.parseDouble(PreferenceHelper.readString(context, "shoppay", "MemMoney", "0"))) {
                            ToastUtils.showToast(context, "余额不足");
                            return;
                        }
                    }
                    double jfh = CommonUtils.del(yfmoney, et_jifenmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_jifenmoney.getText().toString()));
                    double yueh = CommonUtils.del(jfh, et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString()));
                    double xjh = CommonUtils.del(yueh, et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString()));
                    double yinlianh = CommonUtils.del(xjh, et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString()));
                    paymoney = CommonUtils.del(yinlianh, et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString()));
                    if (paymoney > 0) {
                        isAli = true;
                        isWx = false;
                        BalanceHandle balanceHandle = new BalanceHandle();
                        balanceHandle.isAli = true;
                        balanceHandle.isWx = false;
                        balanceHandle.type = "zfbpay";
                        balanceHandle.jifenmoney = et_jifenmoney.getText().toString().equals("") ? 0.0 : Double.parseDouble(et_jifenmoney.getText().toString());
                        balanceHandle.yuenmoney = et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString());
                        balanceHandle.xjmoney = et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString());
                        balanceHandle.yinlianmoney = et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString());
                        balanceHandle.qitamoney = et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString());
                        et_alimoney.setText(paymoney + "");
                        et_wxmoney.setText("");
                        if (!et_yuemoney.getText().toString().equals("") && sysquanxian.ispassword == 1) {
                            balanceHandle.ispassword = true;
                            handler.onResponse(balanceHandle);
                            dialog.dismiss();

                        } else {
                            balanceHandle.ispassword = false;
                            handler.onResponse(balanceHandle);
                            dialog.dismiss();
                        }
                    } else {
                        ToastUtils.showToast(context, "大于折后金额，请检查输入信息");
                    }
                } else

                {
                    ToastUtils.showToast(context, "支付宝支付权限为标记模式");
                }
            }
        });
        rl_jiesuan.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                String jifen = CommonUtils.multiply(et_jifenmoney.getText().toString().equals("") ? "0" : et_jifenmoney.getText().toString(), sysquanxian.jifenbili + "");
                if (!et_yuemoney.getText().toString().equals("") && Double.parseDouble(et_yuemoney.getText().toString()) - Double.parseDouble(PreferenceHelper.readString(context, "shoppay", "MemMoney", "0")) > 0) {
                    Toast.makeText(MyApplication.context, "余额不足",
                            Toast.LENGTH_SHORT).show();
                } else if (!et_jifenmoney.getText().toString().equals("") && Double.parseDouble(jifen) - Double.parseDouble(PreferenceHelper.readString(context, "shoppay", "jifenall", "0")) > 0) {
                    Toast.makeText(MyApplication.context, "积分不足",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (CommonUtils.checkNet(MyApplication.context)) {
                        if (sysquanxian.iszfbpay == 0 && !et_alimoney.getText().toString().equals("")) {
                            ToastUtils.showToast(MyApplication.context,"支付宝付非标记模式，必须扫码支付");
                        } else if (sysquanxian.iszfbpay == 0 && !et_alimoney.getText().toString().equals("")) {
                            ToastUtils.showToast(MyApplication.context,"微信支付非标记模式，必须扫码支付");

                        }else {
                            double jfh = CommonUtils.del(yfmoney, et_jifenmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_jifenmoney.getText().toString()));
                            double yueh = CommonUtils.del(jfh, et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString()));
                            double xjh = CommonUtils.del(yueh, et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString()));
                            double yinlianh = CommonUtils.del(xjh, et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString()));
                            double qitah = CommonUtils.del(yinlianh, et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString()));
                            double wxh = CommonUtils.del(qitah, et_wxmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_wxmoney.getText().toString()));
                            double alih = CommonUtils.del(wxh, et_alimoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_alimoney.getText().toString()));
                            if (alih == 0) {
                                isWx = false;
                                isAli = false;
                                paymoney = 0.0;
                                if (!et_yuemoney.getText().toString().equals("") && sysquanxian.ispassword == 1) {
                                    DialogUtil.pwdDialog(context, 1, new InterfaceBack() {
                                        @Override
                                        public void onResponse(Object response) {
                                            jiesuan(loading, type, handler, dialog, context, response.toString(), DateUtils.getCurrentTime("yyyyMMddHHmmss"));
                                        }

                                        @Override
                                        public void onErrorResponse(Object msg) {

                                        }
                                    });
                                } else {
                                    jiesuan(loading, type, handler, dialog, context, "", DateUtils.getCurrentTime("yyyyMMddHHmmss"));
                                }
                            } else {
                                Toast.makeText(MyApplication.context, "输入金额不等于折后金额",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(MyApplication.context, "请检查网络是否可用",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        Window window = dialog.getWindow();
        switch (showingLocation)

        {
            case 0:
                window.setGravity(Gravity.TOP); // 此处可以设置dialog显示的位置
                break;
            case 1:
                window.setGravity(Gravity.CENTER);
                break;
            case 2:
                window.setGravity(Gravity.BOTTOM);
                break;
            case 3:
                WindowManager.LayoutParams params = window.getAttributes();
                dialog.onWindowAttributesChanged(params);
                params.x = screenWidth - dip2px(context, 100);// 设置x坐标
                params.gravity = Gravity.TOP;
                params.y = dip2px(context, 45);// 设置y坐标
                Log.d("xx", params.y + "");
                window.setGravity(Gravity.TOP);
                window.setAttributes(params);
                break;
            default:
                window.setGravity(Gravity.CENTER);
                break;
        }
        return dialog;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    public static void jiesuan(final Dialog loading, final String type, final InterfaceBack handle, final Dialog dialog, final Context context, final String pwd, String orderNum) {
        loading.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        client.setCookieStore(myCookieStore);
        final DBAdapter dbAdapter = DBAdapter.getInstance(context);
        List<ShopCar> list = dbAdapter.getListShopCar(PreferenceHelper.readString(context, "shoppay", "account", "123"));
        List<ShopCar> shoplist = new ArrayList<>();
        double yfmoney = 0.0;
        double zfmoney = 0.0;
        int point = 0;
        int num = 0;
        for (ShopCar numShop : list) {
            if (numShop.count == 0) {
            } else {
                shoplist.add(numShop);
                zfmoney = CommonUtils.add(zfmoney, Double.parseDouble(numShop.discountmoney));
                yfmoney = CommonUtils.add(yfmoney, Double.parseDouble(CommonUtils.multiply(numShop.count + "", numShop.price)));
                num = num + numShop.count;
                point = point + (int) numShop.point;
            }
        }
        RequestParams params = new RequestParams();
        params.put("MemID", PreferenceHelper.readString(context, "shoppay", "memid", ""));
        params.put("OrderAccount", orderNum);
        params.put("TotalMoney", yfmoney);
        params.put("DiscountMoney", zfmoney);
        params.put("OrderPoint", point);
        params.put("PayCard", et_yuemoney.getText().toString().equals("") ? 0.0 : Double.parseDouble(et_yuemoney.getText().toString()));//number	余额支付金额
        params.put("PayCash", et_xjmoney.getText().toString().equals("") ? 0.0 : Double.parseDouble(et_xjmoney.getText().toString()));//	number	现金支付金额 "
        params.put("PayBink", et_yinlianmoney.getText().toString().equals("") ? 0.0 : Double.parseDouble(et_yinlianmoney.getText().toString()));//	number	银联支付金额
        params.put("PayWeChat", et_wxmoney.getText().toString().equals("") ? 0.0 : Double.parseDouble(et_wxmoney.getText().toString()));//	number	微信支付金额
        params.put("PayAli", et_alimoney.getText().toString().equals("") ? 0.0 : Double.parseDouble(et_alimoney.getText().toString()));//	number	支付宝支付金额
        params.put("PayOtherPayment", et_qitamongy.getText().toString().equals("") ? 0.0 : Double.parseDouble(et_qitamongy.getText().toString()));//	number	其他支付金额
        if (et_jifenmoney.getText().toString().equals("")) {
            params.put("PointMoney", 0);//	Number	积分抵扣金额
            params.put("PayPoint", 0);//	Int	积分抵扣数量
        } else {
            params.put("PointMoney", et_jifenmoney.getText().toString());//	Number	积分抵扣金额
            params.put("PayPoint", (int) Double.parseDouble(CommonUtils.multiply(et_jifenmoney.getText().toString(), sysquanxian.jifenbili + "")));//	Int	积分抵扣数量
        }
        params.put("UserPwd", pwd);
        params.put("GlistCount", shoplist.size());
        LogUtils.d("xxparams", shoplist.size() + "");
        for (int i = 0; i < shoplist.size(); i++) {
            LogUtils.d("xxparams", shoplist.get(i).discount);
            params.put("Glist[" + i + "][GoodsID]", shoplist.get(i).goodsid);
            params.put("Glist[" + i + "][number]", shoplist.get(i).count);
            params.put("Glist[" + i + "][GoodsPoint]", point);
            params.put("Glist[" + i + "][discountedprice]", shoplist.get(i).discount);
            params.put("Glist[" + i + "][Price]", shoplist.get(i).discountmoney);
            params.put("Glist[" + i + "][GoodsPrice]", shoplist.get(i).price);
        }
        LogUtils.d("xxparams", params.toString());
        String url = UrlTools.obtainUrl(context, "?Source=3", "GoodsExpense");
        LogUtils.d("xxurl", url);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    loading.dismiss();
                    LogUtils.d("xxjiesuanS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
                        Toast.makeText(context, jso.getString("msg"), Toast.LENGTH_LONG).show();
                        JSONObject jsonObject = (JSONObject) jso.getJSONArray("print").get(0);
                        if (jsonObject.getInt("printNumber") == 0) {
                            dbAdapter.deleteShopCar();
                            BalanceHandle balanceHandle = new BalanceHandle();
                            balanceHandle.type = "";
                            handle.onResponse(balanceHandle);
                        } else {
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (bluetoothAdapter.isEnabled()) {
                                BluetoothUtil.connectBlueTooth(MyApplication.context);
                                BluetoothUtil.sendData(DayinUtils.dayin(jsonObject.getString("printContent")), jsonObject.getInt("printNumber"));
                                dbAdapter.deleteShopCar();
                                BalanceHandle balanceHandle = new BalanceHandle();
                                balanceHandle.type = "";
                                handle.onResponse(balanceHandle);
                            } else {
                                dbAdapter.deleteShopCar();
                                BalanceHandle balanceHandle = new BalanceHandle();
                                balanceHandle.type = "";
                                handle.onResponse(balanceHandle);
                            }
                        }
                    } else {
                        Toast.makeText(context, jso.getString("msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    loading.dismiss();
                }
//				printReceipt_BlueTooth(context,xfmoney,yfmoney,jf,et_zfmoney,et_yuemoney,tv_dkmoney,et_jfmoney);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                loading.dismiss();
                Toast.makeText(context, "结算失败，请重新结算",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}
