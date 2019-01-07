package com.shoppay.wgzq;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.wgzq.bean.FastShopZhehMoney;
import com.shoppay.wgzq.bean.SystemQuanxian;
import com.shoppay.wgzq.bean.VipInfo;
import com.shoppay.wgzq.bean.VipInfoMsg;
import com.shoppay.wgzq.card.ReadCardOpt;
import com.shoppay.wgzq.card.ReadCardOptHander;
import com.shoppay.wgzq.http.InterfaceBack;
import com.shoppay.wgzq.tools.ActivityStack;
import com.shoppay.wgzq.tools.BluetoothUtil;
import com.shoppay.wgzq.tools.CommonUtils;
import com.shoppay.wgzq.tools.DateUtils;
import com.shoppay.wgzq.tools.DayinUtils;
import com.shoppay.wgzq.tools.DialogUtil;
import com.shoppay.wgzq.tools.LogUtils;
import com.shoppay.wgzq.tools.NoDoubleClickListener;
import com.shoppay.wgzq.tools.NullUtils;
import com.shoppay.wgzq.tools.PreferenceHelper;
import com.shoppay.wgzq.tools.StringUtil;
import com.shoppay.wgzq.tools.ToastUtils;
import com.shoppay.wgzq.tools.UrlTools;
import com.shoppay.wgzq.wxcode.MipcaActivityCapture;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by songxiaotao on 2017/7/1.
 */

public class VipNewFragment extends Fragment {
    private EditText et_card, et_xfmoney;
    private TextView tv_vipname, tv_vipjf, tv_zhmoney, tv_obtainjf, tv_vipyue, tv_vipdengji, mVipTvKamcard;
    private RelativeLayout rl_jiesuan;
    private String editString;
    private Dialog dialog;
    private Dialog paydialog;
    private String xfmoney;
    private RelativeLayout rl_password;
    private RelativeLayout rb_money, rb_wx, rb_zhifubao, rb_isYinlian, rb_yue, rb_qita;
    private EditText et_password;
    private boolean isSuccess = false;
    private RelativeLayout rl_tvcard, rl_card;
    private TextView tv_tvcard;
    private boolean isVipcar = false;
    private EditText et_jifenmoney, et_xjmoney, et_yuemoney, et_qitamongy, et_yinlianmoney;
    private TextView tv_jfbl, tv_jfmsg;
    private RelativeLayout rl_wxpaysao, rl_alipaysao;
    private boolean isWx = false, isAli = false;
    private double paymoney = 0.0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    VipInfo info = (VipInfo) msg.obj;
                    tv_vipname.setText(info.getMemName());
                    tv_vipyue.setText(info.getMemMoney());
                    tv_vipdengji.setText(info.getLevelName());
                    tv_vipjf.setText(info.getMemPoint());
                    mVipTvKamcard.setText(NullUtils.noNullHandle(info.MemCardNumber).toString());
                    tv_jfmsg.setText(NullUtils.noNullHandle(info.Message).toString());
                    try {
                        if (isVipcar) {
                            new ReadCardOptHander().overReadCard();
                        } else {
                            new ReadCardOpt().overReadCard();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    PreferenceHelper.write(getActivity(), "shoppay", "memid", info.getMemID());
                    PreferenceHelper.write(getActivity(), "shoppay", "vipcar", et_card.getText().toString());
                    PreferenceHelper.write(getActivity(), "shoppay", "Discount", info.getDiscount());
                    PreferenceHelper.write(getActivity(), "shoppay", "DiscountPoint", info.getDiscountPoint());
                    PreferenceHelper.write(getActivity(), "shoppay", "jifen", info.getMemPoint());
                    isSuccess = true;
                    break;
                case 2:
                    isSuccess = false;
                    tv_vipname.setText("");
                    tv_vipjf.setText("");
                    tv_vipyue.setText("");
                    tv_vipdengji.setText("");
                    mVipTvKamcard.setText("");
                    tv_jfmsg.setText("");
                    break;


                case 3:
                    FastShopZhehMoney zh = (FastShopZhehMoney) msg.obj;
                    tv_zhmoney.setText(StringUtil.twoNum(zh.Money));
                    tv_obtainjf.setText(Integer.parseInt(zh.Point) + "");
                    break;
                case 4:
                    tv_zhmoney.setText("0.00");
                    tv_obtainjf.setText("0");
                    break;


            }
        }
    };
    private String password = "";
    private MsgReceiver msgReceiver;
    private String orderAccount;
    private SystemQuanxian sysquanxian;
    private MyApplication app;
    //    private Intent intent;
