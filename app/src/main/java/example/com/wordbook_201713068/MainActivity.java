package example.com.wordbook_201713068;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import butterknife.ButterKnife;
import butterknife.OnClick;
import example.com.wordbook_201713068.database.AppDatabase;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    private int mVocaCnt = -1;

    private AppDatabase mDb;
    private Disposable mVocaCntDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                AppDatabase.DATABASE_NAME).build();
    }

    //시작지점
    @Override
    protected void onResume() {
        super.onResume();
        subscribeVocaCnt();
    }

    //디비 불러옴
    protected void subscribeVocaCnt() {
        mVocaCntDisposable = mDb.vocaDao().getCount().observeOn(Schedulers.io())
                .subscribeOn(Schedulers.computation())
                .subscribe(integer -> {
                    mVocaCnt = integer.intValue();
                });
    }

    // 공부하기 버튼 클릭시 디비에 저장되어진 단어개수 확인후 액티비티 시작
    @OnClick(R.id.button_study)
    public void onClickStudyBtn() {
        if (mVocaCnt == 0) {
            Toast.makeText(this,
                    getString(R.string.error_limit_1_word), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, StudyActivity.class));
    }

    @OnClick(R.id.button_test)
    public void onClickTestBtn() {
        if (mVocaCnt < 4) {
            Toast.makeText(this,
                    getString(R.string.error_limit_4_word), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, TestActivity.class));
    }

    @OnClick(R.id.button_register)
    public void onClickRegisterBtn() {
        startActivity(new Intent(this, VocaListActivity.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVocaCntDisposable.dispose();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
