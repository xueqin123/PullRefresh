package qin.xue.refresh;

/**
 * Created by qinxue on 2018/8/25.
 */

public class Module {
    public static final int HEAD_TYPE = 0;
    public static final int NORMAL_TYPE = 1;

    //0代表头 1代表正常元素
    public Module(int val, int type) {
        this.val = val;
        this.type = type;
    }

    int val;
    int type;
}