//    private Dialog weixinDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vipconsumption_new, null);
        app = (MyApplication) getActivity().getApplication();
        sysquanxian = app.getSysquanxian();
        initView(view);
        dialog = DialogUtil.loadingDialog(getActivity(), 1);
        paydialog = DialogUtil.payloadingDialog(getActivity(), 1);
        PreferenceHelper.write(MyApplication.context, "shoppay", "memid", "123");
        PreferenceHelper.write(MyApplication.context, "shoppay", "vipdengjiid", "123");
        PreferenceHelper.write(MyApplication.context, "shoppay", "jifenpercent", "123");
        PreferenceHelper.write(MyApplication.context, "shoppay", "viptoast", "未查询到会员");


        // 注册广播
        msgReceiver = new MsgReceiver();
        IntentFilter iiiff = new IntentFilter();
        iiiff.addAction("com.shoppay.wy.fastsaomiao");
        getActivity().registerReceiver(msgReceiver, iiiff);

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
                    tv_jfbl.setText(editable.toString() + "*" + NullUtils.noNullHandle(sysquanxian.jifenbili).toString());
                }

            }
        });

        et_card.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (delayRun != null) {
                    //每次editText有变化的时候，则移除上次发出的延迟线程
                    handler.removeCallbacks(delayRun);
                }
                editString = editable.toString();

                //延迟800ms，如果不再输入字符，则执行该线程的run方法
                handler.postDelayed(delayRun, 800);
            }
        });
        et_xfmoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                    tv_zhmoney.setText("0.00");
                    tv_obtainjf.setText("0.00");
                } else {
                    if (!isSuccess) {
                        Toast.makeText(MyApplication.context, PreferenceHelper.readString(MyApplication.context, "shoppay", "viptoast", "未查询到会员"), Toast.LENGTH_SHORT).show();
                        et_xfmoney.setText("");
                    } else {
                        xfmoney = editable.toString();
                        String zhmoney = CommonUtils.multiply(CommonUtils.div(Double.parseDouble(PreferenceHelper.readString(getActivity(), "shoppay", "Discount", "0")), 100, 2) + "", xfmoney);
                        tv_zhmoney.setText(StringUtil.twoNum(zhmoney));
                        tv_obtainjf.setText((int) CommonUtils.div(Double.parseDouble(zhmoney), Double.parseDouble(PreferenceHelper.readString(getActivity(), "shoppay", "DiscountPoint", "1")), 2) + "");
                    }
                }
            }
        });

        return view;
    }

    /**
     * 广播接收器
     *
     * @author len
     */
    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到进度，更新UI
            String code = intent.getStringExtra("code");
            if (isVipcar) {
                tv_tvcard.setText(code);
                editString = code;
                obtainVipInfo();
            } else {
                et_card.setText(code);
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (isVipcar) {
            new ReadCardOptHander(new InterfaceBack() {
                @Override
                public void onResponse(Object response) {
                    tv_tvcard.setText(response.toString());
                    editString = tv_tvcard.getText().toString();
                    obtainVipInfo();
                }

                @Override
                public void onErrorResponse(Object msg) {

                }
            });
        } else {
            new ReadCardOpt(et_card);
        }
    }

    @Override
    public void onStop() {
        //终止检卡
        try {
            if (isVipcar) {
                new ReadCardOptHander().overReadCard();
            } else {
                new ReadCardOpt().overReadCard();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onStop();

        if (delayRun != null) {
            //每次editText有变化的时候，则移除上次发出的延迟线程
            handler.removeCallbacks(delayRun);
        }
    }

    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {

        @Override
        public void run() {
            //在这里调用服务器的接口，获取数据
            obtainVipInfo();
        }
    };


    private void obtainVipInfo() {
        if (isVipcar) {
            dialog.show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.put("MemCard", editString);
        LogUtils.d("xxparams", params.toString());
        String url = UrlTools.obtainUrl(getActivity(), "?Source=3", "GetMem");
        LogUtils.d("xxurl", url);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    if (isVipcar) {
                        dialog.dismiss();
                    }
                    LogUtils.d("xxVipinfoS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
                        Gson gson = new Gson();
                        VipInfoMsg infomsg = gson.fromJson(new String(responseBody, "UTF-8"), VipInfoMsg.class);
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = infomsg.getVdata().get(0);
                        handler.sendMessage(msg);
                    } else {
                        Message msg = handler.obtainMessage();
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    if (isVipcar) {
                        dialog.dismiss();
                    }
                    Message msg = handler.obtainMessage();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (isVipcar) {
                    dialog.dismiss();
                }
                Message msg = handler.obtainMessage();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        });
    }

    private void initView(View view) {
        et_card = view.findViewById(R.id.vip_et_card);
        et_xfmoney = view.findViewById(R.id.vip_et_xfmoney);
        mVipTvKamcard = view.findViewById(R.id.vip_tv_kamcard);
        et_password = view.findViewById(R.id.vip_et_password);
        tv_vipdengji = view.findViewById(R.id.vip_tv_vipdengji);
        tv_vipname = view.findViewById(R.id.vip_tv_name);
        tv_vipjf = view.findViewById(R.id.vip_tv_jifen);
        tv_vipyue = view.findViewById(R.id.vip_tv_vipyue);
        tv_zhmoney = view.findViewById(R.id.vip_tv_zhmoney);
        tv_obtainjf = view.findViewById(R.id.vip_tv_hasjf);

        et_jifenmoney = view.findViewById(R.id.vip_et_jfmoney);
        et_yuemoney = view.findViewById(R.id.vip_et_yuemoney);
        et_xjmoney = view.findViewById(R.id.vip_et_xjmoney);
        et_yinlianmoney = view.findViewById(R.id.vip_et_yinlianmoney);
        et_qitamongy = view.findViewById(R.id.vip_et_qitamoney);

        tv_jfbl = view.findViewById(R.id.vip_tv_jfkj);
        tv_jfmsg = view.findViewById(R.id.vip_tv_jfsm);

        rl_wxpaysao = view.findViewById(R.id.rl_wxpaysao);
        rl_alipaysao = view.findViewById(R.id.rl_alipaysao);

        rb_isYinlian = view.findViewById(R.id.rl_yinlianpay);
        rb_money = view.findViewById(R.id.rl_xjpay);
        rb_zhifubao = view.findViewById(R.id.rl_alipay);
        rb_wx = view.findViewById(R.id.rl_wxpay);
        rb_yue = view.findViewById(R.id.rl_yuepay);
        rb_qita = view.findViewById(R.id.rl_qitapay);
        rl_tvcard = view.findViewById(R.id.rl_tvcard);
        tv_tvcard = view.findViewById(R.id.tv_tvcard);
        rl_card = view.findViewById(R.id.rl_etcard);
        if (Integer.parseInt(NullUtils.noNullHandle(sysquanxian.isvipcard).toString()) == 0) {
            rl_tvcard.setVisibility(View.GONE);
            rl_card.setVisibility(View.VISIBLE);
            isVipcar = false;
        } else {
            rl_tvcard.setVisibility(View.VISIBLE);
            rl_card.setVisibility(View.GONE);
            isVipcar = true;
        }
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

        rl_jiesuan = view.findViewById(R.id.vip_rl_jiesuan);
        rl_password = view.findViewById(R.id.vip_rl_password);
        rl_wxpaysao.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                if (sysquanxian.iswxpay == 0) {
                    if (!isSuccess) {
                        Toast.makeText(MyApplication.context, "请输入会员卡号",
                                Toast.LENGTH_SHORT).show();
                    } else if (et_xfmoney.getText().toString().equals("")
                            || et_xfmoney.getText().toString() == null) {
                        Toast.makeText(MyApplication.context, "请输入消费金额",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (!et_jifenmoney.getText().toString().equals("")) {
                            String jifen = CommonUtils.multiply(et_jifenmoney.getText().toString(), sysquanxian.jifenbili + "");
                            if (Double.parseDouble(jifen) > Double.parseDouble(tv_vipjf.getText().toString())) {
                                ToastUtils.showToast(getActivity(), "积分不足");
                                return;
                            }
                        }
                        if (!et_yuemoney.getText().toString().equals("")) {
                            if (Double.parseDouble(et_yuemoney.getText().toString()) > Double.parseDouble(tv_vipyue.getText().toString())) {
                                ToastUtils.showToast(getActivity(), "余额不足");
                                return;
                            }
                        }
                        double jfh = CommonUtils.del(Double.parseDouble(tv_zhmoney.getText().toString()), et_jifenmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_jifenmoney.getText().toString()));
                        double yueh = CommonUtils.del(jfh, et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString()));
                        double xjh = CommonUtils.del(yueh, et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString()));
                        double yinlianh = CommonUtils.del(xjh, et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString()));
                        paymoney = CommonUtils.del(yinlianh, et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString()));
                        if (paymoney > 0) {
                            Intent mipca = new Intent(getActivity(), MipcaActivityCapture.class);
                            mipca.putExtra("type", "pay");
                            startActivityForResult(mipca, 222);
                        } else {
                            ToastUtils.showToast(getActivity(), "大于折后金额，请检查输入信息");
                        }
                    }
                } else {
                    ToastUtils.showToast(getActivity(), "微信支付权限为标记模式");
                }
            }
        });

        rl_alipaysao.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                if (sysquanxian.iszfbpay == 0) {
                    if (!isSuccess) {
                        Toast.makeText(MyApplication.context, "请输入会员卡号",
                                Toast.LENGTH_SHORT).show();
                    } else if (et_xfmoney.getText().toString().equals("")
                            || et_xfmoney.getText().toString() == null) {
                        Toast.makeText(MyApplication.context, "请输入消费金额",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (!et_jifenmoney.getText().toString().equals("")) {
                            String jifen = CommonUtils.multiply(et_jifenmoney.getText().toString(), sysquanxian.jifenbili + "");
                            if (Double.parseDouble(jifen) > Double.parseDouble(tv_vipjf.getText().toString())) {
                                ToastUtils.showToast(getActivity(), "积分不足");
                                return;
                            }
                        }
                        if (!et_yuemoney.getText().toString().equals("")) {
                            if (Double.parseDouble(et_yuemoney.getText().toString()) > Double.parseDouble(tv_vipyue.getText().toString())) {
                                ToastUtils.showToast(getActivity(), "余额不足");
                                return;
                            }
                        }
                        double jfh = CommonUtils.del(Double.parseDouble(tv_zhmoney.getText().toString()), et_jifenmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_jifenmoney.getText().toString()));
                        double yueh = CommonUtils.del(jfh, et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString()));
                        double xjh = CommonUtils.del(yueh, et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString()));
                        double yinlianh = CommonUtils.del(xjh, et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString()));
                        paymoney = CommonUtils.del(yinlianh, et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString()));
                        if (paymoney >= 0) {
                            Intent mipca = new Intent(getActivity(), MipcaActivityCapture.class);
                            mipca.putExtra("type", "pay");
                            startActivityForResult(mipca, 222);
                        } else {
                            ToastUtils.showToast(getActivity(), "大于折后金额，请检查输入信息");
                        }
                    }
                } else {
                    ToastUtils.showToast(getActivity(), "支付宝支付权限为标记模式");
                }
            }
        });
        rl_jiesuan.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                String jifen = CommonUtils.multiply(et_jifenmoney.getText().toString().equals("") ? "0" : et_jifenmoney.getText().toString(), sysquanxian.jifenbili + "");
                if (!isSuccess) {
                    Toast.makeText(MyApplication.context, "请输入会员卡号",
                            Toast.LENGTH_SHORT).show();
                } else if (et_xfmoney.getText().toString().equals("")
                        || et_xfmoney.getText().toString() == null) {
                    Toast.makeText(MyApplication.context, "请输入消费金额",
                            Toast.LENGTH_SHORT).show();
                } else if (!et_yuemoney.getText().toString().equals("") && Double.parseDouble(et_yuemoney.getText().toString()) - Double.parseDouble(tv_vipyue.getText().toString()) > 0) {
                    Toast.makeText(MyApplication.context, "余额不足",
                            Toast.LENGTH_SHORT).show();
                } else if (!et_jifenmoney.getText().toString().equals("") && Double.parseDouble(jifen) - Double.parseDouble(tv_vipjf.getText().toString()) > 0) {
                    Toast.makeText(MyApplication.context, "积分不足",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (CommonUtils.checkNet(MyApplication.context)) {
                        double jfh = CommonUtils.del(Double.parseDouble(tv_zhmoney.getText().toString()), et_jifenmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_jifenmoney.getText().toString()));
                        double yueh = CommonUtils.del(jfh, et_yuemoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yuemoney.getText().toString()));
                        double xjh = CommonUtils.del(yueh, et_xjmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_xjmoney.getText().toString()));
                        double yinlianh = CommonUtils.del(xjh, et_yinlianmoney.getText().toString().equals("") ? 0 : Double.parseDouble(et_yinlianmoney.getText().toString()));
                        double qitah = CommonUtils.del(yinlianh, et_qitamongy.getText().toString().equals("") ? 0 : Double.parseDouble(et_qitamongy.getText().toString()));
                        if (qitah == 0) {
                            if (!et_yuemoney.getText().toString().equals("") && sysquanxian.ispassword == 1) {
                                DialogUtil.pwdDialog(getActivity(), 1, new InterfaceBack() {
                                    @Override
                                    public void onResponse(Object response) {
                                        password = (String) response;
                                        jiesuan(DateUtils.getCurrentTime("yyyyMMddHHmmss"));
                                    }

                                    @Override
                                    public void onErrorResponse(Object msg) {

                                    }
                                });
                            } else {
                                jiesuan(DateUtils.getCurrentTime("yyyyMMddHHmmss"));
                            }
                        } else {
                            Toast.makeText(MyApplication.context, "输入金额不等于折后金额",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MyApplication.context, "请检查网络是否可用",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void jiesuan(String orderNum) {
        dialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(MyApplication.context);
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.put("MemID", PreferenceHelper.readString(MyApplication.context, "shoppay", "memid", "123"));
        params.put("OrderAccount", orderNum);
//        (订单折后总金额/标记B)取整
        params.put("OrderPoint", (int) CommonUtils.div(Double.parseDouble(tv_zhmoney.getText().toString()), Double.parseDouble(PreferenceHelper.readString(getActivity(), "shoppay", "DiscountPoint", "1")), 2));
        params.put("TotalMoney", et_xfmoney.getText().toString());
        params.put("DiscountMoney", tv_zhmoney.getText().toString());
//        0=现金 1=银联 2=微信 3=支付宝 4=其他支付 5=余额(散客禁用)
        params.put("UserPwd", password);
        LogUtils.d("xxparams", params.toString());
        String url = UrlTools.obtainUrl(getActivity(), "?Source=3", "QuickExpense");
        LogUtils.d("xxurl", url);
        client.setTimeout(120 * 1000);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    dialog.dismiss();
                    LogUtils.d("xxjiesuanS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
                        Toast.makeText(getActivity(), jso.getString("msg"), Toast.LENGTH_LONG).show();
                        JSONObject jsonObject = (JSONObject) jso.getJSONArray("print").get(0);
                        if (jsonObject.getInt("printNumber") == 0) {
                            ActivityStack.create().finishActivity(FastConsumptionActivity.class);
                        } else {
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (bluetoothAdapter.isEnabled()) {
                                BluetoothUtil.connectBlueTooth(MyApplication.context);
                                BluetoothUtil.sendData(DayinUtils.dayin(jsonObject.getString("printContent")), jsonObject.getInt("printNumber"));
                                ActivityStack.create().finishActivity(FastConsumptionActivity.class);
                            } else {
                                ActivityStack.create().finishActivity(FastConsumptionActivity.class);
                            }
                        }

                    } else {
                        Toast.makeText(MyApplication.context, jso.getString("msg"),
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MyApplication.context, "结算失败，请重新结算",
                            Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dialog.dismiss();
                Toast.makeText(MyApplication.context, "结算失败，请重新结算",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroy() {
        // TODO 自动生成的方法存根
        super.onDestroy();
//        if (intent != null) {
//
//            getActivity().stopService(intent);
//        }
//
//        //关闭闹钟机制启动service
//        AlarmManager manager = (AlarmManager)getActivity(). getSystemService(Context.ALARM_SERVICE);
//        int anHour =2 * 1000; // 这是一小时的毫秒数 60 * 60 * 1000
//        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
//        Intent i = new Intent(getActivity(), AlarmReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, i, 0);
//        manager.cancel(pi);
//        //注销广播
        getActivity().unregisterReceiver(msgReceiver);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 222:
                if (resultCode == Activity.RESULT_OK) {
                    pay(data.getStringExtra("codedata"));
                }
                break;
        }
    }

    private void pay(String codedata) {
        paydialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
        client.setCookieStore(myCookieStore);
        RequestParams map = new RequestParams();
        map.put("auth_code", codedata);
        map.put("UserID", PreferenceHelper.readString(getActivity(), "shoppay", "UserID", ""));
//        （1会员充值7商品消费9快速消费11会员充次）
        map.put("ordertype", 9);
        orderAccount = DateUtils.getCurrentTime("yyyyMMddHHmmss");
        map.put("account", orderAccount);
        map.put("money", tv_zhmoney.getText().toString());
//        0=现金 1=银联 2=微信 3=支付宝
        if (isWx) {
            map.put("payType", 2);
        } else if (isAli) {
            map.put("payType", 3);
        } else {
            map.put("payType", 3);
        }
        client.setTimeout(120 * 1000);
        LogUtils.d("xxparams", map.toString());
        String url = UrlTools.obtainUrl(getActivity(), "?Source=3", "PayOnLine");
        LogUtils.d("xxurl", url);
        client.post(url, map, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    paydialog.dismiss();
                    LogUtils.d("xxpayS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {

//                        JSONObject jsonObject = (JSONObject) jso.getJSONArray("print").get(0);
//                        DayinUtils.dayin(jsonObject.getString("printContent"));
//                        if (jsonObject.getInt("printNumber") == 0) {
//                        } else {
//                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                            if (bluetoothAdapter.isEnabled()) {
//                                BluetoothUtil.connectBlueTooth(MyApplication.context);
//                                BluetoothUtil.sendData(DayinUtils.dayin(jsonObject.getString("printContent")), jsonObject.getInt("printNumber"));
//                            } else {
//                            }
//                        }
                        jiesuan(orderAccount);
                    } else {
                        Toast.makeText(getActivity(), jso.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    paydialog.dismiss();
                    Toast.makeText(getActivity(), "支付失败，请稍后再试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                paydialog.dismiss();
                Toast.makeText(getActivity(), "支付失败，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
