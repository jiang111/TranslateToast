package com.jiang.android.translatetoast.db;

import com.jiang.android.translatetoast.db.dao.TranslateDao;

/**
 * Created by jiang on 5/20/16.
 */

public class DbUtil {
    private static TranslateService translateService;


    private static TranslateDao getPersonDao() {
        return DbCore.getDaoSession().getTranslateDao();
    }



    public static TranslateService getTranslateService() {
        if (translateService == null) {
            translateService = new TranslateService(getPersonDao());
        }
        return translateService;
    }
}
