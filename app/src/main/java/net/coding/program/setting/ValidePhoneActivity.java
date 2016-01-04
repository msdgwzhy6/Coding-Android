package net.coding.program.setting;

import android.app.Activity;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.util.ViewStyleUtil;
import net.coding.program.common.widget.LoginEditText;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chenchao on 15/12/28.
 */

@EActivity(R.layout.activity_valide_phone)
public class ValidePhoneActivity extends BackActivity {

    private static final String TAG_GENERATE_CODE = "TAG_GENERATE_CODE";
    private static final String TAG_SET_USER_INFO = "TAG_SET_USER_INFO";

    @ViewById
    LoginEditText editPhone, editCode;

    @ViewById
    TextView loginButton;

    @ViewById
    TextView sendPhoneMessage;

    UserObject user;
    private CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {

        public void onTick(long millisUntilFinished) {

            sendPhoneMessage.setText(millisUntilFinished / 1000 + "秒");
        }

        public void onFinish() {
            sendPhoneMessage.setEnabled(true);
            sendPhoneMessage.setText("发送验证码");
        }
    };

    @AfterViews
    final void initValidePhoneActivity() {
        ViewStyleUtil.editTextBindButton(loginButton, editPhone, editCode);
        user = AccountInfo.loadAccount(this);
    }

    @Click
    void sendPhoneMessage() {
        String phoneString = editPhone.getTextString();
        String url = Global.HOST_API + "/user/generate_phone_code";
        RequestParams params = new RequestParams();
        params.put("phone", phoneString);
        postNetwork(url, params, TAG_GENERATE_CODE);
        sendPhoneMessage.setEnabled(false);
        countDownTimer.start();
        editCode.requestFocus();
    }

    @Override
    protected void onStop() {
        countDownTimer.cancel();
        countDownTimer.onFinish();
        super.onStop();
    }

    @Click
    void loginButton() {
        final String url = Global.HOST_API + "/user/updateInfo";
        RequestParams params = new RequestParams();
        try {
            params.put("phone", user.phone);
            params.put("tags", user.tags);
            params.put("job", user.job);
            params.put("sex", user.sex);
            String phoneString = editPhone.getTextString();
            user.phone = phoneString;
            params.put("phone", user.phone);
            params.put("birthday", user.birthday);
            params.put("location", user.location);
            params.put("company", user.company);
            params.put("slogan", user.slogan);
            params.put("introduction", user.introduction);
            params.put("lavatar", user.lavatar);
            params.put("global_key", user.global_key);
            params.put("name", user.name);
            params.put("email", user.email);
            params.put("id", user.id);
            String phoneCodeString = editCode.getTextString();
            params.put("code", phoneCodeString);

            postNetwork(url, params, TAG_SET_USER_INFO);
            showProgressBar(true, "");
        } catch (Exception e) {
            showMiddleToast(e.toString());
        }
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_GENERATE_CODE)) {
            if (code == 0) {
                showMiddleToast("验证码已发送");
            } else {
                showErrorMsgMiddle(code, respanse);
                countDownTimer.cancel();
                countDownTimer.onFinish();
            }
        } else if (tag.equals(TAG_SET_USER_INFO)) {
            showProgressBar(false, "");
            if (code == 0) {
                showMiddleToast("修改成功");
                setResult(Activity.RESULT_OK);
                AccountInfo.saveAccount(this, user);
                MyApp.sUserObject = user;
                finish();
            } else {
                showErrorMsgMiddle(code, respanse);
            }
        }
    }


}
