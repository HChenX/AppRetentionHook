package Com.HChen.Hook.Ui;

import android.content.Context;

import Com.HChen.Hook.Base.BaseGetKey;

public class Application extends android.app.Application {
    @Override
    protected void attachBaseContext(Context base) {
        BaseGetKey.getSharedPrefs(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
