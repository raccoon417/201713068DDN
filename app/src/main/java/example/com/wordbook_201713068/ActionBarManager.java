package example.com.wordbook_201713068;
/*
201713068 이민혁
*/


import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.balysv.materialmenu.MaterialMenuDrawable;

// 기본 상단바 메니저 생성
public class ActionBarManager {

    public static void initBackArrowActionbar(AppCompatActivity activity, Toolbar toolbar, String title) {
        initDefault(activity, toolbar, title);

        MaterialMenuDrawable menuDrawable = new MaterialMenuDrawable(activity, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        menuDrawable.setIconState(MaterialMenuDrawable.IconState.ARROW);

        toolbar.setNavigationIcon(menuDrawable);
    }

    protected static void initDefault(AppCompatActivity activity, Toolbar toolbar, String title) {
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
