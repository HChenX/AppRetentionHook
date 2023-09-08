package Com.HChen.Hook;

import android.content.Context;

import Com.HChen.Hook.Base.BasePutKey;

public class Application extends android.app.Application {
    @Override
    protected void attachBaseContext(Context base) {
        BasePutKey.getSharedPrefs(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
