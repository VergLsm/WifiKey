package verg.wifikey;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
//        WifiMasterKey wifiMasterKey = new WifiMasterKey();
//        wifiMasterKey.RegisterNewDevice(null, null);
//        String pwd = "C3D3A398B305166BE029310699585D42B930FF192E285063F1A8B7A81CBD61A9";
//        String p = WifiMasterKey.decrypt(pwd);
//        Assert.assertEquals(p, "07582894288");
//        int rNum = new Random().nextInt(100000);
//        rNum = 7054;
//        String ii = TextUtils.str2hexstr(wmk.getMD5(String.valueOf(rNum).getBytes()));
//        Assert.assertEquals(ii, "812469e49663025b39e8d25fdaad81a7");
//        String imei = ii.substring(0, 12);
//        Assert.assertEquals(imei,"812469e49663");
        changeDataSet_2(getList(), -80);

    }

    public List<Integer> getList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i > -100; i -= 10) {
            list.add(i);
        }
        return list;
    }

    public void changeDataSet_2(List<Integer> wifiList, int nV) {
        int i = 5;
        wifiList.set(i, nV);

        int toPosition = i;
        while ((toPosition > 0) && (nV >= wifiList.get(toPosition - 1)))
            toPosition--;

        while ((toPosition < wifiList.size() - 1) && (nV < wifiList.get(toPosition + 1)))
            toPosition++;

        int temp = wifiList.remove(i);
        wifiList.add(toPosition, temp);
        return;
    }


}