package com.jiang.android.translatetoast.db;

import com.jiang.android.translatetoast.db.dao.TranslateDao;
import com.jiang.android.translatetoast.db.model.Translate;

/**
 * Created by jiang on 5/20/16.
 */

public class TranslateService extends BaseService<Translate,Long> {
    public TranslateService(TranslateDao dao) {
        super(dao);
    }
}
