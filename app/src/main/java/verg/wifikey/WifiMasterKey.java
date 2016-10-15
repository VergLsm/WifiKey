package verg.wifikey;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import verg.lib.TextUtils;

/**
 * WifiMasterKey
 * Created by verg on 16-5-13.
 */
class WifiMasterKey {
    private String ii;
    private String mac;
    private String dhid;
    private String salt;
    private String TAG = this.getClass().getSimpleName();
    private boolean isSigned = false;

    WifiMasterKey() {

    }

    void setSigned(boolean signed) {
        isSigned = signed;
    }

    private static String sign(LinkedHashMap<String, String> data, String salt) {
//        Map<String, String> sortMap = new TreeMap<>(new Comparator<String>() {
//            @Override
//            public int compare(String str1, String str2) {
//                return str1.compareTo(str2);
//            }
//        });
//        sortMap.putAll(data);
        StringBuilder request_str = new StringBuilder();
//        for (Map.Entry<String, String> entry : sortMap.entrySet()) {
//            request_str.append(entry.getValue());
//        }
        for (String s : data.values()) {
            request_str.append(s);
        }
        request_str.append(salt);
        return getMD5(request_str.toString()).toUpperCase();
    }

    private static String decrypt(String ciphertext) {
        String aesKey = "k%7Ve#8Ie!5Fb&8E";
        String aesIV = "y!0Oe#2Wj#6Pw!3V";

        SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(aesIV.getBytes()));
            byte[] decrypted = cipher.doFinal(TextUtils.hexstr2str(ciphertext));
            String temp = new String(decrypted).trim();
            temp = URLDecoder.decode(temp, "utf-8");
            return temp.substring(3, temp.length() - 13);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    StringRequest getQuestRequest(Map<String, String> apMap, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        final LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("appid", "0008");
        data.put("bssid", apMap.get("bssid"));
        data.put("chanid", "gw");
        data.put("dhid", dhid);
        data.put("ii", ii);
        data.put("lang", "cn");
        data.put("mac", mac);
        //data.put("method", "getSecurityCheckSwitch");
        data.put("method", "getDeepSecChkSwitch");
        //data.put("pid", "qryapwithoutpwd:commonswitch");
        data.put("pid", "qryapwd:commonswitch");
        data.put("ssid", apMap.get("ssid"));
        data.put("st", "m");
        data.put("uhid", "a0000000000000000000000000000001");
        data.put("v", "324");
        data.put("sign", sign(data, salt));

        return new KeyRequest(data, listener, errorListener);
    }

    StringRequest getSignRequest(Response.Listener<String> listener, Response.ErrorListener errorListener) {
        salt = "1Hf%5Yh&7Og$1Wh!6Vr&7Rs!3Nj#1Aa$";
        final LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("appid", "0008");
        data.put("chanid", "gw");
        ii = getMD5(String.valueOf(new Random().nextInt(10000)));
        data.put("ii", ii);
        data.put("imei", ii);
        data.put("lang", "cn");
        mac = ii.substring(0, 12);
        data.put("mac", mac);
        data.put("manuf", "Apple");
        data.put("method", "getTouristSwitch");
        data.put("misc", "Mac OS");
        data.put("model", "10.10.3");
        data.put("os", "Mac OS");
        data.put("osver", "10.10.3");
        data.put("osvercd", "10.10.3");
        data.put("pid", "initdev:commonswitch");
        data.put("scrl", "813");
        data.put("scrs", "1440");
        data.put("st", "m");
        data.put("v", "324");
        data.put("wkver", "324");
        data.put("sign", sign(data, salt));

        return new KeyRequest(data, listener, errorListener);
    }

    private static String getMD5(String input) {
        return TextUtils.toHexString(getMD5(input.getBytes()));
    }

    private static byte[] getMD5(byte[] input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    static LinkedHashMap<String, Map<String, String>> parsingResponse(JSONObject jsonObject) {
        JSONObject psws = jsonObject.optJSONObject("qryapwd").optJSONObject("psws");
        LinkedHashMap<String, Map<String, String>> apList = new LinkedHashMap<>();
        Iterator it = psws.keys();
        while (it.hasNext()) {
            HashMap<String, String> ap = new HashMap<>();
            String key = (String) it.next();
            JSONObject jsonObject_ap = psws.optJSONObject(key);
            ap.put("ssid", jsonObject_ap.optString("ssid"));
            ap.put("password", decrypt(jsonObject_ap.optString("pwd")));
            apList.put(jsonObject_ap.optString("bssid"), ap);
        }
        return apList;
    }

    void setDhid(String dhid) {
        this.dhid = dhid;
        isSigned = true;
    }

    void setSalt(String salt) {
        this.salt = salt;
    }

    boolean isSigned() {
        return isSigned;
    }

    private static class KeyRequest extends StringRequest {

        private Map<String, String> params;
        private static String URL = "http://wifiapi02.51y5.net/wifiapi/fa.cmd";


        KeyRequest(Map<String, String> params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(Request.Method.POST, URL, listener, errorListener);
            this.params = params;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "WiFiMasterKey/1.1.0 (Mac OS X Version 10.10.3 (Build 14D136))");
            return headers;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return params;
        }

    }
}
